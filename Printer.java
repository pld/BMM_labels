/*
 * Steps.java
 *
 * Created on 27 november 2005, 12:55
 *
 */

package BMM_labels;

/**
 *
 * @author peter
 * @author gideon
 */

import java.util.*;
import java.io.*;
import java.text.*;

import BMM_labels.Grammar;
import BMM_labels.Induced_Grammar;
import BMM_labels.Main;
import BMM_labels.partialParse;

public class Printer {
    /** Creates a new instance of Printer */
    public Printer() {
    }
   
    // presently not in use
    public void writeRandomSentencesToTextFile(String[][] randomSentences) throws Exception {
        BufferedWriter out = new BufferedWriter(new FileWriter(Main.OUTPUT_DIRECTORY + "/" + "RandomTestSentences.txt"));
        for (String[] mySentence : randomSentences) {     
           out.write(Utils.arrayToSentence(mySentence, 0) + ".");
           out.newLine();
        }
        out.flush();
        out.close();
    }

    // presently not in use
    public static void printToFileGrammarTargetFormat(Induced_Grammar inducedGrammar) throws IOException {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(Main.OUTPUT_DIRECTORY + "/" + "TargetGrammar.txt"));
            ArrayList<String> nonTArray = new ArrayList<String>();
            ArrayList<ArrayList<String>> myRules;
            String[] oneRule;

            nonTArray.addAll(inducedGrammar.nonTerminals.keySet());
            Collections.sort(nonTArray);
            for (String nonT : nonTArray) {
                myRules = inducedGrammar.GetListofRulesPlusCounts(nonT);
                // iterate over rules of nonT
                for (ArrayList<String> ruleArray : myRules) {
                    oneRule = ruleArray.toArray(new String[0]);
                    for (int j=0; j<oneRule.length-1; j++) {
                        out.write( oneRule[j] + " ");
                    }
                    // last one is count:
                    out.write("#" + oneRule[oneRule.length-1].substring(1, oneRule[oneRule.length-1].length()-1));
                    out.newLine();
                }
            }
            out.flush();
            out.close();
        } catch (IOException e)    { System.out.println(e.getMessage());  }
    }

    public static void printDuplicateRulesToFile() throws IOException {
        try {
           BufferedWriter out = new BufferedWriter(new FileWriter(Main.OUTPUT_DIRECTORY + "/" + Induced_Grammar.inputFile + "_duprules.txt"));
           out.write("#################### DUPLICATE RULES #######################");
           out.newLine();
           for (String dupRuleEntry :  Induced_Grammar.dupRulesForPrint) {
               out.write(dupRuleEntry);
               out.newLine();
           }
           out.flush();
           out.close();
        } catch (IOException e)    { System.out.println(e.getMessage());  }
    }

    public static void printMergesAndChunks(Induced_Grammar inducedGrammar) throws IOException {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(Main.OUTPUT_DIRECTORY + "/Merges_and_Chunks.csv"));
            out.write("##########################################################");
            out.newLine();
            out.write("#####################     MERGES     #####################");
            out.newLine();
            out.write("##########################################################");
            out.newLine();
            out.newLine();

            for (String myMerge : Induced_Grammar.mergesForPrint.keySet()) {
                int iCounter=0;
                for (String mergeEntry :  Induced_Grammar.mergesForPrint.get(myMerge)) {
                   iCounter++;
                   // Note: sequence may not be longer than 256 for import in Excel
                   if (iCounter%250==0) {
                       out.newLine();
                       out.write("$$$CONTINUE$$$ " + myMerge + ", ");
                   }
                   out.write(mergeEntry);
                   out.write(", ");
                }
                out.newLine();
            }

            out.write("##########################################################");
            out.newLine();
            out.write("#####################     CHUNKS     #####################");
            out.newLine();
            out.write("##########################################################");
            out.newLine();
            out.newLine();

            for (String myChunk : Induced_Grammar.chunksForPrint.keySet()) {
               for (String chunkEntry :  Induced_Grammar.chunksForPrint.get(myChunk)) {
                   out.write(chunkEntry);
                   out.write(", ");
               }
               out.newLine();
            }

            out.write("##########################################################");
            out.newLine();
            out.write("###################     NON-TERMINALS     ##################");
            out.newLine();
            out.write("##########################################################");
            out.newLine();
            out.newLine();

            int myCounter = 0;
            ArrayList<String> nonTArray = new ArrayList<String>();
            nonTArray.addAll(inducedGrammar.nonTerminals.keySet());
            Collections.sort(nonTArray);
            for (String nonT : nonTArray) {
               myCounter++;
               out.write(nonT + ", ");
               if (myCounter%10==0) out.newLine();
            }
            out.newLine();
            out.newLine();

            out.write("##########################################################");
            out.newLine();
            out.write("######################     RULES     #####################");
            out.newLine();
            out.write("##########################################################");
            out.newLine();
            out.newLine();

            for (String nonT : nonTArray) {
                // skip TOP and skip preTerminals
                if (!nonT.equals("TOP") && !nonT.startsWith("_")) {
                    ArrayList<ArrayList<String>> myRules = inducedGrammar.GetListofRulesPlusCounts(nonT);
                    // iterate over rules of nonT
                    for (ArrayList<String> ruleArray : myRules) {
                        String[] oneRule = ruleArray.toArray(new String[0]);
                        out.write(oneRule[0] + ", --> " );
                        for (int j=1; j<oneRule.length; j++)
                            out.write( ", " + oneRule[j]);
                        out.newLine();
                    }
                }
            }

           out.write("##########################################################");
           out.newLine();
           out.write("###############     PARAMETER SETTINGS     ###############");
           out.newLine();
           out.write("##########################################################");
           out.newLine();
           out.newLine();

           out.write("INCLUDE_POISSON, " + Main.INCLUDE_POISSON);
           out.newLine();
           if (Main.INCLUDE_POISSON) {
               out.write("MU, " + Main.MU);
               out.newLine();
           }
           out.write("INCLUDE_DIRICHLET, " + Main.INCLUDE_DIRICHLET);
           out.newLine();
           out.write("MAXLOOKAHEAD, " + Main.MAXLOOKAHEAD);
           out.newLine();
           out.write("BEAM_SIZE, " + Main.BEAM_SIZE);
           out.newLine();
           out.write("MININUM_REMOVED_SYMBOLS, " + Main.MININUM_REMOVED_SYMBOLS);
           out.newLine();

           if (Main.DO_STOLCKE_CONTROL) {
               out.write("##########################################################");
               out.newLine();
               out.write("############     DESCRIPTION LENGTH VALUES     ############");
               out.newLine();
               out.write("##########################################################");
               out.newLine();
               out.newLine();

               NumberFormat numberFormatter;
               numberFormatter = NumberFormat.getNumberInstance();

               out.write("INITIAL TOTAL DL," + numberFormatter.format(Induced_Grammar.storedMDLValues.get("INITIAL TOTAL DL").doubleValue()).toString().replace(',', '.'));
               out.newLine();
               out.write("INITIAL LIKELIHOOD," + numberFormatter.format(Induced_Grammar.storedMDLValues.get("INITIAL LIKELIHOOD").doubleValue()).toString().replace(',', '.'));
               out.newLine();
               out.write("INITIAL STRUCTURE PRIOR," + numberFormatter.format(Induced_Grammar.storedMDLValues.get("INITIAL STRUCTURE PRIOR").doubleValue()).toString().replace(',', '.'));
               out.newLine();
               out.write("INITIAL #RULES," + Induced_Grammar.storedMDLValues.get("INITIAL #RULES").toString());
               out.newLine();
               out.write("INITIAL #TERMINALS," + Induced_Grammar.storedMDLValues.get("INITIAL #TERMINALS").toString());
               out.newLine();
               out.write("INITIAL #NON-TERMINALS," + Induced_Grammar.storedMDLValues.get("INITIAL #NON-TERMINALS").toString());
               out.newLine();
               out.write("INITIAL #SYMBOLS," + Induced_Grammar.storedMDLValues.get("INITIAL #SYMBOLS").toString());
               out.newLine();
               out.write("FINAL TOTAL DL," + numberFormatter.format(Induced_Grammar.storedMDLValues.get("FINAL TOTAL DL").doubleValue()).toString().replace(',', '.'));
               out.newLine();
               out.write("FINAL LIKELIHOOD," + numberFormatter.format(Induced_Grammar.storedMDLValues.get("FINAL LIKELIHOOD").doubleValue()).toString().replace(',', '.'));
               out.newLine();
               out.write("FINAL STRUCTURE PRIOR," + numberFormatter.format(Induced_Grammar.storedMDLValues.get("FINAL STRUCTURE PRIOR").doubleValue()).toString().replace(',', '.'));
               out.newLine();
               out.write("FINAL #RULES," + Induced_Grammar.storedMDLValues.get("FINAL #RULES").toString());
               out.newLine();
               out.write("FINAL #TERMINALS," + Induced_Grammar.storedMDLValues.get("FINAL #TERMINALS").toString());
               out.newLine();
               out.write("FINAL #NON-TERMINALS," + Induced_Grammar.storedMDLValues.get("FINAL #NON-TERMINALS").toString());
               out.newLine();
               out.write("FINAL #SYMBOLS," + Induced_Grammar.storedMDLValues.get("FINAL #SYMBOLS").toString());
               out.newLine();
           }
            out.flush();
            out.close();
        } catch (IOException e)    { System.out.println(e.getMessage());  }
    }

    // presently not in use
    public void printSummaryScores(BufferedWriter out_summary) throws IOException {
        try {
            out_summary.write("##########################################################");
            out_summary.newLine();
            out_summary.newLine();

            out_summary.write("INCLUDE_POISSON, " + Main.INCLUDE_POISSON);
            out_summary.newLine();
            if (Main.INCLUDE_POISSON) {
               out_summary.write("MU, " + Main.MU);
               out_summary.newLine();
            }
            out_summary.write("INCLUDE_DIRICHLET, " + Main.INCLUDE_DIRICHLET);
            out_summary.newLine();

            out_summary.write("##########################################################");
            out_summary.newLine();
            out_summary.write("############     DESCRIPTION LENGTH VALUES     ############");
            out_summary.newLine();
            out_summary.write("##########################################################");
            out_summary.newLine();
            out_summary.newLine();

            NumberFormat numberFormatter;
            numberFormatter = NumberFormat.getNumberInstance();

            out_summary.write("INITIAL TOTAL DL," + numberFormatter.format(Induced_Grammar.storedMDLValues.get("INITIAL TOTAL DL").doubleValue()).toString().replace(',', '.'));
            out_summary.newLine();
            out_summary.write("INITIAL LIKELIHOOD," + numberFormatter.format(Induced_Grammar.storedMDLValues.get("INITIAL LIKELIHOOD").doubleValue()).toString().replace(',', '.'));
            out_summary.newLine();
            out_summary.write("INITIAL STRUCTURE PRIOR," + numberFormatter.format(Induced_Grammar.storedMDLValues.get("INITIAL STRUCTURE PRIOR").doubleValue()).toString().replace(',', '.'));
            out_summary.newLine();

            out_summary.write("FINAL TOTAL DL," + numberFormatter.format(Induced_Grammar.storedMDLValues.get("FINAL TOTAL DL").doubleValue()).toString().replace(',', '.'));
            out_summary.newLine();
            out_summary.write("FINAL LIKELIHOOD," + numberFormatter.format(Induced_Grammar.storedMDLValues.get("FINAL LIKELIHOOD").doubleValue()).toString().replace(',', '.'));
            out_summary.newLine();
            out_summary.write("FINAL STRUCTURE PRIOR," + numberFormatter.format(Induced_Grammar.storedMDLValues.get("FINAL STRUCTURE PRIOR").doubleValue()).toString().replace(',', '.'));
            out_summary.newLine();
          } catch (IOException e) { System.out.println(e.getMessage()); }
    }

    public static void printViterbiParsesAndSpans(boolean printLabeled, boolean printUnLabeled, boolean printSpans, boolean printSpansBackup)  throws IOException {
        ArrayList<String> labeledSentences = new ArrayList<String> ();
        ArrayList<String> unlabeledSentences = new ArrayList<String> ();

        StringBuffer originalSentenceString ;
        StringBuffer spanSentenceBuffer;
        int totalNrApprovedSpans = 0, totalNrUnapprovedSpans = 0;

        for (ArrayList<String> myRHS : Induced_Grammar.partialParseForest.keySet() ) {
            partialParse myPartialParse = Induced_Grammar.partialParseForest.get(myRHS);

            // if it was duplicate original sentence, then print it multiple
            // times, so score is weighted
            for (int i=1; i<=myPartialParse.getCount(); i++) {
                labeledSentences.add(myPartialParse.printWSJFormat());
                originalSentenceString = new StringBuffer();
                for (String myWord : myPartialParse.getOriginalSentence()) {
                    // you don't want non-terminals but terminals, so take off
                    // _ and turn into lowercase
                    originalSentenceString.append(myWord.substring(1, myWord.length()).toLowerCase() + " ");
                }
                originalSentenceString.append(".");
                unlabeledSentences.add(originalSentenceString.toString());
            }
        }

        // write labeled text to file
        if (printLabeled) {
            BufferedWriter out = new BufferedWriter(new FileWriter(Main.OUTPUT_DIRECTORY + "/" + "Viterbi_parses.csv"));
            int myCounter=0;
            for (String myLabeledSentence : labeledSentences) {
                out.write(unlabeledSentences.get(myCounter) + ";" + myLabeledSentence);
                out.newLine();
                myCounter++;
            }
            out.flush();
            out.close();
        }
    }

    // only called if (Main.WRITE_INTERMEDIATE_GRAMMARS_AND_PARSES)
    public static void writeParsesToFile(Induced_Grammar inducedGrammar)  throws IOException {
        // write OVIS format parses into file 
        BufferedWriter out_OVIS = new BufferedWriter(new FileWriter(Main.OUTPUT_DIRECTORY + "/" + "WSJ_parses_" + Induced_Grammar.nrChunk + ".txt"));
        parseTree myParseTree;
        for (ArrayList<String> key : Induced_Grammar.partialParseForest.keySet()) {
            partialParse myPP = Induced_Grammar.partialParseForest.get(key);
            myParseTree = myPP.getTree();
            boolean blnWithUnderScore = false;
            out_OVIS.write(myParseTree.printOVISFormat(blnWithUnderScore));
            out_OVIS.newLine();
        }
        out_OVIS.flush();
        out_OVIS.close();
    }

    public static void printoutTerminalsNonTerminalsAndRules(Grammar myGrammar, String sortGrammar) {
        String[] myTerminals = myGrammar.GetListofTerminals();
        String[] myNonTerminals = myGrammar.GetListofNonTerminals();
        System.out.println("The Terminals of the " + sortGrammar + " grammar are {" +  Utils.arrayToSentence(myTerminals, 0) + "} " );        
        System.out.println("");
        System.out.println("The nonTerminals of the " + sortGrammar + " grammar are {" +  Utils.arrayToSentence(myNonTerminals, 0) + "} " );        
        System.out.println("");
        System.out.println("The rules of the " + sortGrammar + " grammar are:");
        for (int k = 0; k < myNonTerminals.length; k++){
            // don't print preterminal rules
            if (!myNonTerminals[k].startsWith("_")) {
                ArrayList<ArrayList<String>> myRules = myGrammar.GetListofRulesPlusCounts(myNonTerminals[k]);
                // iterate over rules
                for (ArrayList<String> oneRuleArray : myRules) {
                    String[] oneRule = oneRuleArray.toArray(new String[0]);
                    System.out.println(oneRule[0] + " --> "  + Utils.arrayToSentence(oneRule, 1) );
                }
            }
        }
    }

    public static void printToFileTerminalsNonTerminalsAndRules(Grammar myGrammar) throws IOException {
        String[] myTerminals = myGrammar.GetListofTerminals();
        String[] myNonTerminals = myGrammar.GetListofNonTerminals();
        try {
            String grammarFile = null;
            grammarFile = Main.OUTPUT_DIRECTORY + "/" + "Induced_Grammar.txt";
            BufferedWriter out = new BufferedWriter(new FileWriter(grammarFile));
            out.write("TERMINALS");
            out.newLine();
            for (String myTerminal : myTerminals) {
                out.write(myTerminal);
                out.newLine();
            }
            out.write("NONTERMINALS");
            out.newLine();
            for (String myNonTerminal : myNonTerminals) {
               // if (Main.INITIALIZE_FROM_TARGET_GRAMMAR) don't include Postags in nonT nor preterminal rules
               if (!myGrammar.Terminals.containsKey(myNonTerminal.toLowerCase())) { 
                    out.write(myNonTerminal);
                    out.newLine();
               }
            }

            out.write("PRODUCTION RULES");
            out.newLine();

            printRulesAccordingToRHS( myGrammar, true, out);

            out.flush();
            out.close();
        } catch (IOException e)    {  System.out.println("ging mis");  }
    }

    public static void printRulesAccordingToRHS(Grammar myGrammar, boolean bWriteToFile, BufferedWriter out) throws IOException {
        String myRHSOne, myRHSTwo;

        for(nonTerminal nt : myGrammar.nonTerminals.values()){
            // if (Main.INITIALIZE_FROM_TARGET_GRAMMAR) don't include Postags in nonT nor preterminal rules
            if (!myGrammar.Terminals.containsKey(nt.getName().toLowerCase())) { 
                if (bWriteToFile) {
                    out.write("RULESOFNONTERMINAL " + nt.getName());
                    out.newLine();
                }
                for(Rule myRule : nt.getRules()){
                    StringBuffer rhsStringBuffer = new StringBuffer();
                    // make a string out of RHS of rule
                    for ( String myConstituent : myRule.getRightHandSide()) {
                        // if (Main.INITIALIZE_FROM_TARGET_GRAMMAR) write POSTAGs in lowercase
                        if (!myGrammar.Terminals.containsKey(myConstituent.toLowerCase())) { 
                            rhsStringBuffer.append(myConstituent).append('*');
                        } else {
                            rhsStringBuffer.append(myConstituent.toLowerCase()).append('*');
                        }
                    }
                    String rhsString = rhsStringBuffer.toString();
                    if (bWriteToFile) {
                        out.write(rhsString + "#" + myRule.getRuleProbability(nt));
                        out.newLine();
                    }
                }
            }
        }
    }
}
