package leviathan143.fantasticchainsaw.util;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;

public class TypeHelper 
{
	public static boolean isOfType(Expression expression, IType typeIn)
	{
		if(expression instanceof Name || expression instanceof ArrayAccess)
		{
			ITypeBinding typeBinding = expression.resolveTypeBinding();
			
			if(typeBinding.isArray())
			{
				return false;
			}
			return typeBinding.getJavaElement().equals(typeIn);
		}
		else if(expression instanceof MethodInvocation)
		{
			MethodInvocation methodInvocation = (MethodInvocation) expression;
			IMethodBinding typeBinding = methodInvocation.resolveMethodBinding();
			ITypeBinding returnTypeBinding = typeBinding.getReturnType();
			if(returnTypeBinding.isArray()) 
			{
				return false;
			}
			return returnTypeBinding.getJavaElement().equals(typeIn);
		}
		return false;
	}
}
