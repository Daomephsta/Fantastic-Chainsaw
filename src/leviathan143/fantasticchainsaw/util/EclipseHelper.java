package leviathan143.fantasticchainsaw.util;

import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class EclipseHelper
{
	public static IProject getCurrentSelectedProject()
	{
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null)
		{
			IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection();
			Object firstElement = selection.getFirstElement();
			if (firstElement instanceof IAdaptable)
			{
				return (IProject)((IAdaptable)firstElement).getAdapter(IProject.class);
			}
		}
		return null;
	}
	
	public static IDocument getSharedWorkingCopy(ITextFileBufferManager manager, CompilationUnit comp) throws CoreException
	{
		IPath path = comp.getJavaElement().getPath();
		manager.connect(path, LocationKind.IFILE, null);
		ITextFileBuffer buffer = manager.getTextFileBuffer(path, LocationKind.IFILE);
		return buffer.getDocument();
	}
}
