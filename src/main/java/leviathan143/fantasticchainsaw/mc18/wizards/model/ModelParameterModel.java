package leviathan143.fantasticchainsaw.mc18.wizards.model;

import java.util.List;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import leviathan143.fantasticchainsaw.interfaces.minecraft.model.JSONModel;

public class ModelParameterModel
{
    public static final IContentProvider CONTENT_PROVIDER = new ParameterModelContentProvider();

    private final List<TextureVariable> textureVariables = Lists.newArrayList();

    /*public void parse()
    {
	textureVariables.clear();
	for(String textureVar : model.getTextureVariables()) textureVariables.add(new TextureVariable(textureVar, model.getTextureVariableValue(textureVar)));
    }*/
    
    public void parse(JSONModel model)
    {
	textureVariables.clear();
	for(String textureVar : model.getTextureVariables()) textureVariables.add(new TextureVariable(textureVar, model.getTextureVariableValue(textureVar)));
    }
    
    public void applyParametersToModel(JSONModel model)
    {
	for(TextureVariable textureVar : textureVariables)
	{
	    //Only set variables that have changed
	    if(!textureVar.getValue().equals(textureVar.getDefaultValue()))
		model.setTextureVariableValue(textureVar.getName(), textureVar.getValue());
	}
    }
    
    public List<TextureVariable> getTextureVariables()
    {
	return ImmutableList.copyOf(textureVariables);
    }

    public static class TextureVariable
    {
	private final String name;
	private String value;
	/**The initial value of this variable**/
	private String defaultValue;

	public TextureVariable(String name, String value)
	{
	    this.name = name;
	    this.defaultValue = this.value = value;
	}

	public String getName()
	{
	    return name;
	}

	public String getValue()
	{
	    return value;
	}

	public void setValue(String value)
	{
	    this.value = value;
	}
	
	public boolean hasDefaultValue()
	{
	    return !defaultValue.isEmpty();
	}
	
	public String getDefaultValue()
	{
	    return defaultValue;
	}
    }

    private static class ParameterModelContentProvider implements IStructuredContentProvider 
    {	
	@Override
	public Object[] getElements(Object parent) 
	{
	    ModelParameterModel model = (ModelParameterModel) parent;
	    return model.textureVariables.toArray();
	}
    }
}