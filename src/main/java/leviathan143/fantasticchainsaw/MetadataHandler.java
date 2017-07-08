package leviathan143.fantasticchainsaw;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import leviathan143.fantasticchainsaw.gradle.ForgeModel;
import leviathan143.fantasticchainsaw.gradle.GradleInterface;

public class MetadataHandler 
{
	private static LoadingCache<IJavaProject, ProjectMetadata> projectToMetadata = CacheBuilder.newBuilder().maximumSize(25).initialCapacity(10).
			build(new CacheLoader<IJavaProject, ProjectMetadata>() 
			{
				@Override
				public ProjectMetadata load(IJavaProject project) 
				{
					ProjectMetadata metadata = null;
					
					ForgeModel forgeModel = GradleInterface.getModel(project);
					metadata = new ProjectMetadata(forgeModel.getVersion(), forgeModel.getMappings());
					
					return metadata;
				}
			});
	
	private static File getMetadataFile(IJavaProject project)
	{
		IPath projectRoot = project.getResource().getFullPath();
		return new File(projectRoot.toFile(), project.getElementName() + ".fcdata");
	}
	
	public static ProjectMetadata getMetadata(IJavaProject project)
	{
		return projectToMetadata.getUnchecked(project);
	}
	
	public static class ProjectMetadata
	{
		private final String mcVersion;
		private final String mappingVersion;
		
		public ProjectMetadata(String mcVersion, String mappingVersion) 
		{
			this.mcVersion = mcVersion;
			this.mappingVersion = mappingVersion;
		}
		
		public String getMcVersion() 
		{
			return mcVersion;
		}
		
		public String getMappingVersion() 
		{
			return mappingVersion;
		}
	}
}
