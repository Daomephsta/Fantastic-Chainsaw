package leviathan143.fantasticchainsaw.base;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import leviathan143.fantasticchainsaw.util.SWTHelper;

public class ArchiveFileDialog extends Dialog 
{
    private static final ITreeContentProvider CONTENT_PROVIDER = new PathTreeContentProvider();

    private FileSystem archiveFS;
    private String result;
    private String[] extensionFilters = new String[0];
    private String pathFilter = "";
    private String text = "";

    private TreeViewer archiveTreeViewer;

    public ArchiveFileDialog(Shell parentShell, Path archive) 
    {
	super(parentShell);
	try
	{
	    this.archiveFS = FileSystems.newFileSystem(archive, null);
	}
	catch (IOException e)
	{
	    e.printStackTrace();
	}
    }

    @Override
    protected void configureShell(Shell newShell)
    {
	super.configureShell(newShell);
	newShell.addListener(SWT.Close, event -> 
	{
	    try
	    {
		archiveFS.close();
	    }
	    catch (IOException e)
	    {
		e.printStackTrace();
	    }
	});
	newShell.setText(text);
    }

    @Override
    protected Control createDialogArea(Composite parent) 
    {
	Composite container = (Composite) super.createDialogArea(parent);
	FillLayout fl_container = new FillLayout(SWT.HORIZONTAL);
	fl_container.marginWidth = 5;
	fl_container.marginHeight = 5;
	container.setLayout(fl_container);

	createJARFileTree(container);

	return container;
    }

    private void createJARFileTree(Composite container)
    {
	ColumnLabelProvider labelProvider = new PathLabelProvider();

	archiveTreeViewer = new TreeViewer(container, SWT.BORDER);
	archiveTreeViewer.setContentProvider(CONTENT_PROVIDER);
	archiveTreeViewer.setLabelProvider(labelProvider);
	archiveTreeViewer.setInput(archiveFS);
	archiveTreeViewer.addFilter(new ExtensionAndPathFilter());
	archiveTreeViewer.addSelectionChangedListener(e ->
	{
	    Path path = (Path) e.getStructuredSelection().getFirstElement();
	    this.result = path.toAbsolutePath().toString();
	});

	SWTHelper.createColumn(archiveTreeViewer, "", 200).setLabelProvider(labelProvider);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) 
    {
	createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    public void setFilterExtensions(String[] extensions)
    {
	this.extensionFilters = extensions;
    }

    public void setFilterPath(String filterPath)
    {
	this.pathFilter = filterPath;
    }
    
    public void setText(String text)
    {
	this.text = text;
    }

    @Override
    protected Point getInitialSize() 
    {
	return new Point(450, 300);
    }

    public String getResult()
    {
	return result;
    }

    private static class PathTreeContentProvider implements ITreeContentProvider
    {
	@Override
	public Object[] getElements(Object element) 
	{
	    try
	    {
		FileSystem fs = (FileSystem) element;
		return Files.list(fs.getPath("/")).toArray();
	    }
	    catch (IOException e)
	    {
		e.printStackTrace();
	    }
	    return null;
	}

	@Override
	public Object getParent(Object element) 
	{
	    Path path = (Path) element;
	    int pathSegments = path.getNameCount();
	    if(pathSegments > 0) return path.subpath(0, pathSegments - 1);
	    return null;
	}

	@Override
	public Object[] getChildren(Object element) 
	{
	    Path path = (Path) element;

	    if(!Files.isDirectory(path)) return null;
	    try
	    {
		return Files.list(path).toArray();
	    }
	    catch (IOException e)
	    {
		e.printStackTrace();
	    }
	    return null;
	}

	@Override
	public boolean hasChildren(Object element) 
	{
	    Path path = (Path) element;
	    if(!Files.isDirectory(path)) return false;
	    try
	    {
		return Files.list(path).count() > 0;
	    }
	    catch (IOException e)
	    {
		e.printStackTrace();
	    }
	    return false;
	}
    }

    private static class PathLabelProvider extends ColumnLabelProvider
    {
	@Override
	public String getText(Object element)
	{
	    Path path = (Path) element;
	    String name = path.getFileName().toString();
	    if(name.endsWith("/")) name = name.substring(0, name.length() - 1);
	    return name;
	}

	@Override
	public Image getImage(Object element)
	{
	    Path path = (Path) element;
	    if(Files.isDirectory(path)) return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
	    return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
	}
    }

    private class ExtensionAndPathFilter extends ViewerFilter 
    {
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element)
	{
	    Path path = (Path) element;
	    if(!pathFilter.isEmpty() && !path.startsWith(pathFilter)) return false;
	    if(!Files.isDirectory(path) && extensionFilters.length > 0 && !isExtensionValid(path)) return false;
	    return true;
	}

	private boolean isExtensionValid(Path path)
	{	
	    boolean validExtension = false;
	    for(String extension : extensionFilters)
	    {
		if(path.toString().endsWith(extension)) validExtension = true;
	    }
	    return validExtension;
	}
    }
}
