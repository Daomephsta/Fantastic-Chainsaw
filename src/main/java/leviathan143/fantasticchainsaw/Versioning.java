package leviathan143.fantasticchainsaw;

public class Versioning 
{
	private static final int MAX_VERSION_SEGMENTS = 3;

	public static interface IVersionConstraint
	{
		public boolean acceptsVersion(Version v);
	}

	public static class Version
	{
		//It's unlikely Mojang will ever reach three digit version numbers, so this should do
		private static final int DEFAULT_NORMALISE_WIDTH = 3;
		
		private String versionString;
		private final long normal;

		public Version(String versionString) 
		{
			this.versionString = versionString;
			normal = calculateNormal(versionString);
		}
		
		private static long calculateNormal(String versionString)
		{
			return calculateNormal(versionString, DEFAULT_NORMALISE_WIDTH);
		}
		
		private static long calculateNormal(String version, int width)
		{

			String[] versionSegments = version.split("\\.");
			if(versionSegments.length > MAX_VERSION_SEGMENTS || versionSegments.length == 0 || versionSegments.length == 1) throw new IllegalArgumentException(version + " is not a valid version");
			String main = versionSegments[0];
			String major = versionSegments[1];
			String minor = "000";
			if(versionSegments.length > 2) minor = versionSegments[2];
			
			while(main.length() < width)
				main = "0" + main;
			while(major.length() < width)
				major = "0" + major;
			while(minor.length() < width)
				minor = "0" + minor;
			//TODO Investigate creating my own base 10 long parser for increased performance
			return Long.parseLong(main + major + minor);
		}
		
		public long getNormal() 
		{
			return normal;
		}
		
		@Override
		public boolean equals(Object obj) 
		{
			if(obj == this) return true;
			if(obj instanceof Version)
			{
				return ((Version) obj).normal == this.normal; 
			}
			return false;
		}
		
		@Override
		public String toString() 
		{
			return versionString;
		}
	}
	
	public static IVersionConstraint createVersionConstraint(String versionString)
	{
		//Maximum version
		if(versionString.endsWith("-"))
		{
			return new MaximumVersion(versionString.substring(0, versionString.length() - 1));
		}
		//Minimum version
		if(versionString.endsWith("+"))
		{
			return new MinimumVersion(versionString.substring(0, versionString.length() - 1));
		}
		//Version range
		if(versionString.contains("-"))
		{
			String[] versions = versionString.split("-");
			return new VersionRange(versions[0], versions[1]);
		}
		//Specific version
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
			return v.getNormal() >= minimumVersion.getNormal() && v.getNormal() <= maximumVersion.getNormal();
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
			return v.getNormal() >= minimumVersion.getNormal();
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

		public MaximumVersion(String maximumVersion) {
			this.maximumVersion = new Version(maximumVersion);
		}

		@Override
		public boolean acceptsVersion(Version v) 
		{
			return v.getNormal() <= maximumVersion.getNormal();
		}
		
		@Override
		public String toString() 
		{
			return maximumVersion + "-";
		}
	}
}
