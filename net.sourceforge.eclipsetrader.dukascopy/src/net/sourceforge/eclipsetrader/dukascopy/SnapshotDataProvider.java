package net.sourceforge.eclipsetrader.dukascopy;

import net.sourceforge.eclipsetrader.IExtendedData;
import net.sourceforge.eclipsetrader.RealtimeChartDataProvider;
import net.sourceforge.eclipsetrader.TraderPlugin;
import net.sourceforge.eclipsetrader.dukascopy.internal.FeederConnector;
import ru.dukascopy.feeder.client.DataListener;

/**
 * @author Steve Bate
 *  
 */
public class SnapshotDataProvider extends RealtimeChartDataProvider {
    private FeederConnector feed;
    private int quoteId = 1;

    public synchronized void setData(IExtendedData[] data) {
        IExtendedData[] previousData = getData();
        super.setData(data);
        if (isStreaming()) {
            subscribe(data);
            for (int i = 0; i < previousData.length; i++) {
                IExtendedData d = null;
                for (int j = 0; j < data.length; j++) {
                    if (data[j].getSymbol().equals(previousData[i].getSymbol())) {
                        d = data[j];
                        break;
                    }
                }
                // Previous data item was not found, so it must have been
                // removed.
                if (d == null) {
                    feed.unsubscribe(previousData[i].getSymbol());
                }
            }
        }
    }

    public synchronized void startStreaming() {
        DukascopyPlugin.getDefault().log(Messages.getString("SnapshotDataProvider.streamingStarted")); //$NON-NLS-1$
        super.startStreaming();
        feed = new FeederConnector();
        feed.setDataListener(new DataListener() {
            public void onNewTick(int id, double price, int volume) {
                IExtendedData d = null;
                IExtendedData[] data = getData();
                String symbol = feed.getSymbolForQuoteId(id);
                for (int i = 0; i < data.length; i++) {
                    if (data[i].getSymbol().equals(symbol)) {
                        d = data[i];
                        break;
                    }
                }
                if (d != null) {
                    //System.out.println("UPDATE " + d.getSymbol() + " " + price +
                    // " " + volume);
                    d.setLastPrice(price);
                	fireDataUpdated();
                }
            }
        });
        feed.connect();
        if (TraderPlugin.getData() != null) {
            subscribe(TraderPlugin.getData());
        }

    }

    public void stopStreaming() {
        DukascopyPlugin.getDefault().log(Messages.getString("SnapshotDataProvider.streamingStopped")); //$NON-NLS-1$
        super.stopStreaming();
        feed.disconnect();
    }

    private void subscribe(IExtendedData[] data) {
        for (int i = 0; i < data.length; i++) {
            String symbol = data[i].getSymbol();
            if (!feed.isSubscribed(symbol)) {
                feed.subscribe(quoteId++, symbol);
                DukascopyPlugin.getDefault().log(
                        Messages.getString("SnapshotDataProvider.subscribed") + " " + quoteId + " " + symbol); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        }
    }
}