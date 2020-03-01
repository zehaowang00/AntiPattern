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
import org.eclipse.jdt.core.JavaModelException;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import tutorial691online.patterns.AbstractFinder;

public class ThrowVisitor extends ASTVisitor{
	
	Map<String, Type> throwException = new HashMap<String, Type>();
	Set<String> visitedMethods; // record methods that already have been visited to prevent infinite loop for the recursive methods, i.e. methodA calls methodA
	Set<String> javadocExceptions = new HashSet<String>();
	
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

	public Map<String, Type> getThrowException() {
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
				throwException.put(cic.getType().toString().intern(), cic.getType());
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
		ThrowVisitor visitor = new ThrowVisitor(this.visitedMethods);
		
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
		if (cu != null) {
			// find called methods and get all the throw Exception in method body
			// Add the Exceptions to the set exceptionTypes
			ASTNode methodNode = cu.findDeclaringNode(methodBinding.getKey());
			methodNode.accept(visitor);
			
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
				Set<String> resolvedThrownExceptions = new HashSet<String>();
				Set<String> resolvedJavadocExceptions = new HashSet<String>();
				while(iter.hasNext()){
		             CatchClause catchClause = (CatchClause) iter.next();
		             Type caughtExceptionType = catchClause.getException().getType();
		             ITypeBinding caughtExceptionTypeBinding = caughtExceptionType.resolveBinding();
		             // record all thrown exceptions that will be resolved by the current catch clause
		             for (String type : visitor.getThrowException().keySet()) {
		            	 ITypeBinding thrownExceptionType = visitor.getThrowException().get(type).resolveBinding();
		            	 if (thrownExceptionType.isSubTypeCompatible(caughtExceptionTypeBinding)) {
		            		 resolvedThrownExceptions.add(catchClause.getException().getType().toString().intern());
		            	 }
		             }
		             
		             // check if the current catch could handle the java doc exception
		             String caughtExceptionTypeName = caughtExceptionTypeBinding.getQualifiedName();
		             for (String jdocException : visitor.getJavadocExceptions()) {
		            	 if (jdocException.equals(caughtExceptionTypeName) || superExceptions.contains(caughtExceptionTypeName)) {
		            		 resolvedJavadocExceptions.add(jdocException);
		            	 }
		             }
		        }
				for (String resolvedThrownException : resolvedThrownExceptions) {
					visitor.getThrowException().remove(resolvedThrownException.intern());
				}
				visitor.getJavadocExceptions().removeAll(resolvedJavadocExceptions);
			}
			this.throwException.putAll(visitor.getThrowException());
			this.javadocExceptions.addAll(visitor.javadocExceptions);
		}
		
		// analyze javadoc for third-party libs
		if (iMethod.isBinary()){
			try {
				String javadocStr = iMethod.getAttachedJavadoc(null);
				if (javadocStr != null) {
					Document javadoc = Jsoup.parse(javadocStr);
					Element throwsLabel = javadoc.selectFirst("dt span:matches([Tt]hrows)");
					Element dd = throwsLabel.parent().nextElementSibling();
					while(dd != null) {
						Element a = dd.selectFirst("code a");
						String cls = a.text();
						String pkg = a.attr("title").split(" ")[2];
						javadocExceptions.add(pkg + "." + cls);
						dd = dd.nextElementSibling();
					}
				}
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
		return super.visit(node);
	}
}
