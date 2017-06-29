package leviathan143.fantasticchainsaw;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IJavaProject;

import leviathan143.fantasticchainsaw.gradle.ForgeModel;
import leviathan143.fantasticchainsaw.gradle.GradleInterface;

public class MetadataHandler 
{
	private static Map<String, ProjectMetadata> projectToMetadata = new HashMap<String, ProjectMetadata>();
	
	public static ProjectMetadata getMetadata(IJavaProject project)
	{
		String projectName = project.getElementName();
		if(!projectToMetadata.containsKey(projectName))
			createMetaData(project);
		return projectToMetadata.get(projectName);
	}
	
	private static void createMetaData(IJavaProject project)
	{
		ForgeModel forgeModel = GradleInterface.getModel(project);
		projectToMetadata.put(project.getElementName(), new ProjectMetadata(forgeModel.getVersion(), forgeModel.getMappings()));
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
