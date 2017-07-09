package leviathan143.fantasticchainsaw.base;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.CompilationParticipant;

import leviathan143.fantasticchainsaw.MetadataHandler;
import leviathan143.fantasticchainsaw.Versioning;
import leviathan143.fantasticchainsaw.Versioning.IVersionConstraint;

public class VersionSpecificCompilationParticipant extends CompilationParticipant 
{
	private final IVersionConstraint versionConstraint;
	
	public VersionSpecificCompilationParticipant(String versionConstraintString) 
	{
		this.versionConstraint = Versioning.createVersionConstraint(versionConstraintString);
	}
	
	@Override
	public boolean isActive(IJavaProject project) 
	{
		return project.isOpen() && versionConstraint.acceptsVersion(MetadataHandler.getMetadata(project).getMcVersion());
	}
}
