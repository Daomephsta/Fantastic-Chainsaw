package leviathan143.fantasticchainsaw.util;

import java.util.Iterator;

import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TreeColumn;

public class SWTHelper
{
	public static TableViewerColumn createColumn(TableViewer viewer, String title, int defaultWidth)
	{
		TableColumn column = new TableColumn(viewer.getTable(), SWT.NONE);
		column.setText(title);
		column.setWidth(defaultWidth);
		column.setResizable(true);
		return new TableViewerColumn(viewer, column);
	}

	public static <T> void addDeletionSupport(TableViewer tableViewer, IDeletionHandler<T> deletionHandler)
	{
		tableViewer.getTable().addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				if (e.character == SWT.DEL) deleteSelectedEntries(tableViewer, deletionHandler);
			}
		});
	}

	@SuppressWarnings("unchecked")
	public static <T> void deleteSelectedEntries(TableViewer tableViewer, IDeletionHandler<T> deletionHandler)
	{
		IStructuredSelection selection = tableViewer.getStructuredSelection();
		for (Iterator<T> iter = selection.iterator(); iter.hasNext();)
		{
			T node = iter.next();
			deletionHandler.deleteNode(node);
			tableViewer.remove(node);
		}
	}

	public interface IDeletionHandler<T>
	{
		public void deleteNode(T node);
	}

	public static void setDoubleClickToEdit(TableViewer tableViewer)
	{
		TableViewerEditor.create(tableViewer, new ColumnViewerEditorActivationStrategy(tableViewer)
		{
			@Override
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event)
			{
				if (event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION) return true;
				else if (event.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION) return false;
				return super.isEditorActivationEvent(event);
			}
		}, ColumnViewerEditor.DEFAULT);
	}

	public static TreeViewerColumn createColumn(TreeViewer viewer, String title, int defaultWidth)
	{
		TreeColumn column = new TreeColumn(viewer.getTree(), SWT.NONE);
		column.setText(title);
		column.setWidth(defaultWidth);
		column.setResizable(true);
		return new TreeViewerColumn(viewer, column);
	}
}
