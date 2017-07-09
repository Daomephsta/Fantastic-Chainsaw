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
		private final int version, major, minor;

		public Version(String version) 
		{
			String[] versionSegments = version.split("\\.");
			if(versionSegments.length > MAX_VERSION_SEGMENTS || versionSegments.length == 0 || versionSegments.length == 1) throw new IllegalArgumentException(version + " is not a valid version");
			this.version = parseVersionSegment(versionSegments[0]);
			this.major = parseVersionSegment(versionSegments[1]);
			if(versionSegments.length > 2) this.minor = parseVersionSegment(versionSegments[2]);
			else minor = 0;
		}

		private static int parseVersionSegment(String segment)
		{
			return Integer.parseInt(segment);
		}
		
		@Override
		public String toString() 
		{
			return version + "." + major + "." + minor;
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
			if(v.version < minimumVersion.version || v.version > maximumVersion.version) return false;
			if(v.major < minimumVersion.major || v.major > maximumVersion.major) return false;
			if(v.minor < minimumVersion.minor || v.minor > maximumVersion.minor) return false;
			return true;
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
			if(v.version < minimumVersion.version || v.major < minimumVersion.major || v.minor < minimumVersion.minor) return false;
			return true;
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
			if(v.version > maximumVersion.version || v.major > maximumVersion.major || v.minor > maximumVersion.minor) return false;
			return true;
		}
		
		@Override
		public String toString() 
		{
			return maximumVersion + "-";
		}
	}
}
