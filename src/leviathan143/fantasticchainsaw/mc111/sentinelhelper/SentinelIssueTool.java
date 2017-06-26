package leviathan143.fantasticchainsaw.mc111.sentinelhelper;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import leviathan143.fantasticchainsaw.util.EclipseHelper;

public abstract class SentinelIssueTool extends AbstractHandler 
{
	private final String taskDesc;
	
	public SentinelIssueTool(String taskDesc) 
	{
		this.taskDesc = taskDesc;
	}
	
	@Override
	public Object execute(ExecutionEvent arg0) throws ExecutionException 
	{
		IJavaProject currentProject = null;

		ASTParser parser = ASTParser.newParser(AST.JLS8);
		try
		{
			System.out.println("Starting task '" + taskDesc + "'");
			long startTime = System.currentTimeMillis();
			for(ICompilationUnit comp : EclipseHelper.getCurrentSelectedCompilationUnits())
			{
				if(!comp.isStructureKnown()) System.err.println("Could not analyse " + comp.getElementName() + " because of syntax errors!");
				if(currentProject != comp.getJavaProject())
				{
					System.out.println("Project has changed, refetching types.");
					currentProject = comp.getJavaProject();
					TypeFetcher.fetchTypes(currentProject);
				}
				setupASTParser(parser, currentProject, comp);
				performTask((CompilationUnit) parser.createAST(null), comp);
			}
			System.out.println("Task '" + taskDesc + "' completed in " + (System.currentTimeMillis() - startTime) + " ms");
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return null;
	}
		
	protected void performTask(CompilationUnit compUnit, ICompilationUnit comp) throws Exception
	{
		
	}
	
	private void setupASTParser(ASTParser parser, IJavaProject project, ICompilationUnit comp)
	{
		parser.setProject(project);
		parser.setSource(comp);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
	}
}
