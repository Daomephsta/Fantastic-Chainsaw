package leviathan143.fantasticchainsaw;

import java.lang.reflect.Type;
import java.util.regex.Pattern;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class Versioning
{
    public static final Version V1_11_2 = new Version("1.11.2");
    public static final Version V1_11 = new Version("1.11");
    public static final Version V1_10_2 = new Version("1.10.2");
    public static final Version V1_10 = new Version("1.10");
    public static final Version V1_9_4 = new Version("1.9.4");
    public static final Version V1_9 = new Version("1.9");
    public static final Version V1_8_9 = new Version("1.8.9");
    public static final Version V1_8 = new Version("1.8");
    public static final Version V1_7_10 = new Version("1.7.10");
    public static final Version V1_7_2 = new Version("1.7.2");

    private static final Pattern PERIOD = Pattern.compile("\\.");

    public static interface IVersionConstraint
    {
	public boolean acceptsVersion(Version v);
    }

    // TODO Benchmark this whole thing
    public static class Version implements Comparable<Version>
    {
	private String versionString;

	public Version(String versionString)
	{
	    this.versionString = versionString;
	}

	public static boolean isValidVersionString(String version)
	{
	    if (!version.contains(".")) return false;
	    for (char c : version.toCharArray())
	    {
		if (!Character.isDigit(c) && !Character.valueOf('.').equals(c)) return false;
	    }
	    return true;
	}

	public long calculateNormal(int width)
	{
	    return calculateNormal(width, this.getSegmentCount());
	}

	private long calculateNormal(int width, int segments)
	{
	    String[] versionSegments = PERIOD.split(versionString);
	    if (!isValidVersionString(versionString))
		throw new IllegalArgumentException(versionString + " is not a valid version");
	    StringBuilder normalBuilder = new StringBuilder();
	    for (String segment : versionSegments)
	    {
		while (segment.length() < width)
		{
		    segment = "0" + segment;
		}
		normalBuilder.append(segment);
	    }
	    if (versionSegments.length < segments)
	    {
		for (int i = 0; i < (segments - versionSegments.length) * width; i++)
		{
		    normalBuilder.append("0");
		}
	    }
	    // TODO Investigate creating my own base 10 long parser for increased
	    // performance
	    return Long.parseLong(normalBuilder.toString());
	}

	private int calculateMaxSegmentWidth()
	{
	    int maxWidth = 0;
	    for (String segment : PERIOD.split(versionString))
	    {
		if (segment.length() > maxWidth) maxWidth = segment.length();
	    }
	    return maxWidth;
	}

	private int getSegmentCount()
	{
	    return PERIOD.split(versionString).length;
	}

	@Override
	public boolean equals(Object obj)
	{
	    if (obj == this) return true;
	    if (obj instanceof Version)
	    {
		return this.compareTo((Version) obj) == 0;
	    }
	    return false;
	}

	@Override
	public String toString()
	{
	    return versionString;
	}

	public static class Serialiser implements JsonSerializer<Version>, JsonDeserializer<Version>
	{
	    @Override
	    public Version deserialize(JsonElement json, Type type, JsonDeserializationContext context)
		    throws JsonParseException
	    {
		return new Version(json.getAsString());
	    }

	    @Override
	    public JsonElement serialize(Version version, Type type, JsonSerializationContext context)
	    {
		return new JsonPrimitive(version.versionString);
	    }
	}

	@Override
	public int compareTo(Version other)
	{
	    int width = Math.max(this.calculateMaxSegmentWidth(), other.calculateMaxSegmentWidth());
	    int segments = Math.max(this.getSegmentCount(), other.getSegmentCount());
	    long thisNormal = this.calculateNormal(width, segments);
	    long otherNormal = other.calculateNormal(width, segments);
	    return thisNormal < otherNormal ? -1 : (thisNormal == otherNormal ? 0 : 1);
	}
    }

    public static IVersionConstraint createVersionConstraint(String versionString)
    {
	// Maximum version
	if (versionString.endsWith("-"))
	{
	    return new MaximumVersion(versionString.substring(0, versionString.length() - 1));
	}
	// Minimum version
	if (versionString.endsWith("+"))
	{
	    return new MinimumVersion(versionString.substring(0, versionString.length() - 1));
	}
	// Version range
	if (versionString.contains("-"))
	{
	    String[] versions = versionString.split("-");
	    return new VersionRange(versions[0], versions[1]);
	}
	// Specific version
	return new SpecificVersion(versionString);
    }

    private static class SpecificVersion implements IVersionConstraint
    {
	private final Version targetVersion;

	public SpecificVersion(String targetVersion)
	{
	    this.targetVersion = new Version(targetVersion);
	}

	@Override
	public boolean acceptsVersion(Version v)
	{
	    return targetVersion.equals(v);
	}

	@Override
	public String toString()
	{
	    return targetVersion.toString();
	}
    }

    private static class VersionRange implements IVersionConstraint
    {
	private final Version minimumVersion;
	private final Version maximumVersion;

	public VersionRange(String minimum, String maximum)
	{
	    this.minimumVersion = new Version(minimum);
	    this.maximumVersion = new Version(maximum);
	}

	@Override
	public boolean acceptsVersion(Version v)
	{
	    return v.compareTo(minimumVersion) >= 0 && v.compareTo(maximumVersion) <= 0;
	}

	@Override
	public String toString()
	{
	    return minimumVersion + "-" + maximumVersion;
	}
    }

    private static class MinimumVersion implements IVersionConstraint
    {
	private final Version minimumVersion;

	public MinimumVersion(String minimumVersion)
	{
	    this.minimumVersion = new Version(minimumVersion);
	}

	@Override
	public boolean acceptsVersion(Version v)
	{
	    return v.compareTo(minimumVersion) >= 0;
	}

	@Override
	public String toString()
	{
	    return minimumVersion + "+";
	}
    }

    private static class MaximumVersion implements IVersionConstraint
    {
	private final Version maximumVersion;

	public MaximumVersion(String maximumVersion)
	{
	    this.maximumVersion = new Version(maximumVersion);
	}

	@Override
	public boolean acceptsVersion(Version v)
	{
	    return v.compareTo(maximumVersion) <= 0;
	}

	@Override
	public String toString()
	{
	    return maximumVersion + "-";
	}
    }
}
