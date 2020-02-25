package tutorial691online.patterns;

import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.JavaModelException;

import tutorial691online.handlers.SampleHandler;
import tutorial691online.visitors.CatchClauseVisitor;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;

public class ExceptionFinder {
	HashMap<MethodDeclaration, String> suspectMethods = new HashMap<>();
	
	public void findExceptions(IProject project) throws JavaModelException {
		IPackageFragment[] packages = JavaCore.create(project).getPackageFragments();
		for(IPackageFragment mypackage : packages){
			findTargetCatchClauses(mypackage);
		}
	}
	
	private void findTargetCatchClauses(IPackageFragment packageFragment) throws JavaModelException {
		for (ICompilationUnit unit : packageFragment.getCompilationUnits()) {
			CompilationUnit parsedCompilationUnit = parse(unit);
			
			//do method visit here and check stuff
			CatchClauseVisitor exceptionVisitor = new CatchClauseVisitor();
			parsedCompilationUnit.accept(exceptionVisitor);

			getMethodsWithTargetCatchClauses(exceptionVisitor);
		}
	}
	
	private void getMethodsWithTargetCatchClauses(CatchClauseVisitor catchClauseVisitor) {
		
		for(CatchClause emptyCatch: catchClauseVisitor.getEmptyCatches()) {
			suspectMethods.put(findMethodForCatch(emptyCatch), "EmptyCatch");
		}	
		
		for(CatchClause dummyCatch: catchClauseVisitor.getDummyCatches()) {
			suspectMethods.put(findMethodForCatch(dummyCatch), "DummyCatch");
		}
		
	}
	
	private ASTNode findParentMethodDeclaration(ASTNode node) {
		if(node.getParent().getNodeType() == ASTNode.METHOD_DECLARATION) {
			return node.getParent();
		} else {
			return findParentMethodDeclaration(node.getParent());
		}
	}
	
	private MethodDeclaration findMethodForCatch(CatchClause catchClause) {
		return (MethodDeclaration) findParentMethodDeclaration(catchClause);
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
	
	public static CompilationUnit parse(ICompilationUnit unit) {
		@SuppressWarnings("deprecation")
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.setStatementsRecovery(true);
		return (CompilationUnit) parser.createAST(null); // parse
	}
}
