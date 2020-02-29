package tutorial691online.visitors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.TryStatement;

public class TryStatementVisitor extends AbstractVisitor{

	public boolean visit(TryStatement node) {
		if (node.getBody().statements()!=null) {
			node.getBody().accept(new ASTVisitor() {
				public boolean visit(TryStatement ts) {
					if(ts.getBody()!=null) {
						antipatternNodes.add(node);			
					}	
					return false;	
				}
			});	
			
		}
		return super.visit(node);
	}
}
