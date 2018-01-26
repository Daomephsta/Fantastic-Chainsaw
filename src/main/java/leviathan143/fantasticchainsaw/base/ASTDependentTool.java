package leviathan143.fantasticchainsaw.base;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.ui.console.MessageConsoleStream;

import leviathan143.fantasticchainsaw.FantasticPlugin;
import leviathan143.fantasticchainsaw.util.EclipseHelper;

public abstract class ASTDependentTool extends AbstractHandler
{
    private final String taskDesc;

    public ASTDependentTool(String taskDesc)
    {
	this.taskDesc = taskDesc;
    }

    @Override
    public Object execute(ExecutionEvent arg0) throws ExecutionException
    {
	IJavaProject currentProject = null;
	MessageConsoleStream consoleStream = EclipseHelper.getOrCreateConsole(FantasticPlugin.NAME).newMessageStream();

	ASTParser parser = ASTParser.newParser(AST.JLS8);
	try
	{
	    consoleStream.println("Starting task '" + taskDesc + "'");
	    long startTime = System.currentTimeMillis();
	    for (ICompilationUnit comp : EclipseHelper.getCurrentSelectedCompilationUnits())
	    {
		if (!comp.isStructureKnown())
		    consoleStream.println("Could not analyse " + comp.getElementName() + " because of syntax errors!");
		if (currentProject != comp.getJavaProject())
		{
		    consoleStream.println("Project has changed, refetching types.");
		    currentProject = comp.getJavaProject();
		    TypeFetcher.fetchTypes(currentProject);
		}
		setupASTParser(parser, currentProject, comp);
		performTask((CompilationUnit) parser.createAST(null), comp);
	    }
	    consoleStream.println(
		    "Task '" + taskDesc + "' completed in " + (System.currentTimeMillis() - startTime) + " ms");
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
