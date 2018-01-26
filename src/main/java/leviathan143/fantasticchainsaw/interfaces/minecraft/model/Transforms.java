package leviathan143.fantasticchainsaw.interfaces.minecraft.model;

public class Transforms
{
    private Transform thirdperson_righthand;
    private Transform thirdperson_lefthand;
    private Transform firstperson_righthand;
    private Transform firstperson_lefthand;
    private Transform gui;
    private Transform head;
    private Transform ground;
    private Transform fixed;

    public Transform getTransform(TransformType type)
    {
	switch (type)
	{
	case TRANSFORM_1P_LH: return firstperson_lefthand;
	case TRANSFORM_1P_RH: return firstperson_righthand;
	case TRANSFORM_3P_LH: return thirdperson_lefthand;
	case TRANSFORM_3P_RH: return thirdperson_righthand;
	case TRANSFORM_FIXED: return fixed;
	case TRANSFORM_GROUND: return ground;
	case TRANSFORM_GUI: return gui;
	case TRANSFORM_HEAD: return head;
	default: return null;
	}
    }

    public static class Transform
    {
	public Vec3i rotation;
	public Vec3i translation;
	public Vec3i scale;
    }

    public enum TransformType
    {
	TRANSFORM_3P_RH,
	TRANSFORM_3P_LH,
	TRANSFORM_1P_RH,
	TRANSFORM_1P_LH,
	TRANSFORM_GUI,
	TRANSFORM_HEAD,
	TRANSFORM_GROUND,
	TRANSFORM_FIXED;
    }
}