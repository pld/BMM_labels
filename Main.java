/*
 * Main.java
 *
 * Created on 30 december 2006, 19:45
 *
 */

package BMM_labels;

/**
 *
 * @author peter
 * @author gideon
 */

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Main {
    public Induced_Grammar inducedGrammar;

    // special symbols
    public final static boolean PRINT_OUTPUT = false;
    public static boolean timer = false;
    public final static boolean DO_STOLCKE = true;

    /**
     * DO_STOLCKE_CONTROL computes for every accepted merge or chunk the DL 
     * values according to the `absolute' values of the Stolcke algorithm, so
     * that it can be compared against the relative values of the eGrids algo,
     * as an extra control
     */
    public final static boolean DO_STOLCKE_CONTROL = false;

    /**
     * although INCLUDE_POISSON option can be enabled, it doesn't make sense for 
     * unsupervised labelling so default=false
     */
    public static boolean INCLUDE_POISSON = false;
    public static double MU = 3.0;
    public static boolean INCLUDE_DIRICHLET = false;

    /**
     * dynamic and incremental update of merge bigrams: default=TRUE (faster)
     */
    public static boolean UPDATE_MERGE_BIGRAMS = true;

    /**
     * DO_LHS_MERGES is still not working, because results in clustering
     * everything into TOP category
     */
    public static boolean DO_LHS_MERGES = false;
    public final static boolean PRINT_DEBUG = false;
    public final static boolean PRINT_DUPLICATE_RULES_TO_SCREEN = false;
    public final static boolean PRINT_REMOVED_RULES_TO_SCREEN = false;
    public final static boolean PRINT_DUPLICATE_RULES_TO_FILE = false;

    public static String UNLABELED_FILE = "";
    public static String SPAN_FILE = "";

    // SPAN_IMPORT_FORMAT_MANUAL only relevant when reading in .csv file
    // with annotation for v=checked, etc
    public static boolean SPAN_IMPORT_FORMAT_MANUAL = false;
    
    public static boolean INDUCE_FROM_POSTAGS = false;
    public static int MININUM_REMOVED_SYMBOLS = 0;
    public static int MAXLOOKAHEAD = 10;
    public static int BEAM_SIZE = 10;
    public static boolean INDUCE_MULTIGRAMS = false;
    public static boolean WRITE_INTERMEDIATE_GRAMMARS_AND_PARSES = false;
    public static String OUTPUT_DIRECTORY="./Output";

    public final static int TERMINAL = 1;
    public final static int NONTERMINAL = 2;

    // extra functionality
    public final static boolean PRINT_TARGET_GRAMMAR = false;
    public final static boolean COMPUTE_TARGET_GRAMMAR_MDL = false;

    /**
     * Filters out only those sentences for which 70% of words in sentence occur
     * more than x times
     */
    public final static boolean FILTER_WSJ_SENTENCES = false;

    // targets a specific number of non-lexical non-terminals
    // ignored if set to 0
    public final static int NON_LEX_NON_TERMINAL_MAX = 0;

    /** Creates a new instance of Main */
    public Main(String[] args) throws Exception {
        // collect options, usage:
        // BMM_labels plain_textfile span_file [postag] [dirichlet] [multigrams]
        // [lookahead=la] [beam=b]


        UNLABELED_FILE = args[0];
        SPAN_FILE = args[1];

        for (String s: args) {
            if (s.toLowerCase().equals("postag")) INDUCE_FROM_POSTAGS = true;
            if (s.toLowerCase().equals("dirichlet")) INCLUDE_DIRICHLET = true;
            if (s.toLowerCase().equals("multigrams")) INDUCE_MULTIGRAMS = true;
            if (s.toLowerCase().startsWith("lookahead=")) {
              MAXLOOKAHEAD = java.lang.Integer.parseInt(s.split("=")[1]);
            }
            if (s.toLowerCase().startsWith("beam=")) {
              BEAM_SIZE = java.lang.Integer.parseInt(s.split("=")[1]);
            }
        }

        Reader myReader = new Reader();
        myReader.readSentencesFromTextFile(Main.UNLABELED_FILE, Main.SPAN_FILE);

        // initialize induced_grammar from training samples
        // creates Terminals, nonTerminals, rules + counts, and initial
        // mergeBigrams
        inducedGrammar = new Induced_Grammar(myReader.getSentences());

        System.out.println("# non-terminals =" + inducedGrammar.nonTerminals.size());
        System.out.println("# TOP-rules =" + inducedGrammar.nonTerminals.get("TOP").getRules().size());
        if (PRINT_OUTPUT) {
            Printer.printoutTerminalsNonTerminalsAndRules(inducedGrammar, "induced");
        }

       if (Main.DO_STOLCKE) {
            //do merging and chunking
            System.out.println("Starting IteratedMergingAndChunking...");
            inducedGrammar.doIteratedMerging();
            System.out.println("Starting writeGrammarToFile...");
            Printer.printMergesAndChunks(inducedGrammar);
            if (Main.PRINT_DUPLICATE_RULES_TO_FILE) Printer.printDuplicateRulesToFile();
            if (Main.PRINT_TARGET_GRAMMAR)  Printer.printToFileGrammarTargetFormat (inducedGrammar);
            Printer.printViterbiParsesAndSpans(true, true, false, false);
            if (Main.WRITE_INTERMEDIATE_GRAMMARS_AND_PARSES) Printer.writeParsesToFile(inducedGrammar);
            Printer.printToFileTerminalsNonTerminalsAndRules(inducedGrammar);
       }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        new Main(args);
    }
}
