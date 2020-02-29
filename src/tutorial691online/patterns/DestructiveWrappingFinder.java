package tutorial691online.patterns;

import tutorial691online.visitors.CatchWithThrowVisitor;

public class DestructiveWrappingFinder extends AbstractFinder {
	
	public DestructiveWrappingFinder() {
		this.type = "DestuctiveWrapping";
		this.visitor = new CatchWithThrowVisitor();
	}
}
