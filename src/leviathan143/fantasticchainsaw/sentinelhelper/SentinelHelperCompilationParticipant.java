package leviathan143.fantasticchainsaw.sentinelhelper;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.jdt.core.compiler.ReconcileContext;
import org.eclipse.jdt.core.dom.ASTNode;

import leviathan143.fantasticchainsaw.util.MarkerHelper;

public class SentinelHelperCompilationParticipant extends CompilationParticipant
{
	static IType ITEMSTACK_TYPE = null;

	@Override
	public void reconcile(ReconcileContext context)
	{
		try
		{
			IResource baseResource = context.getWorkingCopy().getResource();
			baseResource.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
			BinaryExpressionVisitor visitor = new BinaryExpressionVisitor();
			context.getAST8().accept(visitor);
			for(ASTNode node : visitor.matchingNodes)
			{
				MarkerHelper.createNormalWarning(baseResource, String.format("%1$s is of type ItemStack. Use ItemStack#isEmpty() instead of null-checking the returned value.", node.toString()), context.getAST8().getLineNumber(node.getStartPosition()));
			}
		} catch (JavaModelException e)
		{
			e.printStackTrace();
		} catch (CoreException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public boolean isActive(IJavaProject project)
	{
		return true;
	}

	@Override
	public int aboutToBuild(IJavaProject project)
	{
		try
		{
			ITEMSTACK_TYPE = project.findType("net.minecraft.item.ItemStack");
		} catch (JavaModelException e)
		{
			e.printStackTrace();
		}
		return READY_FOR_BUILD;
	}
}
