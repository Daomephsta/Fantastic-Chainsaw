package leviathan143.fantasticchainsaw.interfaces.minecraft;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * A collection of all locations that store MC resources used by a project. Used
 * to resolve {@link ResourceLocation ResourceLocations} to filesystem
 * resources.
 * 
 * @author Daomephsta (Leviathan143)
 *
 */
public class MCResourceRepositoryAggregate implements Closeable
{
	public static final String TEXTURE_RESOURCE = "textures";
	public static final String MODEL_RESOURCE = "models";
	public static final String BLOCKSTATE_RESOURCE = "blockstates";

	// Repositories that aren't directories, .jars, .zips, etc
	private final List<FileSystem> archiveRepositories = Lists.newArrayList();
	// Repositories that are directories
	private final List<Path> directoryRepositories = Lists.newArrayList();

	public MCResourceRepositoryAggregate(Path... defaultRepositories)
	{
		try
		{
			for (Path path : defaultRepositories)
			{
				if (Files.isDirectory(path)) directoryRepositories.add(path);
				else archiveRepositories.add(FileSystems.newFileSystem(path, null));
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public Path getResourcePath(String subFolder, ResourceLocation location)
	{
		for (FileSystem archiveRepo : archiveRepositories)
		{
			if (containsDomain(archiveRepo, location.getDomain()))
			{
				Path resourcePath = archiveRepo.getPath("assets", location.getDomain(), subFolder,
						location.getPath() + ".json");
				if (Files.exists(resourcePath)) return resourcePath;
			}
		}
		for (Path dirRepo : directoryRepositories)
		{
			if (containsDomain(dirRepo, location.getDomain()))
			{
				Path resourcePath = dirRepo.resolve(
						String.join("/", "assets", location.getDomain(), subFolder, location.getPath() + ".json"));
				if (Files.exists(resourcePath)) return resourcePath;
			}
		}
		return null;
	}

	private boolean containsDomain(FileSystem repo, String targetDomain)
	{
		try
		{
			if (Files.list(repo.getPath("assets")).anyMatch(domain -> domain.endsWith(targetDomain))) return true;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	private boolean containsDomain(Path repo, String targetDomain)
	{
		try
		{
			if (Files.list(repo.resolve("assets")).anyMatch(domain -> domain.endsWith(targetDomain))) return true;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void close() throws IOException
	{
		for (FileSystem repo : archiveRepositories)
			repo.close();
	}
}
