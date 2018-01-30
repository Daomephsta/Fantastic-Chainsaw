package leviathan143.fantasticchainsaw.metadata;

import java.io.*;
import java.nio.file.Files;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ui.console.MessageConsoleStream;

import com.google.common.cache.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import leviathan143.fantasticchainsaw.FantasticPlugin;
import leviathan143.fantasticchainsaw.Versioning.Version;
import leviathan143.fantasticchainsaw.gradle.ForgeModel;
import leviathan143.fantasticchainsaw.interfaces.gradle.GradleInterface;
import leviathan143.fantasticchainsaw.util.EclipseHelper;

public class MetadataHandler
{
	private static LoadingCache<IJavaProject, ProjectMetadata> projectToMetadata = CacheBuilder.newBuilder()
			.maximumSize(25).initialCapacity(10).removalListener(new RemovalListener<IJavaProject, ProjectMetadata>()
			{
				@Override
				public void onRemoval(RemovalNotification<IJavaProject, ProjectMetadata> notification)
				{
					// Delete the metadata file when the metadata entry is
					// invalidated
					try
					{
						Files.delete(getMetadataFile(notification.getKey()).toPath());
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}).build(new CacheLoader<IJavaProject, ProjectMetadata>()
			{
				@Override
				public ProjectMetadata load(IJavaProject project)
				{
					ProjectMetadata metadata = null;
					File metadataFile = getMetadataFile(project);

					try
					{
						// If the metadata file exists, load the metadata from
						// it
						if (metadataFile.exists())
						{
							MessageConsoleStream consoleStream = EclipseHelper.getOrCreateConsole(FantasticPlugin.NAME)
									.newMessageStream();
							consoleStream.println("Loading metadata for " + project.getElementName() + " from file");
							metadata = ProjectMetadata.createFromFile(metadataFile);
						}
						else
						{
							MessageConsoleStream consoleStream = EclipseHelper.getOrCreateConsole(FantasticPlugin.NAME)
									.newMessageStream();
							consoleStream.println(
									"Fetching metadata for " + project.getElementName() + " from build.gradle");
							// If it doesn't, load the metadata from the
							// build.gradle, create the metadata file and write
							// the metadata to the file
							if(project.getProject().hasNature(ForgeNature.NATURE_ID))
							{
								ForgeModel forgeModel = GradleInterface.getForgeModel(project);
								metadata = new ForgeProjectMetadata(forgeModel.getMCVersion(), forgeModel.getForgeVersion(),
										forgeModel.getMappings());
							}
							else metadata = new NonForgeProjectMetadata();
							metadata.writeToFile(metadataFile);
						}
					}
					catch (IOException | CoreException e)
					{
						e.printStackTrace();
					}

					return metadata;
				}

			});

	private static File getMetadataFile(IJavaProject project)
	{
		IPath state = Platform.getStateLocation(EclipseHelper.getPluginBundle());
		return new File(state.toFile(), project.getElementName() + ".fcdata");
	}

	public static ProjectMetadata getMetadata(IJavaProject project)
	{
		return projectToMetadata.getUnchecked(project);
	}

	public static void invalidateMetadata(IJavaProject project)
	{
		projectToMetadata.invalidate(project);
	}

	public static abstract class ProjectMetadata
	{
		private static final Gson METADATA_SERIALISER = new GsonBuilder()
				.registerTypeAdapter(Version.class, new Version.Serialiser()).setPrettyPrinting().create();

		public static ProjectMetadata createFromFile(File file) throws IOException
		{
			try (Reader reader = new BufferedReader(new FileReader(file)))
			{
				return METADATA_SERIALISER.fromJson(reader, ProjectMetadata.class);
			}
		}

		public void writeToFile(File file) throws IOException
		{
			try (Writer writer = new BufferedWriter(new FileWriter(file)))
			{
				writer.write(METADATA_SERIALISER.toJson(this));
			}
		}
	}

	public static class ForgeProjectMetadata extends ProjectMetadata
	{
		private final Version mcVersion;
		private final Version forgeVersion;
		private final String mappingVersion;

		public ForgeProjectMetadata(String mcVersion, String forgeVersion, String mappingVersion)
		{
			this.mcVersion = new Version(mcVersion);
			this.forgeVersion = new Version(forgeVersion);
			this.mappingVersion = mappingVersion;
		}

		public Version getMCVersion()
		{
			return mcVersion;
		}

		public Version getForgeVersion()
		{
			return forgeVersion;
		}

		public String getMappingVersion()
		{
			return mappingVersion;
		}
	}

	public static class NonForgeProjectMetadata extends ProjectMetadata {}
}
