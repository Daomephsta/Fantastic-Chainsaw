package leviathan143.fantasticchainsaw.mc111.sentinelhelper;


import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import leviathan143.fantasticchainsaw.util.ASTHelper;
import leviathan143.fantasticchainsaw.util.MarkerHelper;
import leviathan143.fantasticchainsaw.util.TypeHelper;

public class SentinelIssueFinder extends SentinelIssueTool
{
	public SentinelIssueFinder() 
	{
		super("Find sentinel issues");
	}

	@Override
	protected void performTask(CompilationUnit compUnit, ICompilationUnit comp) throws CoreException
	{
		IResource baseResource = comp.getResource();
		baseResource.deleteMarkers(MarkerHelper.SENTINEL_ISSUE, true, IResource.DEPTH_INFINITE);

		ItemStackNullCheckFinder stackNullCheckFinder = new ItemStackNullCheckFinder();
		compUnit.accept(stackNullCheckFinder);
		for(ASTNode node : stackNullCheckFinder.matchingNodes)
		{
			MarkerHelper.createSentinelIssueMarker(baseResource, String.format("%1$s is of type ItemStack, which is non-nullable! Use ItemStack#isEmpty() instead of null-checking the returned value.", node.toString()), compUnit.getLineNumber(node.getStartPosition()));
		}

		ItemStackNullAssignmentFinder stackNullAssignmentFinder = new ItemStackNullAssignmentFinder();
		compUnit.accept(stackNullAssignmentFinder);
		for(ASTNode node : stackNullAssignmentFinder.matchingNodes)
		{
			MarkerHelper.createSentinelIssueMarker(baseResource, String.format("%1$s is of type ItemStack, which is non-nullable! Use ItemStack.EMPTY instead of null.", node.toString()), compUnit.getLineNumber(node.getStartPosition()));
		}

		ItemStackNullReturnFinder nullReturnFinder = new ItemStackNullReturnFinder();
		compUnit.accept(nullReturnFinder);
		for(ASTNode node : nullReturnFinder.matchingNodes)
		{
			MarkerHelper.createSentinelIssueMarker(baseResource, String.format("%1$s is of type ItemStack, which is non-nullable! Return ItemStack.EMPTY instead of null.", ASTHelper.getParentOfType(node, ASTNode.METHOD_DECLARATION).getStructuralProperty(MethodDeclaration.NAME_PROPERTY)), compUnit.getLineNumber(node.getStartPosition()));
		}

		ItemStackNullParameterFinder nullParameterFinder = new ItemStackNullParameterFinder();
		compUnit.accept(nullParameterFinder);
		for(Entry<ASTNode, String> nodeNodeNamePair : nullParameterFinder.matchingNodes)
		{
			MarkerHelper.createSentinelIssueMarker(baseResource, String.format("Parameter %1$s is of type ItemStack, which is non-nullable! Use ItemStack.EMPTY instead of null.", nodeNodeNamePair.getValue()), compUnit.getLineNumber(nodeNodeNamePair.getKey().getStartPosition()));
		}
	}


	public static class ItemStackNullCheckFinder extends ASTVisitor
	{
		List<ASTNode> matchingNodes = new ArrayList<ASTNode>();

		@Override
		public boolean visit(InfixExpression node)
		{
			if(node.getOperator() == Operator.EQUALS || node.getOperator() == Operator.NOT_EQUALS)
			{
				Expression left = node.getLeftOperand();
				Expression right = node.getRightOperand();
				if(left instanceof NullLiteral && TypeHelper.isOfType(right, TypeFetcher.ITEMSTACK_TYPE) && !ASTHelper.hasNullableAnnotation(right))
				{
					matchingNodes.add(right);
				}
				else if(right instanceof NullLiteral && TypeHelper.isOfType(left, TypeFetcher.ITEMSTACK_TYPE) && !ASTHelper.hasNullableAnnotation(left))
				{
					matchingNodes.add(left);
				}
			}
			return node.getOperator() == Operator.CONDITIONAL_AND || node.getOperator() == Operator.CONDITIONAL_OR;
		}
	}

	public static class ItemStackNullAssignmentFinder extends ASTVisitor
	{
		List<ASTNode> matchingNodes = new ArrayList<ASTNode>();

		@Override
		public boolean visit(VariableDeclarationFragment node) 
		{
			if(node.getInitializer() instanceof NullLiteral && TypeHelper.isOfType(node.getName(), TypeFetcher.ITEMSTACK_TYPE) && !ASTHelper.hasNullableAnnotation(node.getName()))
			{
				matchingNodes.add(node.getName());
			}
			return false;
		}

		@Override
		public boolean visit(Assignment node)
		{
			if(node.getOperator() != Assignment.Operator.ASSIGN) return false;
			if(node.getRightHandSide() instanceof NullLiteral && TypeHelper.isOfType(node.getLeftHandSide(), TypeFetcher.ITEMSTACK_TYPE) && !ASTHelper.hasNullableAnnotation(node.getLeftHandSide()))
			{
				matchingNodes.add(node.getLeftHandSide());
			}	
			return false;
		}
	}

	public static class ItemStackNullReturnFinder extends ASTVisitor
	{
		List<ASTNode> matchingNodes = new ArrayList<ASTNode>();

		@Override
		public boolean visit(ReturnStatement node) 
		{
			if(node.getExpression() instanceof NullLiteral)
			{
				MethodDeclaration parentMethod = (MethodDeclaration) ASTHelper.getParentOfType(node, ASTNode.METHOD_DECLARATION);
				if(TypeHelper.isOfType(parentMethod, TypeFetcher.ITEMSTACK_TYPE) && !ASTHelper.hasNullableAnnotation(parentMethod))
				{
					matchingNodes.add(node);
				}
			}
			return false;
		}
	}	

	public static class ItemStackNullParameterFinder extends ASTVisitor
	{
		List<Map.Entry<ASTNode, String>> matchingNodes = new ArrayList<Map.Entry<ASTNode, String>>();

		@Override
		public boolean visit(MethodInvocation node) 
		{
			ITypeBinding[] paramTypes = node.resolveMethodBinding().getParameterTypes();
			//Get an IMethod from the IMethodBinding
			IMethodBinding declarationBinding = node.resolveMethodBinding().getMethodDeclaration();
			//Use the IMethod to get the parameter names
			try 
			{ 
				String[] paramNames = ((IMethod) declarationBinding.getJavaElement()).getParameterNames();

				for(int argIndex = 0; argIndex < node.arguments().size(); argIndex++)
				{
					Expression arg = (Expression) node.arguments().get(argIndex);

					if(arg instanceof NullLiteral)
					{	
						ITypeBinding paramType = paramTypes[argIndex];
						boolean hasNullableAnnotation = false;
						for(IAnnotationBinding annotationBinding : declarationBinding.getParameterAnnotations(argIndex))
						{
							if(annotationBinding.getAnnotationType().getJavaElement().equals(TypeFetcher.NULLABLE_ANNOTATION_TYPE)) hasNullableAnnotation = true;
						}

						if(TypeHelper.isOfType(paramType, TypeFetcher.ITEMSTACK_TYPE) && !hasNullableAnnotation)
						{
							//Finally, use the index of the arg to retrieve the parameter name
							matchingNodes.add(new AbstractMap.SimpleEntry<ASTNode, String>(node, paramNames[argIndex]));
						}
					}
				} 
			} 
			catch (JavaModelException e) 
			{
				e.printStackTrace();
			}
			return false;
		}
	}
}
