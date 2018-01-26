package leviathan143.fantasticchainsaw.interfaces.minecraft.model;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import leviathan143.fantasticchainsaw.interfaces.minecraft.MCResourceRepositoryAggregate;
import leviathan143.fantasticchainsaw.interfaces.minecraft.ResourceLocation;
import leviathan143.fantasticchainsaw.interfaces.minecraft.model.ModelElement.FaceSet;

public abstract class JSONModel
{   
    private static final Gson SERIALISER = new GsonBuilder()
	    .registerTypeHierarchyAdapter(JSONModel.class, new JSONModel.Serialiser())
	    .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serialiser())
	    .registerTypeAdapter(Vec3i.class, new Vec3i.Serialiser())
	    .registerTypeAdapter(Vec4i.class, new Vec4i.Serialiser()).setPrettyPrinting().create();
    private static final Pattern DOMAIN_AND_PATH_PATTERN = Pattern.compile("assets(?:\\/|\\\\)(\\w+)(?:\\/|\\\\)models(?:\\/|\\\\)([\\w\\/]+)");

    //Data that is serialised
    private JSONModel parent;
    private Transforms display;
    private Map<String, String> textures;
    private List<ModelElement> elements;
    //Runtime data that is not serialised
    private ResourceLocation parentLocation;
    private ResourceLocation location;
    protected ModelType type;

    public JSONModel() {}

    public static JSONModel createChild(JSONModel parentModel, ResourceLocation childID)
    {
	boolean isBlockModel = parentModel.getClass() == BlockModel.class;
	JSONModel child = isBlockModel ? new BlockModel() : new ItemModel();
	child.parent = parentModel;
	child.display = parentModel.display;
	child.textures = new HashMap<>();
	child.elements = parentModel.elements;
	
	child.parentLocation = parentModel.location;
	child.location = childID;
	child.type = isBlockModel ? ModelType.BLOCK : ModelType.ITEM;
	
	return child;
    }
    
    public List<ModelElement> getModelElements()
    {
	if(elements != null) return elements;
	return parent != null ? parent.getModelElements() : Collections.emptyList();
    }

    public Collection<String> getTextureKeys()
    {
	return textures != null ? textures.keySet() : Collections.emptyList();
    }

    public Collection<String> getTextureValues()
    {
	return textures != null ? textures.values() : Collections.emptyList();
    }

    public Collection<String> getTextureVariables()
    {
	return getTextureVariables(new ArrayList<>(), true);
    }

    //The boolean exists to prevent redundant checking of model elements for texture variables
    private Collection<String> getTextureVariables(List<String> variables, boolean checkModelElements)
    {
	if(checkModelElements)
	{
	    for(ModelElement element : this.getModelElements())
	    {
		for(FaceSet.FaceDir dir : FaceSet.FaceDir.values())
		{
		    String texture = element.getFaces().getFace(dir).texture;
		    if(texture.startsWith("#")) variables.add(texture.substring(1));
		}
	    }
	}
	Set<String> textureVariablesTextures = this.getTextureValues().stream().filter(value -> value.startsWith("#")).map(value -> value.substring(1)).collect(Collectors.toSet());
	variables.addAll(textureVariablesTextures);
	if(parent != null) parent.getTextureVariables(variables, false);
	return variables;
    }

    public String getTextureVariableValue(String varName)
    {
	String value = null;
	if(textures != null) value = textures.get(varName);
	if(value == null && parent != null) value = parent.getTextureVariableValue(varName);
	return value != null ? value : "";
    }
    
    public void setTextureVariableValue(String varName, String value)
    {
	if(!getTextureVariables().contains(varName)) throw new IllegalArgumentException("Texture variable " + varName + " does not exist");
	textures.put(varName, value);
    }

    protected abstract void deserialise(JsonElement jsonElement);

    protected abstract void serialise(JsonElement jsonElement);

    public static JSONModel deserialise(Path modelPath, MCResourceRepositoryAggregate repoAggregate) throws IOException
    {	
	JSONModel model = null; 
	if(modelPath.toString().contains("item")) model = SERIALISER.fromJson(Files.newBufferedReader(modelPath), ItemModel.class);
	if(modelPath.toString().contains("block")) model = SERIALISER.fromJson(Files.newBufferedReader(modelPath), BlockModel.class);
	
	if(model == null) throw new IllegalArgumentException(modelPath + " is not an item model or a block model.");

	model.location = getResLocFromModelPath(modelPath);
	if(model.hasParent())
	{
	    Path parentPath = repoAggregate.getResourcePath(MCResourceRepositoryAggregate.MODEL_RESOURCE, model.parentLocation);
	    model.parent = JSONModel.deserialise(parentPath, repoAggregate);
	}
	return model;
    }
    
    public static ResourceLocation getResLocFromModelPath(Path modelPath)
    {
	Matcher matcher = DOMAIN_AND_PATH_PATTERN.matcher(modelPath.toString());
	if(matcher.find()) return new ResourceLocation(matcher.group(1), matcher.group(2));
	throw new IllegalArgumentException(modelPath + " is not an item model or a block model.");
    }

    public static String serialise(JSONModel model) throws IOException
    {	
	JsonElement element = SERIALISER.toJsonTree(model);
	model.serialise(element);
	return SERIALISER.toJson(element);
    }
    
    

    public boolean hasParent()
    {
	return parentLocation != null;
    }

    public static class Serialiser implements JsonSerializer<JSONModel>, JsonDeserializer<JSONModel>
    {
	@Override
	public JSONModel deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException
	{
	    JSONModel model = type == ItemModel.class ? new ItemModel() : new BlockModel();
	    JsonObject jsonObj = element.getAsJsonObject();
	    if(jsonObj.has("parent")) model.parentLocation = new ResourceLocation(jsonObj.get("parent").getAsString());
	    if(jsonObj.has("display")) model.display = context.deserialize(jsonObj.get("display"), Transforms.class);
	    if(jsonObj.has("textures")) model.textures = context.deserialize(jsonObj.get("textures"), new TypeToken<Map<String, String>>() {}.getType());
	    if(jsonObj.has("elements")) model.elements = context.deserialize(jsonObj.get("elements"), new TypeToken<List<ModelElement>>() {}.getType());
	    model.deserialise(jsonObj);
	    return model;
	}

	@Override
	public JsonElement serialize(JSONModel model, Type type, JsonSerializationContext context)
	{
	    JsonObject jsonObj = new JsonObject();
	    if(model.parentLocation != null) jsonObj.addProperty("parent", model.parentLocation.toString());
	    if(model.display != null) jsonObj.add("display", context.serialize(model.display));
	    if(model.textures != null) jsonObj.add("textures", context.serialize(model.textures));
	    if(model.elements != null) jsonObj.add("elements", context.serialize(model.elements));
	    model.serialise(jsonObj);
	    return jsonObj;
	}	
    }

    public enum ModelType {ITEM, BLOCK;}
}
