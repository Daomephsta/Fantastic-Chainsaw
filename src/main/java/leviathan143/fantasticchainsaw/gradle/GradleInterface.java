package leviathan143.fantasticchainsaw.gradle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

public class GradleInterface 
{	
	public static ForgeModel getModel(IJavaProject project)
	{
		ProjectConnection connection = GradleConnector.newConnector()
				.forProjectDirectory(project.getResource().getLocation().toFile())
				.connect();
		
		InputStream initScriptStream = GradleInterface.class.getResourceAsStream("fantastic-chainsaw-init.gradle");
		try
		{
			IPath state = Platform.getStateLocation(Platform.getBundle("leviathan143.fantasticchainsaw"));
			File initScriptDest = new File(state.toFile(), "fantastic-chainsaw-init.gradle");
			
			if(!initScriptDest.exists())
			{
				Files.copy(initScriptStream, initScriptDest.toPath());
				initScriptStream.close();
			}
			
			return connection.model(ForgeModel.class).withArguments("--init-script", initScriptDest.getCanonicalPath()).get();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		finally 
		{
			connection.close();
		}
		return null;
	}
}
