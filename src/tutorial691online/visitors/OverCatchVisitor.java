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

public class OverCatchVisitor extends ASTVisitor{
	
	// all possible thrown exceptions of the current try block
	private Set<String> exceptionTypes = new HashSet<String>();
	public final Set<String> getExceptionTypes() {
		return exceptionTypes;
	}

	@Override
	public boolean visit(CatchClause node) {
		return super.visit(node);
	}
	
	@Override
	public boolean visit(TryStatement node) {
		MethodInvocationVisitor miv = new MethodInvocationVisitor();
		node.getBody().accept(miv);
		@SuppressWarnings("unchecked")
		List<CatchClause> catches = node.catchClauses();
		for (CatchClause cc : catches) {
			String exName = cc.getException().getType().toString().intern();
			if(!miv.thrownException.containsKey(exName) && !this.exceptionTypes.contains(exName)) {
				System.out.println(node);
			}
		}
		return super.visit(node);
	}
	
	class MethodInvocationVisitor extends ASTVisitor {
		Map<String, Type> thrownException = new HashMap<String, Type>();
		@Override
		public boolean visit(MethodInvocation node) {
			IMethodBinding methodBinding = node.resolveMethodBinding();
			for(ITypeBinding typeBinding : methodBinding.getExceptionTypes()) {
				exceptionTypes.add(typeBinding.getName().intern());
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
