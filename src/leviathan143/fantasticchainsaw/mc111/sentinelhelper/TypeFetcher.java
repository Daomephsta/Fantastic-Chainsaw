package leviathan143.fantasticchainsaw.mc111.sentinelhelper;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import leviathan143.fantasticchainsaw.util.TypeHelper;

public class TypeFetcher
{
	static boolean typesFetched;
	public static IType ITEMSTACK_TYPE = null;
	public static IType NULLABLE_ANNOTATION_TYPE = null;

	public static void fetchTypes(IJavaProject project)
	{
		if(typesFetched) return;
		System.out.println("Fetching types");
		long startTime = System.currentTimeMillis();
		try
		{
			ITEMSTACK_TYPE = project.findType(TypeHelper.ITEMSTACK_NAME);
			NULLABLE_ANNOTATION_TYPE = project.findType("javax.annotation.Nullable");
			System.out.println("Done in " + (System.currentTimeMillis() - startTime) + " ms");
		} 
		catch (JavaModelException e)
		{
			e.printStackTrace();
		}
	}
}
