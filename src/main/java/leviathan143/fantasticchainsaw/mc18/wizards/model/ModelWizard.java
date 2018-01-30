package leviathan143.fantasticchainsaw.mc18.wizards.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.wizards.newresource.BasicNewFileResourceWizard;

import leviathan143.fantasticchainsaw.i18n.ModelWizardMessages;

public class ModelWizard extends BasicNewFileResourceWizard
{
	private ModelFileCreationPage newFilePage;
	private ModelParameterPage modelParamPage;

	public ModelWizard()
	{

	}

	@Override
	public void addPages()
	{
		newFilePage = new ModelFileCreationPage(getSelection());
		newFilePage.setFileExtension("json");
		newFilePage.setTitle(ModelWizardMessages.titleP1);
		newFilePage.setTitle(ModelWizardMessages.descriptionP1);
		addPage(newFilePage);

		modelParamPage = new ModelParameterPage();
		modelParamPage.setTitle(ModelWizardMessages.titleP2);
		modelParamPage.setTitle(ModelWizardMessages.descriptionP2);
		newFilePage.addModelPathObserver(modelParamPage);
		addPage(modelParamPage);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection currentSelection)
	{
		super.init(workbench, currentSelection);
	}

	@Override
	public boolean canFinish()
	{
		return newFilePage.isPageComplete() && modelParamPage.isPageComplete();
	}

	@Override
	public boolean performFinish()
	{
		try
		{
			IFile file = newFilePage.createNewFile();
			file.setContents(modelParamPage.getFinalModel(file.getLocation().toFile().toPath()), false, false, null);
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}
		return true;
	}
}
