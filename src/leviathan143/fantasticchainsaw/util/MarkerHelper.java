package leviathan143.fantasticchainsaw.util;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import leviathan143.fantasticchainsaw.FantasticPlugin;

public class MarkerHelper
{
	public static IMarker createNormalError(IResource resource, String message, int line)
	{
		return createError(resource, message, line, IMarker.PRIORITY_NORMAL);
	}
	
	public static IMarker createError(IResource resource, String message, int line, int priority)
	{
		return createMarker(resource, message, line, IMarker.SEVERITY_ERROR, priority);
	}
	
	public static IMarker createNormalWarning(IResource resource, String message, int line)
	{
		return createWarning(resource, message, line, IMarker.PRIORITY_NORMAL);
	}
	
	public static IMarker createWarning(IResource resource, String message, int line, int priority)
	{
		return createMarker(resource, message, line, IMarker.SEVERITY_WARNING, priority);
	}
	
	public static IMarker createMarker(IResource resource, String message, int line, int severity, int priority)
	{
		try
		{
			IMarker marker = resource.createMarker(IMarker.PROBLEM);
			if(marker.exists())
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
