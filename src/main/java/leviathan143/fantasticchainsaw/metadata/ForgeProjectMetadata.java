package leviathan143.fantasticchainsaw.metadata;

import com.google.gson.JsonObject;

import leviathan143.fantasticchainsaw.Versioning.Version;
import leviathan143.fantasticchainsaw.metadata.MetadataHandler.ProjectMetadata;

public class ForgeProjectMetadata extends ProjectMetadata
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

	@Override
	public void serialise(JsonObject json)
	{
		json.addProperty("mcVersion", mcVersion.toString());
		json.addProperty("forgeVersion", forgeVersion.toString());
		json.addProperty("mappings", mappingVersion);
	}
}