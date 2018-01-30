package leviathan143.fantasticchainsaw.util;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.util.Collections;
import java.util.Map;

public class NIOHelper
{
	public static FileSystem getOrCreateFileSystem(URI uri) throws IOException
	{
		return getOrCreateFileSystem(uri, Collections.emptyMap());
	}

	public static FileSystem getOrCreateFileSystem(URI uri, Map<String, ?> env) throws IOException
	{
		try
		{
			return FileSystems.getFileSystem(uri);
		}
		catch (FileSystemNotFoundException e)
		{
			return FileSystems.newFileSystem(uri, env);
		}
	}
}
