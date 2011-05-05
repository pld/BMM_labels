/*
 * Induced_Grammar.java
 *
 * Created on 26 november 2005, 15:32
 *
 */

package BMM_labels;

/**
 *
 * @author peter
 * @author gideon
 */

import java.util.*;
import java.text.*;
import java.io.*;
import java.lang.Math;

import BMM_labels.Printer;
import BMM_labels.Terminal;
import BMM_labels.Utils;

public class Induced_Grammar extends Grammar {
    // keeps track of nr of created nonTerminals
    protected static int maxNrNonTerminal = 1;
    protected static int nrChunk = 0;
    protected static int chunkApplied = 0;
    // keeps track of nr of created nonTerminals
    public static HashMap<String, Double> storedMDLValues = new HashMap<String, Double>();
    protected String[] bestCandidatePair = new String[2];
    public HashMap<String, MergeBigram> mergeBigrams = new HashMap<String, MergeBigram>();
    public static LinkedHashMap<String, ArrayList<String>> mergesForPrint = new LinkedHashMap<String, ArrayList<String>>();
    public static LinkedHashMap<String, ArrayList<String>> chunksForPrint = new LinkedHashMap<String, ArrayList<String>>();
    public static ArrayList<String> dupRulesForPrint = new ArrayList<String>();
    public static LinkedHashMap<ArrayList<String>, partialParse> partialParseForest = new LinkedHashMap<ArrayList<String>, partialParse>();

    public static String inputFile = null;
    public ArrayList<String> lookaheadMerges = new ArrayList<String>();
    protected HashMap<String, Integer> chunkNgrams = new HashMap<String, Integer>();

    public static HashMap<String, nonTerminal> inputGrammarLookupTable = new HashMap<String, nonTerminal>();
    public static int myTestFreq = 0;
    protected double poissonGainOfChunk = 0d;

    protected static double previousTotalDescriptionLength = 0d;
    protected static double previousTotalStructurePrior = 0d;
    protected static double previousTotalLogLikelihood = 0d;
    protected static int previousTotalRules = 0;
    protected static int previousTotalCountofSymbolsinRHS = 0;
    protected static double previousTotalPoisson = 0d;
    protected static double previousTotalDirichletPrior = 0d;
    protected static double MDLGainDifference = 0d;
    protected static int dupRulesdifference = 0;
    protected static double testLikelihood = 0d;
    protected static String chunkInfoForPrinting = null;

    protected int changeOfNrRHSSymbols;
    public static boolean printSpansNextTime = false;
    public static boolean searchForSelectedChunk = false;
    public static ArrayList<String> manuallySelectedChunk = null;
    public static boolean annotationCompleted = false;
    public static boolean blnShowConsistentChunks = false;
    public static boolean blnShowInConsistentChunks = true;
    public static boolean blnApplyChunkAcrossTheBoard = false;
    public static boolean blnRemoveChunkAcrossTheBoard = false;
    public static boolean blnSkipBracketingCorrectionForChunk = false;
    public static int nrPopups = 0;
    public static ArrayList<String> processedChunks = new ArrayList<String>();
    public static ArrayList<String> decisionListForATBChunks = new ArrayList<String>();
    public static ArrayList<String> decisionListForSkipChunks = new ArrayList<String>();

    /** Creates a new instance of Induced_Grammar */
    public Induced_Grammar(String[][] Sentences) {
        // call constructor of superclass, this creates empty ArrayLists of
        // Terminals, HashMap of nonTerminals, and creates the first
        // nonTerminal "TOP"
        super();
        Initialize(Sentences);

        // you must initialize the value of the static variable
        // nrTerminalsInGrammar
        Grammar.nrTerminalsInGrammar = this.Terminals.size();
    }

    /*
     * creates empty grammar
     */
     public Induced_Grammar() {
        // call constructor of superclass, this creates empty ArrayLists of
        // Terminals, HashMap of nonTerminals, and creates the first
        // nonTerminal "TOP"
        super();
    }

    /* input array of randomly generated sentences (by artificial grammar)
     * creates initial grammar, in which every sentence is transformed into a
     * production S -> A B C
     * and in which nonTerminals A B C are created for every word in the
     * sentence
     */
    public void Initialize(String[][] Sentences) {
        // iterate over sentences
        ArrayList<String> rhsOfSentence;
        HashSet<ArrayList<String>> allSentences = new HashSet<ArrayList<String>>();
        nonTerminal myNonTerminal;
        int myCounter = 0;  // for spanArray

        for (String[] mySentence : Sentences) {
            rhsOfSentence = new ArrayList<String>();

            // for every sentence, turn every word into a nonTerminal, and add
            // associated rule
            for (String myWord : mySentence) {
                //search duplicateRuleContextPlusAppendix in list of Terminals
                Terminal candidateTerminal = new Terminal(myWord);

                // if word not found in list of Terminals, then turn word into
                // Terminal and add it to list
                if (this.Terminals.get(candidateTerminal) == null)
                    this.Terminals.put(myWord, candidateTerminal);

                // if it is not found in list of nonTerminals then create
                // nonTerminal and add it to HashMap of nonTerminals
                String upperCaseWord = "_" + myWord.toUpperCase();
                if (this.nonTerminals.get(upperCaseWord) == null) {
                    myNonTerminal = new nonTerminal(upperCaseWord);
                    // LEXICAL RULES: with rule creation countOfUse is
                    // automatically set to 1
                    myNonTerminal.addRule(myWord);
                    this.nonTerminals.put(upperCaseWord, myNonTerminal);
                } else {
                    // INCREASE COUNT OF LEXICAL RULES
                    this.nonTerminals.get(upperCaseWord).getRules().get(0).increaseCount();
                }
                // keep an array of nonTerminals for this sentence only
                rhsOfSentence.add(upperCaseWord);
            }
            String myWSJParse = Utils.doCreateWSJParseFromSpans(rhsOfSentence, Reader.getSpanArray().get(myCounter));
            parseTree myParseTree = Utils.extractParseFromWSJText(myWSJParse, false);

            // calculate depth of nodes
            myParseTree.calculateNodeDepth();
            myParseTree.calculateNodeSpans();
            // invent names for the nodes: *+daughter names
            myParseTree.renameNodes();
            if (Main.PRINT_DEBUG) {
                System.out.println("parse: " + myParseTree.printWSJFormat());
            }
            addParseTreeToGrammar(myParseTree);
            partialParse myPartialParse = null;

            // add parsetrees plus their freqency to the partialParseForest
            // allSentences = HashSet: check if no duplicates
            if (allSentences.add(rhsOfSentence)) {
                myPartialParse = new partialParse(myParseTree, rhsOfSentence);
            }
            else {
                  // update count of PP in forest (rulecounts are already updated)
                  myPartialParse = partialParseForest.get(rhsOfSentence);
                  myPartialParse.setCount(myPartialParse.getCount() + 1);
            }
            // put it into forest; rhsOfSentence is index; 
            // there are no dups here (because of test with HashSet allsentences
            partialParseForest.put(rhsOfSentence, myPartialParse);
            myCounter++;
        }
    }

    /*
     * enumerate the nodes of the parseTree, start with deepest level
     * add Terminals and NonTerminals to grammar, and create 8 rules for each
     * NonTerminal node add them to grammar with appropriate probabilities
     */
    public void addParseTreeToGrammar(parseTree myParseTree) {
        nonTerminal myNonTerminal;
        Terminal myTerminal;

        // start with deepest level and climb up the tree
        for (int currentDepth = myParseTree.deepestLevel; currentDepth >= 0; currentDepth--) {
            for (Node myNode : myParseTree.getNodes()) {
                // LOOP over all nodes, including terminal nodes
                if (myNode.getDepth() == currentDepth) { 
                    String myNodeName = myNode.getName();
                    // PART 1: create Terminals or nonTerminals for the grammar
                    if (myNode.getType() == Main.TERMINAL) {  //TERMINALS
                    } else {  //NONTERMINALS
                        // create an EXTERIOR NonTerminal, but first check if it already exists
                        if (!this.nonTerminals.containsKey(myNodeName)){
                            // create NonTerminal
                            myNonTerminal = new nonTerminal(myNodeName);
                            // add it to HashMap of nonTerminals of the grammar
                            this.nonTerminals.put(myNodeName, myNonTerminal);
                            }
                        else {
                            // return reference
                            myNonTerminal = this.nonTerminals.get(myNodeName);
                        }
                        // PART 2: add rules - only if current node is NONTERMINAL
                        // find the children
                        ArrayList<String> childNodes = new ArrayList<String>();
                        for (Node childNode : myNode.getChildNodes()) {
                            childNodes.add(childNode.getName());
                        }
                        // this rule may not be unique
                        Rule tempRule = new Rule(childNodes);
                        boolean bExists = false;
                        for (Rule existingRule : myNonTerminal.getRules()) {
                            if (tempRule.equals(existingRule)) {
                                // update rulecount
                                existingRule.increaseCount();
                                bExists = true;
                            }
                        }
                        if (!bExists ) myNonTerminal.addRule(childNodes);
                    }
                }
            }
        }
    }

    public void initializeMergePairs(ArrayList<nonTerminal> inputGrammar) throws IOException {
        // the aim is to determine the merge pairs that yield duplicate rules
        // within the nonT (non terminal)
        this.mergeBigrams.clear();
        boolean blnRHSOnly = false;

        // temporarily create a LookupTable out of arraylist inputGrammar: you
        // need this for call to computeDirichletGainForMerge
        inputGrammarLookupTable.clear();    // global var
        for (int m = 0; m < inputGrammar.size(); m++) {
            inputGrammarLookupTable.put(inputGrammar.get(m).getName(), inputGrammar.get(m));
        }

        HashSet<ArrayList<String>> myRHSRules = new HashSet<ArrayList<String>>();
        HashMap<String, HashMap<ArrayList<String>, Integer>> ruleCounts = new HashMap<String, HashMap<ArrayList<String>, Integer>>();
        StringBuffer ruleString = null;

         // enumerate nonT of grammar, do findCandidateMergePairs for every set
         // of rules create HashSet<ArrayList<String>> of myRHSRules and
         // myLHSRules
         for (nonTerminal myNonTerminal : inputGrammar) {
             String LHS = myNonTerminal.getName();
             // only non-Terminals that expand to more than 1 rule
             // initially that is only S
             if (myNonTerminal.getRules().size() > 1) {
                 if (Main.PRINT_DEBUG)
                     System.out.println("myNonTerminal=" + myNonTerminal.getName() + "; #rules=" + myNonTerminal.getRules().size());
                 myRHSRules.clear();
                 // add rule, including LHS to HashSet
                 for (Rule myRule: myNonTerminal.getRules()){
                     // only consider merging rules with nonTerminals in RHS:
                     // watch it: names like Jack and Joe
                     if (!(myRule.getRightHandSide().size() == 1 && this.Terminals.containsKey(myRule.getRightHandSide().get(0)))) {
                        myRHSRules.add((ArrayList<String>) myRule.getRightHandSide().clone());
                        // add LHS + # + ruleString + count to HashMap
                        HashMap<ArrayList<String>, Integer> ruleCountsForSameLHS = null;
                        // check if LHS exists
                        if (ruleCounts.get(LHS) == null) {
                            ruleCountsForSameLHS = new HashMap<ArrayList<String>, Integer>();
                            ruleCounts.put(LHS, ruleCountsForSameLHS);
                        }
                        else ruleCountsForSameLHS = ruleCounts.get(LHS);
                        ruleCountsForSameLHS.put(myRule.getRightHandSide(), myRule.getCount());
                     }
                 }
                 blnRHSOnly = true;
                 // all rules belonging to same non-terminals, so with same LHS
                 // this will create mergeBigrams within nonT with same LHS,
                 // containing all the dup rules
                 Utils.findDuplicateRules(myRHSRules, blnRHSOnly, myNonTerminal.getName(), this.mergeBigrams);
             }
        }
        System.out.println("Finished findDuplicateRules... there are " + this.mergeBigrams.size() + " mergeBigrams");
        if (Main.PRINT_DEBUG) printTest();
        int myCounter = 0;
        // compute TotalRemovedSymbols, Poisson etc from dup rules, and store their values in MergeBigram
        // before you start, update totalRuleCounts for all nonT, you will need them for Dirichlet
        for (nonTerminal myNonTerminal : inputGrammar) {
            myNonTerminal.computeTotalRuleCount();
            if (Main.INCLUDE_DIRICHLET) {
                // don't count on Stolcke computePriorsAndLikelihood
                myNonTerminal.computelogDirichlet();
            }
        }

        for (String myBigramKey : this.mergeBigrams.keySet()) {
            myCounter++;
            if (Main.timer && myCounter % 10000 == 0)
                System.out.println("In MB loop in initialize: myCounter=" + myCounter);
            if (Main.PRINT_DEBUG) System.out.println(myBigramKey);
            // computes duplicate rules from redundant set of duplicate rules,
            // then computes totalRemovedSymbols on basis of duplicate rules
            this.mergeBigrams.get(myBigramKey).computeTotalRemovedSymbolsOfMerge(myBigramKey);
            if (Main.INCLUDE_DIRICHLET)
                this.mergeBigrams.get(myBigramKey).computeDirichletGainForMerge(myBigramKey, ruleCounts);
        }
        if (Main.timer)
            System.out.println("Finished initializeMergePairs MB loop.");
    }

    /**
     * as long as merge with look-ahead improves MDL, do findBestMergeinGrammar
     * merge with look-ahead: while (DL_test>=DL_original) do findbestmerge
     * with outputgrammar, max 3 iterations (look-ahead). if succesful, adjust
     * real grammar stop when no improvement is made
     */
    public void doIteratedMerging() throws IOException {
        // create testgrammar (for Lookahead) and computePriorsAndLikelihood for all NonTerminals
        ArrayList<nonTerminal> testGrammar = new ArrayList<nonTerminal>();
        for(String key : this.nonTerminals.keySet()) {
            testGrammar.add(this.nonTerminals.get(key));
        }

        int lookAhead;
        boolean blMergingWithLookaheadSuccesful = true;
        HashSet<String> currentRoundMerges = new HashSet<String>();
        int mergeLoop = 0;

        if (Main.UPDATE_MERGE_BIGRAMS) initializeMergePairs(testGrammar);

        // main loop (continues until merging (with lookahead) doesn't yield negative MDLGain
        while (blMergingWithLookaheadSuccesful) {
            mergeLoop++;
            // create initial mergeBigrams from scratch
            if (Main.PRINT_DEBUG)
                System.out.println("Starting initializeMergePairs...");

            if (!Main.UPDATE_MERGE_BIGRAMS) initializeMergePairs(testGrammar);
            if (Main.PRINT_DEBUG && mergeLoop > 30)
                Printer.printoutTerminalsNonTerminalsAndRules(this, "induced");
            double TotalMDLGainOverManyLookaheads = 0d;

            lookaheadMerges.clear();
            lookAhead = 1;
            int remember_maxNrNonTerminal = maxNrNonTerminal;

            if (Main.PRINT_DEBUG)
                System.out.println("Continue merging... mergeLoop=" + mergeLoop);
            if (this.mergeBigrams.size() > 0) {
                // NOTE changes to testGrammar and to mergeBigrams are applied
                // in findBestMerge after every merge, whether MDLGain < 0 or no
                double maximumDLGain = findBestMerge(testGrammar, mergeLoop, currentRoundMerges);
                TotalMDLGainOverManyLookaheads += maximumDLGain;

                if (Main.PRINT_TARGET_GRAMMAR)
                    printToScreenGrammarTargetFormat (testGrammar);

                // merge with look-ahead: only necessary if MDLGain >= 0
                // TotalMDLGainOverManyLookahead is updated in findBestMerge
                while (TotalMDLGainOverManyLookaheads >= 0 && lookAhead < Main.MAXLOOKAHEAD ) {
                    System.out.println("LookAhead for merge: " + lookAhead);
                    lookAhead++;
                    if (!Main.UPDATE_MERGE_BIGRAMS)
                        initializeMergePairs(testGrammar);
                    if (this.mergeBigrams.size() > 0) {
                        maximumDLGain = findBestMerge(testGrammar, mergeLoop, currentRoundMerges);
                        TotalMDLGainOverManyLookaheads += maximumDLGain;
                        if (Main.PRINT_TARGET_GRAMMAR)
                            printToScreenGrammarTargetFormat (testGrammar);
                    }
                }

                if ((maximumDLGain < Double.POSITIVE_INFINITY && TotalMDLGainOverManyLookaheads < 0) || (Main.NON_LEX_NON_TERMINAL_MAX > 0 && tooManyNonTerminals(testGrammar))) {
                    // apply changes to real grammar, so you remember last good
                    // grammar
                    this.nonTerminals.clear();
                    for (nonTerminal myNonTerminal : testGrammar) {
                        this.nonTerminals.put(myNonTerminal.getName(), myNonTerminal);
                    }

                    // PREPARE PRINT (if lookaheadMerges not empty)
                    for (String mergeInfo : lookaheadMerges) {
                        if (Main.PRINT_DEBUG)
                            System.out.println("going to do doPartialParsesUpdateAfterMerge");
                        Utils.storeMergeInfo(mergesForPrint, chunksForPrint, mergeInfo.split("@")[0], mergeInfo.split("@")[1], mergeInfo.split("@")[2], mergeInfo.split("@")[3]);
                        doPartialParsesUpdateAfterMerge(mergeInfo.split("@")[0], mergeInfo.split("@")[1], mergeInfo.split("@")[2]);
                    }
                } else {
                    // else: MDLGain > 0, so Merging With Lookahead was not
                    // succesful: you must exit merging loop without applying
                    // changes to grammar and go to chunking
                    blMergingWithLookaheadSuccesful = false;
                    maxNrNonTerminal = remember_maxNrNonTerminal;
                    System.out.println("exiting iterated merging. Last merge was cancelled..."); 
                }
            } else {
                blMergingWithLookaheadSuccesful = false;
                System.out.println("No more merge bigrams to enumerate. exiting iterated merging...");
                if (Main.NON_LEX_NON_TERMINAL_MAX > 0 && tooManyNonTerminals(testGrammar)) {
                    System.out.println("No more merge bigrams to enumerate. reinitializing merge pairs...");
                    // TODO this initialization should be leninet with the merge
                    // pairs it allows
                    initializeMergePairs(testGrammar);
                    // only unset if we found pairs
                    if (this.mergeBigrams.size() > 0)
                        blMergingWithLookaheadSuccesful = true;
                }
            }
        }
    }

    /*
     * given a set of mergebigrams with Lj's and head sets, find the mergebigram with the biggest DeltaMDL
     * NO: remember bestCandidatePair[0] and [1]: you have to apply it later
     * N.B. even if DL does not decrease, you may do another findBestMerge based on this grammar
     */
    public double findBestMerge(ArrayList<nonTerminal> testGrammar, int nrLoop, HashSet<String> currentRoundMerges) {
        double maxDLGain = Double.POSITIVE_INFINITY;

        if (Main.PRINT_DEBUG)
            System.out.println("Starting findBestMerge...");

        NumberFormat numberFormatter = NumberFormat.getNumberInstance();
        nonTerminal bestCandidate1 = null, bestCandidate2 = null;
        String bestBigramKey = null;

        // you want the most negative MDLGain
        // temporarily create a HashMap out of arraylist testGrammar
        inputGrammarLookupTable.clear();    // global var
        for (int m = 0; m < testGrammar.size(); m++) {
            inputGrammarLookupTable.put(testGrammar.get(m).getName(), testGrammar.get(m));
        }

        // precompute a list of all occurrences in Grammar of nonTerminals
        HashMap<String, Integer> frequencyOfNonTerminalsInRHS = new HashMap<String, Integer>();
        HashMap<String, Integer> nrOfRulesOfNonT = new HashMap<String, Integer>();
        HashMap<String, Integer> totalRuleCountForLHSNonTerminal = new HashMap<String, Integer>();
        HashMap<String, HashMap<ArrayList<String>, Integer>> ruleCounts = new HashMap<String, HashMap<ArrayList<String>, Integer>>();

        // fill frequencyOfNonTerminalsInRHS with zeros for every nonTerminal
        for (int k = 0; k < testGrammar.size(); k++) {
            frequencyOfNonTerminalsInRHS.put(testGrammar.get(k).getName(), new Integer(0));
            totalRuleCountForLHSNonTerminal.put(testGrammar.get(k).getName(), new Integer(0));
        }

        int F_tot = 0;
        for (int k = 0; k < testGrammar.size(); k++) {
            F_tot = 0;
            for (Rule myRule : testGrammar.get(k).getRules()) {
                F_tot += myRule.getCount();
            }
            totalRuleCountForLHSNonTerminal.put(testGrammar.get(k).getName(), F_tot);
        }

        int sumFRofTOP = 0;
        StringBuffer ruleString = null;
        ruleCounts.clear();
        for (int k = 0; k < testGrammar.size(); k++) {
            nrOfRulesOfNonT.put(testGrammar.get(k).getName(), new Integer(testGrammar.get(k).getRules().size()));
            if (testGrammar.get(k).getName().equals("TOP")) {
                for (Rule myRule : testGrammar.get(k).getRules()) {
                    sumFRofTOP += myRule.getCount();
                }
            }

            // enumerate rules
            HashMap<ArrayList<String>, Integer> ruleCountsForSameLHS = null;
            ruleCountsForSameLHS = new HashMap<ArrayList<String>, Integer>();
            ruleCounts.put(testGrammar.get(k).getName(), ruleCountsForSameLHS);

            for (Rule myRule : testGrammar.get(k).getRules()) {
                // enumerate nonTerminals in RHS
                for (String myConstituent : myRule.getRightHandSide()) {
                    // check whether it exists in testGrammarLookupTable: then it is nonTerminal
                    if (inputGrammarLookupTable.get(myConstituent) != null) {
                        // add nonT k to the HashSet belonging to constituent in rule body
                        frequencyOfNonTerminalsInRHS.put(myConstituent, new Integer(frequencyOfNonTerminalsInRHS.get(myConstituent).intValue() + myRule.getCount()));
                    }
                }
                ruleCountsForSameLHS.put(myRule.getRightHandSide(), myRule.getCount());
            }
        }

        // firstTermOfMDLGain is constant for all mergeBigrams
        double firstTermOfMDLGain = computeFirstTermOfStructurePriorGain(testGrammar, true);

        // enumerate mergeBigrams, these are all candidate merge pairs that
        // yield duplicate rules
        double MDLGain, myLikelihood3 = 0d, myLikelihood4 = 0d;
        double bestLikelihood3 = 0d, bestLikelihood4 = 0d, myLikelihood = 0d;
        double myStructurePrior = 0d, bestStructurePrior = 0d;
        nonTerminal mergeA, mergeB;

        // compute some values before entering loop
        // e-grids p9: A_{unt} + 1., but A_{unt} is excluding TOP (and excluding
        // STOP, but so is grammar.size())
        // Poisson: log(#nonT) instead of log(#nonT+1) because no STOP to encode
        // based on grammar size AFTER merge, so everything -1 including STOP,
        // but not including TOP, because unique nonT of RHS alone
        // Aunt =def A_{unt}
        double logAunt = Math.log((double) testGrammar.size() - 1) / Math.log(2.);
        // not including STOP and not including TOP
        double logAuntForPoisson = Math.log((double) testGrammar.size() - 2) / Math.log(2.);

        // LOOP OVER MERGEBIGRAMS
        int myCounter = 0;
        boolean blnHasBeenInLoop = false;
        for (String myBigramKey : this.mergeBigrams.keySet()) {
            if (this.mergeBigrams.get(myBigramKey).getTotalSymbolsInLHSOfOmega1() + this.mergeBigrams.get(myBigramKey).getTotalSymbolsInRHSOfOmega1() + this.mergeBigrams.get(myBigramKey).getTotalSymbolsInLHSOfOmega3() + this.mergeBigrams.get(myBigramKey).getTotalSymbolsInRHSOfOmega3() >= Main.MININUM_REMOVED_SYMBOLS) {
                 // if INITIALIZE_FROM_TARGET_GRAMMAR then prevent merging of two preterminals
                 if ((!this.Terminals.containsKey(myBigramKey.split("@")[0].toLowerCase()) && !this.Terminals.containsKey(myBigramKey.split("@")[1].toLowerCase()))) {
                    myCounter++;
                    if (Main.timer && myCounter % 10000 == 0)
                        System.out.println("in FindBestMerge: Loop over MBs=" + myCounter);
                    // if INDUCE_FROM_POSTAGS: allow merge only if both nonT
                    // from merge pair start with either CHNK~ or MRG~ if
                    // INDUCE_FROM_POSTAGS no preterminals are allowed to merge
                    if (!Main.INDUCE_FROM_POSTAGS || !(this.Terminals.containsKey(myBigramKey.split("@")[0].substring(1).toLowerCase()) || this.Terminals.containsKey(myBigramKey.split("@")[1].substring(1).toLowerCase()))) {
                        blnHasBeenInLoop = true;
                        mergeA = inputGrammarLookupTable.get(myBigramKey.split("@")[0]);
                        mergeB = inputGrammarLookupTable.get(myBigramKey.split("@")[1]);
                        if (mergeA == null || mergeB == null) System.out.println("Merge is not a nonTerminal! myBigramKey=" + myBigramKey + "; mergeB=" + myBigramKey.split("@")[1]);
                        // NOTE: this assumes totalRemovedSymbols, totalPoisson
                        // and totalDirichlet are known! their values are only
                        // updated when necessary: after application of merge
                        // or chunk
                        myStructurePrior = this.mergeBigrams.get(myBigramKey).computeSecondTermOfStructurePriorGainOfMergeBigram(logAunt, logAuntForPoisson, Main.PRINT_DEBUG);
                        // 3rd and 4th term: uses occurrences of nonT in bodies
                        // of all rules
                        myLikelihood3 = this.mergeBigrams.get(myBigramKey).computeThirdTermofLikelihoodGainOfMergeBigram(mergeA, mergeB) ;
                        if (Main.PRINT_DEBUG)
                            System.out.println("3rd term myLikelihood=" + myLikelihood);
                        myLikelihood4 = this.mergeBigrams.get(myBigramKey).computeFourthTermofLikelihoodGainOfMergeBigram(frequencyOfNonTerminalsInRHS, nrOfRulesOfNonT, totalRuleCountForLHSNonTerminal, sumFRofTOP, ruleCounts, testGrammar, myBigramKey, Main.PRINT_DEBUG);
                        MDLGain = firstTermOfMDLGain + myStructurePrior + myLikelihood3 + myLikelihood4;
                        // compare with best so far; if they are equal then prefer the alphabetically smaller bigramkey
                        if (MDLGain < maxDLGain || ((MDLGain == maxDLGain) && (myBigramKey.compareTo(bestBigramKey)<0))) {
                            maxDLGain = MDLGain;
                            bestCandidate1= mergeA;
                            bestCandidate2= mergeB;
                            bestBigramKey = myBigramKey;
                            bestStructurePrior = myStructurePrior;
                            bestLikelihood3 = myLikelihood3;
                            bestLikelihood4 = myLikelihood4;
                        }
                    }
                }
            }
        }

        // APPLY CHANGES, RENAME
        // ? only if a mergeBigram with MDL<0 found IS FOUND!
        if (bestBigramKey != null) {
            String newName = createNameForMerge(bestCandidate1, bestCandidate2);
            currentRoundMerges.add(newName);
            // remember for later printing
            int totalDupRules = this.mergeBigrams.get(bestBigramKey).getNrOfDuplicateRules();
            int totalRemovedSymbols = this.mergeBigrams.get(bestBigramKey).getTotalSymbolsInLHSOfOmega1() + this.mergeBigrams.get(bestBigramKey).getTotalSymbolsInRHSOfOmega1() + this.mergeBigrams.get(bestBigramKey).getTotalSymbolsInLHSOfOmega3() + this.mergeBigrams.get(bestBigramKey).getTotalSymbolsInRHSOfOmega3();

            // PRINT egrids variables
            System.out.println("Merge #" + (maxNrNonTerminal-1) + ": (" + bestCandidate1.getName() + ", " + bestCandidate2.getName() + ")");
            System.out.println("#duprules=" + this.mergeBigrams.get(bestBigramKey).getNrOfDuplicateRules() + ";  #non-T=" + testGrammar.size() + "; #MB=" + this.mergeBigrams.size() + "; maxDLGain " + numberFormatter.format(maxDLGain));
            System.out.println("(SPGain=" + numberFormatter.format(bestStructurePrior + firstTermOfMDLGain)  + "; SP1=" + numberFormatter.format(firstTermOfMDLGain) + "; SP2=" + numberFormatter.format(bestStructurePrior) + "; LikhGain=" + numberFormatter.format(bestLikelihood3+bestLikelihood4) + "; ToReSy= " + totalRemovedSymbols + ")");

            if (Main.PRINT_DUPLICATE_RULES_TO_FILE) printDuplicateRules(bestBigramKey, newName);
            if (Main.PRINT_DUPLICATE_RULES_TO_SCREEN) {
                System.out.println(this.mergeBigrams.get(bestBigramKey).getNrOfDuplicateRules() + " dupli rules for Mrg(" + bestBigramKey + ") -->" + newName);
                HashSet<String> setOfDupRules = null;
                for (String myLHS : this.mergeBigrams.get(bestBigramKey).getSetOfSetsOfDuplicatesWithSameLHS().keySet()){
                    // private HashMap<String, ArrayList<HashSet<String>>> setOfSetsOfDuplicatesWithSameLHS;
                    // for each LHS there are one or several HashSets (in ArrayList) of dup rules
                    // key is HashMap<String (substitutedRule), HashSet<String>>
                    for (String key : this.mergeBigrams.get(bestBigramKey).getSetOfSetsOfDuplicatesWithSameLHS().get(myLHS).keySet()) {
                        int m = 0;
                        System.out.println("***************** SET OF DUPRULES **************");
                        for(ArrayList<String> ruleStringIndexed : this.mergeBigrams.get(bestBigramKey).getSetOfSetsOfDuplicatesWithSameLHS().get(myLHS).get(key)) {
                        m++;
                        System.out.println(m + ") " + ruleStringIndexed);
                        }
                    }
                }
            }

            // apply merge substitutions of best merge pair to testgrammar, so you can continue to find duplicates with lookahead
            // substitutes MrgAB for A and B in all the rules of grammar
            // delete original MergeBigram
            this.mergeBigrams.remove(bestCandidate1.getName() + "@" + bestCandidate2.getName());
            this.mergeBigrams.remove(bestCandidate2.getName() + "@" + bestCandidate1.getName());
            // substitutes MrgAB for A and B in bigramKeys: removes all
            // mergeBigrams that contain A or B in key, and copies them to new
            // MB, adds contents of MB(A,X) to MB(B,X) after Mrg(A,B)
            // substitues MrgAB for A and B in all the dup rules
            // renames OLD MB, the new ones always get only new names, because
            // only substituted rules are send to findDuplicateRules
            if (Main.UPDATE_MERGE_BIGRAMS) renameMergeBigrams(bestCandidate1.getName(), bestCandidate2.getName(), newName, ruleCounts);

            // apply merge substitutions of best merge pair to testgrammar, so
            // you can continue to find duplicates with lookahead
            // substitutes MrgAB for A and B in all the rules of grammar
            HashMap<String, MergeBigram> newMergeBigramsAfterMerge = new HashMap<String, MergeBigram>();

            // collects all rules that had substitution and sends them to
            // findCandidate and FindCandidateStep2
            // it sends rules with substituted names of nonT, so should produce
            // correct BigramKey
            // a temp set of updatesForMergeBigrams is created
            ArrayList<nonTerminal> newGrammar = updateTestGrammarAfterMerge(bestCandidate1, bestCandidate2, newName, testGrammar, newMergeBigramsAfterMerge);
            testGrammar.clear();
            testGrammar.addAll(newGrammar);

            // enumerates updatesForMergeBigrams and adds to this.mergeBigrams
            // with corresponding bigramKey
            // if dup rules were added to original MB, then again
            // computeTotalRemovedSymbolsOfMerge and Poisson
            if (Main.UPDATE_MERGE_BIGRAMS) addNewMergeBigramsAfterMerge(newMergeBigramsAfterMerge, testGrammar, ruleCounts);

            String extraInfo = "  #" + (maxNrNonTerminal-1) + "  (" + totalDupRules + "  " + ((int) Math.floor(maxDLGain)) + "  " + ((int) Math.floor(bestStructurePrior + firstTermOfMDLGain)) + "  " + ((int) Math.floor(bestLikelihood3 + bestLikelihood4)) + ")";
            if (Main.DO_STOLCKE_CONTROL && (MDLGainDifference > 0.001 || MDLGainDifference < -0.001)) {
               extraInfo = extraInfo + " Error! " + dupRulesdifference + "  " + numberFormatter.format(MDLGainDifference);
            }
            extraInfo=extraInfo.replace(',', '.');

            // don't store anything for real in any case; temporarily store it
            lookaheadMerges.add(newName + "@" + bestCandidate1.getName() + "@" + bestCandidate2.getName() + "@" + extraInfo);
            // only for comparison with Stolcke, compute differences in
            // structure prior and likelihood compared to before merge
            // PRINT Stolcke variables
            if (Main.DO_STOLCKE_CONTROL)
                computeTotalPriorsAndLikelihood(testGrammar, true, maxDLGain, totalDupRules, totalRemovedSymbols);
            return maxDLGain;
        } else {
            System.out.println("blnHasBeenInLoop=" + blnHasBeenInLoop + "; No merges found with MDLGain<0. this.mergeBigrams.size()=" + this.mergeBigrams.size());
        }
        return maxDLGain;
    }

    public double computeFirstTermOfStructurePriorGain(ArrayList<nonTerminal> inputGrammar, boolean blnForMerge) {
        // compute A_R from formula 6 e-grids: A_R = total rules from SB1 +
        // SB3 (=total rules - #Terminals)
        // compute A_NT from formula 6 e-grids: A_NT =#occurrences of all
        // nonT in entire grammar, including LHS
        int A_tot = 0, A_NT = 0, A_R = 0, A_S = 0, A_RHS_of_SB3 = 0;
        int A_RHS_of_SB1 = 0;
        double logAunt = 0d, logAuntPoisson = 0d, firstTerm = 0d;
        int countOfNonTerminalsinRHS = 0;

        for (int k = 0; k < inputGrammar.size(); k++) {
            countOfNonTerminalsinRHS = inputGrammar.get(k).computenrOfNonTerminalsInRHS(this);
            A_tot += inputGrammar.get(k).getRules().size();
            // A_S = nr rules in the start symbol subset
            if (inputGrammar.get(k).getName().equals("TOP")) {
                A_S = inputGrammar.get(k).getRules().size();
                A_RHS_of_SB1 += countOfNonTerminalsinRHS;
            } else { 
                // A_{RHS of SB3} (SB2 has non nonT in RHS, so its RHS is not
                // included)
                A_RHS_of_SB3 += countOfNonTerminalsinRHS;
            }
            A_NT += countOfNonTerminalsinRHS;  // still without LHS
        }
        if (Main.PRINT_DEBUG) System.out.println("so far: A_NT=" + A_NT);
        // A_R = total rules from SB1 + SB3
        A_R = A_tot - nrTerminalsInGrammar;
        // add LHS to A_NT
        A_NT += A_tot;

        // compute log(A_{unt}/(A_{unt}+1)
        // Merge: log(A_{unt}/(A_{unt}+1)); chunk log((A_{unt}+2)/(A_{unt}+1))
        // Poisson: one less for all of them!!!
        if (blnForMerge) {
            // log(A_{unt}/(A_{unt} +1))
            logAunt = Math.log(((double) inputGrammar.size() -1.)/((double) inputGrammar.size()))/Math.log(2.);
            // log((A_{unt}-1)/(A_{unt}))
            logAuntPoisson = Math.log(((double) inputGrammar.size() -2.)/((double) inputGrammar.size()-1.))/Math.log(2.);
        } else {
            // chunks
            // log((A_{unt}+2)/(A_{unt}+1))
            logAunt = Math.log(((double) inputGrammar.size() +1.)/((double) inputGrammar.size()))/Math.log(2.);
            // log((A_{unt}+1)/(A_{unt}))
            logAuntPoisson = Math.log(((double) inputGrammar.size() )/((double) inputGrammar.size()-1.))/Math.log(2.);
        }

        if (!Main.INCLUDE_POISSON) {
            firstTerm = ((double) A_NT + A_R - A_S + 2)*logAunt;
            if (Main.PRINT_DEBUG)
                System.out.println("Egrids firstTerm of merge=" + firstTerm + "; A_NT=" + A_NT + "; A_tot=" + A_tot + "; A_RHS_of_SB1=" + A_RHS_of_SB1 + "; A_RHS_of_SB3=" + A_RHS_of_SB3 + "; A_R=" + A_R + "; A_S=" + A_S + "; A_NT - A_S + 2 -T=" + (A_NT +  A_R - A_S + 2 - Grammar.nrTerminalsInGrammar) + "; first log=" + logAunt );
        }
        else {
            // you have to take SB2 (LHS of terminal set) out and calculate them
            // apart, also leave out A_R, which represents #Stop symbols
            firstTerm = ((double) A_NT - A_S + 2 - Grammar.nrTerminalsInGrammar) * logAuntPoisson;
            firstTerm += ((double) Grammar.nrTerminalsInGrammar) * logAunt;
        }
        if (Main.PRINT_DEBUG) {
            System.out.println("Egrids firstTerm of merge=" + firstTerm + "; A_NT=" + A_NT + "; A_tot=" + A_tot + "; A_RHS_of_SB1=" + A_RHS_of_SB1 + "; A_RHS_of_SB3=" + A_RHS_of_SB3 + "; A_R=" + A_R + "; A_S=" + A_S + "; A_NT - A_S + 2 -T=" + (A_NT +  A_R - A_S + 2 - Grammar.nrTerminalsInGrammar) + "; first log=" + logAunt );
            System.out.println("size=" + (inputGrammar.size() +1) + "; firstterm=" + firstTerm);
            System.out.println("inside firstTerm=" + firstTerm);
        }
        return firstTerm;
    }

    public String createNameForMerge(nonTerminal Constituent1, nonTerminal Constituent2) {
        // create a new nonTerminal for the merge, and invent a name for merged category
        // String newName = "MRG_" + maxNrNonTerminal++;

        // take the name of the most frequent of the two
        int freqOfConstituent1 = 0, freqOfConstituent2 = 0;
        String newName;

        if (Constituent1.getName().equals("TOP") || Constituent2.getName().equals("TOP")) {
            newName = "TOP";
            maxNrNonTerminal++;
        }
        else {
            for (Rule myRule : Constituent1.getRules()) {
                freqOfConstituent1 += myRule.getCount();
            }
            for (Rule myRule : Constituent2.getRules()) {
                freqOfConstituent2 += myRule.getCount();
            }

            newName = ((freqOfConstituent1 > freqOfConstituent2) ? Constituent1.getName() : Constituent2.getName()) + "~";

            // if there is MRG in name then it looks like MRG_xxx_number, 
            // so keep only middle part xxx
            // TODO handle case with ~ in words
            if (newName.contains("MRG~") || newName.contains("CHNK~")) {
                StringBuffer newNameBuffer;
                // take off all MRG~ and CHNK~ prefixes, and take off last number
                String[] splitName = newName.split("~");
                newNameBuffer = new StringBuffer();
                // -1 to avoid last number
                for (int m = 0; m < splitName.length - 1; m++) {
                    if (!splitName[m].equals("MRG") && !splitName[m].equals("CHNK")) {
                        newNameBuffer.append(splitName[m]).append("~"); 
                    }
                }
                newName = newNameBuffer.toString();
            }
            newName = "MRG~" + newName + maxNrNonTerminal++;
        }
        return newName;
    }

    /*
     * creates new nonT for merge, finds all nonT where substitution took place and clones them
     * this must be done so that you can find additional mergeBigrams after application of merge in lookahead case
     */
    public ArrayList<nonTerminal> updateTestGrammarAfterMerge(nonTerminal Constituent1, nonTerminal Constituent2, String newName, ArrayList<nonTerminal> inputGrammar, HashMap<String, MergeBigram> newMergeBigramsAfterMerge) {
        // only called from find best merge
        if (Main.PRINT_DEBUG)
            System.out.println("In updateTestGrammarAfterMerge with Constituent1=" + Constituent1.getName() + "; Constituent2=" + Constituent2.getName() + "; newName=" + newName);
        ArrayList<nonTerminal> outputGrammar = new ArrayList<nonTerminal>();
        Rule tempRule;

        // rulesWithSubstitution is list of rules where substitution of nonT
        // took place: save these if you want to check for additional merges
        HashSet<ArrayList<String>> rulesWithSubstitution  = new  HashSet<ArrayList<String>>();

        StringBuffer ruleString = null;

        // CHANGES FOR  NEW MERGED NONTERMINAL
        // this is for LHS merges: group substituted rules by ruleSize and then
        // by LHS
        HashMap<Integer, HashMap<String, HashSet<ArrayList<String>>>> substitutedRulesGroupedBySizeAndLHS = new HashMap<Integer, HashMap<String, HashSet<ArrayList<String>>>>();
        for (int i = 1; i<11 ; i++) {
            substitutedRulesGroupedBySizeAndLHS.put(i, new HashMap<String, HashSet<ArrayList<String>>>());
        }

        // create a new nonTerminal w/o any rules, and add to list of grammar
        nonTerminal mergedNonTerminal = new nonTerminal(newName);

        // add the rules of the Constituent1 as well as Constituent2 to mergedNonTerminal
        for (Rule myRule : Constituent1.getRules()) {
            tempRule = (Rule) myRule.clone();
            // do substitution
            tempRule.substituteConstituent(Constituent1.getName(), newName);
            tempRule.substituteConstituent(Constituent2.getName(), newName );
            // and add rules to new nonTerminal
            mergedNonTerminal.getRules().add(tempRule);
            // put substituted rules in HashSet rulesWithSubstitution for checking for new mergeBigrams
            if (Main.UPDATE_MERGE_BIGRAMS) {
                // no preterminal rules XXX not quite right
                if (myRule.getRightHandSide().size() >1 || myRule.getRightHandSide().get(0).startsWith("MRG~"))
                   rulesWithSubstitution.add((ArrayList<String>) tempRule.getRightHandSide().clone());
                if (Main.DO_LHS_MERGES) {
                    ArrayList<String> myRHS = tempRule.getRightHandSide();
                    HashSet<ArrayList<String>> substitutedRulesWithSameLHS = null;
                    if (Main.PRINT_DEBUG)
                        System.out.println("myRHS.size()=" + myRHS.size() + "; myLHS=" + newName); 
                    if (substitutedRulesGroupedBySizeAndLHS.get(myRHS.size()).get(newName) == null) {
                        substitutedRulesWithSameLHS = new HashSet<ArrayList<String>>();
                        substitutedRulesGroupedBySizeAndLHS.get(myRHS.size()).put(newName, substitutedRulesWithSameLHS);
                    } else
                        substitutedRulesWithSameLHS = substitutedRulesGroupedBySizeAndLHS.get(myRHS.size()).get(newName);
                    substitutedRulesWithSameLHS.add(tempRule.getRightHandSide());
                }
            }
        }
        if (Main.PRINT_DEBUG)
            System.out.println("Rules of Constituent2 " + Constituent2.getName() + ":");
        for (Rule myRule : Constituent2.getRules()) {
           tempRule = (Rule) myRule.clone();
           // do substitution
           tempRule.substituteConstituent(Constituent1.getName(), newName );
           tempRule.substituteConstituent(Constituent2.getName(), newName );
           // and add rules to new nonTerminal
           mergedNonTerminal.getRules().add(tempRule);
           // put substituted rules in HashSet rulesWithSubstitution for
           // checking for new mergeBigrams
           if (Main.UPDATE_MERGE_BIGRAMS) {
               // no preterminal rules  XXX klopt niet helemaal
               if (myRule.getRightHandSide().size() > 1 || myRule.getRightHandSide().get(0).startsWith("CHNK~") || myRule.getRightHandSide().get(0).startsWith("MRG~"))
                    // LHS = newName
                    rulesWithSubstitution.add((ArrayList<String>) tempRule.getRightHandSide().clone());
                if (Main.DO_LHS_MERGES) {
                    ArrayList<String> myRHS = tempRule.getRightHandSide();
                    HashSet<ArrayList<String>> substitutedRulesWithSameLHS = null;
                    if (Main.PRINT_DEBUG)
                        System.out.println("myRHS.size()=" + myRHS.size() + "; myLHS=" + newName); 
                    if (substitutedRulesGroupedBySizeAndLHS.get(myRHS.size()).get(newName) == null) {
                        substitutedRulesWithSameLHS = new HashSet<ArrayList<String>>();
                        substitutedRulesGroupedBySizeAndLHS.get(myRHS.size()).put(newName, substitutedRulesWithSameLHS);
                    } else
                        substitutedRulesWithSameLHS = substitutedRulesGroupedBySizeAndLHS.get(myRHS.size()).get(newName);
                    substitutedRulesWithSameLHS.add(tempRule.getRightHandSide());    
                }
            }
        }
        // find dup rules for RHS merges
        if (Main.UPDATE_MERGE_BIGRAMS && rulesWithSubstitution.size() > 0)
            Utils.findDuplicateRules(rulesWithSubstitution, true, newName, newMergeBigramsAfterMerge);
        boolean blnRuleRemoved = mergedNonTerminal.removeDuplicateRules();
        outputGrammar.add(mergedNonTerminal);
        // update values for changed nonT
        if (Main.DO_STOLCKE_CONTROL) mergedNonTerminal.computePriorsAndLikelihoodForNonTerminal(this);
        // CHANGES TO OTHER NONTERMINALS
        // enumerate nonTerminals for substitution
        for (nonTerminal myNonTerminal : inputGrammar) {
            // per non-terminal compare substituted rules
            rulesWithSubstitution.clear();
            // skip nonTerminals with LHS equal to Constituent1 and 2 (because
            // they are replaced by the merged one)
            if (!myNonTerminal.equals(Constituent1) && !myNonTerminal.equals(Constituent2)) {
                ArrayList<Rule> myRules = myNonTerminal.getRules();
                // check if myNonTerminal contains Constituent1 or Constituent2
                // in RHS of rules, in that case, you'll have to substitute,
                // so clone it, and add clone to outputGrammar
                boolean blCreateNewNonTerminal = false;
                for(Rule myRule : myRules) {
                     if (myRule.containsConstituent(Constituent1.getName()) || myRule.containsConstituent(Constituent2.getName())) blCreateNewNonTerminal = true;
                }

                if (blCreateNewNonTerminal) {
                    // now you must replace nonTerminal and rules in grammar
                    if (Main.PRINT_DEBUG)
                        System.out.println("Constituent1 or 2 found in RHS of nonTerminal " + myNonTerminal.getName());
                    // create a dummy nonTerminal, in which you can substitute
                    nonTerminal newNonTerminal = new nonTerminal(myNonTerminal.getName());
                    // enumerate rulesand clone them
                    for(Rule myRule : myRules) {
                        tempRule = (Rule) myRule.clone();
                        // do substitution
                        if (tempRule.containsConstituent(Constituent1.getName()) || tempRule.containsConstituent(Constituent2.getName())) {
                            tempRule.substituteConstituent(Constituent1.getName(), newName ); 
                            tempRule.substituteConstituent(Constituent2.getName(), newName ); 
                            // put substituted rules in HashSet rulesWithSubstitution for checking for new mergeBigrams
                            // you want it without the LHS: ruleString.append(myLHS).append("#");
                            if (Main.UPDATE_MERGE_BIGRAMS) {
                                if (Main.PRINT_DEBUG) {
                                    System.out.println("Constituent1=" + Constituent1.getName() + "; Constituent2=" + Constituent2.getName() + "; newName=" + newName);
                                    System.out.println(myNonTerminal.getName() + "-->" + tempRule.getRightHandSide());
                                }
                                rulesWithSubstitution.add((ArrayList<String>) tempRule.getRightHandSide().clone());
                                if (Main.DO_LHS_MERGES) {
                                    String myLHS = myNonTerminal.getName();
                                    ArrayList<String> myRHS = tempRule.getRightHandSide();

                                    HashSet<ArrayList<String>> substitutedRulesWithSameLHS = null;
                                    if (Main.PRINT_DEBUG)
                                        System.out.println("myRHS.size()=" + myRHS.size() + "; myLHS=" + myLHS); 
                                    if (substitutedRulesGroupedBySizeAndLHS.get(myRHS.size()).get(myLHS)==null) {
                                        substitutedRulesWithSameLHS = new HashSet<ArrayList<String>>();
                                        substitutedRulesGroupedBySizeAndLHS.get(myRHS.size()).put(myLHS, substitutedRulesWithSameLHS);
                                    } else
                                        substitutedRulesWithSameLHS = substitutedRulesGroupedBySizeAndLHS.get(myRHS.size()).get(myLHS);
                                    substitutedRulesWithSameLHS.add(tempRule.getRightHandSide());
                               }
                           }
                        }
                        // and add rule to dummy nonTerminal
                        newNonTerminal.getRules().add(tempRule);
                    }
                    // determine if substitution equalizes any of the rules
                    // within the terminals
                    // remove duplicate rules (automatically sums counts of
                    // duplicate rules)
                    blnRuleRemoved = newNonTerminal.removeDuplicateRules();
                    // check if there are additional merges that yield duplicate rules 
                    // this method will update updatesForMergeBigrams A,B that
                    // yield dup rules within mergedNonTerminal
                    if (Main.UPDATE_MERGE_BIGRAMS && rulesWithSubstitution.size()>0) Utils.findDuplicateRules(rulesWithSubstitution, true, myNonTerminal.getName(), newMergeBigramsAfterMerge );
                    // the values of the mergeBigrams (dupRules, removedSymbols,
                    // etc) are updated only after newMergeBigramsAfterMerge are
                    // added to old MergeBigrams

                    // only if nonTerminal has changed you must recalculate
                    // logLikelihood and Dirichlet

                    // update values for changed nonT
                    if (Main.DO_STOLCKE_CONTROL)
                        newNonTerminal.computePriorsAndLikelihoodForNonTerminal(this);

                    outputGrammar.add(newNonTerminal);
                } else outputGrammar.add(myNonTerminal);
            }
        }
        // for LHS merges, combine every single one of the substituted rules
        // separately with all rules of the grammar of the same size
        if (Main.UPDATE_MERGE_BIGRAMS && Main.DO_LHS_MERGES) findLHSMerges(substitutedRulesGroupedBySizeAndLHS, newMergeBigramsAfterMerge);
        return outputGrammar;
    }

    /*
     * combine every ruleWithSubstitution separately with every rule in the
     * grammar (except for same LHS) of the same size that is exactly the same
     * except for the positions where LHS occurs or another occurrence of LHS
     * so A --> B C merges with C --> B A and A --> B A merges with C --> B C
     * but you don't have to preselect, because not quadratic nr of pairings:
     * you can send any rule of the same size to Jelle's method
     */
    public static void findLHSMerges(HashMap<Integer, HashMap<String, HashSet<ArrayList<String>>>> substitutedRulesGroupedBySizeAndLHS, HashMap<String, MergeBigram> newMergeBigramsAfterMerge) {
        String ruleString = null;
        for (Integer ruleSize : substitutedRulesGroupedBySizeAndLHS.keySet()) {
            for (String LHS1 : substitutedRulesGroupedBySizeAndLHS.get(ruleSize).keySet()) {
                for (String LHS2 : substitutedRulesGroupedBySizeAndLHS.get(ruleSize).keySet()) {
                    // be careful not to compare the same sets of rules twice,
                    // and don't compare rules with equal LHSs
                    if (LHS1.compareTo(LHS2) < 0) {
                        // since they are already of the same size, you can
                        // compare every substituted rule of LHS1 with every
                        // substituted rule of LHS2
                        for (ArrayList<String> myRule1 : substitutedRulesGroupedBySizeAndLHS.get(ruleSize).get(LHS1)) {
                            for (ArrayList<String> myRule2 : substitutedRulesGroupedBySizeAndLHS.get(ruleSize).get(LHS2)) {
                                //special case: group merge sets with different
                                // LHS within the set under title (=first) Merge
                                Utils.jellesMethod(myRule1, myRule2, newMergeBigramsAfterMerge, false, LHS1 + "@" + LHS2);
                            }
                       }
                   }
               }
           }
       }
   }

    public void doPartialParsesUpdateAfterMerge(String newName, String mergeA, String mergeB) {
        // enumerate all partialParses, and replace in their parseTrees mergeA_flat and mergeB_flat by newName
        // for partialParse which was substituted:
        // Step 1: make HM index with old RHS and value substitution RHS, do
        // further nix (or former HM RHS RHS nwe)
        // Step 2: iterate over all RHS, and remove mbv key, change key
        // (=value for the HM)
        // restore with new key, if not the increment the count

        // make new index for RHS which merges the substituted
        HashMap<ArrayList<String>, ArrayList<String>> substitutedListOfRHS = new HashMap<ArrayList<String>, ArrayList<String>>();
        ArrayList<String> substitutedRHS;
        for (ArrayList<String> myRHS : partialParseForest.keySet()) {
            if (myRHS.indexOf(mergeA) >= 0 || myRHS.indexOf(mergeB) >= 0) {
                // substituteConstituents in new RHS
                substitutedRHS = new ArrayList<String>();
                substitutedRHS.addAll(myRHS);
                for (int i = 0; i < substitutedRHS.size(); i++) {
                    if (substitutedRHS.get(i).equals(mergeA) || substitutedRHS.get(i).equals(mergeB)) substitutedRHS.set(i, newName);
                }
                substitutedListOfRHS.put(myRHS, substitutedRHS);
            }
            // replace the names of the nodes, for ALL tempPartialParses in
            // myOriginalPartialParse
            partialParse myPP = partialParseForest.get(myRHS);
            for (Node myNode : myPP.getNodes()) {
              if (myNode.getName().equals(mergeA) || myNode.getName().equals(mergeB)) {
                  myNode.setName(newName);
              }
            }
        }

        if (Main.PRINT_DEBUG)
          System.out.println("there are " + substitutedListOfRHS.size() + " substituted rules");

        // now you have list (HM) of indices that must be replaced; remove them
        // by index
        partialParse myOriginalPartialParse = null;
        ArrayList<String>  newRHS;

        // do substitution everywhere
        for (ArrayList<String> originalRHS : substitutedListOfRHS.keySet()) {
            newRHS = substitutedListOfRHS.get(originalRHS);
            if (Main.PRINT_DEBUG)
                System.out.println("newMergeRHS=" + newRHS);
            myOriginalPartialParse = partialParseForest.remove(originalRHS);
            // insert partialParse back in forest indexed by new substitutedRHS
            if (partialParseForest.get(newRHS)==null) {
            //key doesn't exist yet
            // EquivalentSentenceGroup myOriginalPartialParse
            if (Main.PRINT_DEBUG)
                System.out.println("put a myOriginalPartialParse in forest indexed by " + newRHS);
            // where nodes are replaced
            partialParseForest.put(newRHS, myOriginalPartialParse);
            if (Main.PRINT_DEBUG && partialParseForest.get(newRHS).toString() == null)
                System.out.println("XXXXXXXXXXXXXXXXX PUT NULL IN FOREST");
            }
        }
    }

    public void renameMergeBigrams(String mergeA, String mergeB, String newName, HashMap<String, HashMap<ArrayList<String>, Integer>> ruleCounts) {
        String newName_flat = newName + "#";

        // enumerate all MergeBigrams, and find those that have bestCandidate1/2
        // in the name of their key
        // copy content of these to newly created MergeBigram and remove the
        // original
        String newBigramKey = null;
        String newSubstitutedRuleIndex = null;

        boolean blnMBExisted = false;
        ArrayList<String> mergeBigramsToBeDeleted = new ArrayList<String>();
        HashMap<String, MergeBigram> mergeBigramsToBeAdded = new HashMap<String,MergeBigram>();

        for( String myBigramKey : this.mergeBigrams.keySet()) {
            blnMBExisted = false;
            if (myBigramKey.split("@")[0].equals(mergeA) || myBigramKey.split("@")[0].equals(mergeB) || myBigramKey.split("@")[1].equals(mergeA) || myBigramKey.split("@")[1].equals(mergeB)) {
                // the case where both left side and right side of myBigramKey equal mergeA or mergeB has been deleted before
                // create new bigramKey
                if (myBigramKey.split("@")[0].equals(mergeA) || myBigramKey.split("@")[0].equals(mergeB)) {
                    // alphabetically ordered, to be consistent with newly created MB
                    newBigramKey = ((newName.compareTo(myBigramKey.split("@")[1]) < 0) ? newName + "@" + myBigramKey.split("@")[1] : myBigramKey.split("@")[1] + "@" + newName);
                } else
                    newBigramKey = ((newName.compareTo(myBigramKey.split("@")[0]) < 0) ? newName + "@" + myBigramKey.split("@")[0] : myBigramKey.split("@")[0] + "@" + newName );

                // if not exists, then add it to bigrams
                // e.g. MB(a,x) and MB(b,x) existed, after Mrg(a,b) you first
                // created MB(mrg,x) and then do not have to create again for b
                if (mergeBigramsToBeAdded.get(newBigramKey) ==null) {
                    mergeBigramsToBeAdded.put(newBigramKey, new MergeBigram());
                } else blnMBExisted = true;

                // MBs in mergeBigramsToBeAdded are still empty: copy duplicates
                // from original to new BG
                // put all redundant dup rules of the original in the MB with
                // the new name

                // you must add getRedundantDuplicateRules one by one, so they
                // get placed in setsofsets

                for (String myLHS : this.mergeBigrams.get(myBigramKey).getSetOfSetsOfDuplicatesWithSameLHS().keySet()) {
                    for (String mySubstitutedRule : this.mergeBigrams.get(myBigramKey).getSetOfSetsOfDuplicatesWithSameLHS().get(myLHS).keySet()) {
                        for (ArrayList<String> myRedundantDuplicateRule : this.mergeBigrams.get(myBigramKey).getSetOfSetsOfDuplicatesWithSameLHS().get(myLHS).get(mySubstitutedRule)) {
                            // add duprule + LHS, if successfull then MB has changed
                            // this is still the old name for substitutedRule
                            // and also still the old name for LHS
                            mergeBigramsToBeAdded.get(newBigramKey).addRedundantDuplicateRule(myRedundantDuplicateRule, mySubstitutedRule, myLHS);
                        }
                    }
                }

                mergeBigramsToBeAdded.get(newBigramKey).computeTotalRemovedSymbolsOfMerge(myBigramKey);
                if (Main.INCLUDE_POISSON) mergeBigramsToBeAdded.get(newBigramKey).computePoissonGainForMerge();
                if (Main.INCLUDE_DIRICHLET) mergeBigramsToBeAdded.get(newBigramKey).computeDirichletGainForMerge(myBigramKey, ruleCounts);

                // remove original one
                mergeBigramsToBeDeleted.add(myBigramKey);

            }
        }
        // delete original ones
        for (String bigramKeyToDelete : mergeBigramsToBeDeleted) {
            this.mergeBigrams.remove(bigramKeyToDelete);
        }
        // add new ones
        for (String myNewBigramKey : mergeBigramsToBeAdded.keySet()) {
            this.mergeBigrams.put(myNewBigramKey, mergeBigramsToBeAdded.get(myNewBigramKey));
        }

        // now replace the name of the original nonT in all of the dup rules by the merge name
        String beginningOfRule = null, endOfRule = null;
        if (Main.timer)
            System.out.println("Start of MB loop in renameMergeBigrams...");
        for (String myBigramKey : this.mergeBigrams.keySet()) {
            // keep track if there have been subsitutions: in that case also
            // rename duplicateRules and recompute totalSymbols etc
            boolean hasBeenSubstitution = false;
            HashSet<ArrayList<String>> setOfSubstitutedDupRules = new HashSet<ArrayList<String>>();
            ArrayList<String> substituteLHS = new ArrayList<String>();
            ArrayList<String> indicesOfSetsWithSubstitution = new ArrayList<String>();

            // enumerate all sets of sets of redundantduprules
            // first do only LHS replacements
            for (String myLHS : this.mergeBigrams.get(myBigramKey).getSetOfSetsOfDuplicatesWithSameLHS().keySet()) {
                if (myLHS.equals(mergeA) || myLHS.equals(mergeB)) substituteLHS.add(myLHS);
            }

            // replace LHSs
            for (String oldLHS : substituteLHS) {
                HashMap<String, HashSet<ArrayList<String>>> removedLHS = this.mergeBigrams.get(myBigramKey).getSetOfSetsOfDuplicatesWithSameLHS().remove(oldLHS);
                this.mergeBigrams.get(myBigramKey).getSetOfSetsOfDuplicatesWithSameLHS().put(newName, removedLHS);
            }

            // again with corrected LHSs
            for (String myLHS : this.mergeBigrams.get(myBigramKey).getSetOfSetsOfDuplicatesWithSameLHS().keySet()) {
                indicesOfSetsWithSubstitution.clear();
                for (String substitutedRuleIndex : this.mergeBigrams.get(myBigramKey).getSetOfSetsOfDuplicatesWithSameLHS().get(myLHS).keySet()) {
                    // note the case that MergeA or MergeB are in bigramKey:
                    // then index contains mergeA_flat@mergeB_flat@# or one of
                    // them.
                    // then you don't find mergeA_flat# or mergeB_flat#, so you
                    // must also look for mergeA_flat@ and mergeB_flat@!!!
                    // if substitutedRuleIndex contains mergeA_flat# or
                    // mergeB_flat# then remove it from HM, recompute
                    // substitutedRuleIndex.
                    // substitute all its rules, and add them again
                    // CAUTION this is not the old substitutedRuleIndex rewritten in
                    // the first part
                    //NB! dit is nog de oude substitutedRuleIndex NIET herschreven in eerste gedeelte
                    if (substitutedRuleIndex.contains(mergeA + "#") || substitutedRuleIndex.contains(mergeB + "#") || substitutedRuleIndex.contains(mergeA + "@") || substitutedRuleIndex.contains(mergeB + "@")) {
                        hasBeenSubstitution = true;
                        indicesOfSetsWithSubstitution.add(substitutedRuleIndex);
                    }
                }

                // if hasBeenSubstitution=true: remove complete HM, and store
                // it temporarily complete with substitutedRule
                if (indicesOfSetsWithSubstitution.size() > 0) {
                    if (Main.PRINT_DEBUG)
                        System.out.println("indicesOfSetsWithSubstitution.size()>0; myBigram=" + myBigramKey);
                    for (String oldSubstitutedRuleIndex : indicesOfSetsWithSubstitution) {
                        // remove the set from the HM and at the same time get
                        // a reference
                        setOfSubstitutedDupRules = this.mergeBigrams.get(myBigramKey).getSetOfSetsOfDuplicatesWithSameLHS().get(myLHS).remove(oldSubstitutedRuleIndex);
                        newSubstitutedRuleIndex = null;

                        // add all rules from the set again to the MB, HashMaps
                        // indexed by new substitutedRule are auto-created in
                        // the process
                        for (ArrayList<String> myDuplicateRule : setOfSubstitutedDupRules) {
                            // never replace "TOP"
                            if (!mergeA.equals("TOP"))
                                myDuplicateRule = replaceSymbols(myDuplicateRule, mergeA, newName);
                            if (!mergeB.equals("TOP"))
                                myDuplicateRule = replaceSymbols(myDuplicateRule, mergeB, newName);
                            newSubstitutedRuleIndex = myLHS + "#" + Utils.createRuleString(myDuplicateRule);
                            newSubstitutedRuleIndex = replaceSymbolsInString(newSubstitutedRuleIndex, myBigramKey.split("@")[0] + "#", myBigramKey + "@#");
                            newSubstitutedRuleIndex = replaceSymbolsInString(newSubstitutedRuleIndex, myBigramKey.split("@")[1] + "#", myBigramKey + "@#");
                            this.mergeBigrams.get(myBigramKey).addRedundantDuplicateRule(myDuplicateRule, newSubstitutedRuleIndex, myLHS);
                        }
                    }
                }
            }
            if (hasBeenSubstitution) {
                this.mergeBigrams.get(myBigramKey).computeTotalRemovedSymbolsOfMerge(myBigramKey);
                if (Main.INCLUDE_POISSON) this.mergeBigrams.get(myBigramKey).computePoissonGainForMerge();
                if (Main.INCLUDE_DIRICHLET) this.mergeBigrams.get(myBigramKey).computeDirichletGainForMerge(myBigramKey, ruleCounts);
            }
        }
        if (Main.timer) System.out.println("End of MB loop in renameMergeBigrams...");
    }

    public static ArrayList<String> replaceSymbols(ArrayList<String> myRule, String oldSymbol, String newSymbol) {
        for (int i=0; i< myRule.size(); i++) {
            if (myRule.get(i).equals(oldSymbol)) myRule.set(i, newSymbol);
        }
        return myRule;
    }

    public static String replaceSymbolsInString(String myRule, String oldSymbol, String newSymbol) {
        while (myRule.contains(oldSymbol)) {
            String beginningOfRule = myRule.substring(0, myRule.indexOf(oldSymbol));
            String endOfRule = myRule.substring(myRule.indexOf(oldSymbol) + oldSymbol.length());
            myRule = beginningOfRule + newSymbol + endOfRule;
        }
        return myRule;
    }

    /*
     * compare duplicate rules from updatesForMergeBigrams with duplicate rules from MergeBigrams
     * keep only those duplicate rules in updatesForMergeBigrams that have different nonT: 
     */
    public void addNewMergeBigramsAfterMerge(HashMap<String, MergeBigram> newMergeBigramsAfterMerge, ArrayList<nonTerminal> inputGrammar, HashMap<String, HashMap<ArrayList<String>, Integer>> ruleCounts) {
        // enumerate all additional mergeBigrams
        for (String myBigramKey : newMergeBigramsAfterMerge.keySet()) {
            if (Main.PRINT_DEBUG)
                System.out.println("myBigramKey=" + myBigramKey);
            boolean mergeBigramHasChanged = false;
            String merge1 = myBigramKey.split("@")[0];
            String merge2 = myBigramKey.split("@")[1];

            if (this.mergeBigrams.get(myBigramKey)==null) { 
                this.mergeBigrams.put(myBigramKey, new MergeBigram());
                mergeBigramHasChanged = true;
            } else if (Main.PRINT_DEBUG)
                System.out.println("MB " + myBigramKey + "existed");

            // add all updatesForMergeBigrams
            // you must add getRedundantDuplicateRules one by one, so they get
            // placed in setsofsets
            for (String myLHS : newMergeBigramsAfterMerge.get(myBigramKey).getSetOfSetsOfDuplicatesWithSameLHS().keySet()) {
                for (String mySubstitutedRule : newMergeBigramsAfterMerge.get(myBigramKey).getSetOfSetsOfDuplicatesWithSameLHS().get(myLHS).keySet()) {
                    for (ArrayList<String> myRedundantDuplicateRule : newMergeBigramsAfterMerge.get(myBigramKey).getSetOfSetsOfDuplicatesWithSameLHS().get(myLHS).get(mySubstitutedRule)) {
                        // add duprule + LHS, if successfull then MB has changed
                        if (this.mergeBigrams.get(myBigramKey).addRedundantDuplicateRule(myRedundantDuplicateRule, mySubstitutedRule, myLHS)) mergeBigramHasChanged = true;
                    }
                }
            }

            // recompute values of Poisson and totalRemoved only for changed
            // mergeBigrams
            // NOTE computeStructurePriorGainOfMergeBigram, etc. is computed
            // every time again while searching for best merge
            if (mergeBigramHasChanged) {
                this.mergeBigrams.get(myBigramKey).computeTotalRemovedSymbolsOfMerge(myBigramKey);
                if (Main.INCLUDE_POISSON) this.mergeBigrams.get(myBigramKey).computePoissonGainForMerge();
                if (Main.INCLUDE_DIRICHLET) this.mergeBigrams.get(myBigramKey).computeDirichletGainForMerge(myBigramKey, ruleCounts);
            }
        }
    }

    /*
     * compute DescriptionLength of testGrammar (logLikelihood and logDirichlet
     * should be stored in the terminals)
     */
    public static double computeTotalPriorsAndLikelihood(ArrayList<nonTerminal> testGrammar, boolean doPrint, double eGridsMaxDLGain, int eGridsDupRules, int eGridsRemovedSymbols) {
        double totalDirichletPrior = 0d;
        double totalStructurePrior = 0d;
        double totalLogLikelihood = 0d;
        double totalPoisson = 0d;
        double totalPoissonForSB1 = 0d;
        int totalCountofSymbolsinRHS = 0;
        int totalCountofNonTerminalsinRHSofSB1 = 0;
        int totalCountofNonTerminalsinRHSofSB3 = 0;
        int totalCountofSymbolsinRHSofNonLexicalRules = 0;
        int totalCountofSymbolsofNonLexicalRulesIncludingLHS = 0;
        double totalDescriptionLength = 0d;

        NumberFormat numberFormatter;
        numberFormatter = NumberFormat.getNumberInstance();

        int totalRules = 0;
        int nrTOPRules = 0;
        for(nonTerminal myNonTerminal : testGrammar) {
            totalDirichletPrior += myNonTerminal.getDirichlet();
            totalLogLikelihood += myNonTerminal.getLikelihood();
            if (Main.PRINT_DEBUG)
                System.out.println("Stolcke-likelihood of nonT " + myNonTerminal.getName() + "= " + myNonTerminal.getLikelihood());
            totalPoisson += myNonTerminal.getPoissonPartOfStructurePrior();
            if (Main.PRINT_DEBUG)
                System.out.println("Stolcke-Poisson of nonT " + myNonTerminal.getName() + "= " + myNonTerminal.getPoissonPartOfStructurePrior());

            totalRules += myNonTerminal.getRules().size();
            if (myNonTerminal.getName().equals("TOP") ) {
                nrTOPRules = myNonTerminal.getRules().size();
                totalCountofNonTerminalsinRHSofSB1 += myNonTerminal.getCountOfNonTerminalsinRHS();
                totalPoissonForSB1 += myNonTerminal.getPoissonPartOfStructurePrior();
            } else {
                totalCountofNonTerminalsinRHSofSB3 += myNonTerminal.getCountOfNonTerminalsinRHS();
            }
            totalCountofSymbolsinRHS += myNonTerminal.getCountOfSymbolsinRHS();
        }

        // NOTE: totalCountofSymbolsinRHS does not include STOP symbol, nor LHS
        // symbol (needed only for non-S) but it does includes lexical rules

        // remove count of RHS symbols of lexical rules
        // in case without Poisson you have to add
        // Sum(Stop symbols) = (totalRules - Grammar.nrTerminalsInGrammar);
        totalCountofSymbolsinRHSofNonLexicalRules = totalCountofSymbolsinRHS - Grammar.nrTerminalsInGrammar;

        // add LHS, but not for TOP-rules, so add
        // (totalRules - #rules of TOP - #lexical rules)
        // as long as there are no chunks the contribution is zero.
        totalCountofSymbolsofNonLexicalRulesIncludingLHS = totalCountofSymbolsinRHSofNonLexicalRules + (totalRules - Grammar.nrTerminalsInGrammar) - nrTOPRules;

        // adaptation of size: e-grids p9: Aunt  + 1., but A_{unt} is excluding
        // TOP (and excluding STOP but so is grammar.size())
        // log(A_{unt} + 1)
        double nrBitsToEncodeNonTerminalIncludingStop = Math.log((double) testGrammar.size() ) / Math.log(2.);
        // log(A_{unt}) : Poisson: log(#nonT) instead of log(#nonT+1) because no STOP to encode
        double nrBitsToEncodeNonTerminalWithoutStop = Math.log((double) testGrammar.size() -1) / Math.log(2.);
        int totalNonTerminals;

        if (! Main.INCLUDE_POISSON) {   // without Poisson
            // egrids p 9:
            // GDL = log(Aunt+1)*( Sum_{SB1} (|NT_{R} +1|)  + Sum_{SB3} (|NT_{R} +2|) + #Terminals + 2) ) + #Terminals*logT
            // Sum_{SB1} (|NT_{R} +1|) = totalCountofNonTerminalsinRHSofSB1 + nrTOPRules
            // Sum_{SB3} (|NT_{R} +2|) = totalCountofNonTerminalsinRHSofSB3 + 2*(totalRules - nrTOPRules - Grammar.nrTerminalsInGrammar)
            totalStructurePrior = nrBitsToEncodeNonTerminalIncludingStop*((double) totalCountofNonTerminalsinRHSofSB1 + nrTOPRules + totalCountofNonTerminalsinRHSofSB3 + 2*(totalRules - nrTOPRules - Grammar.nrTerminalsInGrammar) + Grammar.nrTerminalsInGrammar + 2);
            // CONSTANT TERM: totalStructurePrior += ((double) Grammar.nrTerminalsInGrammar)*Math.log((double) Grammar.nrTerminalsInGrammar)/Math.log(2.);
            if (Main.PRINT_DEBUG)
                System.out.println("totalCountofNonTerminalsinRHSofSB1="+ totalCountofNonTerminalsinRHSofSB1 + "; totalCountofNonTerminalsinRHSofSB3=" + totalCountofNonTerminalsinRHSofSB3 + "; nrTOPRules=" + nrTOPRules);
        } else {  //Poisson
            // p9 GDL = log(Aunt+1)*( Sum_{SB1} (|NT_{R} +1|)  + Sum_{SB3} (|NT_{R} +2|) + #Terminals + 2) ) + #Terminals*logT
            // Sum_{SB1} (|NT_{R} XXX+1XXX|) = totalCountofNonTerminalsinRHSofSB1 XXX+ nrTOPRulesXXX
            // Sum_{SB3} (|NT_{R} XXX+1XXX|) = totalCountofNonTerminalsinRHSofSB3 + 1*(totalRules - nrTOPRules - Grammar.nrTerminalsInGrammar)

            // leave out SB2: + Grammar.nrTerminalsInGrammar*nrBitsToEncodeNonTerminalWithoutStop, because you want including STOP here
            totalStructurePrior = nrBitsToEncodeNonTerminalWithoutStop*((double) totalCountofNonTerminalsinRHSofSB1 + totalCountofNonTerminalsinRHSofSB3 + 1.*(totalRules - nrTOPRules - Grammar.nrTerminalsInGrammar)  + 2d); //+ Grammar.nrTerminalsInGrammar
            if (Main.PRINT_DEBUG)
                System.out.println("Contribution of chunk: " + nrBitsToEncodeNonTerminalWithoutStop*((double) totalCountofNonTerminalsinRHSofSB3 + totalRules - nrTOPRules - Grammar.nrTerminalsInGrammar));
            // totalNonTerminals = totalCountofNonTerminalsinRHSofSB1 + totalCountofNonTerminalsinRHSofSB3 + totalRules - nrTOPRules - Grammar.nrTerminalsInGrammar  + 2; 
            // CONSTANT TERM: totalStructurePrior += ((double) Grammar.nrTerminalsInGrammar)*Math.log((double) Grammar.nrTerminalsInGrammar)/Math.log(2.);
            // SB2 was w/o Poisson correction: T(log(Aunt+1) + logT) : here you have to take +1 again, LHS rather than STOP
            totalStructurePrior += ((double) Grammar.nrTerminalsInGrammar)*nrBitsToEncodeNonTerminalIncludingStop;
            if (Main.PRINT_DEBUG)
                System.out.println("Stolcke: Total DL = " + numberFormatter.format(totalStructurePrior));
            totalStructurePrior += totalPoisson;
        }

        if (Main.INCLUDE_DIRICHLET) totalStructurePrior += totalDirichletPrior;
        totalDescriptionLength = totalLogLikelihood + totalStructurePrior;

        // PRINTING
        if (doPrint && eGridsDupRules!=0)
            System.out.println("Stolcke: Total DL = " + numberFormatter.format(totalDescriptionLength) + "; Total MDLGain = " + numberFormatter.format(totalDescriptionLength - previousTotalDescriptionLength) +  "; #duplicate rules Stolcke=" + (totalRules - previousTotalRules) + "; symbolsDifference=" + (totalCountofSymbolsinRHS - previousTotalCountofSymbolsinRHS) + "; totalStructurePriorGain=" + numberFormatter.format(totalStructurePrior-previousTotalStructurePrior) + "; LikelihoodGainStolcke= " + numberFormatter.format(totalLogLikelihood-previousTotalLogLikelihood));
        if (doPrint && eGridsDupRules!=0)
            System.out.println("Comparison: MDLGain difference = " + numberFormatter.format(totalDescriptionLength - previousTotalDescriptionLength - eGridsMaxDLGain) + "; Duprulesdiff = " + (totalRules - previousTotalRules + eGridsDupRules) + "; removedSymbolsDiff=" + (totalCountofSymbolsinRHS - previousTotalCountofSymbolsinRHS + eGridsRemovedSymbols + (totalRules - previousTotalRules)));
        MDLGainDifference = totalDescriptionLength - previousTotalDescriptionLength - eGridsMaxDLGain;
        dupRulesdifference = totalRules - previousTotalRules + eGridsDupRules;

        System.out.println("Stolcke: Total DL = " + numberFormatter.format(totalDescriptionLength) +  "; StructurePrior=" + numberFormatter.format(totalStructurePrior) + "; Likelihood= " + numberFormatter.format(totalLogLikelihood) + "; PoissonGain= " + numberFormatter.format(totalPoisson - previousTotalPoisson) + "; # rules=" + totalRules  + "; # total symbols=" + (totalCountofSymbolsinRHS + totalRules)  + "; # nonT=" + testGrammar.size() + "; # T=" + Grammar.nrTerminalsInGrammar);
        if (Main.PRINT_DEBUG)
            System.out.println("XXXXXXX TEST: totalDirichletPrior=" + totalDirichletPrior + "; totalPoisson= " + totalPoisson);
        // write initial values in HM
        if (maxNrNonTerminal == 1) {
            // initial values
            storedMDLValues.put("INITIAL TOTAL DL", new Double(totalDescriptionLength));
            storedMDLValues.put("FINAL TOTAL DL", new Double(totalDescriptionLength));
            storedMDLValues.put("INITIAL LIKELIHOOD", new Double(totalLogLikelihood));
            storedMDLValues.put("INITIAL STRUCTURE PRIOR", new Double(totalStructurePrior));
            storedMDLValues.put("INITIAL #RULES", new Double((double) totalRules));
            storedMDLValues.put("INITIAL #TERMINALS", new Double((double) Grammar.nrTerminalsInGrammar));
            storedMDLValues.put("INITIAL #NON-TERMINALS", new Double((double) testGrammar.size()));
            storedMDLValues.put("INITIAL #SYMBOLS", new Double((double) totalCountofSymbolsinRHS + totalRules));
        }
        // write final values in HM
        if (totalDescriptionLength < storedMDLValues.get("FINAL TOTAL DL").doubleValue()) { 
            storedMDLValues.put("FINAL TOTAL DL", new Double(totalDescriptionLength));
            storedMDLValues.put("FINAL LIKELIHOOD", new Double(totalLogLikelihood));
            storedMDLValues.put("FINAL STRUCTURE PRIOR", new Double(totalStructurePrior));
            storedMDLValues.put("FINAL #RULES", new Double((double) totalRules));
            storedMDLValues.put("FINAL #TERMINALS", new Double((double) Grammar.nrTerminalsInGrammar));
            storedMDLValues.put("FINAL #NON-TERMINALS", new Double((double) testGrammar.size()));
            storedMDLValues.put("FINAL #SYMBOLS", new Double((double) totalCountofSymbolsinRHS + totalRules));
        }
        if (Main.PRINT_DEBUG)
            System.out.println("XXXX totalNonTerminals =" + totalNonTerminals + "; Aunt=" + (testGrammar.size() + 1));

        previousTotalStructurePrior = totalStructurePrior;
        previousTotalLogLikelihood = totalLogLikelihood;
        previousTotalCountofSymbolsinRHS = totalCountofSymbolsinRHS;
        previousTotalRules = totalRules;
        previousTotalPoisson = totalPoisson;
        previousTotalDirichletPrior = totalDirichletPrior;
        previousTotalDescriptionLength = totalDescriptionLength;

        return totalDescriptionLength;
    }

    public void printDuplicateRules(String bestBigramKey, String newName) {
        dupRulesForPrint.add("#################################################################");
        dupRulesForPrint.add("Duplicate rule sets for MRG(" + bestBigramKey + ") -->" + newName); 
        for (String myLHS : this.mergeBigrams.get(bestBigramKey).getSetOfSetsOfDuplicatesWithSameLHS().keySet()) {
            for (String key : this.mergeBigrams.get(bestBigramKey).getSetOfSetsOfDuplicatesWithSameLHS().get(myLHS).keySet()) {
                for(ArrayList<String> myRule : this.mergeBigrams.get(bestBigramKey).getSetOfSetsOfDuplicatesWithSameLHS().get(myLHS).get(key)) {
                    dupRulesForPrint.add(Utils.createRuleString(myRule));
                }
                dupRulesForPrint.add("");
            }
        }
    }

    public void printToScreenGrammarTargetFormat(ArrayList<nonTerminal> testGrammar) {
        ArrayList<String> nonTArray = new ArrayList<String>();
        ArrayList<Rule> myRules;
        String[] oneRule;
        StringBuffer strBuff = null;

        for (nonTerminal nonT : testGrammar) {
            // iterate over rules of nonT
            for (Rule myRule : nonT.getRules()) {
                strBuff = new StringBuffer();
                // LHS
                strBuff.append(nonT.getName() + " ");
                for (String rhsWord : myRule.getRightHandSide()) {
                    strBuff.append(rhsWord + " ");
                }
                strBuff.append("#" + myRule.getCount());
                System.out.println(strBuff.toString());
            }
        }
    }

    public static void printTest() throws IOException {
        HashSet<String> testDuplicateRulePairsForLength2old = new HashSet<String>();
        // read it
        BufferedReader buffUnlabeled = new BufferedReader(new FileReader(Main.OUTPUT_DIRECTORY + "/" + "testduprules2.txt"));
        String mySentence = null;
        while ((mySentence = buffUnlabeled.readLine()) !=null){
            testDuplicateRulePairsForLength2old.add(mySentence);
        }
        // for test print differences
        HashSet<String> testold = (HashSet<String>) testDuplicateRulePairsForLength2old.clone();
        HashSet<String> testnew = (HashSet<String>) Utils.testDuplicateRulePairsForLength2.clone();
        testold.removeAll(Utils.testDuplicateRulePairsForLength2);
        testnew.removeAll(testDuplicateRulePairsForLength2old);

        System.out.println(testold.size() + " duprules in old method not found in new method out of " + testDuplicateRulePairsForLength2old.size());
        for (String diff1 : testold) {
            System.out.println(diff1);
        }
        System.out.println(testnew.size() + " duprules in new method not found in old method out of " + Utils.testDuplicateRulePairsForLength2.size());
        for (String diff2 : testnew) {
            System.out.println(diff2);
        }
    }

    public boolean tooManyNonTerminals(ArrayList<nonTerminal> testGrammar) {
        // count non-lexical nonTerminals
        int nonLexicalNonTerminals = 0;
        for (nonTerminal myNonTerminal : testGrammar) {
            if (myNonTerminal.getCountOfNonTerminalsinRHS() > 0)
                nonLexicalNonTerminals++;
        }
        return nonLexicalNonTerminals > Main.NON_LEX_NON_TERMINAL_MAX;
    }
}
