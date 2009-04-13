// $Id: CGLIBLazyInitializer.java 11296 2007-03-18 04:10:09Z scottmarlownovell $
package org.hibernate.proxy.pojo.cglib;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InvocationHandler;
import net.sf.cglib.proxy.NoOp;

import org.hibernate.HibernateException;
import org.hibernate.LazyInitializationException;
import org.hibernate.proxy.pojo.BasicLazyInitializer;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.type.AbstractComponentType;
import org.hibernate.util.ReflectHelper;

import org.apache.commons.logging.LogFactory;

/**
 * A <tt>LazyInitializer</tt> implemented using the CGLIB bytecode generation library
 */
public final class CGLIBLazyInitializer extends BasicLazyInitializer implements InvocationHandler {
	
	private static final CallbackFilter FINALIZE_FILTER = new CallbackFilter() {
		public int accept(Method method) {
			if ( method.getParameterTypes().length == 0 && method.getName().equals("finalize") ){
				return 1;
			}
			else {
				return 0;
			}
		}
	};

	private Class[] interfaces;
	private boolean constructed = false;

	static HibernateProxy getProxy(final String entityName, final Class persistentClass,
			final Class[] interfaces, final Method getIdentifierMethod,
			final Method setIdentifierMethod, AbstractComponentType componentIdType,
			final Serializable id, final SessionImplementor session) throws HibernateException {
		// note: interfaces is assumed to already contain HibernateProxy.class
		
		try {
			final CGLIBLazyInitializer instance = new CGLIBLazyInitializer(
					entityName,
					persistentClass,
					interfaces,
					id,
					getIdentifierMethod,
					setIdentifierMethod,
					componentIdType,
					session 
				);
			
			final HibernateProxy proxy;
			Class factory = getProxyFactory(persistentClass,  interfaces);
			proxy = getProxyInstance(factory, instance);
			instance.constructed = true;
			return proxy;
		}
		catch (Throwable t) {
			LogFactory.getLog( BasicLazyInitializer.class )
				.error( "CGLIB Enhancement failed: " + entityName, t );
			throw new HibernateException( "CGLIB Enhancement failed: " + entityName, t );
		}
	}

	public static HibernateProxy getProxy(final Class factory, final String entityName,
			final Class persistentClass, final Class[] interfaces,
			final Method getIdentifierMethod, final Method setIdentifierMethod,
			final AbstractComponentType componentIdType, final Serializable id,
			final SessionImplementor session) throws HibernateException {
		
		final CGLIBLazyInitializer instance = new CGLIBLazyInitializer(
				entityName,
				persistentClass,
				interfaces,
				id,
				getIdentifierMethod,
				setIdentifierMethod,
				componentIdType,
				session 
			);
		
		final HibernateProxy proxy;
		try {
			proxy = getProxyInstance(factory, instance);
		}
		catch (Exception e) {
			throw new HibernateException( "CGLIB Enhancement failed: " + persistentClass.getName(), e );
		}
		instance.constructed = true;

		return proxy;
	}

    private static HibernateProxy getProxyInstance(Class factory, CGLIBLazyInitializer instance) throws InstantiationException, IllegalAccessException {
		HibernateProxy proxy;
		try {
			Enhancer.registerCallbacks(factory, new Callback[]{ instance, null });
			proxy = (HibernateProxy)factory.newInstance();
		} finally {
			// HHH-2481 make sure the callback gets cleared, otherwise the instance stays in a static thread local.
			Enhancer.registerCallbacks(factory, null);
		}
		return proxy;
	}

    public static Class getProxyFactory(Class persistentClass, Class[] interfaces)
			throws HibernateException {
		Enhancer e = new Enhancer();
		e.setSuperclass( interfaces.length == 1 ? persistentClass : null );
		e.setInterfaces(interfaces);
		e.setCallbackTypes(new Class[]{
			InvocationHandler.class,
			NoOp.class,
	  		});
  		e.setCallbackFilter(FINALIZE_FILTER);
  		e.setUseFactory(false);
		e.setInterceptDuringConstruction( false );
		return e.createClass();
	}

	private CGLIBLazyInitializer(final String entityName, final Class persistentClass,
			final Class[] interfaces, final Serializable id, final Method getIdentifierMethod,
			final Method setIdentifierMethod, final AbstractComponentType componentIdType,
			final SessionImplementor session) {
		super(
				entityName,
				persistentClass,
				id,
				getIdentifierMethod,
				setIdentifierMethod,
				componentIdType,
				session 
			);
		this.interfaces = interfaces;
	}

	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		if ( constructed ) {
			Object result = invoke( method, args, proxy );
			if ( result == INVOKE_IMPLEMENTATION ) {
				Object target = getImplementation();
				try {
					final Object returnValue;
					if ( ReflectHelper.isPublic( persistentClass, method ) ) {
						if ( ! method.getDeclaringClass().isInstance( target ) ) {
							throw new ClassCastException( target.getClass().getName() );
						}
						returnValue = method.invoke( target, args );
					}
					else {
						if ( !method.isAccessible() ) {
							method.setAccessible( true );
						}
						returnValue = method.invoke( target, args );
					}
					return returnValue == target ? proxy : returnValue;
				}
				catch ( InvocationTargetException ite ) {
					throw ite.getTargetException();
				}
			}
			else {
				return result;
			}
		}
		else {
			// while constructor is running
			if ( method.getName().equals( "getHibernateLazyInitializer" ) ) {
				return this;
			}
			else {
				throw new LazyInitializationException( "unexpected case hit, method=" + method.getName() );
			}
		}
	}

	protected Object serializableProxy() {
		return new SerializableProxy(
				getEntityName(),
				persistentClass,
				interfaces,
				getIdentifier(),
				getIdentifierMethod,
				setIdentifierMethod,
				componentIdType 
			);
	}

}
