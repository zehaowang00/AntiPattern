package tutorial691online.visitors;

import java.awt.List;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.TryStatement;

public class OverCatchVisitor extends ASTVisitor{
	
	private HashMap<Name, java.util.List<?>> exceptionType = new HashMap<Name, java.util.List<?>>();
	
	@Override
	public boolean visit(MethodDeclaration node) {
		if(!node.thrownExceptionTypes().isEmpty()) {
		   exceptionType.put(node.getName(), node.thrownExceptionTypes());
		   System.out.println("222");
		}
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(TryStatement node) {
		java.util.List<?> cc = node.catchClauses();
		System.out.println("333");
		for( Name aName : exceptionType.keySet()) {
			System.out.println("11");
			System.out.println(aName);
		}
		
//		node.getBody().accept(new ASTVisitor() {
//			@Override
//			public boolean visit(MethodInvocation node1) {
//				node1.getName()
//				// TODO Auto-generated method stub
//				return super.visit(node);
//			}
//		});
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	

}
