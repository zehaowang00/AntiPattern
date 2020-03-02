package tutorial691online.visitors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import tutorial691online.patterns.AbstractFinder;

public class ThrowVisitor extends ASTVisitor{
	
	private Map<String, ITypeBinding> throwException = new HashMap<String, ITypeBinding>();
	private Set<String> visitedMethods; // record methods that already have been visited to prevent infinite loop for the recursive methods, i.e. methodA calls methodA
	private Set<String> javadocExceptions = new HashSet<String>();
	private Map<String, ITypeBinding> localJavadocExceptions = new HashMap<String,ITypeBinding>();
	
	public Map<String, ITypeBinding> getLocalJavadocExceptions() {
		return localJavadocExceptions;
	}

	// to handle javadoc Exceptions
	static Set<String> superExceptions = new HashSet<String>();
	static {
		superExceptions.add("java.lang.RuntimeException");
		superExceptions.add("java.lang.Exception");
		superExceptions.add("java.lang.Throwable");
	}
	public ThrowVisitor(Set<String> visitedMethods) {
		this.visitedMethods = visitedMethods;
	}

	public Map<String, ITypeBinding> getThrowException() {
		return throwException;
	}
	
	public Set<String> getJavadocExceptions() {
		return javadocExceptions;
	}

	@Override
	public boolean visit(ThrowStatement node) {
		node.accept(new ASTVisitor() {
			@Override
			public boolean visit(ClassInstanceCreation cic) {
				ITypeBinding typeBinding = cic.getType().resolveBinding();
				throwException.put(typeBinding.getQualifiedName(), typeBinding);
				return super.visit(node);
			}

			@Override
			public boolean visit(SimpleName name) {
				ITypeBinding typeBinding = name.resolveTypeBinding();
				throwException.put(typeBinding.getQualifiedName(), typeBinding);
				return super.visit(node);
			}
		});
		return super.visit(node);
	}
	
	@Override
	public boolean visit(MethodInvocation node) {
		// check if the method has been visited, prevent infinite loop
		IMethodBinding methodBinding = node.resolveMethodBinding();
		IMethod iMethod = (IMethod) methodBinding.getJavaElement();
		if (!this.visitedMethods.add(iMethod.toString())) {
			return super.visit(node);
		}
		
		CompilationUnit cu = null;

		// get compilationUnit for binary files and source files
		if (iMethod.isBinary()) {
			IClassFile icf = iMethod.getClassFile();
			if (icf != null) {
				cu = AbstractFinder.parse(icf);
			}
		} else {
			ICompilationUnit icu = iMethod.getCompilationUnit();
			if (icu != null) {
				cu = AbstractFinder.parse(icu);
			}
		}
		
		boolean hasLocalJavadoc = false;
		if (cu != null) {
			ThrowVisitor visitor = new ThrowVisitor(this.visitedMethods);
			// find called methods and get all the throw Exception in method body
			// Add the Exceptions to the set exceptionTypes
			MethodDeclaration methodNode = (MethodDeclaration)cu.findDeclaringNode(methodBinding.getKey());
			methodNode.accept(visitor);

			Map<String, ITypeBinding> localJavadocExceptions = Util.getLocalJavadocExceptions(methodNode.getJavadoc());
			hasLocalJavadoc = localJavadocExceptions != null;
			if (hasLocalJavadoc) {
				this.localJavadocExceptions.putAll(localJavadocExceptions);
			}
			if (!hasLocalJavadoc) {
				if (iMethod.isBinary()){
					this.javadocExceptions.addAll(Util.getJavadocExceptions(iMethod));
				}
			}
			this.throwException.putAll(visitor.getThrowException());
			this.javadocExceptions.addAll(visitor.getJavadocExceptions());
			this.localJavadocExceptions.putAll(visitor.getLocalJavadocExceptions());
			
			ASTNode nodeParent = node.getParent();
			///Check whether a method is in try block
			while(!(nodeParent.getNodeType() == ASTNode.METHOD_DECLARATION || nodeParent.getNodeType() == ASTNode.TRY_STATEMENT)) {
				nodeParent = nodeParent.getParent();
			}
			
			if(nodeParent.getNodeType() == ASTNode.TRY_STATEMENT) {
				///to find out exception type in catch block 
				TryStatement tryNode = (TryStatement) nodeParent;
				@SuppressWarnings("unchecked")
				List<CatchClause> catchBodys = tryNode.catchClauses();
				Iterator<CatchClause> iter = catchBodys.iterator();
				Set<String> resolvedThrownExceptions = new HashSet<String>();
				Set<String> resolvedJavadocExceptions = new HashSet<String>();
				Set<String> resolvedLocalJavadocExceptions = new HashSet<String>();
				while(iter.hasNext()){
		             CatchClause catchClause = iter.next();
		             Type caughtExceptionType = catchClause.getException().getType();
		             ITypeBinding caughtExceptionTypeBinding = caughtExceptionType.resolveBinding();
		             // record all thrown exceptions that will be resolved by the current catch clause
		             for (String type : this.throwException.keySet()) {
		            	 ITypeBinding thrownExceptionType = this.throwException.get(type);
		            	 if (thrownExceptionType.isSubTypeCompatible(caughtExceptionTypeBinding)) {
		            		 resolvedThrownExceptions.add(type);
		            	 }
		             }
		             
		             // check if the current catch could handle the local java doc exceptions 
		             for (String type : this.localJavadocExceptions.keySet()) {
		            	 ITypeBinding localJavadocException = this.localJavadocExceptions.get(type);
		            	 if (localJavadocException.isSubTypeCompatible(caughtExceptionTypeBinding)) {
		            		 resolvedLocalJavadocExceptions.add(type);
		            	 }
		             }
		             
		             // check if the current catch could handle the java doc exception
		             String caughtExceptionTypeName = caughtExceptionTypeBinding.getQualifiedName();
		             for (String jdocException : this.javadocExceptions) {
		            	 if (jdocException.equals(caughtExceptionTypeName) || superExceptions.contains(caughtExceptionTypeName)) {
		            		 resolvedJavadocExceptions.add(jdocException);
		            	 }
		             }
		        }
				for (String resolvedThrownException : resolvedThrownExceptions) {
					visitor.getThrowException().remove(resolvedThrownException.intern());
				}
				for (String localJavadocException : resolvedLocalJavadocExceptions) {
					visitor.getLocalJavadocExceptions().remove(localJavadocException);
				}
				visitor.getJavadocExceptions().removeAll(resolvedJavadocExceptions);
			}
		}
		
		return super.visit(node);
	}
}
