package leviathan143.fantasticchainsaw;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import leviathan143.fantasticchainsaw.i18n.ForgeMavenInterfaceMessages;
import leviathan143.fantasticchainsaw.i18n.MCPBotInterfaceMessages;
import leviathan143.fantasticchainsaw.i18n.MiscMessages;
import leviathan143.fantasticchainsaw.interfaces.forgemaven.ForgeMavenInterface;
import leviathan143.fantasticchainsaw.interfaces.mcpbot.MCPBotInterface;

public class RefreshMCPAndForgeHandler extends AbstractHandler
{
    @Override
    public Object execute(ExecutionEvent arg0) throws ExecutionException
    {
	Job fetchMCPAndForgeVersions = new Job(MiscMessages.retrievingForgeAndMCPVersions)
	    {
		@Override
		protected IStatus run(IProgressMonitor monitor)
		{
		    monitor.subTask(ForgeMavenInterfaceMessages.retrievingForgeVersions);
		    ForgeMavenInterface.updateForgeVersions();
		    monitor.subTask(MCPBotInterfaceMessages.retrievingMappings);
		    MCPBotInterface.updateMCPVersions();
		    return Status.OK_STATUS;
		}
	    };
	fetchMCPAndForgeVersions.schedule();
	return null;
    }
}
