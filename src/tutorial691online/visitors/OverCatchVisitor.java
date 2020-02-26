package tutorial691online.visitors;


import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TryStatement;

public class OverCatchVisitor extends ASTVisitor{
	
//    private HashMap<Name, java.util.List<?>> exceptionType = new HashMap<Name, java.util.List<?>>();
//	
//	public HashMap<Name, java.util.List<?>> getExceptionType() {
//		return exceptionType;
//	}
//
//	public void setExceptionType(HashMap<Name, java.util.List<?>> exceptionType) {
//		this.exceptionType = exceptionType;
//	}

//	@Override
//	public boolean visit(TryStatement node) {
//		List<String> differentException = new ArrayList<String>();
//		List<String> catchName = new ArrayList<String>();
//		List<?> catchBodys = node.catchClauses();
//		Iterator<?> iter=catchBodys.iterator();
//		while(iter.hasNext()){  
//             CatchClause ca = (CatchClause) iter.next();
//             differentException.add(ca.getException().getType().toString());
//         }
//		node.getBody().accept(new ASTVisitor() {
//			@Override
//			public boolean visit(MethodInvocation node) {
//				catchName.add(node.getName().toString());
//				// TODO Auto-generated method stub
//				return super.visit(node);
//			}
//		});
//		// TODO Auto-generated method stub
//		for(String s : differentException) {
//			System.out.println("This is exception in catch "+ s);
//		}
//		
//		for(String s: catchName) {
//			System.out.println("This is method invocation " + s);
//		}
//		
//		for(List<?> excetpion: exceptionType.values()) {
//			for(Object a: excetpion) {
//				System.out.print("this is in the method declearation type " + a.toString()+"-->");
//			}
//		}
//		return super.visit(node);
//	}
	
	// all possible thrown exceptions of the current try block
	private Set<ITypeBinding> exceptionTypes = new HashSet<ITypeBinding>();

	@Override
	public boolean visit(CatchClause node) {
		return super.visit(node);
	}
	
	@Override
	public boolean visit(TryStatement node) {
		node.getBody().accept(new ASTVisitor() {
			@Override
			public boolean visit(MethodInvocation node) {
				IMethodBinding methodBinding = node.resolveMethodBinding();
				for(ITypeBinding typeBinding : methodBinding.getExceptionTypes()) {
					exceptionTypes.add(typeBinding);
				}
				return super.visit(node);
			}
		});
		return super.visit(node);
	}
}
