package leviathan143.fantasticchainsaw.interfaces.minecraft.model;

import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;

import leviathan143.fantasticchainsaw.interfaces.minecraft.ResourceLocation;

public class ItemModel extends JSONModel
{
    private List<ModelOverride> overrides;
    
    @Override
    protected void deserialise(JsonElement jsonElement)
    {
	this.type = ModelType.ITEM;
    }

    @Override
    protected void serialise(JsonElement jsonElement)
    {
	
    }
    
    public class ModelOverride
    {
	private Map<ResourceLocation, Float> predicate;
	public JSONModel model;
    }
}
