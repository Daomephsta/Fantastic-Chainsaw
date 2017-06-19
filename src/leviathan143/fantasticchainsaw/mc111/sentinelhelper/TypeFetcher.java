package leviathan143.fantasticchainsaw.mc111.sentinelhelper;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

public class TypeFetcher
{
	static boolean typesFetched;
	static IType ITEMSTACK_TYPE = null;
	static IType NULLABLE_ANNOTATION_TYPE = null;

	public static void fetchTypes(IJavaProject project)
	{
		if(typesFetched) return;
		System.out.println("Fetching types");
		try
		{
			ITEMSTACK_TYPE = project.findType("net.minecraft.item.ItemStack");
			NULLABLE_ANNOTATION_TYPE = project.findType("javax.annotation.Nullable");
			System.out.println("Done");
		} 
		catch (JavaModelException e)
		{
			e.printStackTrace();
		}
	}
}
