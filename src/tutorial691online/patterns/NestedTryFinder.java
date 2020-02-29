package tutorial691online.patterns;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import tutorial691online.visitors.TryStatementVisitor;

public class NestedTryFinder extends AbstractFinder {
	
	public NestedTryFinder() {
		this.visitor = new TryStatementVisitor();
		this.type = "NestedTry";
	}
	
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
			parsedCompilationUnit.accept(this.visitor);
			getAntipatternMethods();
		}
	}
}
