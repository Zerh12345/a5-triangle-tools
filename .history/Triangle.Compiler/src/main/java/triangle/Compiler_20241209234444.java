package triangle;

import org.apache.commons.cli.*;

import triangle.abstractSyntaxTrees.Program;
import triangle.codeGenerator.Emitter;
import triangle.codeGenerator.Encoder;
import triangle.contextualAnalyzer.Checker;
import triangle.optimiser.ConstantFolder;
import triangle.syntacticAnalyzer.Parser;
import triangle.syntacticAnalyzer.Scanner;
import triangle.syntacticAnalyzer.SourceFile;
import triangle.treeDrawer.Drawer;

/**
 * The main driver class for the Triangle compiler.
 *
 * @version 2.1 7 Oct 2003
 * @author Deryck F. Brown
 */
public class Compiler {

    /** The filename for the object program, normally obj.tam. */
    static String objectName = "obj.tam";
    static boolean showTree = false;
    static boolean folding = false;
    static boolean showTreeAfter = false;

    private static Scanner scanner;
    private static Parser parser;
    private static Checker checker;
    private static Encoder encoder;
    private static Emitter emitter;
    private static ErrorReporter reporter;
    private static Drawer drawer;

    /** The AST representing the source program. */
    private static Program theAST;

    /**
     * Compile the source program to TAM machine code.
     *
     * @param sourceName   the name of the file containing the source program.
     * @param objectName   the name of the file containing the object program.
     * @param showingAST   true iff the AST is to be displayed after contextual
     *                     analysis.
     * @param showingTable true iff the object description details are to be
     *                     displayed during code generation (not currently
     *                     implemented).
     * @return true iff the source program is free of compile-time errors, otherwise
     *         false.
     */
 static boolean compileProgram(String sourceName, String objectName, boolean showingAST, boolean showingTable) {
    SourceFile source = loadSourceFile(sourceName);
    initializeCompilationTools(source);

    theAST = parseProgram();
    if (hasErrors()) return false;

    performContextualAnalysis();
    if (showingAST) drawer.draw(theAST);

    if (folding) applyFoldingOptimization();

    return generateObjectCode(objectName, showingTable);
}

private static SourceFile loadSourceFile(String sourceName) {
    SourceFile source = SourceFile.ofPath(sourceName);
    if (source == null) {
        System.out.println("Can't access source file " + sourceName);
        System.exit(1);
    }
    return source;
}

private static void initializeCompilationTools(SourceFile source) {
    scanner = new Scanner(source);
    reporter = new ErrorReporter(false);
    parser = new Parser(scanner, reporter);
    checker = new Checker(reporter);
    emitter = new Emitter(reporter);
    encoder = new Encoder(emitter, reporter);
    drawer = new Drawer();
}

private static boolean hasErrors() {
    return reporter.getNumErrors() > 0;
}

private static void performContextualAnalysis() {
    checker.check(theAST);
}

private static void applyFoldingOptimization() {
    theAST.visit(new ConstantFolder());
}

private static boolean generateObjectCode(String objectName, boolean showingTable) {
    if (hasErrors()) return false;

    encoder.encodeRun(theAST, showingTable);
    boolean successful = !hasErrors();
    if (successful) emitter.saveObjectProgram(objectName);

    return successful;
}


    /**
     * Triangle compiler main program.
     *
     * @param args the only command-line argument to the program specifies the
     *             source filename.
     */
    public static void main(String[] args) {
    if (args.length < 1) {
        printUsageAndExit();
    }

    CommandLineArgs parsedArgs = parseArgs(args);

    boolean compiledOK = compileProgram(parsedArgs.sourceFile, parsedArgs.objectName, parsedArgs.showTree, false);

    handlePostCompilation(parsedArgs.showTree, compiledOK);
}

private static void printUsageAndExit() {
    System.out.println("Usage: tc filename [-o=outputfilename] [tree] [folding]");
    System.exit(1);
}

private static CommandLineArgs parseArgs(String[] args) {
    Options options = new Options();

    options.addOption("o", "objectName", true, "Specify the output object name.");
    options.addOption("s", "showTree", false, "Display the syntax tree.");
    options.addOption("f", "folding", false, "Enable folding optimization.");
    options.addOption("a", "showTreeAfter", false, "Show the tree after folding.");

    CommandLineParser parser = new DefaultParser();
    CommandLine cmd;
    CommandLineArgs parsedArgs = new CommandLineArgs();

    try {
        cmd = parser.parse(options, args);
        parsedArgs.sourceFile = cmd.getArgList().get(0);
        parsedArgs.objectName = cmd.getOptionValue("objectName", "obj.tam");
        parsedArgs.showTree = cmd.hasOption("showTree");
        parsedArgs.showTreeAfter = cmd.hasOption("showTreeAfter");
        parsedArgs.folding = cmd.hasOption("folding");

    } catch (ParseException e) {
        System.err.println("Error parsing command-line arguments: " + e.getMessage());
        System.exit(1);
    }

    return parsedArgs;
}

private static void handlePostCompilation(boolean showTree, boolean compiledOK) {
    if (showTree) {
        drawer.draw(theAST);
    }
    System.exit(compiledOK ? 0 : 1);
}

private static class CommandLineArgs {
    String sourceFile;
    String objectName;
    boolean showTree;
    boolean showTreeAfter;
    boolean folding;
}

}
