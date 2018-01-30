package leviathan143.fantasticchainsaw.base;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.console.MessageConsoleStream;

import leviathan143.fantasticchainsaw.FantasticPlugin;
import leviathan143.fantasticchainsaw.util.EclipseHelper;
import leviathan143.fantasticchainsaw.util.TypeHelper;

public class TypeFetcher
{
	static boolean typesFetched;
	public static IType ITEMSTACK_TYPE = null;
	public static IType NULLABLE_ANNOTATION_TYPE = null;

	public static void fetchTypes(IJavaProject project)
	{
		if (typesFetched) return;
		MessageConsoleStream consoleStream = EclipseHelper.getOrCreateConsole(FantasticPlugin.NAME).newMessageStream();

		consoleStream.println("Fetching types");
		long startTime = System.currentTimeMillis();
		try
		{
			ITEMSTACK_TYPE = project.findType(TypeHelper.ITEMSTACK_NAME);
			NULLABLE_ANNOTATION_TYPE = project.findType("javax.annotation.Nullable");
			consoleStream.println("Done in " + (System.currentTimeMillis() - startTime) + " ms");
			typesFetched = true;
		}
		catch (JavaModelException e)
		{
			e.printStackTrace();
		}
	}
}
