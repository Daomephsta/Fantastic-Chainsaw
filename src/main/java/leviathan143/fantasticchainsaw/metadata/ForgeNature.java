package leviathan143.fantasticchainsaw.metadata;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import com.google.common.collect.ObjectArrays;

import leviathan143.fantasticchainsaw.util.EclipseHelper;

public class ForgeNature implements IProjectNature
{
	public static final String NATURE_ID = "leviathan143.fantasticchainsaw.mcforgemod";
	
	private IProject project;

	public static void add(IProject project) throws CoreException
	{
		IProjectDescription desc = project.getDescription();
		desc.setNatureIds(ObjectArrays.concat(desc.getNatureIds(), NATURE_ID));
		project.setDescription(desc, null);
	}
	
	@Override
	public void configure() throws CoreException
	{
	}

	@Override
	public void deconfigure() throws CoreException
	{
		IJavaProject project = JavaCore.create(EclipseHelper.getFirstSelectedProject());
		MetadataHandler.invalidateMetadata(project);
	}

	@Override
	public IProject getProject()
	{
		return project;
	}

	@Override
	public void setProject(IProject project)
	{
		this.project = project;
	}
}
