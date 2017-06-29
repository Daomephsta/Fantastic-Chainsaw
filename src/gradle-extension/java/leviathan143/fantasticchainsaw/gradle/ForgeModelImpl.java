package leviathan143.fantasticchainsaw.gradle;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ForgeModelImpl implements ForgeModel, Serializable
{
	private final String version;
	private final String mappings;
	
	public ForgeModelImpl(String version, String mappings) 
	{
		this.version = version;
		this.mappings = mappings;
	}
	
	@Override
	public String getVersion() 
	{
		return version;
	}
	
	@Override
	public String getMappings() 
	{
		return mappings;
	}
}
