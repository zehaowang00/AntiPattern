package tutorial691online.visitors;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ThrowStatement;

public class CatchWithThrowVisitor extends AbstractVisitor {
	@Override
	public boolean visit(CatchClause node) {		
		///important logic    
		//1.找到每个catch中exception类型 
		//2. 找到catchblock中包含new exception 
		//3. 判断是否相同 
		//4.如果相同加入
		
		if (node.getException()!=null) {
			String exceptionType = node.getException().getType().toString();
//			System.out.println("exceptionType:"+exceptionType);
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
