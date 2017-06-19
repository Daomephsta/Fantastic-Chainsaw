package leviathan143.fantasticchainsaw.mc111.sentinelhelper;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import leviathan143.fantasticchainsaw.util.ASTHelper;
import leviathan143.fantasticchainsaw.util.EclipseHelper;
import leviathan143.fantasticchainsaw.util.MarkerHelper;
import leviathan143.fantasticchainsaw.util.TypeHelper;

public class SentinelIssueFinder extends AbstractHandler
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		IJavaProject project = JavaCore.create(EclipseHelper.getCurrentSelectedProject());
		TypeFetcher.fetchTypes(project);

		ASTParser parser = ASTParser.newParser(AST.JLS8);
		try
		{
			System.out.println("Finding sentinel issues");
			for(IPackageFragment fragment : project.getPackageFragments())
			{
				for(ICompilationUnit comp : fragment.getCompilationUnits())
				{
					setupASTParser(parser, project, comp);
					CompilationUnit compUnit = (CompilationUnit) parser.createAST(null);
					IResource baseResource = comp.getResource();

					baseResource.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);

					ItemStackNullCheckFinder stackNullCheckFinder = new ItemStackNullCheckFinder();
					compUnit.accept(stackNullCheckFinder);
					for(ASTNode node : stackNullCheckFinder.matchingNodes)
					{
						MarkerHelper.createNormalWarning(baseResource, String.format("%1$s is of type ItemStack. Use ItemStack#isEmpty() instead of null-checking the returned value.", node.toString()), compUnit.getLineNumber(node.getStartPosition()));
					}
					
					ItemStackNullAssignmentFinder stackNullAssignmentFinder = new ItemStackNullAssignmentFinder();
					compUnit.accept(stackNullAssignmentFinder);
					for(ASTNode node : stackNullAssignmentFinder.matchingNodes)
					{
						MarkerHelper.createNormalWarning(baseResource, String.format("%1$s is of type ItemStack, which is non-nullable! Use ItemStack.EMPTY instead of null.", node.toString()), compUnit.getLineNumber(node.getStartPosition()));
					}
				}
			}
			System.out.println("Done");
		} catch (JavaModelException e)
		{
			e.printStackTrace();
		} catch (CoreException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	private void setupASTParser(ASTParser parser, IJavaProject project, ICompilationUnit comp)
	{
		parser.setProject(project);
		parser.setSource(comp);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
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
					matchingNodes.add(node);
				}
				else if(right instanceof NullLiteral && TypeHelper.isOfType(left, TypeFetcher.ITEMSTACK_TYPE) && !ASTHelper.hasNullableAnnotation(left))
				{
					matchingNodes.add(node);
				}
			}
			return node.getOperator() == Operator.CONDITIONAL_AND || node.getOperator() == Operator.CONDITIONAL_OR;
		}
	}
	
	public static class ItemStackNullAssignmentFinder extends ASTVisitor
	{
		List<ASTNode> matchingNodes = new ArrayList<ASTNode>();

		@Override
		public boolean visit(VariableDeclarationFragment node) {
			if(node.getInitializer() instanceof NullLiteral && TypeHelper.isOfType(node.getName(), TypeFetcher.ITEMSTACK_TYPE) && !ASTHelper.hasNullableAnnotation(node.getName()))
			{
				matchingNodes.add(node);
			}
			return false;
		}
		
		@Override
		public boolean visit(Assignment node)
		{
			if(node.getOperator() != Assignment.Operator.ASSIGN) return false;
			if(node.getRightHandSide() instanceof NullLiteral && TypeHelper.isOfType(node.getLeftHandSide(), TypeFetcher.ITEMSTACK_TYPE) && !ASTHelper.hasNullableAnnotation(node.getLeftHandSide()))
			{
				matchingNodes.add(node);
			}
			
			return false;
		}
		
		private boolean checkFragment(VariableDeclarationFragment fragment)
		{
			return false;
		}
	}
}