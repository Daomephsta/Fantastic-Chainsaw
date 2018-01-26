package leviathan143.fantasticchainsaw.util;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import leviathan143.fantasticchainsaw.FantasticPlugin;

public class MarkerHelper
{
    public static final String SENTINEL_ISSUE = "leviathan143.fantasticchainsaw.mc111.sentinelissuemarker";

    public static IMarker createSentinelIssueMarker(IResource resource, String message, int line)
    {
	return createMarker(resource, SENTINEL_ISSUE, message, line, IMarker.SEVERITY_WARNING, IMarker.PRIORITY_NORMAL);
    }

    public static IMarker createMarker(IResource resource, String id, String message, int line, int severity,
	    int priority)
    {
	try
	{
	    IMarker marker = resource.createMarker(id);
	    if (marker.exists())
	    {
		marker.setAttribute(IMarker.LINE_NUMBER, line);
		marker.setAttribute(IMarker.MESSAGE, message);
		marker.setAttribute(IMarker.SEVERITY, severity);
		marker.setAttribute(IMarker.PRIORITY, priority);
		marker.setAttribute(IMarker.SOURCE_ID, FantasticPlugin.NAME);
	    }
	    return marker;
	}
	catch (CoreException e)
	{
	    e.printStackTrace();
	}
	return null;
    }
}
