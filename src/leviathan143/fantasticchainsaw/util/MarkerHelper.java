package leviathan143.fantasticchainsaw.util;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class MarkerHelper
{
	public static void createNormalError(IResource resource, String message, int line)
	{
		createError(resource, message, line, IMarker.PRIORITY_NORMAL);
	}
	
	public static void createError(IResource resource, String message, int line, int priority)
	{
		createMarker(resource, message, line, IMarker.SEVERITY_ERROR, priority);
	}
	
	public static void createNormalWarning(IResource resource, String message, int line)
	{
		createWarning(resource, message, line, IMarker.PRIORITY_NORMAL);
	}
	
	public static void createWarning(IResource resource, String message, int line, int priority)
	{
		createMarker(resource, message, line, IMarker.SEVERITY_WARNING, priority);
	}
	
	public static void createMarker(IResource resource, String message, int line, int severity, int priority)
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
			}
		} 
		catch (CoreException e)
		{
			e.printStackTrace();
		}
	}
}
