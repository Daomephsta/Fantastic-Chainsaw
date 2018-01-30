package leviathan143.fantasticchainsaw.base;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SearchableListDialog<T> extends Dialog
{
	private LabelProvider provider;
	private T[] input;
	private T result;
	private String title;

	private Text searchField;
	private ListViewer resultList;

	public SearchableListDialog(Shell parentShell, LabelProvider provider, T[] input)
	{
		super(parentShell);
		this.provider = provider;
		this.input = input;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite container = (Composite) super.createDialogArea(parent);
		searchField = new Text(container, SWT.BORDER);
		searchField.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		searchField.addModifyListener(e -> resultList.refresh());
		searchField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		resultList = new ListViewer(container, SWT.BORDER);
		resultList.setContentProvider(ArrayContentProvider.getInstance());
		resultList.setInput(input);
		resultList.setLabelProvider(provider);
		resultList.addFilter(new SearchViewerFilter());
		resultList.addSelectionChangedListener(e ->
		{
			this.result = (T) e.getStructuredSelection().getFirstElement();
		});

		org.eclipse.swt.widgets.List list = resultList.getList();
		list.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		return container;
	}

	@Override
	protected void configureShell(Shell newShell)
	{
		super.configureShell(newShell);
		newShell.setText(title);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent)
	{
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected Point getInitialSize()
	{
		return new Point(450, 300);
	}

	public T getResult()
	{
		return result;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	private String getLabel(Object element)
	{
		return ((LabelProvider) resultList.getLabelProvider()).getText(element);
	}

	private class SearchViewerFilter extends ViewerFilter
	{
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element)
		{
			return getLabel(element).contains(searchField.getText());
		}
	}
}
