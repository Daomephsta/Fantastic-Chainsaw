package leviathan143.fantasticchainsaw.interfaces.minecraft.model;

import com.google.gson.JsonElement;

public class BlockModel extends JSONModel
{
	@Override
	protected void deserialise(JsonElement jsonElement)
	{
		this.type = ModelType.BLOCK;
	}

	@Override
	protected void serialise(JsonElement jsonElement)
	{

	}
}
