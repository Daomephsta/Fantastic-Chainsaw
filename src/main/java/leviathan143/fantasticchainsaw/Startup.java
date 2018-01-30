package leviathan143.fantasticchainsaw;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IStartup;

import leviathan143.fantasticchainsaw.i18n.ForgeMavenInterfaceMessages;
import leviathan143.fantasticchainsaw.i18n.MCPBotInterfaceMessages;
import leviathan143.fantasticchainsaw.i18n.MiscMessages;
import leviathan143.fantasticchainsaw.interfaces.forgemaven.ForgeMavenInterface;
import leviathan143.fantasticchainsaw.interfaces.mcpbot.MCPBotInterface;

public class Startup implements IStartup
{

	@Override
	public void earlyStartup()
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
	}

}
