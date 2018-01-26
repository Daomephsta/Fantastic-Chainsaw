package leviathan143.fantasticchainsaw.interfaces.minecraft.model;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class Vec4i
{
    public int w, x, y, z;
    
    public Vec4i(int w, int x, int y, int z)
    {
	this.w = w;
	this.x = x;
	this.y = y;
	this.z = z;
    }
    
    public static class Serialiser implements JsonSerializer<Vec4i>, JsonDeserializer<Vec4i>
    {
	@Override
	public Vec4i deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException
	{
	    JsonArray array = element.getAsJsonArray();
	    int w = array.get(0).getAsInt();
	    int x = array.get(1).getAsInt();
	    int y = array.get(2).getAsInt();
	    int z = array.get(3).getAsInt();
	    return new Vec4i(w, x, y, z);
	}

	@Override
	public JsonElement serialize(Vec4i vector, Type type, JsonSerializationContext context)
	{
	    JsonArray array = new JsonArray();
	    array.add(vector.w);
	    array.add(vector.x);
	    array.add(vector.y);
	    array.add(vector.z);
	    return array;
	}	
    }
}
