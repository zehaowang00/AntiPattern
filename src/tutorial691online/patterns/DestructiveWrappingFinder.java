package tutorial691online.patterns;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import tutorial691online.visitors.CatchWithThrowVisitor;

public class DestructiveWrappingFinder extends AbstractFinder {
	
	public DestructiveWrappingFinder() {
		this.type = "DestuctiveWrapping";
		this.visitor = new CatchWithThrowVisitor();
	}
	
	public void findExceptions(IProject project) throws JavaModelException {
		IPackageFragment[] packages = JavaCore.create(project).getPackageFragments();
		for(IPackageFragment mypackage : packages){
			findTargetCatchWithThrow(mypackage);
		}
	}
	
	private void findTargetCatchWithThrow(IPackageFragment packageFragment) throws JavaModelException {
		for (ICompilationUnit unit : packageFragment.getCompilationUnits()) {
			CompilationUnit parsedCompilationUnit = parse(unit);
			
			//do method visit here and check stuff
			parsedCompilationUnit.accept(this.visitor);
			getMethodsWithTargetCatchClauses(this.visitor);
		}
	}
	
	private void getMethodsWithTargetCatchClauses(ASTVisitor cwt) {
		for(CatchClause destuctiveWrapping: ((CatchWithThrowVisitor)cwt).getDestuctiveWrapping()) {
			suspectMethods.put(findParentMethodDeclaration(destuctiveWrapping), this.type);
		}
	}
}
