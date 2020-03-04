package tutorial691online.visitors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ThrowStatement;

public class CatchWithThrowVisitor extends AbstractVisitor {
	@Override
	public boolean visit(CatchClause node) {		
		///important logic    
		//1. find out every exception type in catch block  
		//2. find out new exception in throw or variable 
		//3. whether same  
		
		if (node.getException()!=null) {
			String exceptionType = node.getException().getType().toString();
			String variableName = node.getException().getName().toString();
			System.out.println("variable:"+variableName);
			node.getBody().accept(new ASTVisitor(){
				
				public boolean visit(ThrowStatement ts) {
					ts.accept(new ASTVisitor() {
						public boolean visit(ClassInstanceCreation cic) {
							String exceptionInstanceType = cic.getType().toString();
							if (exceptionInstanceType.equals(exceptionType)) {
								antipatternNodes.add(node);
							}
							return false;
						}
					});
					
					return false;
				}
			});
			
//			System.out.println(node.getException());
		}
		return super.visit(node);
	}
}
