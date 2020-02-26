package tutorial691online.patterns;

import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import tutorial691online.handlers.SampleHandler;
import tutorial691online.visitors.ExceptionMethodVistor;
import tutorial691online.visitors.OverCatchVisitor;

public class OverCatchFinder extends AbstractFinder {
HashMap<MethodDeclaration, String> suspectMethods = new HashMap<>();
	
	public void findExceptions(IProject project) throws JavaModelException {
		IPackageFragment[] packages = JavaCore.create(project).getPackageFragments();
		for(IPackageFragment mypackage : packages){
			findTargetCatchClauses(mypackage);
		}
	}
	
	private void findTargetCatchClauses(IPackageFragment packageFragment) throws JavaModelException {
		ExceptionMethodVistor exceptionMethodVistor = new ExceptionMethodVistor();
		for (ICompilationUnit unit : packageFragment.getCompilationUnits()) {
			CompilationUnit parsedCompilationUnit = parse(unit);
			
			//do method visit here and check stuff
			parsedCompilationUnit.accept(exceptionMethodVistor);
            
			//getMethodsWithTargetCatchClauses(exceptionVisitor);
		}
		for (ICompilationUnit unit : packageFragment.getCompilationUnits()) {
			CompilationUnit parsedCompilationUnit = parse(unit);
			
			//do method visit here and check stuff
			OverCatchVisitor exceptionVisitor = new OverCatchVisitor();
			exceptionVisitor.setExceptionType(exceptionMethodVistor.getExceptionType());
			parsedCompilationUnit.accept(exceptionVisitor);
            
			//getMethodsWithTargetCatchClauses(exceptionVisitor);
		}
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
