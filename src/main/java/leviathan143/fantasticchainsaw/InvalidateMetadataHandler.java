package leviathan143.fantasticchainsaw;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import leviathan143.fantasticchainsaw.util.EclipseHelper;

public class InvalidateMetadataHandler extends AbstractHandler
{
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException
	{
		IJavaProject project = JavaCore.create(EclipseHelper.getFirstSelectedProject());
		MetadataHandler.invalidateMetadata(project);

		return null;
	}
}
