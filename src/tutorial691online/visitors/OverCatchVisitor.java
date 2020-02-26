package tutorial691online.visitors;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.TryStatement;

public class OverCatchVisitor extends ASTVisitor{
	
    private HashMap<Name, java.util.List<?>> exceptionType = new HashMap<Name, java.util.List<?>>();
	
	public HashMap<Name, java.util.List<?>> getExceptionType() {
		return exceptionType;
	}

	public void setExceptionType(HashMap<Name, java.util.List<?>> exceptionType) {
		this.exceptionType = exceptionType;
	}

	@Override
	public boolean visit(TryStatement node) {
		List<String> differentException = new ArrayList<>();
		List<?> catchBodys = node.catchClauses();
		Iterator<?> iter=catchBodys.iterator();
		while(iter.hasNext()){  
             CatchClause ca = (CatchClause) iter.next();
             differentException.add(ca.getException().getType().toString());
         }
		node.getBody().accept(new ASTVisitor() {
			@Override
			public boolean visit(MethodInvocation node) {
				// TODO Auto-generated method stub
				return super.visit(node);
			}
		});
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
}
	

