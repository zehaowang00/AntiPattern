package tutorial691online.visitors;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;

public class ExceptionMethodVistor extends ASTVisitor{
    private HashMap<Name, java.util.List<?>> exceptionType = new HashMap<Name, java.util.List<?>>();
	
	@Override
	public boolean visit(MethodDeclaration node) {
		if(!node.thrownExceptionTypes().isEmpty()) {
		   exceptionType.put(node.getName(), node.thrownExceptionTypes());
		}
		// TODO Auto-generated method stub
		return super.visit(node);
	}

	public HashMap<Name, java.util.List<?>> getExceptionType() {
		return exceptionType;
	}

	public void setExceptionType(HashMap<Name, java.util.List<?>> exceptionType) {
		this.exceptionType = exceptionType;
	}

}
