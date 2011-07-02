/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.news.internal;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.news.core.HeadLineStatus;
import org.eclipsetrader.news.core.IHeadLine;
import org.eclipsetrader.news.core.INewsProvider;
import org.eclipsetrader.news.core.INewsService;
import org.eclipsetrader.news.core.INewsServiceListener;
import org.eclipsetrader.news.core.INewsServiceRunnable;
import org.eclipsetrader.news.core.NewsEvent;
import org.eclipsetrader.news.internal.repository.HeadLine;

public class NewsService implements INewsService, ISchedulingRule {

    public static final String HEADLINES_FILE = "headlines.xml"; //$NON-NLS-1$

    private List<IHeadLine> headLines = new ArrayList<IHeadLine>();
    private Map<ISecurity, List<IHeadLine>> securityMap = new HashMap<ISecurity, List<IHeadLine>>();

    private ListenerList listeners = new ListenerList(ListenerList.IDENTITY);
    private List<HeadLineStatus> status = new ArrayList<HeadLineStatus>();
    private boolean holdNotifications;

    private List<INewsProvider> providers = new ArrayList<INewsProvider>();

    private IJobManager jobManager;
    private final ILock lock;

    public NewsService() {
        jobManager = Job.getJobManager();
        lock = jobManager.newLock();
    }

    public void startUp(IProgressMonitor monitor) throws JAXBException {
        File file = Activator.getDefault().getStateLocation().append(HEADLINES_FILE).toFile();
        if (file.exists()) {
            JAXBContext jaxbContext = JAXBContext.newInstance(HeadLine[].class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setEventHandler(new ValidationEventHandler() {

                @Override
                public boolean handleEvent(ValidationEvent event) {
                    Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                    Activator.getDefault().getLog().log(status);
                    return true;
                }
            });
            JAXBElement<HeadLine[]> element = unmarshaller.unmarshal(new StreamSource(file), HeadLine[].class);
            headLines.addAll(Arrays.asList(element.getValue()));
        }

        IConfigurationElement[] elements = getProvidersConfigurationElements();
        for (int i = 0; i < elements.length; i++) {
            try {
                INewsProvider newsProvider = (INewsProvider) elements[i].createExecutableExtension("class");
                headLines.addAll(Arrays.asList(newsProvider.getHeadLines()));
                providers.add(newsProvider);
            } catch (Exception e) {
                // TODO Log
            }
        }

        Date limitDate = getLimitDate();
        for (Iterator<IHeadLine> iter = headLines.iterator(); iter.hasNext();) {
            if (iter.next().getDate().before(limitDate)) {
                iter.remove();
            }
        }

        updateSecurityMap();
    }

    public void shutDown(IProgressMonitor monitor) throws JAXBException, IOException {
        for (INewsProvider newsProvider : providers) {
            newsProvider.stop();
        }

        File file = Activator.getDefault().getStateLocation().append(HEADLINES_FILE).toFile();
        if (file.exists()) {
            file.delete();
        }

        JAXBContext jaxbContext = JAXBContext.newInstance(HeadLine[].class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setEventHandler(new ValidationEventHandler() {

            @Override
            public boolean handleEvent(ValidationEvent event) {
                Status status = new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, "Error validating XML: " + event.getMessage(), null); //$NON-NLS-1$
                Activator.getDefault().getLog().log(status);
                return true;
            }
        });
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, System.getProperty("file.encoding")); //$NON-NLS-1$

        List<HeadLine> list = new ArrayList<HeadLine>();
        for (IHeadLine h : headLines) {
            if (h instanceof HeadLine) {
                list.add((HeadLine) h);
            }
        }

        JAXBElement<HeadLine[]> element = new JAXBElement<HeadLine[]>(new QName("list"), HeadLine[].class, list.toArray(new HeadLine[list.size()]));
        marshaller.marshal(element, new FileWriter(file));
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.news.core.INewsService#getHeadLines()
     */
    @Override
    public IHeadLine[] getHeadLines() {
        return headLines.toArray(new IHeadLine[headLines.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.news.core.INewsService#getHeadLinesFor(org.eclipsetrader.core.instruments.ISecurity)
     */
    @Override
    public IHeadLine[] getHeadLinesFor(ISecurity security) {
        List<IHeadLine> l = securityMap.get(security);
        return l != null ? l.toArray(new IHeadLine[l.size()]) : new IHeadLine[0];
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.news.core.INewsService#hasHeadLinesFor(org.eclipsetrader.core.instruments.ISecurity)
     */
    @Override
    public boolean hasHeadLinesFor(ISecurity security) {
        List<IHeadLine> l = securityMap.get(security);
        return l != null && l.size() != 0;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.news.core.INewsService#hasUnreadedHeadLinesFor(org.eclipsetrader.core.instruments.ISecurity)
     */
    @Override
    public boolean hasUnreadedHeadLinesFor(ISecurity security) {
        List<IHeadLine> l = securityMap.get(security);
        if (l != null) {
            for (IHeadLine h : l) {
                if (!h.isReaded()) {
                    return true;
                }
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.news.core.INewsService#addNewsServiceListener(org.eclipsetrader.news.core.INewsServiceListener)
     */
    @Override
    public void addNewsServiceListener(INewsServiceListener listener) {
        listeners.add(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.news.core.INewsService#removeNewsServiceListener(org.eclipsetrader.news.core.INewsServiceListener)
     */
    @Override
    public void removeNewsServiceListener(INewsServiceListener listener) {
        listeners.remove(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.news.core.INewsService#addHeadLines(org.eclipsetrader.news.core.IHeadLine[])
     */
    @Override
    public void addHeadLines(IHeadLine[] newHeadLines) {
        synchronized (status) {
            Date limitDate = getLimitDate();
            for (int i = 0; i < newHeadLines.length; i++) {
                if (newHeadLines[i].getDate().before(limitDate)) {
                    continue;
                }
                if (!headLines.contains(newHeadLines[i])) {
                    headLines.add(newHeadLines[i]);
                    status.add(new HeadLineStatus(HeadLineStatus.ADDED, newHeadLines[i]));
                }
            }
            if (!holdNotifications) {
                fireHeadLineStatusEvent();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.news.core.INewsService#removeHeadLines(org.eclipsetrader.news.core.IHeadLine[])
     */
    @Override
    public void removeHeadLines(IHeadLine[] oldHeadLines) {
        synchronized (status) {
            for (int i = 0; i < oldHeadLines.length; i++) {
                if (headLines.contains(oldHeadLines[i])) {
                    headLines.remove(oldHeadLines[i]);
                    status.add(new HeadLineStatus(HeadLineStatus.REMOVED, oldHeadLines[i]));
                }
            }
            if (!holdNotifications) {
                fireHeadLineStatusEvent();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.news.core.INewsService#updateHeadLines(org.eclipsetrader.news.core.IHeadLine[])
     */
    @Override
    public void updateHeadLines(IHeadLine[] updatedHeadLines) {
        synchronized (status) {
            for (int i = 0; i < updatedHeadLines.length; i++) {
                if (headLines.contains(updatedHeadLines[i])) {
                    status.add(new HeadLineStatus(HeadLineStatus.UPDATED, updatedHeadLines[i]));
                }
            }
            if (!holdNotifications) {
                fireHeadLineStatusEvent();
            }
        }
    }

    protected void updateSecurityMap() {
        securityMap.clear();
        for (IHeadLine headLine : headLines) {
            for (ISecurity security : headLine.getMembers()) {
                List<IHeadLine> list = securityMap.get(security);
                if (list == null) {
                    list = new ArrayList<IHeadLine>();
                    securityMap.put(security, list);
                }
                if (!list.contains(headLine)) {
                    list.add(headLine);
                }
            }
        }
    }

    protected void fireHeadLineStatusEvent() {
        NewsEvent event = new NewsEvent(this, status.toArray(new HeadLineStatus[status.size()]));
        status.clear();

        Object[] l = listeners.getListeners();
        for (int i = 0; i < l.length; i++) {
            try {
                ((INewsServiceListener) l[i]).newsServiceUpdate(event);
            } catch (Exception e) {
                Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error notifying event to listener", e); //$NON-NLS-1$
                Activator.getDefault().getLog().log(status);
            } catch (LinkageError e) {
                Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error notifying event to listener", e); //$NON-NLS-1$
                Activator.getDefault().getLog().log(status);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.news.core.INewsService#runInService(org.eclipsetrader.news.core.INewsServiceRunnable, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IStatus runInService(INewsServiceRunnable runnable, IProgressMonitor monitor) {
        return runInService(runnable, this, monitor);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.news.core.INewsService#runInService(org.eclipsetrader.news.core.INewsServiceRunnable, org.eclipse.core.runtime.jobs.ISchedulingRule, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IStatus runInService(INewsServiceRunnable runnable, ISchedulingRule rule, IProgressMonitor monitor) {
        IStatus status;
        jobManager.beginRule(rule, monitor);
        try {
            lock.acquire();
            holdNotifications = true;
            try {
                status = runnable.run(monitor);
            } catch (Exception e) {
                status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error running service task", e); //$NON-NLS-1$
                Activator.getDefault().getLog().log(status);
            } catch (LinkageError e) {
                status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error running service task", e); //$NON-NLS-1$
                Activator.getDefault().getLog().log(status);
            }
            updateSecurityMap();
            fireHeadLineStatusEvent();
        } catch (Exception e) {
            status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, "Error running service task", e); //$NON-NLS-1$
            Activator.getDefault().getLog().log(status);
        } finally {
            holdNotifications = false;
            lock.release();
            jobManager.endRule(rule);
        }
        return status;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.jobs.ISchedulingRule#contains(org.eclipse.core.runtime.jobs.ISchedulingRule)
     */
    @Override
    public boolean contains(ISchedulingRule rule) {
        if (this == rule) {
            return true;
        }
        if (rule instanceof MultiRule) {
            MultiRule multi = (MultiRule) rule;
            ISchedulingRule[] children = multi.getChildren();
            for (int i = 0; i < children.length; i++) {
                if (!contains(children[i])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.jobs.ISchedulingRule#isConflicting(org.eclipse.core.runtime.jobs.ISchedulingRule)
     */
    @Override
    public boolean isConflicting(ISchedulingRule rule) {
        if (this == rule) {
            return true;
        }
        return false;
    }

    protected IConfigurationElement[] getProvidersConfigurationElements() {
        List<IConfigurationElement> elements = new ArrayList<IConfigurationElement>();
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(Activator.PROVIDER_EXTENSION_POINT);
        if (extensionPoint != null) {
            for (IConfigurationElement e : extensionPoint.getConfigurationElements()) {
                if (e.getName().equals("provider")) {
                    elements.add(e);
                }
            }
        }
        return elements.toArray(new IConfigurationElement[elements.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.news.core.INewsService#getProviders()
     */
    @Override
    public INewsProvider[] getProviders() {
        return providers.toArray(new INewsProvider[providers.size()]);
    }

    protected Date getLimitDate() {
        Calendar limit = Calendar.getInstance();
        limit.set(Calendar.HOUR_OF_DAY, 0);
        limit.set(Calendar.MINUTE, 0);
        limit.set(Calendar.SECOND, 0);
        limit.set(Calendar.MILLISECOND, 0);
        limit.add(Calendar.DATE, -Activator.getDefault().getPreferenceStore().getInt(Activator.PREFS_DATE_RANGE));
        return limit.getTime();
    }
}
