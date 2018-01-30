package leviathan143.fantasticchainsaw.interfaces.minecraft;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.IJavaProject;

import leviathan143.fantasticchainsaw.interfaces.minecraft.model.JSONModel;

public class MCInterface
{
	private static final Map<IJavaProject, MCResourceRepositoryAggregate> PROJECT_TO_RESOURCE_AGGREGATE = new HashMap<>();

	public static MCResourceRepositoryAggregate getResourceAggregateForProject(IJavaProject project, Path... defaults)
	{
		return new MCResourceRepositoryAggregate(defaults);// return
															// PROJECT_TO_RESOURCE_AGGREGATE.computeIfAbsent(project,
															// proj -> new
															// MCResourceRepositoryAggregate(defaults));
	}

	public static void disposeProjectResourceAggregate(IJavaProject project) throws IOException
	{
		MCResourceRepositoryAggregate aggregate = PROJECT_TO_RESOURCE_AGGREGATE.remove(project);
		aggregate.close();
	}

	public static JSONModel getJavaRepresentation(MCResourceRepositoryAggregate repoAggregate, Path path)
			throws IOException
	{
		return JSONModel.deserialise(path, repoAggregate);
	}
}
