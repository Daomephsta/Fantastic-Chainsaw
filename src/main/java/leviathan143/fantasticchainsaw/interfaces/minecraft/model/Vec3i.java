package leviathan143.fantasticchainsaw.interfaces.minecraft.model;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class Vec3i
{
	public int x, y, z;

	public Vec3i(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public static class Serialiser implements JsonSerializer<Vec3i>, JsonDeserializer<Vec3i>
	{
		@Override
		public Vec3i deserialize(JsonElement element, Type type, JsonDeserializationContext context)
				throws JsonParseException
		{
			JsonArray array = element.getAsJsonArray();
			int x = array.get(0).getAsInt();
			int y = array.get(1).getAsInt();
			int z = array.get(2).getAsInt();
			return new Vec3i(x, y, z);
		}

		@Override
		public JsonElement serialize(Vec3i vector, Type type, JsonSerializationContext context)
		{
			JsonArray array = new JsonArray();
			array.add(vector.x);
			array.add(vector.y);
			array.add(vector.z);
			return array;
		}
	}
}
