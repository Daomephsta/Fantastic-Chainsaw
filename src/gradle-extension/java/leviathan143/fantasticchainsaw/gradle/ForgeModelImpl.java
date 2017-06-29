package leviathan143.fantasticchainsaw.gradle;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ForgeModelImpl implements ForgeModel, Serializable
{
	private final String version;
	
	public ForgeModelImpl(String version) 
	{
		this.version = version;
	}
	
	@Override
	public String getVersion() 
	{
		return version;
	}
}
