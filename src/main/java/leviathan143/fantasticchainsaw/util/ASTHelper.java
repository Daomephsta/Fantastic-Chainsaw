package leviathan143.fantasticchainsaw.util;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;

import leviathan143.fantasticchainsaw.base.TypeFetcher;

public class ASTHelper
{

    public static boolean hasNullableAnnotation(ASTNode node)
    {
	IBinding binding = getBinding(node);
	if (binding == null) return false;
	return hasNullableAnnotation(binding);
    }

    public static boolean hasNullableAnnotation(IBinding binding)
    {
	for (IAnnotationBinding annotationBinding : binding.getAnnotations())
	{
	    if (annotationBinding.getAnnotationType().getJavaElement().equals(TypeFetcher.NULLABLE_ANNOTATION_TYPE))
		return true;
	}
	return false;
    }

    public static IBinding getBinding(ASTNode node)
    {
	if (node instanceof Expression)
	{
	    Expression expression = (Expression) node;
	    IBinding binding = expression.resolveTypeBinding();
	    if (expression instanceof MethodInvocation)
		binding = ((MethodInvocation) expression).resolveMethodBinding();
	    else if (expression instanceof Name) binding = ((Name) expression).resolveBinding();
	    return binding;
	}
	else if (node instanceof MethodDeclaration)
	{
	    return ((MethodDeclaration) node).resolveBinding();
	}
	else return null;
    }

    public static IBinding getTypeBinding(ASTNode node)
    {
	if (node instanceof Expression)
	{
	    Expression expression = (Expression) node;
	    IBinding binding = expression.resolveTypeBinding();
	    if (expression instanceof MethodInvocation)
		binding = ((MethodInvocation) expression).resolveMethodBinding();
	    return binding;
	}
	else if (node instanceof MethodDeclaration)
	{
	    return ((MethodDeclaration) node).resolveBinding();
	}
	else return null;
    }

    public static ASTNode getParentOfType(ASTNode node, int type)
    {
	ASTNode matchingParent = node.getParent();
	int tries = 0;
	while (tries < 100 && matchingParent.getNodeType() != type)
	{
	    tries++;
	    matchingParent = matchingParent.getParent();
	}
	return matchingParent;
    }
}
