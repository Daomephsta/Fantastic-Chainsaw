package leviathan143.fantasticchainsaw.interfaces.mcpbot;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.ui.console.MessageConsoleStream;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import leviathan143.fantasticchainsaw.FantasticPlugin;
import leviathan143.fantasticchainsaw.i18n.MCPBotInterfaceMessages;
import leviathan143.fantasticchainsaw.interfaces.forgemaven.ForgeMavenInterface;
import leviathan143.fantasticchainsaw.util.EclipseHelper;

public class MCPBotInterface
{
	private static final String MCP_VERSIONS_URL = "http://export.mcpbot.bspk.rs/versions.json";
	private static final Gson MCP_VERSION_GSON = new GsonBuilder().create();
	private static Map<String, VersionMappings> versionToMappings;

	private static final class VersionMappings
	{
		private int[] snapshot;
		private int[] stable;
	}

	@SuppressWarnings("serial")
	public static void updateMCPVersions()
	{
		MessageConsoleStream consoleStream = EclipseHelper.getOrCreateConsole(FantasticPlugin.NAME).newMessageStream();
		try
		{
			URL versionJSONURL = new URL(MCP_VERSIONS_URL);
			URLConnection connection = versionJSONURL.openConnection();
			connection.connect();

			InputStream in = connection.getInputStream();
			consoleStream.println(MCPBotInterfaceMessages.retrievingMappings);
			long startTime = System.currentTimeMillis();
			versionToMappings = MCP_VERSION_GSON.fromJson(new InputStreamReader(in),
					new TypeToken<Map<String, VersionMappings>>()
					{}.getType());
			consoleStream.println(
					String.format(MCPBotInterfaceMessages.mappingsRetrieved, System.currentTimeMillis() - startTime));
			in.close();
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			consoleStream.println(MCPBotInterfaceMessages.mappingsNotRetrieved);
			e.printStackTrace();
		}
	}

	public static String[] getAvailableMappings(String mcVersion)
	{
		VersionMappings mappings = null;
		if (versionToMappings.containsKey(mcVersion)) mappings = versionToMappings.get(mcVersion);
		else
		{
			// If there are no mappings for a specific version, try mappings for
			// siblings
			for (String sibling : ForgeMavenInterface.getSiblingVersions(mcVersion))
			{
				if (versionToMappings.containsKey(sibling))
				{
					mappings = versionToMappings.get(sibling);
					break;
				}
			}
			if (mappings == null)
			{
				MessageConsoleStream consoleStream = EclipseHelper.getOrCreateConsole(FantasticPlugin.NAME)
						.newMessageStream();
				consoleStream.println(String.format(MCPBotInterfaceMessages.noMappings, mcVersion));
				return new String[0];
			}
		}

		Stream<String> stable = Arrays.stream(mappings.stable).mapToObj(value ->
		{
			return "stable_" + value;
		});
		Stream<String> snapshot = Arrays.stream(mappings.snapshot).mapToObj(value ->
		{
			return "snapshot_" + value;
		});;
		return Stream.concat(stable, snapshot).toArray(String[]::new);
	}
}
