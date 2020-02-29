package tutorial691online.patterns;

import tutorial691online.visitors.OverCatchVisitor;

public class OverCatchFinder extends AbstractFinder {
	
	public OverCatchFinder() {
		this.type = "OverCatch";
		this.visitor = new OverCatchVisitor();
	}
}
