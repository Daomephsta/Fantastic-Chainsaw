package leviathan143.fantasticchainsaw.gradle;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ForgeModelImpl implements ForgeModel, Serializable
{
	private final String mcVersion;
	private String forgeVersion;
	private final String mappings;

	public ForgeModelImpl(String mcVersion, String forgeVersion, String mappings)
	{
		this.mcVersion = mcVersion;
		this.forgeVersion = forgeVersion;
		this.mappings = mappings;
	}

	@Override
	public String getMCVersion()
	{
		return mcVersion;
	}

	@Override
	public String getForgeVersion()
	{
		return forgeVersion;
	}

	@Override
	public String getMappings()
	{
		return mappings;
	}
}
