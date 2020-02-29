package tutorial691online.visitors;


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;

import tutorial691online.patterns.AbstractFinder;

public class OverCatchVisitor extends AbstractVisitor{
	
	private Set<TryStatement> antipatternNodes = new HashSet<TryStatement>();
	// all possible thrown exceptions of the current try block
	private Set<ITypeBinding> exceptionTypes = new HashSet<ITypeBinding>();

	// check if there are not equal but sub-type compatible cases
	@Override
	public boolean visit(TryStatement node) {
		MethodInvocationVisitor miv = new MethodInvocationVisitor();
		node.getBody().accept(miv);
		@SuppressWarnings("unchecked")
		List<CatchClause> catches = node.catchClauses();
		boolean result = false;
		
		// if over catch checked exceptions
		for (ITypeBinding e : exceptionTypes) {
			for (CatchClause cc : catches) {
				ITypeBinding catchedException = cc.getException().getType().resolveBinding();
				// nested if means it finds a caught exception that is the super class of thrown Exception
				if (e.isSubTypeCompatible(catchedException)) {
					if (!catchedException.getQualifiedName().equals(e.getQualifiedName())) {
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
				Type type = miv.thrownException.get(exceptionName);
				ITypeBinding typeBinding = type.resolveBinding();
				for (CatchClause cc : catches) {
					ITypeBinding catchedException = cc.getException().getType().resolveBinding();
					// nested if means it finds a caught exception that is the super class of thrown Exception
					if (typeBinding.isSubTypeCompatible(catchedException)) {
						if (!catchedException.getQualifiedName().equals(typeBinding.getQualifiedName())) {
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
		if (result) {
			antipatternNodes.add(node);
		}
		return super.visit(node);
	}
	
	class MethodInvocationVisitor extends ASTVisitor {
		Map<String, Type> thrownException = new HashMap<String, Type>();
		@Override
		public boolean visit(MethodInvocation node) {
			IMethodBinding methodBinding = node.resolveMethodBinding();
			for(ITypeBinding typeBinding : methodBinding.getExceptionTypes()) {
				exceptionTypes.add(typeBinding);
			}
			IMethod iMethod = (IMethod) methodBinding.getJavaElement();
			
			// only handle source file. ignore binary(.class) files
			if (!iMethod.isBinary()) {
				// refer to: https://stackoverflow.com/questions/47090784/how-to-get-astnode-definition-in-jdt
				ICompilationUnit icu = iMethod.getCompilationUnit();
				if (icu != null) {
					CompilationUnit cu = AbstractFinder.parse(icu);
					ASTNode methodNode = cu.findDeclaringNode(methodBinding.getKey());
					ThrowVisitor checkThrowVisitor = new ThrowVisitor();
					methodNode.accept(checkThrowVisitor);
					thrownException.putAll(checkThrowVisitor.throwException);
				}
			}
			return super.visit(node);
		}
	}
}
