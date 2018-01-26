package leviathan143.fantasticchainsaw.util;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class TypeHelper
{
    public static String ITEMSTACK_NAME = "net.minecraft.item.ItemStack";

    public static boolean isOfType(ASTNode node, IType typeIn)
    {
	IBinding binding = ASTHelper.getTypeBinding(node);
	return isOfType(binding, typeIn);
    }

    public static boolean isOfType(IBinding binding, IType typeIn)
    {
	if (binding instanceof ITypeBinding)
	{
	    if (((ITypeBinding) binding).isArray())
	    {
		return false;
	    }
	    return binding.getJavaElement().equals(typeIn);
	}
	else if (binding instanceof IMethodBinding)
	{
	    ITypeBinding returnTypeBinding = ((IMethodBinding) binding).getReturnType();
	    if (returnTypeBinding.isArray())
	    {
		return false;
	    }
	    return returnTypeBinding.getJavaElement().equals(typeIn);
	}
	return false;
    }
}
