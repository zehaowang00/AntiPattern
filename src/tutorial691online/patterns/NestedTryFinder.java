package tutorial691online.patterns;

import java.util.HashMap;

import org.eclipse.core.resources.IProject;

import tutorial691online.handlers.SampleHandler;
import tutorial691online.visitors.TryStatementVisitor;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;

public class NestedTryFinder extends AbstractFinder {
	HashMap<MethodDeclaration, String> suspectMethods = new HashMap<>();
	
	public void findExceptions(IProject project) throws JavaModelException {
		IPackageFragment[] packages = JavaCore.create(project).getPackageFragments();
		for(IPackageFragment mypackage : packages){
			findTargetTryStatement(mypackage);
		}
	}
	
	private void findTargetTryStatement(IPackageFragment packageFragment) throws JavaModelException {
		for (ICompilationUnit unit : packageFragment.getCompilationUnits()) {
			CompilationUnit parsedCompilationUnit = parse(unit);
			
			//do method visit here and check stuff
			TryStatementVisitor exceptionVisitor = new TryStatementVisitor();
			parsedCompilationUnit.accept(exceptionVisitor);

			getMethodsWithTargetTryStatement(exceptionVisitor);
		}
	}
	
	private void getMethodsWithTargetTryStatement(TryStatementVisitor tryStatementVisitor) {
		for(TryStatement nestedTry: tryStatementVisitor.getNestedTry()) {
			suspectMethods.put(findMethodForTry(nestedTry), "NestedTry");
		}	
		
		
	}
	
	private ASTNode findParentMethodDeclaration(ASTNode node) {
		if(node.getParent().getNodeType() == ASTNode.METHOD_DECLARATION) {
			return node.getParent();
		} else {
			return findParentMethodDeclaration(node.getParent());
		}
	}
	
	private MethodDeclaration findMethodForTry(TryStatement nestedTry) {
		return (MethodDeclaration) findParentMethodDeclaration(nestedTry);
	}
	
	public HashMap<MethodDeclaration, String> getSuspectMethods() {
		return suspectMethods;
	}
	
	public void printExceptions() {
		for(MethodDeclaration declaration : suspectMethods.keySet()) {
			String type = suspectMethods.get(declaration);
			SampleHandler.printMessage(String.format("The following method suffers from the %s pattern", type));
			SampleHandler.printMessage(declaration.toString());
		}
	}
}
