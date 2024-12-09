package triangle.abstractSyntaxTrees.commands;

import triangle.abstractSyntaxTrees.visitors.CommandVisitor;
import triangle.syntacticAnalyzer.SourcePosition;

public class SquareCommand extends Command {
    public final Identifier identifier;

    public SquareCommand(Identifier identifier, SourcePosition position) {
        super(position);
        this.identifier = identifier;
    }

    @Override
    public <TArg, TResult> TResult visit(CommandVisitor<TArg, TResult> visitor, TArg arg) {
        return visitor.visitSquareCommand(this, arg);
    }
}
