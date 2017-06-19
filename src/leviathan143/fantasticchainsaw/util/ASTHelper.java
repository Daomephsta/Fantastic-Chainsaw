package leviathan143.fantasticchainsaw.util;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import leviathan143.fantasticchainsaw.mc111.sentinelhelper.TypeFetcher;

public class ASTHelper 
{
	public static boolean hasNullableAnnotation(Expression expression)
	{
		IBinding typeBinding = expression.resolveTypeBinding();
		if(expression instanceof MethodInvocation) typeBinding = ((MethodInvocation) expression).resolveMethodBinding();
		for(IAnnotationBinding annotationBinding : typeBinding.getAnnotations())
		{
			if(annotationBinding.getAnnotationType().getJavaElement().equals(TypeFetcher.NULLABLE_ANNOTATION_TYPE)) return true;
		}
		return false;
	}
	
	public static ASTNode getParentOfType(ASTNode node)
	{
		return null;
	}
}
