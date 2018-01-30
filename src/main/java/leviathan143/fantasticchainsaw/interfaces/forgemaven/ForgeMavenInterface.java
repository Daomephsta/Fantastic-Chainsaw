package leviathan143.fantasticchainsaw.interfaces.forgemaven;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.ui.console.MessageConsoleStream;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import leviathan143.fantasticchainsaw.FantasticPlugin;
import leviathan143.fantasticchainsaw.Versioning;
import leviathan143.fantasticchainsaw.Versioning.IVersionConstraint;
import leviathan143.fantasticchainsaw.Versioning.Version;
import leviathan143.fantasticchainsaw.i18n.ForgeMavenInterfaceMessages;
import leviathan143.fantasticchainsaw.util.EclipseHelper;
import leviathan143.fantasticchainsaw.util.VersionTree;

public class ForgeMavenInterface
{
	private static final String FORGE_VERSIONS_URL = "http://files.minecraftforge.net/maven/net/minecraftforge/forge/json";
	private static final Gson FORGE_VERSION_GSON = new GsonBuilder()
			.registerTypeAdapter(Version.class, new Version.Serialiser()).create();
	private static final IVersionConstraint FG_2_3_CONSTRAINT = Versioning.createVersionConstraint("1.12+");
	private static final IVersionConstraint FG_2_2_CONSTRAINT = Versioning.createVersionConstraint("1.9.4-1.11.2");
	private static final IVersionConstraint FG_2_1_CONSTRAINT = Versioning.createVersionConstraint("1.8.8-1.9");
	private static final IVersionConstraint FG_2_0_2_CONSTRAINT = Versioning.createVersionConstraint("1.8");
	private static final IVersionConstraint FG_1_2_CONSTRAINT = Versioning.createVersionConstraint("1.7.2-1.7.10");

	private static Map<Integer, ForgeVersionArtifacts> artifacts;
	private static Multimap<String, ForgeVersionArtifacts> mcVersionToArtifacts = ArrayListMultimap.create(15, 100);
	private static VersionTree mcVersionTree = new VersionTree();
	private static Set<Version> mcVersions = Collections.emptySet();

	@SuppressWarnings("unused")
	private static class ForgeVersionArtifacts implements Comparable<ForgeVersionArtifacts>
	{
		private String branch;
		private int build;
		private String[][] files;
		private String mcversion;
		private float modified;
		private Version version;

		@Override
		public int compareTo(ForgeVersionArtifacts other)
		{
			return version.compareTo(other.version);
		}
	}

	@SuppressWarnings("serial")
	public static void updateForgeVersions()
	{
		MessageConsoleStream consoleStream = EclipseHelper.getOrCreateConsole(FantasticPlugin.NAME).newMessageStream();
		try
		{
			URL versionJSONURL = new URL(FORGE_VERSIONS_URL);
			URLConnection connection = versionJSONURL.openConnection();
			connection.connect();

			InputStream in = connection.getInputStream();
			consoleStream.println(ForgeMavenInterfaceMessages.retrievingForgeVersions);
			long startTime = System.currentTimeMillis();
			JsonObject json = new JsonParser().parse(new InputStreamReader(in)).getAsJsonObject();
			artifacts = FORGE_VERSION_GSON.fromJson(json.get("number"),
					new TypeToken<Map<Integer, ForgeVersionArtifacts>>()
					{}.getType());
			in.close();

			for (Entry<Integer, ForgeVersionArtifacts> entry : artifacts.entrySet())
			{
				mcVersionToArtifacts.put(entry.getValue().mcversion, entry.getValue());
			}
			mcVersionTree.clear();
			mcVersions = mcVersionToArtifacts.keys().stream().filter(verString ->
			{
				return !verString.equals("1.7.10_pre4");
			}).map(Version::new).filter(version -> version.compareTo(Versioning.V1_7_10) >= 0)
					.collect(Collectors.toCollection(() -> new TreeSet<Version>()));
			for (Version version : mcVersions)
			{
				mcVersionTree.put(version.toString());
			}
			consoleStream.println(String.format(ForgeMavenInterfaceMessages.forgeVersionsRetrieved,
					System.currentTimeMillis() - startTime));
		}
		catch (MalformedURLException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			consoleStream.println(ForgeMavenInterfaceMessages.forgeVersionsNotRetrieved);
			e.printStackTrace();
		}
	}

	public static Collection<Version> getAvailableMCVersions()
	{
		return mcVersions;
	}

	public static Collection<String> getSiblingVersions(String version)
	{
		return mcVersionTree.getSiblings(version);
	}

	public static Collection<String> getAvailableForgeVersions(String mcVersion)
	{
		if (!mcVersionToArtifacts.containsKey(mcVersion))
		{
			MessageConsoleStream consoleStream = EclipseHelper.getOrCreateConsole(FantasticPlugin.NAME)
					.newMessageStream();
			consoleStream.println(String.format(ForgeMavenInterfaceMessages.noForgeVersions, mcVersion));
			return Collections.emptyList();
		}
		return mcVersionToArtifacts.get(mcVersion).stream().sorted(Collections.reverseOrder())
				.map(ForgeMavenInterface::getVersionIdentifier).collect(Collectors.toList());
	}

	public static String getVersionIdentifier(int buildNumber)
	{
		return getVersionIdentifier(artifacts.get(buildNumber));
	}

	private static String getVersionIdentifier(ForgeVersionArtifacts artifacts)
	{
		if (artifacts.branch != null) return artifacts.mcversion + "-" + artifacts.version + "-" + artifacts.branch;
		return artifacts.mcversion + "-" + artifacts.version;
	}

	public static String getAppropriateForgeGradleVersion(Version mcVersion)
	{
		if (FG_2_3_CONSTRAINT.acceptsVersion(mcVersion)) return "2.3-SNAPSHOT";
		else if (FG_2_2_CONSTRAINT.acceptsVersion(mcVersion)) return "2.2-SNAPSHOT";
		else if (FG_2_1_CONSTRAINT.acceptsVersion(mcVersion)) return "2.1-SNAPSHOT";
		else if (FG_2_0_2_CONSTRAINT.acceptsVersion(mcVersion)) return "2.0.2-SNAPSHOT";
		else if (FG_1_2_CONSTRAINT.acceptsVersion(mcVersion)) return "1.2-SNAPSHOT";
		else
		{
			MessageConsoleStream consoleStream = EclipseHelper.getOrCreateConsole(FantasticPlugin.NAME)
					.newMessageStream();
			consoleStream.println("Could not determine appropriate ForgeGradle version for MC version " + mcVersion);
			return null;
		}
	}
}
