package tutorial691online.visitors;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.jsoup.select.Elements;

import tutorial691online.patterns.AbstractFinder;

public class ThrowVisitor extends ASTVisitor{
	
	Map<String, Type> throwException = new HashMap<String, Type>();
	Set<String> visitedMethods; // record methods that already have been visited to prevent infinite loop for the recursive methods, i.e. methodA calls methodA
	
	public ThrowVisitor(Set<String> visitedMethods) {
		this.visitedMethods = visitedMethods;
	}

	public Map<String, Type> getThrowException() {
		return throwException;
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
		
		if (!iMethod.isBinary()) {
			// refer to: https://stackoverflow.com/questions/47090784/how-to-get-astnode-definition-in-jdt
			ICompilationUnit icu = iMethod.getCompilationUnit();
			if (icu != null) {
				CompilationUnit cu = AbstractFinder.parse(icu);
				// TODO: find called methods and get all the throw Exception in method body
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
					Set<String> toBeResolved = new HashSet<String>();
					while(iter.hasNext()){
			             CatchClause ca = (CatchClause) iter.next();
			             ITypeBinding catchedExceptionType = ca.getException().getType().resolveBinding();
			             for (String type : visitor.getThrowException().keySet()) {
			            	 ITypeBinding thrownExceptionType = visitor.getThrowException().get(type).resolveBinding();
			            	 if (thrownExceptionType.isSubTypeCompatible(catchedExceptionType)) {
			            		 toBeResolved.add(ca.getException().getType().toString().intern());
			            	 }
			             }
			        }
					for (String tbr : toBeResolved) {
						visitor.getThrowException().remove(tbr.intern());
					}
				}
				this.throwException.putAll(visitor.getThrowException());
			}
		} else {
			try {
				String javadocStr = iMethod.getAttachedJavadoc(null);
				Document javadoc = Jsoup.parse(javadocStr);
				Element throwsLabel = javadoc.selectFirst("dt span:matches([Tt]hrows)");
				System.out.println(throwsLabel);
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
		return super.visit(node);
	}
}
