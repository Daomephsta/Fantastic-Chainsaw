package leviathan143.fantasticchainsaw.interfaces.minecraft.model;

public class ModelElement
{
    public Vec3i from;
    public Vec3i to;
    public Rotation rotation;
    public boolean shade = true;
    private FaceSet faces;

    public ModelElement() {}

    public static ModelElement createWithDefaultValues()
    {
	ModelElement e = new ModelElement();
	e.faces = new FaceSet();
	return e;
    }
    
    public FaceSet getFaces()
    {
	return faces;
    }

    public static class FaceSet
    {
	public Face down;
	public Face up;
	public Face north;
	public Face south;
	public Face west;
	public Face east;

	public Face getFace(FaceDir dir)
	{
	    switch(dir)
	    {
	    case down: return down;
	    case up: return up;
	    case north: return north;
	    case south: return south;
	    case west: return west;
	    case east: return east;
	    default: return null;
	    }
	}
	
	public static class Face
	{
	    public Vec4i uv;
	    public String texture;
	    public FaceDir cullface;
	    public int rotation;
	    public int tintindex;

	    public Face(Vec4i uv, String texture, FaceDir cullface, int rotation, int tintindex)
	    {
		super();
		this.uv = uv;
		this.texture = texture;
		this.cullface = cullface;
		this.rotation = rotation;
		this.tintindex = tintindex;
	    }
	}

	public enum FaceDir {down, up, north, south, west, east;}
    }

    public static class Rotation
    {
	public Vec3i origin;
	public Axis axis;
	public float angle;
	public boolean rescale;

	public enum Axis {x, y, z;}
    }
}
