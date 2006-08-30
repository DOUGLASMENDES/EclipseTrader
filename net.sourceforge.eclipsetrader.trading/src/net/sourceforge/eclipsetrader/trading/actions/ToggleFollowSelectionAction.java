package net.sourceforge.eclipsetrader.trading.actions;

import net.sourceforge.eclipsetrader.trading.views.Level2View;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

public class ToggleFollowSelectionAction extends Action implements IViewActionDelegate
{
    private Level2View view;

    public ToggleFollowSelectionAction(Level2View view)
    {
        init(view);
    }

    public void init(IViewPart view)
    {
        if (view instanceof Level2View)
        {
            this.view = (Level2View) view;
            setText("Follow Security Selection");
            setChecked(this.view.isFollowSelection());
        }
    }

    public void run(IAction action)
    {
        view.setFollowSelection(!view.isFollowSelection());
        action.setChecked(view.isFollowSelection());
    }

    public void run()
    {
        run(this);
    }

    public void selectionChanged(IAction action, ISelection selection)
    {
    }
}
