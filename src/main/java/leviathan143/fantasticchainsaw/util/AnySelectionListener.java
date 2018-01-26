package leviathan143.fantasticchainsaw.util;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

public abstract class AnySelectionListener implements SelectionListener 
{
	@Override
	public void widgetDefaultSelected(SelectionEvent e) 
	{
		handleSelection(e);
	}

	@Override
	public void widgetSelected(SelectionEvent e)
	{
		handleSelection(e);
	}
	
	public abstract void handleSelection(SelectionEvent e);
}
