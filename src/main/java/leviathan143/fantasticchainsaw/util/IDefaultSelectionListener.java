package leviathan143.fantasticchainsaw.util;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

public interface IDefaultSelectionListener extends SelectionListener
{
    @Override
    default void widgetSelected(SelectionEvent arg0)
    {}
}
