package leviathan143.fantasticchainsaw.base;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.CompilationParticipant;

import leviathan143.fantasticchainsaw.MetadataHandler;

public class VersionSpecificCompilationParticipant extends CompilationParticipant 
{
	private final String targetVersion;
	
	public VersionSpecificCompilationParticipant(String targetVersion) 
	{
		this.targetVersion = targetVersion;
	}
	
	@Override
	public boolean isActive(IJavaProject project) 
	{
		return project.isOpen() && MetadataHandler.getMetadata(project).getMcVersion().equals(targetVersion);
	}
}
