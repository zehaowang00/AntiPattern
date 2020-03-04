package tutorial691online.visitors;

import java.util.List;

import javax.xml.bind.annotation.XmlElementDecl.GLOBAL;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.Type;

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
			node.getBody().accept(new ASTVisitor(){
				
				public boolean visit(ThrowStatement ts) {
					ts.accept(new ASTVisitor() {
						public boolean visit(ClassInstanceCreation cic) {
							boolean isGood = false;
							if(cic.arguments()!= null) {
								List<?> argumentsExpression = cic.arguments();
								for(int i = 0; i<argumentsExpression.size(); i++) {
									if(argumentsExpression.get(i).toString()
											.equalsIgnoreCase(variableName)) {
											isGood = true;
									        break;
									}
								}
							}
							if(!isGood) {
								antipatternNodes.add(node);
							}
							return super.visit(node);
						}
					   @Override
					public boolean visit(SimpleName nameNode) {
						 if(!nameNode.toString().equalsIgnoreCase(variableName)) {
							 antipatternNodes.add(nameNode);
						 }
						// TODO Auto-generated method stub
						return super.visit(nameNode);
					}
					});
					
					return super.visit(node);
				}
			});
			
//			System.out.println(node.getException());
		}
		return super.visit(node);
	}
}
