package tutorial691online.visitors;

import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTVisitor;

import org.eclipse.jdt.core.dom.TryStatement;

public class TryStatementVisitor extends ASTVisitor{
	private HashSet<TryStatement> nestedTry = new HashSet<>();
	
	public boolean visit(TryStatement node) {
		if (node.getBody().statements()!=null) {
			node.getBody().accept(new ASTVisitor() {
				public boolean visit(TryStatement ts) {
					if(ts.getBody()!=null) {
						nestedTry.add(node);			
					}	
					return false;	
				}
			});	
			
		}
		return super.visit(node);
	}

	public HashSet<TryStatement> getNestedTry(){
		return nestedTry;
	}
}
