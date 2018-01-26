package leviathan143.fantasticchainsaw.mc18.wizards.model;

import java.util.Observer;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.internal.ide.misc.ContainerSelectionGroup;

import leviathan143.fantasticchainsaw.base.ObservableValue;

@SuppressWarnings("restriction")
public class ModelFileCreationPage extends WizardNewFileCreationPage 
{
    private ObservableValue<IPath> modelFilePath = new ObservableValue<IPath>();

    public ModelFileCreationPage(IStructuredSelection selection) 
    {
	super("createFile", selection);
    }

    @Override
    public void handleEvent(Event event) 
    {
	super.handleEvent(event);

	ContainerSelectionGroup selection = null;
	if(event.type == SWT.Modify)
	{
	    if(event.widget instanceof Control)
	    {
		Control control = (Control) event.widget;
		if(control.getParent() instanceof ContainerSelectionGroup) selection = (ContainerSelectionGroup) control.getParent();
	    }
	}
	else if(event.type == SWT.Selection) selection = (ContainerSelectionGroup) event.widget;

	if(selection != null) modelFilePath.setValue(selection.getContainerFullPath());
    }

    public void addModelPathObserver(Observer observer)
    {
	modelFilePath.addObserver(observer);
    }
}
