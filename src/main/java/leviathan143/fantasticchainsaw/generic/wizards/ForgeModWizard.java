package leviathan143.fantasticchainsaw.generic.wizards;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageTwo;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.gradle.tooling.ProjectConnection;

import com.google.gson.*;

import leviathan143.fantasticchainsaw.FantasticPlugin;
import leviathan143.fantasticchainsaw.generic.wizards.ForgeSetupPage.ForgeSetupData;
import leviathan143.fantasticchainsaw.generic.wizards.ForgeSetupPage.ModInfoData;
import leviathan143.fantasticchainsaw.i18n.ForgeModWizardMessages;
import leviathan143.fantasticchainsaw.interfaces.gradle.GradleInterface;
import leviathan143.fantasticchainsaw.metadata.ForgeNature;
import leviathan143.fantasticchainsaw.util.EclipseHelper;

public class ForgeModWizard extends Wizard implements INewWizard
{
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting()
			.registerTypeHierarchyAdapter(ModInfo.class, new ModInfo.Serialiser()).create();

	private NewJavaProjectWizardPageOne pageOne = new NewJavaProjectWizardPageOne();
	private ForgeSetupPage setupPage = new ForgeSetupPage();
	private NewJavaProjectWizardPageTwo pageThree = new ForgeBuildPathPage(pageOne);

	public ForgeModWizard()
	{
		setWindowTitle(ForgeModWizardMessages.windowTitle);
		pageOne.setTitle(ForgeModWizardMessages.page1Title);
		setupPage.setTitle(ForgeModWizardMessages.page3Title);
		addPage(pageOne);
		addPage(setupPage);
		addPage(pageThree);
	}
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {}

	@Override
	public boolean canFinish()
	{
		return pageOne.isPageComplete() && setupPage.isPageComplete();
	}

	@Override
	public boolean performFinish()
	{
		ProgressMonitorDialog progMonitor = new ProgressMonitorDialog(getShell());

		try
		{
			progMonitor.run(false, true, monitor ->
			{
				try
				{
					pageThree.performFinish(monitor);
				}
				catch (CoreException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		}
		catch (InvocationTargetException | InterruptedException e)
		{
			e.printStackTrace();
		}
		IJavaProject project = pageThree.getJavaProject();
		
		//Add Forge nature
		try
		{
			ForgeNature.add(project.getProject());
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}
		
		ForgeSetupData setupData = setupPage.getForgeSetupData();
		ModInfoData modInfoData = setupPage.getModInfoData();

		Job gradleSetup = new Job("Gradle Setup")
		{
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				ProjectConnection connection = GradleInterface.createConnection(project);

				OutputStream consoleOut = EclipseHelper.getOrCreateConsole(FantasticPlugin.NAME).newOutputStream();

				monitor.beginTask("Project Setup", 1000);
				monitor.setTaskName("Initialising Gradle");
				GradleInterface.gradleInit(connection, consoleOut, monitor);
				monitor.worked(30);
				monitor.setTaskName("Creating build script");
				GradleInterface.createBuildScript(project, setupData);
				monitor.setTaskName("Creating mod info file");
				createModInfoFile(project, modInfoData);
				monitor.setTaskName("Setting up decompiled workspace");
				GradleInterface.gradleSDWEclipse(connection, consoleOut, monitor);
				monitor.worked(955);
				monitor.setTaskName("Refreshing project");
				// Refresh the project so Eclipse sees files created by Gradle
				refreshProject(project, monitor);
				monitor.worked(15);
				monitor.done();

				connection.close();
				return Status.OK_STATUS;
			}
		};
		gradleSetup.setPriority(Job.LONG);
		gradleSetup.schedule();
		return true;
	}

	private void refreshProject(IJavaProject project, IProgressMonitor monitor)
	{
		try
		{
			project.getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}
	}

	private void createModInfoFile(IJavaProject project, ModInfoData modInfoData)
	{
		IFile modInfoFile = project.getProject().getFile(new Path("src/main/resources/mcmod.info"));
		if (!modInfoFile.exists())
		{
			try
			{
				modInfoFile.create(
						new ByteArrayInputStream(
								GSON.toJson(new ModInfo(modInfoData)).getBytes(StandardCharsets.UTF_8)),
						IResource.NONE, null);
			}
			catch (CoreException e)
			{
				e.printStackTrace();
			}
		}
	}

	private static class ModInfo
	{
		private String modid, name, description, logoFile, credits, mcversion, version;
		private String[] authors;

		public ModInfo(ModInfoData infoData)
		{
			this.modid = infoData.getModID();
			this.name = infoData.getName();
			this.description = infoData.getDescription();
			this.logoFile = infoData.getLogoPath();
			this.credits = infoData.getCredits();
			this.mcversion = "${mcversion}";
			this.version = "${version}";
			this.authors = infoData.getAuthors().split(",");
		}

		private ModInfo()
		{}

		private static class Serialiser implements JsonDeserializer<ModInfo>, JsonSerializer<ModInfo>
		{
			@Override
			public JsonElement serialize(ModInfo modInfo, Type type, JsonSerializationContext context)
			{
				JsonObject json = new JsonObject();
				json.addProperty("modid", modInfo.modid);
				json.addProperty("name", modInfo.name);
				if (!modInfo.description.isEmpty()) json.addProperty("description", modInfo.description);
				if (!modInfo.logoFile.isEmpty()) json.addProperty("logoFile", modInfo.logoFile);
				if (!modInfo.credits.isEmpty()) json.addProperty("credits", modInfo.credits);
				json.addProperty("mcversion", modInfo.mcversion);
				json.addProperty("version", modInfo.version);
				JsonArray arr = new JsonArray();
				for (String author : modInfo.authors)
				{
					arr.add(author);
				}
				json.add("authors", arr);
				return json;
			}

			@Override
			public ModInfo deserialize(JsonElement json, Type type, JsonDeserializationContext context)
					throws JsonParseException
			{
				JsonObject jsonObj = json.getAsJsonObject();
				ModInfo modInfo = new ModInfo();
				modInfo.modid = jsonObj.get("modid").getAsString();
				modInfo.name = jsonObj.get("name").getAsString();
				if (jsonObj.has("description")) modInfo.description = jsonObj.get("description").getAsString();
				else modInfo.description = "";
				if (jsonObj.has("logoFile")) modInfo.logoFile = jsonObj.get("logoFile").getAsString();
				else modInfo.logoFile = "";
				if (jsonObj.has("credits")) modInfo.credits = jsonObj.get("credits").getAsString();
				else modInfo.credits = "";
				modInfo.mcversion = jsonObj.get("mcversion").getAsString();
				modInfo.version = jsonObj.get("version").getAsString();
				if (jsonObj.has("authors"))
				{
					JsonArray jsonArr = jsonObj.get("authors").getAsJsonArray();
					modInfo.authors = new String[jsonArr.size()];
					for (int i = 0; i < jsonArr.size(); i++)
					{
						modInfo.authors[i] = jsonArr.get(i).getAsString();
					}
				}
				else modInfo.authors = new String[0];
				return modInfo;
			}

		}
	}
}
