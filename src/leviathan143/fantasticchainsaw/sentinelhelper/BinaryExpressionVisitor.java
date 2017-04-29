package leviathan143.fantasticchainsaw.sentinelhelper;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;;

public class BinaryExpressionVisitor extends ASTVisitor
{
	List<ASTNode> matchingNodes = new ArrayList<ASTNode>();
	
	@Override
	public boolean visit(InfixExpression node)
	{
		if(node.getOperator() == Operator.EQUALS || node.getOperator() == Operator.NOT_EQUALS)
		{
			Expression left = node.getLeftOperand();
			Expression right = node.getRightOperand();
			if(left instanceof NullLiteral && isTypeItemStack(right))
			{
				matchingNodes.add(right);
			}
			else if(right instanceof NullLiteral && isTypeItemStack(left))
			{
				matchingNodes.add(left);
			}
		}
		return node.getOperator() == Operator.CONDITIONAL_AND || node.getOperator() == Operator.CONDITIONAL_OR;
	}
	
	private boolean isTypeItemStack(Expression expression)
	{
		if(expression instanceof Name)
		{
			Name qualName = (Name) expression;
			IType type = (IType) qualName.resolveTypeBinding().getJavaElement();
			return type.equals(SentinelHelperCompilationParticipant.ITEMSTACK_TYPE);
		}
		else if(expression instanceof MethodInvocation)
		{
			MethodInvocation methodInvocation = (MethodInvocation) expression;
			IType returnType = (IType) methodInvocation.resolveMethodBinding().getReturnType().getJavaElement();
			return returnType.equals(SentinelHelperCompilationParticipant.ITEMSTACK_TYPE);
		}
		return false;
	}
}
