package leviathan143.fantasticchainsaw.util;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

public interface ISelectionListener extends SelectionListener
{
    @Override
    default void widgetDefaultSelected(SelectionEvent e)
    {}
}
