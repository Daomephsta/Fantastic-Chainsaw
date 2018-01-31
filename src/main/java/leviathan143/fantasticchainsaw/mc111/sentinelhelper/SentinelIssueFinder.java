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
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;

import com.google.common.collect.Maps;

import leviathan143.fantasticchainsaw.base.ASTDependentTool;
import leviathan143.fantasticchainsaw.base.TypeFetcher;
import leviathan143.fantasticchainsaw.util.ASTHelper;
import leviathan143.fantasticchainsaw.util.MarkerHelper;
import leviathan143.fantasticchainsaw.util.TypeHelper;

public class SentinelIssueFinder extends ASTDependentTool
{
	public static final String NAME = "leviathan143.fantasticchainsaw.mc111.findSentinelIssues";

	public SentinelIssueFinder()
	{
		super("Find sentinel issues");
	}

	@SuppressWarnings("unchecked")
	public static void markIssues(CompilationUnit compUnit, ICompilationUnit comp) throws CoreException
	{
		IResource baseResource = comp.getResource();
		baseResource.deleteMarkers(MarkerHelper.SENTINEL_ISSUE, true, IResource.DEPTH_INFINITE);

		Map<IssueType, List<?>> issues = findIssues(compUnit);

		for (ASTNode node : (List<ASTNode>) issues.get(IssueType.NULL_CHECK))
		{
			MarkerHelper.createSentinelIssueMarker(baseResource, String.format(
					"%1$s is of type ItemStack, which is non-nullable! Use ItemStack#isEmpty() instead of null-checking the returned value.",
					node.toString()), compUnit.getLineNumber(node.getStartPosition()));
		}

		for (ASTNode node : (List<ASTNode>) issues.get(IssueType.NULL_ASSIGNMENT))
		{
			MarkerHelper.createSentinelIssueMarker(baseResource,
					String.format(
							"%1$s is of type ItemStack, which is non-nullable! Use ItemStack.EMPTY instead of null.",
							node.toString()),
					compUnit.getLineNumber(node.getStartPosition()));
		}

		for (ASTNode node : (List<ASTNode>) issues.get(IssueType.NULL_RETURN))
		{
			MarkerHelper.createSentinelIssueMarker(baseResource,
					String.format(
							"%1$s is of type ItemStack, which is non-nullable! Return ItemStack.EMPTY instead of null.",
							ASTHelper.getParentOfType(node, ASTNode.METHOD_DECLARATION)
									.getStructuralProperty(MethodDeclaration.NAME_PROPERTY)),
					compUnit.getLineNumber(node.getStartPosition()));
		}

		for (Entry<ASTNode, String> nodeNodeNamePair : (List<Entry<ASTNode, String>>) issues
				.get(IssueType.NULL_PARAMETER))
		{
			MarkerHelper.createSentinelIssueMarker(baseResource, String.format(
					"Parameter %s is of type ItemStack, which is non-nullable! Use ItemStack.EMPTY instead of null.",
					nodeNodeNamePair.getValue()), compUnit.getLineNumber(nodeNodeNamePair.getKey().getStartPosition()));
		}

		for (ASTNode node : (List<ASTNode>) issues.get(IssueType.EMPTY_STACK_REF_CHECK))
		{
			MarkerHelper.createSentinelIssueMarker(baseResource,
					"Use ItemStack#isEmpty() instead of checking for reference equality with ItemStack.EMPTY",
					compUnit.getLineNumber(node.getStartPosition()));
		}
	}

	public static Map<IssueType, List<?>> findIssues(ASTNode node) throws CoreException
	{
		TypeFetcher.fetchTypes(null);
		Map<IssueType, List<?>> issues = Maps.newEnumMap(IssueType.class);

		ItemStackNullCheckFinder stackNullCheckFinder = new ItemStackNullCheckFinder();
		node.accept(stackNullCheckFinder);
		issues.put(IssueType.NULL_CHECK, stackNullCheckFinder.matchingNodes);

		ItemStackNullAssignmentFinder stackNullAssignmentFinder = new ItemStackNullAssignmentFinder();
		node.accept(stackNullAssignmentFinder);
		issues.put(IssueType.NULL_ASSIGNMENT, stackNullAssignmentFinder.matchingNodes);

		ItemStackNullReturnFinder nullReturnFinder = new ItemStackNullReturnFinder();
		node.accept(nullReturnFinder);
		issues.put(IssueType.NULL_RETURN, nullReturnFinder.matchingNodes);

		ItemStackNullParameterFinder nullParameterFinder = new ItemStackNullParameterFinder();
		node.accept(nullParameterFinder);
		issues.put(IssueType.NULL_PARAMETER, nullParameterFinder.matchingNodes);

		EmptyStackReferenceCheckFinder emptyStackReferenceCheckFinder = new EmptyStackReferenceCheckFinder();
		node.accept(emptyStackReferenceCheckFinder);
		issues.put(IssueType.EMPTY_STACK_REF_CHECK, emptyStackReferenceCheckFinder.matchingNodes);

		return issues;
	}

	@Override
	public void performTask(CompilationUnit compUnit, ICompilationUnit comp) throws CoreException
	{
		markIssues(compUnit, comp);
	}

	public static enum IssueType
	{
		NULL_CHECK, NULL_ASSIGNMENT, NULL_RETURN, NULL_PARAMETER, EMPTY_STACK_REF_CHECK;
	}

	public static class ItemStackNullCheckFinder extends ASTVisitor
	{
		List<ASTNode> matchingNodes = new ArrayList<ASTNode>();

		@Override
		public boolean visit(InfixExpression node)
		{
			if (node.getOperator() == Operator.EQUALS || node.getOperator() == Operator.NOT_EQUALS)
			{
				Expression left = node.getLeftOperand();
				Expression right = node.getRightOperand();
				if (left instanceof NullLiteral && TypeHelper.isOfType(right, TypeFetcher.ITEMSTACK_TYPE)
						&& !ASTHelper.hasNullableAnnotation(right))
				{
					matchingNodes.add(right);
				}
				else if (right instanceof NullLiteral && TypeHelper.isOfType(left, TypeFetcher.ITEMSTACK_TYPE)
						&& !ASTHelper.hasNullableAnnotation(left))
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
			if (node.getInitializer() instanceof NullLiteral
					&& TypeHelper.isOfType(node.getName(), TypeFetcher.ITEMSTACK_TYPE)
					&& !ASTHelper.hasNullableAnnotation(node.getName()))
			{
				matchingNodes.add(node.getName());
			}
			return false;
		}

		@Override
		public boolean visit(Assignment node)
		{
			if (node.getOperator() != Assignment.Operator.ASSIGN) return false;
			if (node.getRightHandSide() instanceof NullLiteral
					&& TypeHelper.isOfType(node.getLeftHandSide(), TypeFetcher.ITEMSTACK_TYPE)
					&& !ASTHelper.hasNullableAnnotation(node.getLeftHandSide()))
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
			if (node.getExpression() instanceof NullLiteral)
			{
				MethodDeclaration parentMethod = (MethodDeclaration) ASTHelper.getParentOfType(node,
						ASTNode.METHOD_DECLARATION);
				if (TypeHelper.isOfType(parentMethod, TypeFetcher.ITEMSTACK_TYPE)
						&& !ASTHelper.hasNullableAnnotation(parentMethod))
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
			// Get an IMethodBinding from the ITypeBinding
			IMethodBinding declarationBinding = node.resolveMethodBinding().getMethodDeclaration();
			/* Some methods, such as {@code Enum#valueOf()} don't have a java
			 * element attached for some reason. These methods can't be
			 * analysed. */
			if (declarationBinding.getJavaElement() == null) return false;
			try
			{
				// Use the IMethod to get the parameter names
				String[] paramNames = ((IMethod) declarationBinding.getJavaElement()).getParameterNames();

				for (int argIndex = 0; argIndex < node.arguments().size(); argIndex++)
				{
					Expression arg = (Expression) node.arguments().get(argIndex);

					if (arg instanceof NullLiteral)
					{
						ITypeBinding paramType = paramTypes[argIndex];
						boolean hasNullableAnnotation = false;
						for (IAnnotationBinding annotationBinding : declarationBinding
								.getParameterAnnotations(argIndex))
						{
							if (annotationBinding.getAnnotationType().getJavaElement()
									.equals(TypeFetcher.NULLABLE_ANNOTATION_TYPE))
								hasNullableAnnotation = true;
						}

						if (TypeHelper.isOfType(paramType, TypeFetcher.ITEMSTACK_TYPE) && !hasNullableAnnotation)
						{
							// Finally, use the index of the arg to retrieve the
							// parameter name
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

	public static class EmptyStackReferenceCheckFinder extends ASTVisitor
	{
		List<ASTNode> matchingNodes = new ArrayList<ASTNode>();

		@Override
		public boolean visit(InfixExpression node)
		{
			if (node.getOperator() == Operator.EQUALS || node.getOperator() == Operator.NOT_EQUALS)
			{
				Expression left = node.getLeftOperand();
				Expression right = node.getRightOperand();
				if (isEmptyStack(left) && TypeHelper.isOfType(right, TypeFetcher.ITEMSTACK_TYPE))
					matchingNodes.add(right);
				else if (isEmptyStack(right) && TypeHelper.isOfType(left, TypeFetcher.ITEMSTACK_TYPE))
					matchingNodes.add(left);
			}
			return node.getOperator() == Operator.CONDITIONAL_AND || node.getOperator() == Operator.CONDITIONAL_OR;
		}

		private boolean isEmptyStack(Expression expression)
		{
			if (expression instanceof Name)
			{
				Name name = (Name) expression;
				return name.getFullyQualifiedName().equals(TypeHelper.ITEMSTACK_NAME + ".EMPTY")
						|| name.getFullyQualifiedName().equals("ItemStack.EMPTY");
			}
			return false;
		}
	}
}
