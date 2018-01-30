package leviathan143.fantasticchainsaw.interfaces.gradle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ui.console.MessageConsoleStream;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProgressEvent;
import org.gradle.tooling.ProgressListener;
import org.gradle.tooling.ProjectConnection;

import leviathan143.fantasticchainsaw.FantasticPlugin;
import leviathan143.fantasticchainsaw.Versioning;
import leviathan143.fantasticchainsaw.Versioning.Version;
import leviathan143.fantasticchainsaw.generic.wizards.ForgeSetupPage.ForgeSetupData;
import leviathan143.fantasticchainsaw.gradle.ForgeModel;
import leviathan143.fantasticchainsaw.interfaces.forgemaven.ForgeMavenInterface;
import leviathan143.fantasticchainsaw.util.EclipseHelper;
import leviathan143.fantasticchainsaw.util.Utils;
import leviathan143.fantasticchainsaw.util.text.TemplateEngine;

public class GradleInterface
{
	private static boolean unpacked = false;
	private static File TEMPLATE_FOLDER;
	private static File GRADLE_EXTENSION_FOLDER;
	private static File BUILD_SCRIPT_TEMPLATE_FILE;
	private static File PROPERTIES_TEMPLATE_FILE;
	private static File INIT_SCRIPT;

	private static void unpackFiles()
	{
		if (unpacked) return;
		File pluginMetadataFolder = Platform.getStateLocation(EclipseHelper.getPluginBundle()).toFile();

		TEMPLATE_FOLDER = new File(pluginMetadataFolder, "templates");
		GRADLE_EXTENSION_FOLDER = new File(pluginMetadataFolder, "gradle-extension");
		BUILD_SCRIPT_TEMPLATE_FILE = new File(TEMPLATE_FOLDER, "build.gradle.template");
		PROPERTIES_TEMPLATE_FILE = new File(TEMPLATE_FOLDER, "gradle.properties.template");
		INIT_SCRIPT = new File(GRADLE_EXTENSION_FOLDER, "fantastic-chainsaw-init.gradle");
		File gradleExtensionDest = new File(GRADLE_EXTENSION_FOLDER, "FCGradleExtension.jar");

		try
		{
			if (!TEMPLATE_FOLDER.exists()) TEMPLATE_FOLDER.mkdirs();
			if (!GRADLE_EXTENSION_FOLDER.exists()) GRADLE_EXTENSION_FOLDER.mkdirs();
			if (!BUILD_SCRIPT_TEMPLATE_FILE.exists())
			{
				InputStream buildScriptTemplate = GradleInterface.class.getClassLoader()
						.getResourceAsStream("../resources/templates/default_build.gradle.template");
				Files.copy(buildScriptTemplate, BUILD_SCRIPT_TEMPLATE_FILE.toPath());
				buildScriptTemplate.close();
			}
			if (!PROPERTIES_TEMPLATE_FILE.exists())
			{
				InputStream gradlePropertiesTemplate = GradleInterface.class.getClassLoader()
						.getResourceAsStream("../resources/templates/default_gradle.properties.template");
				Files.copy(gradlePropertiesTemplate, PROPERTIES_TEMPLATE_FILE.toPath());
				gradlePropertiesTemplate.close();
			}
			if (!INIT_SCRIPT.exists())
			{
				InputStream initScriptStream = GradleInterface.class
						.getResourceAsStream("fantastic-chainsaw-init.gradle");
				Files.copy(initScriptStream, INIT_SCRIPT.toPath());
				initScriptStream.close();
			}
			if (!gradleExtensionDest.exists())
			{
				InputStream gradleExtensionStream = GradleInterface.class.getResourceAsStream("FCGradleExtension.jar");
				Files.copy(gradleExtensionStream, gradleExtensionDest.toPath());
				gradleExtensionStream.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		unpacked = true;
	}

	public static ForgeModel getModel(IJavaProject project)
	{
		unpackFiles();
		ProjectConnection connection = createConnection(project);

		try
		{
			ForgeModel model = connection.model(ForgeModel.class)
					.withArguments("--init-script", INIT_SCRIPT.getCanonicalPath()).get();
			return model;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			MessageConsoleStream consoleStream = EclipseHelper.getOrCreateConsole(FantasticPlugin.NAME)
					.newMessageStream();
			consoleStream.println("Closing Gradle connection");
			connection.close();
		}
		return null;
	}

	public static void gradleSDWEclipse(ProjectConnection connection, OutputStream out, IProgressMonitor monitor)
	{
		connection.newBuild().forTasks("setupDecompWorkspace", "eclipse").setStandardOutput(out).setStandardError(out)
				.addProgressListener(new ProgressMonitorReportingListener(monitor)).run();
	}

	public static void createBuildScript(IJavaProject project, ForgeSetupData settings)
	{
		unpackFiles();
		Version mcVersion = new Version(settings.getMCVersion());
		File projectFolder = project.getResource().getLocation().toFile();
		try
		{
			createFileFromTemplate(BUILD_SCRIPT_TEMPLATE_FILE, new File(projectFolder, "build.gradle"),
					Utils.createMapFromArrays(new String[]{"FGPluginID", "FGPluginVersion"},
							new String[]{mcVersion.compareTo(Versioning.V1_7_10) <= 0 ? "forge"
									: "net.minecraftforge.gradle.forge", "net.minecraftforge.gradle:ForgeGradle:"
											+ ForgeMavenInterface.getAppropriateForgeGradleVersion(mcVersion)}));
			createFileFromTemplate(PROPERTIES_TEMPLATE_FILE, new File(projectFolder, "gradle.properties"),
					Utils.createMapFromArrays(
							new String[]{"mcVer", "forgeVer", "modVer", "mappingsVer", "groupID", "baseName"},
							new String[]{settings.getMCVersion(), settings.getForgeVersion(), settings
									.getModVersion(), settings
											.getMappings(), settings.getGroupID(), settings.getBaseName()}));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static void createFileFromTemplate(File template, File destination, Map<String, String> argMap)
			throws IOException
	{
		new TemplateEngine(template).makeFile(destination, argMap);
	}

	public static void gradleInit(ProjectConnection connection, OutputStream out, IProgressMonitor monitor)
	{
		connection.newBuild().forTasks("init").setStandardOutput(out).setStandardError(out)
				.addProgressListener(new ProgressMonitorReportingListener(monitor)).run();
	}

	public static ProjectConnection createConnection(IJavaProject project)
	{
		return GradleConnector.newConnector().forProjectDirectory(project.getResource().getLocation().toFile())
				.connect();
	}

	private static class ProgressMonitorReportingListener implements ProgressListener
	{
		private IProgressMonitor monitor;

		public ProgressMonitorReportingListener(IProgressMonitor monitor)
		{
			this.monitor = monitor;
		}

		@Override
		public void statusChanged(ProgressEvent e)
		{
			monitor.subTask(e.getDescription());
		}
	}
}
