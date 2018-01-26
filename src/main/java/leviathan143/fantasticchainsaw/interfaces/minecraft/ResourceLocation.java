package leviathan143.fantasticchainsaw.interfaces.minecraft;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ResourceLocation
{
    private String domain;
    private String path;

    public ResourceLocation(String location)
    {
	if(location.contains(":"))
	{
	    String[] split = location.split(":");
	    domain = split[0];
	    path = split[1];
	}
	else
	{
	    domain = "minecraft";
	    path = location;
	}
    }

    public ResourceLocation(String domain, String path)
    {
	this.domain = domain;
	this.path = path;
    }

    @Override
    public String toString()
    {
	return domain + ":" + path;
    }

    public String getDomain()
    {
	return domain;
    }

    public String getPath()
    {
	return path;
    }

    public static class Serialiser implements JsonSerializer<ResourceLocation>, JsonDeserializer<ResourceLocation>
    {
	@Override
	public ResourceLocation deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException
	{
	    return new ResourceLocation(element.getAsString());
	}

	@Override
	public JsonElement serialize(ResourceLocation resLoc, Type type, JsonSerializationContext context)
	{
	    return new JsonPrimitive(resLoc.toString());
	}

    }
}
