package leviathan143.fantasticchainsaw.mc111.sentinelhelper;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MalformedTreeException;

import leviathan143.fantasticchainsaw.base.ASTRewritingTool;
import leviathan143.fantasticchainsaw.base.TypeFetcher;
import leviathan143.fantasticchainsaw.util.ASTHelper;
import leviathan143.fantasticchainsaw.util.MarkerHelper;
import leviathan143.fantasticchainsaw.util.TypeHelper;

public class SentinelIssuePatcher extends ASTRewritingTool
{
	public static final String NAME = "leviathan143.fantasticchainsaw.mc111.patchSentinelIssues";

	public SentinelIssuePatcher()
	{
		super("Patch sentinel issues");
	}

	@Override
	protected void performTask(CompilationUnit compUnit, ICompilationUnit comp)
			throws CoreException, MalformedTreeException, BadLocationException
	{
		System.out.println("HULLO");
		super.performTask(compUnit, comp);
		ResourcesPlugin.getWorkspace().getRoot().deleteMarkers(MarkerHelper.SENTINEL_ISSUE, true,
				IResource.DEPTH_INFINITE);
	}

	@Override
	protected void rewriteAST(CompilationUnit compUnit, ICompilationUnit comp, ASTRewrite rewriter)
	{
		System.out.println("REWRITING");
		ItemStackNullCheckPatcher stackNullCheckPatcher = new ItemStackNullCheckPatcher(rewriter);
		compUnit.accept(stackNullCheckPatcher);

		ItemStackNullAssignmentPatcher nullAssignmentPatcher = new ItemStackNullAssignmentPatcher(rewriter);
		compUnit.accept(nullAssignmentPatcher);

		ItemStackNullReturnPatcher nullReturnPatcher = new ItemStackNullReturnPatcher(rewriter);
		compUnit.accept(nullReturnPatcher);

		ItemStackNullParameterPatcher nullParameterPatcher = new ItemStackNullParameterPatcher(rewriter);
		compUnit.accept(nullParameterPatcher);
		
		compUnit.accept(new EmptyStackReferenceCheckPatcher(rewriter));

		if (!comp.getImport(TypeHelper.ITEMSTACK_NAME).exists()
				&& (nullAssignmentPatcher.workDone || nullParameterPatcher.workDone || nullReturnPatcher.workDone))
		{
			AST ast = compUnit.getAST();
			ListRewrite imports = rewriter.getListRewrite(compUnit, CompilationUnit.IMPORTS_PROPERTY);
			ImportDeclaration itemstackImport = ast.newImportDeclaration();
			itemstackImport.setName(ast.newName(TypeHelper.ITEMSTACK_NAME));
			imports.insertLast(itemstackImport, null);
		}
	}

	public static class ItemStackNullCheckPatcher extends ASTVisitor
	{
		private Expression itemstackExpression;
		private ASTRewrite rewriter;

		public ItemStackNullCheckPatcher(ASTRewrite rewriter)
		{
			this.rewriter = rewriter;
		}

		@Override
		public boolean visit(InfixExpression node)
		{
			if (node.getOperator() == InfixExpression.Operator.EQUALS
					|| node.getOperator() == InfixExpression.Operator.NOT_EQUALS)
			{
				Expression left = node.getLeftOperand();
				Expression right = node.getRightOperand();
				try
				{
					if (left instanceof NullLiteral && TypeHelper.isOfType(right, TypeFetcher.ITEMSTACK_TYPE)
							&& !ASTHelper.hasNullableAnnotation(right))
					{
						itemstackExpression = right;
						patch(node.getAST(), rewriter, node);
					}
					else if (right instanceof NullLiteral && TypeHelper.isOfType(left, TypeFetcher.ITEMSTACK_TYPE)
							&& !ASTHelper.hasNullableAnnotation(left))
					{
						itemstackExpression = left;
						patch(node.getAST(), rewriter, node);
					}
				}
				catch (MalformedTreeException | IllegalArgumentException e)
				{
					e.printStackTrace();
				}
			}
			return node.getOperator() == InfixExpression.Operator.CONDITIONAL_AND
					|| node.getOperator() == InfixExpression.Operator.CONDITIONAL_OR;
		}

		protected void patch(AST ast, ASTRewrite rewriter, InfixExpression node)
		{
			MethodInvocation isEmptyInvocation = ast.newMethodInvocation();
			isEmptyInvocation.setName(ast.newSimpleName("isEmpty"));
			isEmptyInvocation.setExpression((Expression) rewriter.createMoveTarget(itemstackExpression));
			if (node.getOperator() == InfixExpression.Operator.NOT_EQUALS)
			{
				PrefixExpression prefix = ast.newPrefixExpression();
				prefix.setOperand(isEmptyInvocation);
				prefix.setOperator(PrefixExpression.Operator.NOT);
				rewriter.replace(node, prefix, null);
			}
			else
			{
				rewriter.replace(node, isEmptyInvocation, null);
			}
		}
	}

	private static void replaceNullWithEmptyStack(AST ast, ASTRewrite rewriter, Expression nullLiteral)
	{
		Name emptyStack = ast.newName("ItemStack.EMPTY");
		rewriter.replace(nullLiteral, emptyStack, null);
	}

	public static class ItemStackNullAssignmentPatcher extends ASTVisitor
	{
		private ASTRewrite rewriter;
		public boolean workDone;

		public ItemStackNullAssignmentPatcher(ASTRewrite rewriter)
		{
			this.rewriter = rewriter;
		}

		@Override
		public boolean visit(VariableDeclarationFragment node)
		{
			if (node.getInitializer() instanceof NullLiteral
					&& TypeHelper.isOfType(node.getName(), TypeFetcher.ITEMSTACK_TYPE)
					&& !ASTHelper.hasNullableAnnotation(node.getName()))
			{
				workDone = true;
				replaceNullWithEmptyStack(node.getAST(), rewriter, node.getInitializer());
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
				workDone = true;
				replaceNullWithEmptyStack(node.getAST(), rewriter, node.getRightHandSide());
			}
			return false;
		}
	}

	public static class ItemStackNullReturnPatcher extends ASTVisitor
	{
		private ASTRewrite rewriter;
		public boolean workDone;

		public ItemStackNullReturnPatcher(ASTRewrite rewriter)
		{
			this.rewriter = rewriter;
		}

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
					workDone = true;
					replaceNullWithEmptyStack(node.getAST(), rewriter, node.getExpression());
				}
			}
			return false;
		}
	}

	public static class ItemStackNullParameterPatcher extends ASTVisitor
	{
		private ASTRewrite rewriter;
		public boolean workDone;

		public ItemStackNullParameterPatcher(ASTRewrite rewriter)
		{
			this.rewriter = rewriter;
		}

		@Override
		public boolean visit(MethodInvocation node)
		{
			ITypeBinding[] paramTypes = node.resolveMethodBinding().getParameterTypes();
			// Get an IMethod from the IMethodBinding
			IMethodBinding declarationBinding = node.resolveMethodBinding().getMethodDeclaration();
			// Use the IMethod to get the parameter names

			for (int argIndex = 0; argIndex < node.arguments().size(); argIndex++)
			{
				Expression arg = (Expression) node.arguments().get(argIndex);

				if (arg instanceof NullLiteral)
				{
					ITypeBinding paramType = paramTypes[argIndex];
					boolean hasNullableAnnotation = false;
					for (IAnnotationBinding annotationBinding : declarationBinding.getParameterAnnotations(argIndex))
					{
						if (annotationBinding.getAnnotationType().getJavaElement()
								.equals(TypeFetcher.NULLABLE_ANNOTATION_TYPE))
							hasNullableAnnotation = true;
					}
					if (TypeHelper.isOfType(paramType, TypeFetcher.ITEMSTACK_TYPE) && !hasNullableAnnotation)
					{
						workDone = true;
						replaceNullWithEmptyStack(node.getAST(), rewriter, arg);
					}
				}
			}
			return false;
		}
	}
	
	public static class EmptyStackReferenceCheckPatcher extends ASTVisitor
	{
		private Expression itemstackExpression;
		private ASTRewrite rewriter;

		public EmptyStackReferenceCheckPatcher(ASTRewrite rewriter)
		{
			this.rewriter = rewriter;
		}

		@Override
		public boolean visit(InfixExpression node)
		{
			if (node.getOperator() == InfixExpression.Operator.EQUALS
					|| node.getOperator() == InfixExpression.Operator.NOT_EQUALS)
			{
				Expression left = node.getLeftOperand();
				Expression right = node.getRightOperand();
				try
				{
					if (isEmptyStack(left) && TypeHelper.isOfType(right, TypeFetcher.ITEMSTACK_TYPE))
					{
						itemstackExpression = right;
						patch(node.getAST(), rewriter, node);
					}
					else if (isEmptyStack(right) && TypeHelper.isOfType(left, TypeFetcher.ITEMSTACK_TYPE))
					{
						itemstackExpression = left;
						patch(node.getAST(), rewriter, node);
					}
				}
				catch (MalformedTreeException | IllegalArgumentException e)
				{
					e.printStackTrace();
				}
			}
			return node.getOperator() == InfixExpression.Operator.CONDITIONAL_AND
					|| node.getOperator() == InfixExpression.Operator.CONDITIONAL_OR;
		}

		protected void patch(AST ast, ASTRewrite rewriter, InfixExpression node)
		{
			MethodInvocation isEmptyInvocation = ast.newMethodInvocation();
			isEmptyInvocation.setName(ast.newSimpleName("isEmpty"));
			isEmptyInvocation.setExpression((Expression) rewriter.createMoveTarget(itemstackExpression));
			if (node.getOperator() == InfixExpression.Operator.NOT_EQUALS)
			{
				PrefixExpression prefix = ast.newPrefixExpression();
				prefix.setOperand(isEmptyInvocation);
				prefix.setOperator(PrefixExpression.Operator.NOT);
				rewriter.replace(node, prefix, null);
			}
			else
			{
				rewriter.replace(node, isEmptyInvocation, null);
			}
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
