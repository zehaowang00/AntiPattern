package tutorial691online.visitors;

import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

public class AbstractVisitor extends ASTVisitor {
	protected HashSet<ASTNode> antipatternNodes = new HashSet<>();
	public HashSet<ASTNode >getAntipatternNodes() {
		return this.antipatternNodes;
	}
}
