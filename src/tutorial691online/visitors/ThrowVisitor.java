package tutorial691online.visitors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;

import tutorial691online.patterns.AbstractFinder;

public class ThrowVisitor extends ASTVisitor{
	
	HashSet<String> throwException = new HashSet<String>();
	
	HashMap<MethodInvocation, HashSet<?>> methodWithException = new HashMap<MethodInvocation, HashSet<?>>();
	
	public HashSet<String> getThrowException() {
		return throwException;
	}

	public void setThrowException(HashSet<String> throwException) {
		this.throwException = throwException;
	}

	public HashMap<MethodInvocation, HashSet<?>> getMethodWithException() {
		return methodWithException;
	}

	public void setMethodWithException(HashMap<MethodInvocation, HashSet<?>> methodWithException) {
		this.methodWithException = methodWithException;
	}

	@Override
	public boolean visit(ThrowStatement node) {
		node.accept(new ASTVisitor() {
			@Override
			public boolean visit(ClassInstanceCreation cic) {
				String exceptionInsatanceType = cic.getType().toString();
				throwException.add(exceptionInsatanceType);
				// TODO Auto-generated method stub
				return super.visit(node);
			}
		});
		// TODO Auto-generated method stub
		return super.visit(node);
	}
	
	@Override
	public boolean visit(MethodInvocation node) {
		ThrowVisitor visitor = new ThrowVisitor();
		IMethodBinding methodBinding = node.resolveMethodBinding();
		IMethod iMethod = (IMethod) methodBinding.getJavaElement();
		
		if (!iMethod.isBinary()) {
			// refer to: https://stackoverflow.com/questions/47090784/how-to-get-astnode-definition-in-jdt
			ICompilationUnit icu = iMethod.getCompilationUnit();
			if (icu != null) {
				CompilationUnit cu = AbstractFinder.parse(icu);
				// TODO: find called methods and get all the throw Exception in method body
				// Add the Exceptions to the set exceptionTypes
				cu.findDeclaringNode(methodBinding);
				cu.accept(visitor);
				methodWithException.put(node, visitor.getThrowException());
				
				ASTNode nodeParent = node.getParent();
				
				///Check whether a method is in try block
				while(!(nodeParent.toString().equalsIgnoreCase("trystatement") || 
					  nodeParent.toString().equalsIgnoreCase("MethodDeclaration"))) {
					
					nodeParent = nodeParent.getParent();
				}
				
				if(nodeParent.toString().equalsIgnoreCase("trystatement")) {
					///to find out exception type in catch block 
					TryStatement tryNode = (TryStatement) nodeParent;
					List<?> catchBodys = tryNode.catchClauses();
					Iterator<?> iter=catchBodys.iterator();
					Set<String> differentException = new HashSet<String>();
					while(iter.hasNext()){  
			             CatchClause ca = (CatchClause) iter.next();
			             differentException.add(ca.getException().getType().toString());
			         }
					
				}
				else if (nodeParent.toString().equalsIgnoreCase("MethodDeclaration")) {
					//deem as not in try block
				}
			}
		}
		// TODO Auto-generated method stub
		return super.visit(node);
	}

   
}
