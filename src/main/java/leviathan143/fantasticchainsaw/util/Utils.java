package leviathan143.fantasticchainsaw.util;

import java.util.HashMap;
import java.util.Map;

public class Utils
{
	public static <K, V> Map<K, V> createMapFromArrays(K[] argKeys, V[] argValues)
	{
		if (argKeys.length != argValues.length)
			throw new IllegalArgumentException("Key and value arrays must have the same length");
		Map<K, V> map = new HashMap<K, V>();
		for (int e = 0; e < argKeys.length; e++)
		{
			map.put(argKeys[e], argValues[e]);
		}
		return map;
	}
}
