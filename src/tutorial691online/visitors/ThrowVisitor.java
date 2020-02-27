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
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;

import tutorial691online.patterns.AbstractFinder;

public class ThrowVisitor extends ASTVisitor{
	
	HashSet<Type> throwException = new HashSet<Type>();
	
	HashMap<MethodInvocation, HashSet<?>> methodWithException = new HashMap<MethodInvocation, HashSet<?>>();
	
	public HashSet<Type> getThrowException() {
		return throwException;
	}

	@Override
	public boolean visit(ThrowStatement node) {
		node.accept(new ASTVisitor() {
			@Override
			public boolean visit(ClassInstanceCreation cic) {
				throwException.add(cic.getType());
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
				ASTNode methodNode = cu.findDeclaringNode(methodBinding.getKey());
				methodNode.accept(visitor);
				methodWithException.put(node, visitor.getThrowException());
				
				ASTNode nodeParent = node.getParent();
				
				///Check whether a method is in try block
				while(!(nodeParent.getNodeType() == ASTNode.METHOD_DECLARATION || nodeParent.getNodeType() == ASTNode.TRY_STATEMENT)) {
					nodeParent = nodeParent.getParent();
				}
				
				if(nodeParent.getNodeType() == ASTNode.TRY_STATEMENT) {
					///to find out exception type in catch block 
					TryStatement tryNode = (TryStatement) nodeParent;
					List<?> catchBodys = tryNode.catchClauses();
					Iterator<?> iter=catchBodys.iterator();
					Set<Type> toBeResolved = new HashSet<Type>();
					while(iter.hasNext()){
			             CatchClause ca = (CatchClause) iter.next();
			             ITypeBinding catchedExceptionType = ca.getException().getType().resolveBinding();
			             for (Type type : visitor.getThrowException()) {
			            	 ITypeBinding thrownExceptionType = type.resolveBinding();
			            	 if (thrownExceptionType.isSubTypeCompatible(catchedExceptionType)) {
			            		 toBeResolved.add(ca.getException().getType());
			            	 }
			             }
			        }
					visitor.getThrowException().removeAll(toBeResolved);
				} else if (nodeParent.toString().equalsIgnoreCase("MethodDeclaration")) {
					//deem as not in try block
				}
				
				this.throwException.addAll(visitor.getThrowException());
			}
		}
		// TODO Auto-generated method stub
		return super.visit(node);
	}

   
}
