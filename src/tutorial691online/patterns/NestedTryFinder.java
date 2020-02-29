package tutorial691online.patterns;

import tutorial691online.visitors.TryStatementVisitor;

public class NestedTryFinder extends AbstractFinder {
	
	public NestedTryFinder() {
		this.visitor = new TryStatementVisitor();
		this.type = "NestedTry";
	}
}
