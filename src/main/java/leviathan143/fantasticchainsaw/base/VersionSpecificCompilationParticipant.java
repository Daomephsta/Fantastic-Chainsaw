package leviathan143.fantasticchainsaw.base;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.CompilationParticipant;

import leviathan143.fantasticchainsaw.Versioning;
import leviathan143.fantasticchainsaw.Versioning.IVersionConstraint;
import leviathan143.fantasticchainsaw.metadata.ForgeProjectMetadata;
import leviathan143.fantasticchainsaw.metadata.MetadataHandler;
import leviathan143.fantasticchainsaw.metadata.MetadataHandler.ProjectMetadata;

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
		if(!project.isOpen()) return false;
		ProjectMetadata metadata = MetadataHandler.getMetadata(project);
		return metadata instanceof ForgeProjectMetadata && versionConstraint.acceptsVersion(((ForgeProjectMetadata) metadata).getMCVersion());
	}
}
