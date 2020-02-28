package tutorial691online.patterns;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import tutorial691online.visitors.CatchClauseVisitor;

public class ExceptionFinder extends AbstractFinder {
	
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
			suspectMethods.put(findParentMethodDeclaration(emptyCatch), "EmptyCatch");
		}	
		
		for(CatchClause dummyCatch: catchClauseVisitor.getDummyCatches()) {
			suspectMethods.put(findParentMethodDeclaration(dummyCatch), "DummyCatch");
		}
	}
}
