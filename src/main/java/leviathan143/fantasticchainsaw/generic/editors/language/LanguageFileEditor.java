package leviathan143.fantasticchainsaw.generic.editors.language;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

import leviathan143.fantasticchainsaw.i18n.LanguageFileEditorMessages;

public class LanguageFileEditor extends MultiPageEditorPart implements IResourceChangeListener
{
	private int localisationEditorIdx;
	private int textEditorIdx;

	private TextEditor textEditor;
	private LocalisationEditor localisationEditor;

	public LanguageFileEditor()
	{
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	private void createTextEditorPage()
	{
		try
		{
			textEditor = new TextEditor();
			textEditorIdx = addPage(textEditor, getEditorInput());
			setPageText(textEditorIdx, LanguageFileEditorMessages.textEditorTitle);
		}
		catch (PartInitException e)
		{
			ErrorDialog.openError(getSite().getShell(), "Error creating nested text editor", null, e.getStatus());
		}
	}

	private void createLocalisationEditorPage()
	{
		try
		{
			localisationEditor = new LocalisationEditor(textEditor.getDocumentProvider());
			localisationEditorIdx = addPage(localisationEditor, getEditorInput());
			setPageText(localisationEditorIdx, LanguageFileEditorMessages.locEditorTitle);
		}
		catch (PartInitException e)
		{
			ErrorDialog.openError(getSite().getShell(), "Error creating localisation editor", null, e.getStatus());
		}
	}

	protected void createPages()
	{
		createTextEditorPage();
		createLocalisationEditorPage();
	}

	@Override
	protected void pageChange(int newPageIndex)
	{
		if (newPageIndex == localisationEditorIdx)
		{
			localisationEditor.reparseModel();
		}
		super.pageChange(newPageIndex);
	}

	public void dispose()
	{
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}

	public void doSave(IProgressMonitor monitor)
	{
		getEditor(textEditorIdx).doSave(monitor);
	}

	public void doSaveAs()
	{}

	public void gotoMarker(IMarker marker)
	{
		setActivePage(0);
		IDE.gotoMarker(getEditor(0), marker);
	}

	public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException
	{
		if (!(editorInput instanceof IFileEditorInput))
			throw new PartInitException("Invalid Input: Must be IFileEditorInput");
		super.init(site, editorInput);
	}

	public boolean isSaveAsAllowed()
	{
		return false;
	}

	public void resourceChanged(final IResourceChangeEvent event)
	{
		if (event.getType() == IResourceChangeEvent.PRE_CLOSE)
		{
			Display.getDefault().asyncExec(new Runnable()
			{
				public void run()
				{
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
					for (int i = 0; i < pages.length; i++)
					{
						if (((FileEditorInput) textEditor.getEditorInput()).getFile().getProject()
								.equals(event.getResource()))
						{
							IEditorPart editorPart = pages[i].findEditor(textEditor.getEditorInput());
							pages[i].closeEditor(editorPart, true);
						}
					}
				}
			});
		}
	}
}
