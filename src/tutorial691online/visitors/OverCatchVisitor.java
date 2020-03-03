package tutorial691online.visitors;


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;

import tutorial691online.patterns.AbstractFinder;

public class OverCatchVisitor extends AbstractVisitor{
	// all possible thrown exceptions of the current try block
	private Set<ITypeBinding> checkedExceptionTypes = new HashSet<ITypeBinding>();

	// check if there are not equal but sub-type compatible cases
	@Override
	public boolean visit(TryStatement node) {
		@SuppressWarnings("unchecked")
		List<CatchClause> catches = node.catchClauses();
		if (catches == null || catches.isEmpty()) {
			return super.visit(node);
		}
		MethodInvocationVisitor miv = new MethodInvocationVisitor();
		node.getBody().accept(miv);
		boolean result = false;
		
		// if over catch checked exceptions
		for (ITypeBinding e : checkedExceptionTypes) {
			for (CatchClause cc : catches) {
				ITypeBinding caughtException = cc.getException().getType().resolveBinding();
				// nested if means it finds a caught exception that is the super class of thrown Exception
				if (e.isSubTypeCompatible(caughtException)) {
					if (!caughtException.getQualifiedName().equals(e.getQualifiedName())) {
						result |= true;
						break;
					}
				}
			}
			if (result) {
				break;
			}
		}

		// if over catch unchecked exceptions
		if (!result) {
			for (String exceptionName : miv.thrownException.keySet()) {
				ITypeBinding typeBinding = miv.thrownException.get(exceptionName);
				for (CatchClause cc : catches) {
					ITypeBinding caughtException = cc.getException().getType().resolveBinding();
					// nested if means it finds a caught exception that is the super class of thrown Exception
					if (typeBinding.isSubTypeCompatible(caughtException)) {
						if (!caughtException.getQualifiedName().equals(typeBinding.getQualifiedName())) {
							result |= true;
							break;
						}
					}
				}
				if (result) {
					break;
				}
			}
		}
		
		// if over catch local java doc exceptions
		if (!result) {
			for (String type : miv.localJavadocExceptions.keySet()) {
				ITypeBinding typeBinding = miv.localJavadocExceptions.get(type);
				for (CatchClause cc : catches) {
					ITypeBinding caughtException = cc.getException().getType().resolveBinding();
					// nested if means it finds a caught exception that is the super class of thrown Exception
					if (typeBinding.isSubTypeCompatible(caughtException)) {
						if (!caughtException.getQualifiedName().equals(typeBinding.getQualifiedName())) {
							result |= true;
							break;
						}
					}
				}
				if (result) {
					break;
				}
			}
		}
		
		// if over catch online java doc exceptions
		if (!result) {
			boolean caughtSuperException = false;
			for (CatchClause cc : catches) {
				ITypeBinding caughtException = cc.getException().getType().resolveBinding();
				caughtSuperException |= ThrowVisitor.superExceptions.contains(caughtException.getQualifiedName());
				miv.javadocExceptions.remove(caughtException.getQualifiedName());
			}
			// if all exceptions are caught with the exactly same exception type, that's fine, otherwise,
			if (!miv.javadocExceptions.isEmpty()) {
				ASTNode parent = node.getParent();
				while(parent.getNodeType() != ASTNode.METHOD_DECLARATION && parent.getNodeType() != ASTNode.COMPILATION_UNIT) {
					parent = parent.getParent();
				}
				// check if they are thrown
				if (parent.getNodeType() == ASTNode.METHOD_DECLARATION) {
					MethodDeclaration methodDeclaration = (MethodDeclaration)parent;
					@SuppressWarnings("unchecked")
					List<Type> thrownExceptionTypes = methodDeclaration.thrownExceptionTypes();
					for(Type type : thrownExceptionTypes) {
						miv.javadocExceptions.remove(type.resolveBinding().getQualifiedName());
					}
				}
				// if all others are thrown, that is fine; otherwise, if the catch clause catch a super exception, then it could suffer over catch.
				// this strategy may cause false positive.
				if (!miv.javadocExceptions.isEmpty() && caughtSuperException) {
					result |= true;
				}
			}
		}
		
		if (result) {
			antipatternNodes.add(node);
		}
		return super.visit(node);
	}
	
	class MethodInvocationVisitor extends ASTVisitor {
		Map<String, ITypeBinding> thrownException = new HashMap<String, ITypeBinding>();
		Map<String, ITypeBinding> localJavadocExceptions = new HashMap<String, ITypeBinding>();
		Set<String> javadocExceptions = new HashSet<String>();
		@Override
		public boolean visit(MethodInvocation node) {
			IMethodBinding methodBinding = node.resolveMethodBinding();
			if (methodBinding == null) {
				System.out.println(methodBinding);
				return super.visit(node);
			}
			for(ITypeBinding typeBinding : methodBinding.getExceptionTypes()) {
				checkedExceptionTypes.add(typeBinding);
			}
			IMethod iMethod = (IMethod) methodBinding.getJavaElement();
			if (iMethod == null) {
				return super.visit(node);
			}
			CompilationUnit cu = null;
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
				ASTNode astNode = cu.findDeclaringNode(methodBinding.getKey());
				if (astNode != null && astNode.getNodeType() == ASTNode.METHOD_DECLARATION) {
					MethodDeclaration methodNode = (MethodDeclaration) astNode;
					ThrowVisitor checkThrowVisitor = new ThrowVisitor(new HashSet<String>());
					methodNode.accept(checkThrowVisitor);
					thrownException.putAll(checkThrowVisitor.getThrowException());
					javadocExceptions.addAll(checkThrowVisitor.getJavadocExceptions());
					localJavadocExceptions.putAll(checkThrowVisitor.getLocalJavadocExceptions());
					
					Map<String, ITypeBinding> localJdocException = Util.getLocalJavadocExceptions(methodNode.getJavadoc());
					hasLocalJavadoc = localJdocException != null;
					if (hasLocalJavadoc) {
						localJavadocExceptions.putAll(localJdocException);
					}
				}
			}
			
			if (!hasLocalJavadoc) {
				if (iMethod.isBinary()) {
					javadocExceptions.addAll(Util.getJavadocExceptions(iMethod));
				}
			}
			return super.visit(node);
		}
	}
}
