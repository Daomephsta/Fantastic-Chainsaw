package leviathan143.fantasticchainsaw.mc111.sentinelhelper;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.ReconcileContext;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import leviathan143.fantasticchainsaw.base.VersionSpecificCompilationParticipant;
import leviathan143.fantasticchainsaw.util.MarkerHelper;

public class SentinelCompilationParticipant extends VersionSpecificCompilationParticipant 
{	
	private final ASTParser parser;
	
	public SentinelCompilationParticipant() 
	{
		super("1.11.2+");
		this.parser = ASTParser.newParser(AST.JLS8);
	}
	
	@Override
	public void reconcile(ReconcileContext context) 
	{
		TypeFetcher.fetchTypes(context.getWorkingCopy().getJavaProject());
		parser.setProject(context.getWorkingCopy().getJavaProject());
		parser.setSource(context.getWorkingCopy());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		CompilationUnit comp = (CompilationUnit) parser.createAST(null);
		try 
		{
			SentinelIssueFinder.analyse(comp, context.getWorkingCopy());
		} 
		catch (CoreException e) 
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void cleanStarting(IJavaProject project) 
	{
		try 
		{
			project.getCorrespondingResource().deleteMarkers(MarkerHelper.SENTINEL_ISSUE, true, IResource.DEPTH_INFINITE);
		} 
		catch (CoreException e) 
		{
			e.printStackTrace();
		}
	}
}
