package leviathan143.fantasticchainsaw;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import leviathan143.fantasticchainsaw.metadata.ForgeNature;
import leviathan143.fantasticchainsaw.util.EclipseHelper;

public class AddForgeNature extends AbstractHandler
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		try
		{
			IProject project = EclipseHelper.getFirstSelectedProject();
			ForgeNature.add(project);
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
}
