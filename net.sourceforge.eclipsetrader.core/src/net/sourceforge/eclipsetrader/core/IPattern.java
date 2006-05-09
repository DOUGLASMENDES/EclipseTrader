package net.sourceforge.eclipsetrader.core;

import net.sourceforge.eclipsetrader.core.db.Bar;

public interface IPattern
{

    public abstract boolean applies(Bar[] recs);

    public abstract boolean isBullish();

    public abstract int getComplete();

}