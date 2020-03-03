package tutorial691online.patterns;

import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import tutorial691online.handlers.DetectException;
import tutorial691online.visitors.AbstractVisitor;

public class AbstractFinder {

	HashMap<MethodDeclaration, String> suspectMethods = new HashMap<>();
	HashMap<ASTNode, String> suspectNodes = new HashMap<>();
	String type;
	AbstractVisitor visitor;

	public void findExceptions(IProject project) throws JavaModelException {
		IPackageFragment[] packages = JavaCore.create(project).getPackageFragments();
		int i = 0;
		for(IPackageFragment mypackage : packages){
			System.out.println("package " + ++i);
			findTarget(mypackage);
		}
	}
	
	private void findTarget(IPackageFragment packageFragment) throws JavaModelException {
		for (ICompilationUnit unit : packageFragment.getCompilationUnits()) {
			CompilationUnit parsedCompilationUnit = parse(unit);
			
			//do method visit here and check stuff
			parsedCompilationUnit.accept(this.visitor);
			getAntipatternMethods();
		}
	}	
	
	protected MethodDeclaration findParentMethodDeclaration(ASTNode node) {
		try {
			if(node.getParent().getNodeType() == ASTNode.METHOD_DECLARATION) {
				return (MethodDeclaration)node.getParent();
			} else {
				return findParentMethodDeclaration(node.getParent());
			}
		} catch (NullPointerException e) {
			return null;
		}
	}
	
	public void printExceptions() {
		for(MethodDeclaration declaration : suspectMethods.keySet()) {
			String type = suspectMethods.get(declaration);
			DetectException.printMessage(String.format("The following method suffers from the %s pattern", type));
			DetectException.printMessage(declaration.toString());
		}
	}
	
	protected void getAntipatternMethods() {
		for(ASTNode antipatternNode : this.visitor.getAntipatternNodes()) {
			MethodDeclaration methodDec = findParentMethodDeclaration(antipatternNode);
			if (methodDec != null) {
				suspectMethods.put(methodDec, this.type);
			} else {
				suspectNodes.put(antipatternNode, this.type);
			}
		}
	}
	
	public static CompilationUnit parse(IClassFile icf) {
		@SuppressWarnings("deprecation")
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(icf);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.setStatementsRecovery(true);
		return (CompilationUnit) parser.createAST(null); // parse
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
