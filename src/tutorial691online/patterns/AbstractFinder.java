package tutorial691online.patterns;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
	String projectName;
	int total = 0;

	public void findExceptions(IProject project) throws JavaModelException {
		projectName = project.getName();
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
			this.total++;
			MethodDeclaration methodDec = findParentMethodDeclaration(antipatternNode);
			if (methodDec != null) {
				suspectMethods.put(methodDec, this.type);
				try {
					writeFileContent("/home/bo/projects/AntiPattern/data/" + this.projectName + "-" + this.type, 
							"case " + this.total + "\n" + methodDec.toString() + "\n*****\n", true);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				suspectNodes.put(antipatternNode, this.type);
				try {
					writeFileContent("/home/bo/projects/AntiPattern/data/" + this.projectName + "-" + this.type, 
							"case " + this.total + "\n" + antipatternNode.toString() + "\n*****\n", true);
				} catch (IOException e) {
					e.printStackTrace();
				}
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
		CompilationUnit cu = null;
		try {
			cu = (CompilationUnit) parser.createAST(null);
		} catch (IllegalStateException e) {
			return null;
		}
		return cu; // parse
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

	public static void writeFileContent(String path, String content, boolean append) throws IOException {
		File f = new File(path);
		f.getParentFile().mkdirs();
		FileWriter writer = new FileWriter(f, append);
		writer.write(content);
		writer.flush();
		writer.close();
	}
}
