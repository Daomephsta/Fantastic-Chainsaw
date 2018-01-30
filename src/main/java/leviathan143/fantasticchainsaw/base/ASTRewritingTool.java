package leviathan143.fantasticchainsaw.base;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

public abstract class ASTRewritingTool extends ASTDependentTool
{
	public ASTRewritingTool(String taskDesc)
	{
		super(taskDesc);
	}

	@Override
	protected void performTask(CompilationUnit compUnit, ICompilationUnit comp)
			throws CoreException, MalformedTreeException, BadLocationException
	{
		ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
		IPath filePath = compUnit.getJavaElement().getPath();

		bufferManager.connect(filePath, LocationKind.IFILE, null);
		ITextFileBuffer fileBuffer = bufferManager.getTextFileBuffer(filePath, LocationKind.IFILE);
		IDocument doc = fileBuffer.getDocument();

		ASTRewrite rewriter = ASTRewrite.create(compUnit.getAST());

		rewriteAST(compUnit, comp, rewriter);

		TextEdit edits = rewriter.rewriteAST(doc, null);
		edits.apply(doc);

		fileBuffer.commit(null, true);

		bufferManager.disconnect(filePath, LocationKind.IFILE, null);
	}

	protected abstract void rewriteAST(CompilationUnit compUnit, ICompilationUnit comp, ASTRewrite rewriter);
}
