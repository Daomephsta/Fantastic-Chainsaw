package leviathan143.fantasticchainsaw.generic.editors.language;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

import leviathan143.fantasticchainsaw.generic.editors.language.LanguageModel.LanguageNode;

public class TranslationEditingSupport extends EditingSupport
{
	private final CellEditor editor;

	public TranslationEditingSupport(TableViewer viewer)
	{
		super(viewer);
		editor = new TextCellEditor(viewer.getTable());
	}

	@Override
	protected void setValue(Object obj, Object value)
	{
		((LanguageNode) obj).setTranslation((String) value);
		getViewer().update(obj, null);
	}

	@Override
	protected Object getValue(Object obj)
	{
		return ((LanguageNode) obj).getTranslation();
	}

	@Override
	protected CellEditor getCellEditor(Object obj)
	{
		return editor;
	}

	@Override
	protected boolean canEdit(Object obj)
	{
		return true;
	}

}
