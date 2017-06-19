package leviathan143.fantasticchainsaw.mc111.sentinelhelper;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import leviathan143.fantasticchainsaw.util.EclipseHelper;
import leviathan143.fantasticchainsaw.util.TypeHelper;

public class SentinelIssuePatcher extends AbstractHandler
{
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		IJavaProject project = JavaCore.create(EclipseHelper.getCurrentSelectedProject());
		TypeFetcher.fetchTypes(project);

		ASTParser parser = ASTParser.newParser(AST.JLS8);
		try
		{
			for(IPackageFragment fragment : project.getPackageFragments())
			{
				for(ICompilationUnit comp : fragment.getCompilationUnits())
				{
					setupASTParser(parser, project, comp);
					CompilationUnit compUnit = (CompilationUnit) parser.createAST(null);

					ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
					IPath filePath = compUnit.getJavaElement().getPath();
					
					bufferManager.connect(filePath, LocationKind.IFILE, null);
					ITextFileBuffer fileBuffer = bufferManager.getTextFileBuffer(filePath, LocationKind.IFILE);
					IDocument doc = fileBuffer.getDocument();
					
					ASTRewrite rewriter = ASTRewrite.create(compUnit.getAST());
					
					ItemStackNullCheckPatcher stackNullCheckPatcher = new ItemStackNullCheckPatcher(rewriter);
					compUnit.accept(stackNullCheckPatcher);
					
					TextEdit edits = rewriter.rewriteAST(doc, null);
					edits.apply(doc);
							
					fileBuffer.commit(null, true);
					
					bufferManager.disconnect(filePath, LocationKind.IFILE, null);
				}
			}
		} catch (CoreException | MalformedTreeException | BadLocationException e)
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
			if(node.getOperator() == InfixExpression.Operator.EQUALS || node.getOperator() == InfixExpression.Operator.NOT_EQUALS)
			{
				Expression left = node.getLeftOperand();
				Expression right = node.getRightOperand();
				try 
				{
					if(left instanceof NullLiteral && TypeHelper.isOfType(right, TypeFetcher.ITEMSTACK_TYPE))
					{
						itemstackExpression = right;
						patch(node.getAST(), rewriter, node);
					}
					else if (right instanceof NullLiteral && TypeHelper.isOfType(left, TypeFetcher.ITEMSTACK_TYPE))
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
			return node.getOperator() == InfixExpression.Operator.CONDITIONAL_AND || node.getOperator() == InfixExpression.Operator.CONDITIONAL_OR;
		}

		protected void patch(AST ast, ASTRewrite rewriter, InfixExpression node) 
		{
			MethodInvocation isEmptyInvocation = ast.newMethodInvocation();
			isEmptyInvocation.setName(ast.newSimpleName("isEmpty"));
			isEmptyInvocation.setExpression((Expression) rewriter.createMoveTarget(itemstackExpression));
			if(node.getOperator() == InfixExpression.Operator.NOT_EQUALS)
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
}
