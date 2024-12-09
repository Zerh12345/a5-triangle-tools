package triangle.abstractSyntaxTrees;

import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.syntacticAnalyzer.SourcePosition;

public class SquareCommand extends Command {
    public final Identifier identifier;

    public SquareCommand(Identifier identifier, SourcePosition position) {
        super(position);
        this.identifier = identifier;
    }

    @Override
    public Object visit(CommandVisitor visitor, Object o) {
        return visitor.visitSquareCommand(this, o);
    }
}
