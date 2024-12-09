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

        System.out.println("********** " + "Triangle Compiler (Java Version 2.1)" + " **********");

        System.out.println("Syntactic Analysis ...");
        SourceFile source = SourceFile.ofPath(sourceName);

        if (source == null) {
            System.out.println("Can't access source file " + sourceName);
            System.exit(1);
        }

        scanner = new Scanner(source);
        reporter = new ErrorReporter(false);
        parser = new Parser(scanner, reporter);
        checker = new Checker(reporter);
        emitter = new Emitter(reporter);
        encoder = new Encoder(emitter, reporter);
        drawer = new Drawer();

        // scanner.enableDebugging();
        theAST = parser.parseProgram(); // 1st pass
        if (reporter.getNumErrors() == 0) {
            // if (showingAST) {
            // drawer.draw(theAST);
            // }
            System.out.println("Contextual Analysis ...");
            checker.check(theAST); // 2nd pass
            if (showingAST) {
                drawer.draw(theAST);
            }
            if (folding) {
                theAST.visit(new ConstantFolder());
            }
            
            if (reporter.getNumErrors() == 0) {
                System.out.println("Code Generation ...");
                encoder.encodeRun(theAST, showingTable); // 3rd pass
            }
        }

        boolean successful = (reporter.getNumErrors() == 0);
        if (successful) {
            emitter.saveObjectProgram(objectName);
            System.out.println("Compilation was successful.");
        } else {
            System.out.println("Compilation was unsuccessful.");
        }
        return successful;
    }

    /**
     * Triangle compiler main program.
     *
     * @param args the only command-line argument to the program specifies the
     *             source filename.
     */
    public static void main(String[] args) {

        // Setup the CLI options
        Options options = new Options();

        options.addOption("o", "objectName", true, "Specify the output object name.");
        options.addOption("s", "showTree", false, "Display the syntax tree.");
        options.addOption("f", "folding", false, "Enable folding optimization.");
        options.addOption("a", "showTreeAfter", false, "Show the tree after folding.");

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            // Parse the arguments
            CommandLine cmd = parser.parse(options, args);

            // Handle the arguments
            String sourceName = cmd.getArgList().get(0); // The first argument is the source file
            objectName = cmd.getOptionValue("objectName", "obj.tam");
            showTree = cmd.hasOption("showTree");
            folding = cmd.hasOption("folding");
            showTreeAfter = cmd.hasOption("showTreeAfter");

            // Compile the program
            boolean compiledOK = compileProgram(sourceName, objectName, showTree, false);

            if (showTree && showTreeAfter) {
                drawer.draw(theAST); // Display the tree after folding
            }

            System.exit(compiledOK ? 0 : 1);
            
        } catch (ParseException e) {
            System.err.println("Error parsing command-line arguments: " + e.getMessage());
            formatter.printHelp("Triangle Compiler", options);
            System.exit(1);
        }
    }
}
