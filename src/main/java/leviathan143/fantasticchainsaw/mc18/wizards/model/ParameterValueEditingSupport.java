package leviathan143.fantasticchainsaw.mc18.wizards.model;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

public class ParameterValueEditingSupport extends EditingSupport
{
    private final CellEditor editor;

    public ParameterValueEditingSupport(TableViewer viewer)
    {
	super(viewer);
	editor = new TextCellEditor(viewer.getTable());
    }

    @Override
    protected void setValue(Object obj, Object value)
    {
	((ModelParameterModel.TextureVariable) obj).setValue((String) value);
	getViewer().update(obj, null);
    }

    @Override
    protected Object getValue(Object obj)
    {
	return ((ModelParameterModel.TextureVariable) obj).getValue();
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
