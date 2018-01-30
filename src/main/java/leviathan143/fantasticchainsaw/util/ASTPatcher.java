package leviathan143.fantasticchainsaw.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.text.edits.MalformedTreeException;

public class ASTPatcher<T extends ASTNode> extends ASTVisitor
{
	private ASTRewrite rewriter;

	public ASTPatcher(ASTRewrite rewriter)
	{
		this.rewriter = rewriter;
	}

	protected final void patch(T node)
			throws CoreException, MalformedTreeException, IllegalArgumentException, BadLocationException
	{
		this.customPatch(node.getAST(), rewriter, node);
	}

	protected void customPatch(AST ast, ASTRewrite rewriter, T node)
	{}
}
