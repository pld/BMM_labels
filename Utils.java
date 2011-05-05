/*
 * Utils.java
 *
 * Created on 7 juli 2006, 1:04
 *
 */

package BMM_labels;

/**
 *
 * @author peter
 * @author gideon
 */


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.*;

import BMM_labels.Main;
import BMM_labels.MergeBigram;
import BMM_labels.Node;
import BMM_labels.parseTree;
import BMM_labels.partialParse;

public class Utils {

    public static HashMap<Integer, Double> PoissonLookupTable = new HashMap<Integer, Double>();
    public static HashSet<String> testDuplicateRulePairsForLength2 = new HashSet<String>();

    /** Creates a new instance of Utils */
    public Utils() {
    }

    // nested class rulePair
    public class rulePair {
        public ArrayList<String> ruleArray1;
        public ArrayList<String> ruleArray2;
        public String rule1;
        public String rule2;

        public rulePair(ArrayList<String> ruleArray1, ArrayList<String> ruleArray2, String rule1, String rule2){
            this.ruleArray1 = ruleArray1;
            this.ruleArray2 = ruleArray2;
            this.rule1 = rule1;
            this.rule2 = rule2;
        }
    }

    public static void createEntriesForPoissonLookupTable() {
        for (int k =1; k<30; k++) {
            Utils.PoissonLookupTable.put(new Integer(k), new Double(computePoissonGainForRules(k)));
        }
    }

    public static double computePoissonGainForRules(int k) {
        // k=# nonT symbols in RHS
        // Poisson(k,MU) = epow(-MU)*MUpow(k-1)/(k-1)!; 2logX = lnX/ln2
        double logPoisson = 0d;
        double ePOW_mu = Math.exp(- Main.MU);
        double kMin1, k_fac, poisson;

        if (k > 1) {  // Poisson only for non-lexical rules
            // compute (kPlus1-1)!
            kMin1 = (double) k - 1;
            k_fac = 1d;
            for (double r = 2d; r <= kMin1; r++) {
               k_fac = k_fac * r; 
            }
            poisson = ePOW_mu * Math.pow(Main.MU, kMin1) / ( k_fac);
            logPoisson += -Math.log(poisson)/Math.log(2.) ;
        }
        return logPoisson;
    }

    public static ArrayList<String> translateCFKeyToNgrams(String myCFKey, ArrayList<String> myRHS) {
        //System.out.println("In translateCFKeyToNgrams: myCFKey=" + myCFKey + "; RHS=" + myRHS); 
        //CFKey is string of zeros and ones, one indicates that there is a bigram at that position
        ArrayList<String> bigramCF = new ArrayList<String>();
        for (int keyPosition = 0; keyPosition < myCFKey.length(); keyPosition++) {  
            //System.out.println("substring of CFKey=" + myCFKey.substring(keyPosition, keyPosition+1));
            if (myCFKey.substring(keyPosition, keyPosition+1).equals("2")) {    //bigrams
                //System.out.println("CFKey=" + myCFKey + "; sRule=" + this.sRule);
                bigramCF.add(myRHS.get(keyPosition) + "#" + myRHS.get(keyPosition+1));
            }
            if (myCFKey.substring(keyPosition, keyPosition+1).equals("3")) {    //trigrams
                //System.out.println("CFKey=" + myCFKey + "; sRule=" + this.sRule);
                bigramCF.add(myRHS.get(keyPosition) + "#" + myRHS.get(keyPosition+1) + "#" + myRHS.get(keyPosition+2));
            }
            if (myCFKey.substring(keyPosition, keyPosition+1).equals("4")) {    //quartograms
                //System.out.println("CFKey=" + myCFKey + "; sRule=" + this.sRule);
                bigramCF.add(myRHS.get(keyPosition) + "#" + myRHS.get(keyPosition+1) + "#" + myRHS.get(keyPosition+2) + "#" + myRHS.get(keyPosition+3));
            }
        }
        return bigramCF;
   }

    public static HashSet<String> translateCFKeyToSpans(String myCFKey) {
        
        //CFKey is string of zeros and ones, one indicates that there is a bigram at that position
        HashSet<String> bigramCF = new HashSet<String>();
        for (int keyPosition = 0; keyPosition < myCFKey.length(); keyPosition++) {  
            //System.out.println("substring of CFKey=" + myCFKey.substring(keyPosition, keyPosition+1));
            if (myCFKey.substring(keyPosition, keyPosition+1).equals("2")) {    //bigrams
                bigramCF.add("" + keyPosition + "-" + (keyPosition+2));
            }
            if (myCFKey.substring(keyPosition, keyPosition+1).equals("3")) {    //trigrams
                bigramCF.add("" + keyPosition + "-" + (keyPosition+3));
            }
            if (myCFKey.substring(keyPosition, keyPosition+1).equals("4")) {    //quartograms
                bigramCF.add("" + keyPosition + "-" + (keyPosition+4));
            }
        }
        return bigramCF;
   }     
    
    public static ArrayList<String> translatePositionInCFKeyToNgram(String myCFKey, int targetPosition, ArrayList<String> myRHS) {
        
        //CFKey is string of zeros and ones, one indicates that there is a bigram at that position
        ArrayList<String> myNGram = new ArrayList<String>();
        for (int keyPosition = 0; keyPosition < myCFKey.length(); keyPosition++) {  
            //System.out.println("substring of CFKey=" + myCFKey.substring(keyPosition, keyPosition+1));
            if (myCFKey.substring(keyPosition, keyPosition+1).equals("2")) {    //bigrams   
                //if targetPosition within reach of this NGram
                if (targetPosition==keyPosition || targetPosition==(keyPosition+1)) {
                    //return "" + myRHS.get(keyPosition) + "#" + myRHS.get(keyPosition+1);
                    myNGram.add(myRHS.get(keyPosition));
                    myNGram.add(myRHS.get(keyPosition+1));
                    return myNGram;
                }     
            }
            if (myCFKey.substring(keyPosition, keyPosition+1).equals("3")) {    //trigrams
                if (targetPosition==keyPosition || targetPosition==(keyPosition+1) || targetPosition==(keyPosition+2)) {
                    //return "" +  myRHS.get(keyPosition) + "#" + myRHS.get(keyPosition+1) + "#" + myRHS.get(keyPosition+2);
                    myNGram.add(myRHS.get(keyPosition));
                    myNGram.add(myRHS.get(keyPosition+1));
                    myNGram.add(myRHS.get(keyPosition+2));
                    return myNGram;
                }
            }
            if (myCFKey.substring(keyPosition, keyPosition+1).equals("4")) {    //quartograms
                if (targetPosition==keyPosition || targetPosition==(keyPosition+1) || targetPosition==(keyPosition+2) || targetPosition==(keyPosition+3)) {
                    //return "" +  myRHS.get(keyPosition) + "#" + myRHS.get(keyPosition+1) + "#" + myRHS.get(keyPosition+2) + "#" + myRHS.get(keyPosition+3);
                    myNGram.add(myRHS.get(keyPosition));
                    myNGram.add(myRHS.get(keyPosition+1));
                    myNGram.add(myRHS.get(keyPosition+2));
                    myNGram.add(myRHS.get(keyPosition+3));
                    return myNGram;
                }
            }
        }
        return myNGram;
   }     
    
    public static ArrayList<String> findActiveNGramInPositionOfCFKey(String myCFKey, int keyPosition, ArrayList<String> myRHS) {
        
        //CFKey is string of zeros and ones, one indicates that there is a bigram at that position
        ArrayList<String> myNGram = new ArrayList<String>();
        //for (int keyPosition = 0; keyPosition < myCFKey.length(); keyPosition++) {  
            //System.out.println("substring of CFKey=" + myCFKey.substring(keyPosition, keyPosition+1));
            if (myCFKey.substring(keyPosition, keyPosition+1).equals("2")) {    //bigrams
                //System.out.println("CFKey=" + myCFKey + "; sRule=" + this.sRule);
                myNGram.add(myRHS.get(keyPosition));
                myNGram.add(myRHS.get(keyPosition+1));
            }
            if (myCFKey.substring(keyPosition, keyPosition+1).equals("3")) {    //trigrams
                //System.out.println("CFKey=" + myCFKey + "; sRule=" + this.sRule);
                myNGram.add(myRHS.get(keyPosition));
                myNGram.add(myRHS.get(keyPosition+1));
                myNGram.add(myRHS.get(keyPosition+2));
            }
            if (myCFKey.substring(keyPosition, keyPosition+1).equals("4")) {    //quartograms
                //System.out.println("CFKey=" + myCFKey + "; sRule=" + this.sRule);
                myNGram.add(myRHS.get(keyPosition));
                myNGram.add(myRHS.get(keyPosition+1));
                myNGram.add(myRHS.get(keyPosition+2));
                myNGram.add(myRHS.get(keyPosition+3));
            }
        //}
        return myNGram;
   }     

    public static void findDuplicateRules(HashSet<ArrayList<String>> originalRules, boolean blnRHSOnly, String myLHS, HashMap<String, MergeBigram> mergeBigramPairs) {
       int ruleLength = 0;
       HashMap<Integer, HashSet<ArrayList<String>>> rulesGroupedBySize = new HashMap<Integer, HashSet<ArrayList<String>>>();

       // store rules in HashMap ordered by length
       for (ArrayList<String> ruleArray : originalRules) {
            // create ArrayList containing the rule, but not the final index,
            // because that is rulecount
            ruleLength = ruleArray.size();  //including LHS
            // put rule together with ruleArray in HashMap
            if (rulesGroupedBySize.get(new Integer(ruleLength))==null) {
                rulesGroupedBySize.put(new Integer(ruleLength), new HashSet<ArrayList<String>>());
            }
            rulesGroupedBySize.get(ruleLength).add(ruleArray);
        }
        originalRules = null;

        // loop over rules of certain length
        for (Integer ruleSize : rulesGroupedBySize.keySet()) {
            ruleLength = ruleSize.intValue();
            findDupRulesOfSameSize(rulesGroupedBySize.get(ruleSize), blnRHSOnly, myLHS, ruleLength, mergeBigramPairs) ;
            rulesGroupedBySize.get(ruleSize).clear();    //save memory
        }
    }

    public static void findDupRulesOfSameSize(HashSet<ArrayList<String>> rulesGroupedBySize, boolean blnRHSOnly, String myLHS, int ruleLength, HashMap<String, MergeBigram> mergeBigramPairs) {
        HashSet<String> entriesForRule = new HashSet<String> ();
        HashSet<ArrayList<String>> rulesDifferingBySingleNonTerminal = null;
        // First String is entry (StringOfAllButOneNonTerminalsInRule) in table,
        // HashMap<String> is set of rules that map/project to entry
        // tableWithRulesDifferingBySingleNonTerminal has for every entry a set
        // of rules that differ by maximally a single non-terminal
        // every rule gets several entries; each entry another non-terminal is
        // omitted; Indexed By String Of All But One NonTerminals In Rule
        HashMap<String, HashSet<ArrayList<String>>> tableWithRulesDifferingBySingleNonTerminal = new HashMap<String, HashSet<ArrayList<String>>>();
        // loop over all rules of same size
        for (ArrayList<String> ruleArray : rulesGroupedBySize) {
            // find ruleArray
            // create rule entries (HashSet<String>)
            // finds for every rule the HashSet of the (ordered) sets of unique
            // non-terminals (concatenated into a string separated by #)
            // that you get by removing a single non-terminal from the rule
            // this way, you can limit yourself to looking for duplicates only
            // if rules are in a cell with same ruleEntry
            entriesForRule = createStringsOfAllButOneNonTerminalsInRule(ruleArray, ruleLength);

            // look up the entry/index in tableWithRulesDifferingBySingleNonTerminal;
            // if it doesn't exist, then create it
            for (String myRuleEntry : entriesForRule) {
                if (tableWithRulesDifferingBySingleNonTerminal.get(myRuleEntry) == null) {
                    // empty, nothing to compare to
                    // create HashSet, add originalRule to HashSet, add HashSet to HashMap table
                    rulesDifferingBySingleNonTerminal = new HashSet<ArrayList<String>>(0);
                    rulesDifferingBySingleNonTerminal.add(ruleArray);
                    tableWithRulesDifferingBySingleNonTerminal.put(myRuleEntry, rulesDifferingBySingleNonTerminal);
                } else {
                    // for next comparison
                    tableWithRulesDifferingBySingleNonTerminal.get(myRuleEntry).add(ruleArray);
                }
            }
        }
        if (Main.timer) System.out.println("We have just finished grouping " + rulesGroupedBySize.size() + " rules of length " + ruleLength + " into tableWithRulesDifferingBySingleNonTerminal");
        if (Main.timer) System.out.println("tableWithRulesDifferingBySingleNonTerminal has " + tableWithRulesDifferingBySingleNonTerminal.size() + " entries for potential duprules");
        rulesGroupedBySize = null;
        rulesDifferingBySingleNonTerminal = null;
        entriesForRule = null;
        int myCounter = 0;
        if (Main.PRINT_DEBUG && myLHS.equals("TOP") || myLHS.equals("")) System.out.println("Table filled up with rules of length " + ruleLength);
        // this time you need an ArrayList instead of HashSet, because you need
        // to iterate over all pairs
        ArrayList<ArrayList<String>> listOfRulesDifferingBySingleNonTerminal = new ArrayList<ArrayList<String>>(); 

        // now that you have filled up the entries of the table with potential
        // duplicate rules, loop over all entries
        int tempCounter = 0;
        for (String myRuleEntry : tableWithRulesDifferingBySingleNonTerminal.keySet()) {
            tempCounter++;
            listOfRulesDifferingBySingleNonTerminal.clear();
            // this is needed because you want arrayList rather than HashSet,
            // so you can enumerate the ordered pairs i,j
            listOfRulesDifferingBySingleNonTerminal.addAll(tableWithRulesDifferingBySingleNonTerminal.get(myRuleEntry));
            if (listOfRulesDifferingBySingleNonTerminal.size() > 1) {
                if (Main.timer) System.out.println("#" + tempCounter + ") myRuleEntry=" + myRuleEntry + "; #rules for this entry= " + listOfRulesDifferingBySingleNonTerminal.size() );
                // separate for ruleLength == 2
                if (ruleLength == 2) {
                    findMergePairsForRulesOfLength2(myRuleEntry, listOfRulesDifferingBySingleNonTerminal, mergeBigramPairs, myLHS);
                } else {
                    // now you must compare these pairwise for duplicates (with Jelle's method)
                    for (int i = 0; i < listOfRulesDifferingBySingleNonTerminal.size() - 1; i++ ) {
                        for (int j = i + 1; j < listOfRulesDifferingBySingleNonTerminal.size(); j++ ) {
                            // no need to include null MB which is obtained if
                            // rule1=rule2
                            // if blnRHSOnly = false then LHS is included in
                            // string: then you must compare only across rules
                            // with different LHS
                            if (blnRHSOnly || (!blnRHSOnly && !listOfRulesDifferingBySingleNonTerminal.get(i).equals(listOfRulesDifferingBySingleNonTerminal.get(j)))) {
                                jellesMethod(listOfRulesDifferingBySingleNonTerminal.get(i), listOfRulesDifferingBySingleNonTerminal.get(j), mergeBigramPairs, blnRHSOnly, myLHS);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void jellesMethod(ArrayList<String> rule1, ArrayList<String> rule2, HashMap<String, MergeBigram> mergeBigramPairs, boolean blnRHSOnly, String myLHS) {
        String bigramKey = null;
        String secondBigramKey = null;
        StringBuffer substitutedRule = new StringBuffer();
        // the LHS is never included in rule1 and rule2, neither if blnRHSOnly=false
        substitutedRule.append(myLHS + "#");

        // if LHS merges you already start with a bigramKey based on the different LHS
        if (!blnRHSOnly) {
            bigramKey = myLHS;
            myLHS  = myLHS.split("@")[0];
        }

        for (int i = 0; i < rule1.size(); i++) {
            // if they are not equal at some point there is need to create a merge bigram
            if (!rule1.get(i).equals(rule2.get(i))) {
                // if one of them is a terminal then don't create the bigram
                // you need to add a bigramKey at this point
                if (bigramKey==null) {   //create the first and only bigramKey: SORT
                    bigramKey = (rule1.get(i).compareTo(rule2.get(i)) <0 ? rule1.get(i) + "@" + rule2.get(i) : rule2.get(i) + "@" + rule1.get(i));
                }
                else {
                    // only continue if current merge is identical to existing bigramKey
                    secondBigramKey = (rule1.get(i).compareTo(rule2.get(i)) <0 ? rule1.get(i) + "@" + rule2.get(i) : rule2.get(i) + "@" + rule1.get(i));
                    if (!secondBigramKey.equals(bigramKey)) return;
                }
                substitutedRule.append(bigramKey + "@#");
            }
            else substitutedRule.append(rule1.get(i) + "#");
        }
        // only if you passed the entire ruleString w/o encountering additional
        // secondBigramKey unless they are equal you may consider it a merge;
        // check if exists:
        MergeBigram myMB = mergeBigramPairs.get(bigramKey);
        if (myMB == null) {
            myMB = new MergeBigram();
            mergeBigramPairs.put(bigramKey,  myMB);
        }
        myMB.addRedundantDuplicateRule(rule1, substitutedRule.toString(), myLHS);
        myMB.addRedundantDuplicateRuleSimple(rule2, substitutedRule.toString(), myLHS);
    }

    public static void findMergePairsForRulesOfLength2(String myRuleEntry, ArrayList<ArrayList<String>> listOfRulesDifferingBySingleNonTerminal, HashMap<String, MergeBigram> mergeBigramPairs, String myLHS) {
        // separate the rules in those that start with myRuleEntry and those that
        // end with myRuleEntry
        // you need only store the distinguished part TreeMap because you want
        // it to be sorted, so that mergebigram will be automatically in correct
        // order
        TreeMap<String, ArrayList<String>> setOfRulesStartingWithCommonEntry = new  TreeMap<String, ArrayList<String>>();
        TreeMap<String, ArrayList<String>> setOfRulesEndingWithCommonEntry = new  TreeMap<String, ArrayList<String>>();
        ArrayList<ArrayList<String>> listOfRulesStartingWithCommonEntry = new  ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<String>> listOfRulesEndingWithCommonEntry = new  ArrayList<ArrayList<String>>();
        myRuleEntry = myRuleEntry.split("#")[0];

        for (ArrayList<String> ruleOfLength2 : listOfRulesDifferingBySingleNonTerminal) {
            if (ruleOfLength2.get(0).equals(myRuleEntry)) {
                setOfRulesStartingWithCommonEntry.put(ruleOfLength2.get(1), ruleOfLength2);
            }
            // this includes the case that myRuleEntry.equals("")
            else setOfRulesEndingWithCommonEntry.put(ruleOfLength2.get(0), ruleOfLength2);
        }

        // add to list, now the rules are hopefully ordered (???)
        listOfRulesStartingWithCommonEntry.addAll(setOfRulesStartingWithCommonEntry.values());
        listOfRulesEndingWithCommonEntry.addAll(setOfRulesEndingWithCommonEntry.values());
        String substitutionRule = null;

        MergeBigram myMB = null;
        String bigramKey = null;
        String mergeA = null, mergeB=null;
        ArrayList<String> ruleForEmptyEntry = null;

        // create mergeBigrams separately for both lists
        for (int i = 0; i < listOfRulesStartingWithCommonEntry.size() - 1 ; i++) {
            for (int j = i + 1; j < listOfRulesStartingWithCommonEntry.size() ; j++) {
                mergeA = listOfRulesStartingWithCommonEntry.get(i).get(1);
                mergeB = listOfRulesStartingWithCommonEntry.get(j).get(1);
                bigramKey = mergeA + "@" + mergeB;
                if (mergeBigramPairs.get(bigramKey) == null) {
                    myMB = new MergeBigram();
                    mergeBigramPairs.put(bigramKey,  myMB);
                } else myMB = mergeBigramPairs.get(bigramKey);

                if (!myRuleEntry.equals("")) {
                    if (!(myLHS.equals(mergeA) || myLHS.equals(mergeB)))
                        substitutionRule = myLHS + "#";
                    else
                        substitutionRule = bigramKey + "@#";
                    if (!(myRuleEntry.equals(mergeA) || myRuleEntry.equals(mergeB)))
                        substitutionRule += myRuleEntry + "#" + bigramKey + "@#";
                    else
                        substitutionRule += bigramKey + "@#" + bigramKey + "@#";
                    myMB.addRedundantDuplicateRule(listOfRulesStartingWithCommonEntry.get(i), substitutionRule, myLHS);
                    myMB.addRedundantDuplicateRuleSimple(listOfRulesStartingWithCommonEntry.get(j), substitutionRule, myLHS);
                } else {
                    ruleForEmptyEntry = new ArrayList<String>();
                    ruleForEmptyEntry.add(mergeA);
                    ruleForEmptyEntry.add(mergeA);
                    if (!(myLHS.equals(mergeA) || myLHS.equals(mergeB)))
                        substitutionRule = myLHS + "#" + bigramKey + "@#" + bigramKey + "@#";
                    else
                        substitutionRule = bigramKey + "@#" + bigramKey + "@#" + bigramKey + "@#";
                    myMB.addRedundantDuplicateRule(ruleForEmptyEntry, substitutionRule, myLHS);
                    ruleForEmptyEntry = new ArrayList<String>();
                    ruleForEmptyEntry.add(mergeB);
                    ruleForEmptyEntry.add(mergeB);
                    myMB.addRedundantDuplicateRuleSimple(ruleForEmptyEntry, substitutionRule, myLHS);
                }
            }
        }

        ArrayList<String> getRuleEndingWithCommonEntryBack = new ArrayList<String>();
        getRuleEndingWithCommonEntryBack.add("");
        getRuleEndingWithCommonEntryBack.add(myRuleEntry);
        for (int i = 0; i< listOfRulesEndingWithCommonEntry.size() - 1 ; i++) {
            for (int j = i + 1; j< listOfRulesEndingWithCommonEntry.size() ; j++) {
                mergeA = listOfRulesEndingWithCommonEntry.get(i).get(0);
                mergeB = listOfRulesEndingWithCommonEntry.get(j).get(0);
                bigramKey = mergeA + "@" + mergeB;
                if (mergeBigramPairs.get(bigramKey) == null) {
                   myMB = new MergeBigram();
                   mergeBigramPairs.put(bigramKey, myMB);
                } else myMB = mergeBigramPairs.get(bigramKey);
                if (!myRuleEntry.equals("")) {
                    if (!(myLHS.equals(mergeA) || myLHS.equals(mergeB))) substitutionRule = myLHS + "#";
                    else substitutionRule = bigramKey + "@#";
                    if (!(myRuleEntry.equals(mergeA) || myRuleEntry.equals(mergeB))) substitutionRule += bigramKey + "@#" + myRuleEntry + "#";
                    else substitutionRule += bigramKey + "@#" + bigramKey + "@#";
                    myMB.addRedundantDuplicateRule(listOfRulesEndingWithCommonEntry.get(i), substitutionRule, myLHS);
                    myMB.addRedundantDuplicateRuleSimple(listOfRulesEndingWithCommonEntry.get(j), substitutionRule, myLHS);
                } else {
                    ruleForEmptyEntry = new ArrayList<String>();
                    ruleForEmptyEntry.add(mergeA);
                    ruleForEmptyEntry.add(mergeA);
                    if (!(myLHS.equals(mergeA) || myLHS.equals(mergeB)))
                        substitutionRule = myLHS + "#" + bigramKey + "@#" + bigramKey + "@#";
                    else
                        substitutionRule = bigramKey + "@#" + bigramKey + "@#" + bigramKey + "@#";
                    myMB.addRedundantDuplicateRule(ruleForEmptyEntry, substitutionRule, myLHS);
                    ruleForEmptyEntry = new ArrayList<String>();
                    ruleForEmptyEntry.add(mergeB);
                    ruleForEmptyEntry.add(mergeB);
                    myMB.addRedundantDuplicateRuleSimple(ruleForEmptyEntry, substitutionRule, myLHS);
                }
            }
        }
        // find the cases X A merges with A X, by looking up in set
        if (!myRuleEntry.equals("")) {
            for (String firstSymbol : setOfRulesEndingWithCommonEntry.keySet()) {
                if (setOfRulesStartingWithCommonEntry.containsKey(firstSymbol)) {
                    bigramKey = (firstSymbol.compareTo(myRuleEntry) < 0 ? (firstSymbol + "@" + myRuleEntry) : (myRuleEntry + "@" + firstSymbol));
                    if (mergeBigramPairs.get(bigramKey) == null) {
                        myMB = new MergeBigram();
                        mergeBigramPairs.put(bigramKey,  myMB);
                    } else myMB = mergeBigramPairs.get(bigramKey);
                    ruleForEmptyEntry = new ArrayList<String>();
                    ruleForEmptyEntry.add(firstSymbol);
                    ruleForEmptyEntry.add(myRuleEntry);
                    if (!(myLHS.equals(mergeA) || myLHS.equals(mergeB)))
                        substitutionRule = myLHS + "#" + bigramKey + "@#" + bigramKey + "@#";
                    else
                        substitutionRule = bigramKey + "@#" + bigramKey + "@#" + bigramKey + "@#";
                    myMB.addRedundantDuplicateRule(ruleForEmptyEntry, substitutionRule, myLHS);
                    ruleForEmptyEntry = new ArrayList<String>();
                    ruleForEmptyEntry.add(myRuleEntry);
                    ruleForEmptyEntry.add(firstSymbol);
                    myMB.addRedundantDuplicateRuleSimple(ruleForEmptyEntry, substitutionRule, myLHS);
                }
            }
        }
    }

    /* finds for every rule all the (ordered) sets of unique non-terminals
     * (concatenated into a string separated by #)
     * that you get by removing a single non-terminal from the rule
     */
    public static HashSet<String> createStringsOfAllButOneNonTerminalsInRule(ArrayList<String> ruleArray, int ruleLength) {
        HashSet<String> entriesForRule = new HashSet<String>();
        TreeSet<String> combiOfNonTMinusOne = new TreeSet<String>();
        String skipNonT = null;

        // prepare tableWithRulesDifferingBySingleNonTerminal of rules that
        // must be compared (differ only in one nonT)
        // loop over unique nonT of HashSet, create alphabetically ordered
        // String of all unique nonT minus one,
        // (by concatenating original HashSet unless member equals the one in loop)
        for (int i = 0; i < ruleLength; i++) {
            skipNonT = ruleArray.get(i);
            // add all but myIndex to Treeset
            // combination of nonT that appear in rule minus one
            combiOfNonTMinusOne.clear();
            for (int j = 0; j < ruleLength; j++) {
                // TreeSet, so unique
                if (j != i) combiOfNonTMinusOne.add(ruleArray.get(j));
            }
            // concatenate, and add resulting string to entriesForRule
            entriesForRule.add(ruleEntryToString(combiOfNonTMinusOne));

            // add another entry to Treeset, with nonT completely removed (in case nonT occurred more than once in rule):
            if (combiOfNonTMinusOne.remove(skipNonT)) {
                entriesForRule.add(ruleEntryToString(combiOfNonTMinusOne));
            }
        }
        return entriesForRule;
    }

    public static String ruleEntryToString(TreeSet<String> combiOfNonTMinusOne) {
        StringBuffer ruleEntry = new StringBuffer();
        for (String entry : combiOfNonTMinusOne) {
            ruleEntry.append(entry).append("#");
        }
        return ruleEntry.toString();
    }

    public static String createRuleString(ArrayList<String> myRHS) {
        // put substituted rules in HashSet rulesWithSubstitution for checking
        // for new mergeBigrams
        StringBuffer ruleString = new StringBuffer();
        for (String RHSSymbol : myRHS){
           ruleString.append(RHSSymbol).append("#");
        }
        return ruleString.toString();
   }

   public static void storeMergeInfo(LinkedHashMap<String, ArrayList<String>> mergesForPrint, LinkedHashMap<String, ArrayList<String>> chunksForPrint, String newName, String mergeA, String mergeB, String extraInfo) {
       String mergeName = null;
       // take off the number
       if (!newName.equals("TOP"))
           mergeName = newName.split("~")[0] + "~" + newName.split("~")[1];
       else mergeName = "TOP";
       String finishedMerge = null;

       if (mergesForPrint.get(mergeName) == null) {
           // new entry for merge
           ArrayList<String> mergeEntries = new ArrayList<String>();
           // store the word containing the same name as mergename
           // NOTE that sometimes merges that belong to different sequences can
           // have the same mergeName!!
           mergeEntries.add(mergeName);
           mergeEntries.add(mergeA);
           mergeEntries.add(mergeB + extraInfo);
           mergesForPrint.put(mergeName, mergeEntries);
       } else {   // merge exists
            if (mergeA.contains(mergeName)) {
               // add the other one
               mergesForPrint.get(mergeName).add(mergeB + extraInfo);
               // if the other one was a merge, add info at the end of it
               // indicating that it is merged into this one
               if (mergeB.split("~")[0].equals("MRG")) {
                   finishedMerge = "MRG~" + mergeB.split("~")[1];
                   if (mergesForPrint.get(finishedMerge)!=null) {
                       mergesForPrint.get(finishedMerge).add(">>> Absorbed in " + mergeName);
                   }
               }
               // if it was a chunk, add info at end of chunk column, indicating
               // it was absorbed in merge
               if (mergeB.split("~")[0].equals("CHNK") && chunksForPrint.get(mergeB) != null) {
                   chunksForPrint.get(mergeB).add(">>> Absorbed in " + mergeName);
               }
            } else {
                // mergeB contains mergeName (hopefully)
                mergesForPrint.get(mergeName).add(mergeA + extraInfo);
                // if this one was a merge, add info at the end of it indicating
                // that it is merged into this one
                if (mergeA.split("~")[0].equals("MRG")) {
                   finishedMerge = "MRG~" + mergeA.split("~")[1];
                   if (mergesForPrint.get(finishedMerge) != null) {
                       mergesForPrint.get(finishedMerge).add(">>> Absorbed in " + mergeName);
                   }
                }
               // if it was a chunk, add info at end of chunk column, indicating
               // it was absorbed in merge
               if (mergeA.split("~")[0].equals("CHNK") && chunksForPrint.get(mergeA)!=null) {
                   chunksForPrint.get(mergeA).add(">>> Absorbed in " + mergeName);
               }
            }
       }
   }

   public static int computeUniqueBrackets(ArrayList<Node> listOfNodes) {
       HashSet<String> listOfUniqueBrackets = new HashSet<String>();
       for (Node myNode : listOfNodes) {
            if (myNode.getRightSpan()-myNode.getLeftSpan() > 1)
                listOfUniqueBrackets.add(myNode.getLeftSpan() + "-" + myNode.getRightSpan());
        }
       return listOfUniqueBrackets.size();
   }

   /**
    * 0) lees unlabeled en spans
    * 1) voeg TOP toe, en preterminals
    * 2) maak arraylist van unieke spans
    * 3) tel linker- (uit leftSpan) en rechterhaken op elke positie: dwz
    * 2XArrayList<Integer> met #haken voor elke word position 
    * 4) maak zin , voeg haken in op word positions ; spaties tussen l en r-haak
    * na elke l-haak (behalve laatste) moet je naam voor nonT verzinnen, maakt
    * niet uit wat, gevolgd door spatie
    * bijv (CHNK~1126 (MRG~1930 (_DT (dt )) 
    * alleen na l-haak die voor preterminal/woord staat vul je gewone woord in
    * gevolgd door spatie, maw altijd na laatste l-haak
    */
    public static String doCreateWSJParseFromSpans(ArrayList<String> wordsOfSentence, String[] spanArray) {
        String topSpan;
        StringBuffer mySymbol;
        HashSet<String> spanSet = null;

        ArrayList<Integer> leftSpans = null; 
        ArrayList<Integer> rightSpans = null;
        int leftSpanIndex, rightSpanIndex;
        int nonterminalNr = 0;
        int myCounter = 0;

        myCounter++;

        // put spans into HashSet spanSet
        spanSet = new HashSet<String>();
        for (int i = 0; i < spanArray.length; i++) {
            spanSet.add(spanArray[i]);
        }

        // add top-span and preterminal spans
        topSpan = "0-" + (wordsOfSentence.size());
        // spanSet is HashSet, so cannot create duplicate spans!!
        spanSet.add(topSpan);

        // create arrays and fill with zeros
        leftSpans = new ArrayList<Integer>();
        rightSpans = new ArrayList<Integer>();
        for (int i =0; i< wordsOfSentence.size()+1; i++) {
            leftSpans.add(0);
            rightSpans.add(0);
        }
        // compute for every position left and right brackets

        for (String mySpan : spanSet) {
            // get left bracket of span
            leftSpanIndex = java.lang.Integer.parseInt(mySpan.split("-")[0]);
            rightSpanIndex = java.lang.Integer.parseInt(mySpan.split("-")[1]);
            // increase number of brackets at that position by 1
            leftSpans.set(leftSpanIndex, leftSpans.get(leftSpanIndex)+1);
            rightSpans.set(rightSpanIndex, rightSpans.get(rightSpanIndex)+1);
        }

        // create the parse, inserting brackets at the correct positions
        // 4) maak zin , voeg haken in op word positions ; spaties tussen l en r-haak
        // na elke l-haak (behalve laatste) moet je naam voor nonT verzinnen,
        // maakt niet uit wat, gevolgd door spatie
        // bijv (CHNK~1126 (MRG~1930 (_DT (dt )) 
        // alleen na l-haak die voor preterminal/woord staat vul je gewone woord
        // in gevolgd door spatie, maw altijd na laatste l-haak
        StringBuffer WSJParse = new StringBuffer();
        for (int wordPosition =0; wordPosition< wordsOfSentence.size(); wordPosition++) {
            if (leftSpans.get(wordPosition).intValue() > 0) {
                //write a ( with a random non-terminal
                for (int i = 1; i <= leftSpans.get(wordPosition).intValue(); i++) {
                    WSJParse.append("(" + "X" + " ");
                }
            }
            // write one bracket before the word in any case, and one bracket after
            WSJParse.append("(" + wordsOfSentence.get(wordPosition) + " " + ")");
            // right spans, NB look at position behind the word!
            if (rightSpans.get(wordPosition+1).intValue() > 0) {
                // write a ( with a random non-terminal
                for (int i = 1; i <= rightSpans.get(wordPosition + 1).intValue(); i++) {
                    WSJParse.append(")");
                }
            }
            WSJParse.append(" ");
        }

        // nu nog laatste right brackets: nee hoeft niet
        // vervang eerste non-terminal X door TOP, zodat hij niet nog extra TOP toevoegt 
        return  "(TOP" + WSJParse.toString().substring(2);
    }

    public static void printPartialParse(partialParse myPartialParse) {
        StringBuffer parseSimpleFmt = new StringBuffer();
        for (Node myNode : myPartialParse.getNodes()) {
            parseSimpleFmt.append(myNode.getName() + " (" + myNode.getLeftSpan() + "-" + myNode.getRightSpan() + ");  ");
        }
        System.out.println(parseSimpleFmt.toString());
        parseSimpleFmt = new StringBuffer();
        for (String mySpan : myPartialParse.getSpans()) {
            parseSimpleFmt.append(mySpan + "   ");
        }
        System.out.println("Spans: " + parseSimpleFmt.toString());
    }

    protected static parseTree extractParseFromWSJText(String sentence, boolean blnPrintChars) {
        // (a,[(tv,[(nee_,[])]),(vp,[(v,[(dank_,[])]),(per,[(u_,[])])])]).
        // (S (NP (POSTAG hallo)))
        // ( (S (NP-SBJ (NNP Ms.) (NNP Waleson) ) (VP (VBZ is) (NP (NP (DT a) (JJ free-lance) (NN writer) ) (VP (VBN based) (NP (-NONE- *) ) (PP-LOC-CLR (IN in) (NP (NNP New) (NNP York) ))))) (. .) ))
        // create temporary sentence with node structure
        // TOP node is automatically created
        parseTree myParseTree = new parseTree();

        int characterPosition = 0;
        int wordPosition = 0;

        // currentNode is the index of the node in the parseTree (ArrayList of nodes)
        // set currentNode to TOP, which is created in constructor of parseTree
        Node currentNode = myParseTree.getNode(myParseTree.getNodes().size()-1);

        // XXX maar er zit ook TOP in de sentence!!!

        // turn sentence into array of characters
        char[] mySentence = sentence.toCharArray();

        StringBuffer mySymbol;

        while (characterPosition < sentence.length()) {
            // one of three possibilities:
            // if current character is (, then a new Node starts --> move till after ( 
            // if current character is ), then the rule ends --> move till after )
            // otherwise, if next character is space, then stay within the same rule --> move behind space

            // 1) start of new rule:
            if (mySentence[characterPosition] == '(' ) {
                if (blnPrintChars) System.out.println(mySentence[characterPosition]);
                characterPosition++;    // pass the (
                // get name of symbol, or LHS of rule
                mySymbol = new StringBuffer();
                // (s,[(per,[(ik_,[])]),(vp,[(v,[(wil_,[])]),(mp,[(p,[(naar_,[])]),(np,[(np,[(den,[(den_,[])]),(haag,[(haag_,[])])]),(np,[(np,[(centraal_,[])]),(n,[(station_,[])])])])])])]).
                while (mySentence[characterPosition] != ' ') {
                    mySymbol.append(mySentence[characterPosition]);
                    if (blnPrintChars) System.out.println(mySentence[characterPosition]);
                    characterPosition++;
                }

                // only if not again TOP
                if (!mySymbol.toString().toUpperCase().equals("TOP")) {
                    // add the symbol to rule of dominant node
                    // NB at instantiation of myParseTree the first current rule
                    // with LHS TOP is automatically created

                    // make new node out of mySymbol, which is daughter of currentNode
                    // currentNode is pointer to parent node (xxx)
                    Node myNode = new Node(new String(mySymbol), currentNode);
                    myParseTree.addNode(myNode);

                    //add this node as a child to parent node
                    currentNode.addChildNode(myNode);

                    // position is now on space, move ahead till you get behind space
                    if (blnPrintChars) System.out.println(mySentence[characterPosition]);
                    characterPosition++; // pass the space

                    // last word is a Terminal unless current character = (
                    if (mySentence[characterPosition] != '(' ) {
                        // it was a terminal node, move ahead till after )
                        myNode.setType(Main.TERMINAL);
                        // add the word position info in leftSpan and rightSpan fields
                        wordPosition++;
                        myNode.setLeftSpan(wordPosition-1);
                        myNode.setRightSpan(wordPosition);

                        // find the name of the terminal and replace the label of the node
                        // continue till )
                        mySymbol = new StringBuffer();
                        while (mySentence[characterPosition] != ')') {
                            mySymbol.append(mySentence[characterPosition]);
                            if (blnPrintChars) System.out.println(mySentence[characterPosition]);
                            characterPosition++;
                        }
                        // replace label, unless it is one of (, . : `` '' -LRB- or -RRB-),
                        // in which case you delete it!!!
                        if (myNode.getName().equals(",") || myNode.getName().equals(".") || myNode.getName().equals(":") || myNode.getName().equals("``") || myNode.getName().equals("''") || myNode.getName().equals("-LRB-") || myNode.getName().equals("-RRB-") || myNode.getName().equals("$") || myNode.getName().equals("#") || myNode.getName().equals("-NONE-") ) {
                            // remove reference in parent Node
                            currentNode.getChildNodes().remove(myNode);
                            // delete node (cross fingers that it works)
                            myParseTree.getNodes().remove(myNode);
                            wordPosition--;
                        }
                        if (blnPrintChars) System.out.println(mySentence[characterPosition]);
                        characterPosition++; // pass the )
                    } else {
                        // set type to nonTerminal
                        myNode.setType(Main.NONTERMINAL);
                        // make this node the currentNode node
                        currentNode = myNode;
                    }
                    // it was not TOP again
                } else {
                    if (blnPrintChars)
                        System.out.println(mySentence[characterPosition]);
                    characterPosition++; // it was TOP, pass the space
                }
            }
            // end of RHS of rule
            if (mySentence[characterPosition] == ')') {
                // this is the end of all the sister nodes
                // position is now on ), move on till behind the ) 
                if (blnPrintChars)
                    System.out.println(mySentence[characterPosition]);
                characterPosition++; // pass the )
                // update currentNode to parent of current
                currentNode = currentNode.getParentNode();
            }
            // stay with same node
            if (characterPosition < sentence.length()) {
                if (mySentence[characterPosition] == ' ') {
                    if (blnPrintChars) System.out.println(mySentence[characterPosition]);
                    characterPosition++; // pass the space
                }
            }
        }
        return myParseTree;
    }

    /**
     * Converts array of words to a printable sentence.
     */
    public static String arrayToSentence(String[] sentence, int iStart) {
        StringBuffer buff = new StringBuffer();
        for (int i = iStart; i < sentence.length; i++) {
            buff.append(sentence[i]).append(' ');
        }
        return buff.toString();
    }

    public static void getType_Token_Characteristics(String[][] allSentences) throws Exception {
        HashMap<String, Integer> wordFrequencies = new HashMap<String, Integer>();
        HashSet<String[]> uniqueSentences = new HashSet<String[]>();
        // tokenSpectrum: first Integer is token-frequency, second Integer is
        // #types with same token-frequency
        TreeMap<Integer, Integer> tokenSpectrum = new TreeMap<Integer, Integer>();

        // fill in wordFrequencies

        for (String[] mySentence : allSentences) {
            // skip duplicate sentences in word frequency count, because they
            // don't contribute to merges
            if (uniqueSentences.add(mySentence)) {
                for (String myWord : mySentence) {
                    if (wordFrequencies.get(myWord) == null) {
                        wordFrequencies.put(myWord, new Integer(1));
                    } else {
                        wordFrequencies.put(myWord, new Integer(wordFrequencies.get(myWord).intValue() + 1));
                    }
                }
            }
        }

        for (String myWord : wordFrequencies.keySet()) {
            int myTokenFreq = wordFrequencies.get(myWord).intValue();
            if (tokenSpectrum.get(myTokenFreq) == null) {
                tokenSpectrum.put(myTokenFreq, new Integer(1));
            } else
                tokenSpectrum.put(myTokenFreq, new Integer(tokenSpectrum.get(myTokenFreq).intValue() + 1));
        }

        // write to csv file
        BufferedWriter out = new BufferedWriter(new FileWriter("Token_spectrum.csv"));

        // Zipf: x-as is types met token-freq x, y-as is aantal types met diezelfde freq
        // since it is TreeMap token frequencies are ordered
        for (Integer tokenFreq : tokenSpectrum.keySet()) {
            out.write(tokenFreq.intValue() + ", " + tokenSpectrum.get(tokenFreq).intValue());
            out.newLine();
        }
        out.flush();
        out.close();
    }

    public void filterSentences(String[][] allSentences) throws Exception {
        HashMap<String, Integer> wordFrequencies = new HashMap<String, Integer>();
        HashMap<String, Integer> wordFrequenciesOfFilteredSentences = new HashMap<String, Integer>();
        HashSet<String[]> uniqueSentences = new HashSet<String[]>();
        ArrayList<String> labeledSentencesArray = new ArrayList<String>();
        // read in
        BufferedReader buffLabeled = new BufferedReader(new FileReader("./Input/WSJ_labeled_lexical_and_postags_lowercase.txt"));
        String myLabeledSentence=null;

        while ((myLabeledSentence = buffLabeled.readLine()) !=null) {
            labeledSentencesArray.add(myLabeledSentence);
        }

        // fill in wordFrequencies
        for (String[] mySentence : allSentences) {
            // skip duplicate sentences in word frequency count
            if (uniqueSentences.add(mySentence)) {
                for (String myWord : mySentence) {
                    if (wordFrequencies.get(myWord)==null) {
                        wordFrequencies.put(myWord, new Integer(1));
                    } else {
                       wordFrequencies.put(myWord, new Integer(wordFrequencies.get(myWord).intValue() + 1));
                   }
               }
           }
        }
        // now loop again over sentences and check if 80% of words has freq of 3 or more
        // rhsOfSentence = new ArrayList<String>();
        ArrayList<String[]> filteredSentencesArray = new ArrayList<String[]>();
        // TEMP !! XXX
        ArrayList<String> filteredLabeledSentencesArray = new ArrayList<String>();
        int iCounter = 0;
        int nrOfFrequentWords = 0;
        for (String[] mySentence : allSentences) {
            nrOfFrequentWords = 0;
            for (String myWord : mySentence) {
                if (wordFrequencies.get(myWord).intValue() > 3)
                    nrOfFrequentWords++;
            }
            if ((double) nrOfFrequentWords / mySentence.length > .70) {
                filteredSentencesArray.add(mySentence);
                // TEMP !! XXX
                filteredLabeledSentencesArray.add(labeledSentencesArray.get(iCounter));
                // check
                for (String myWord : mySentence) {
                    if (wordFrequenciesOfFilteredSentences.get(myWord)==null) {
                        wordFrequenciesOfFilteredSentences.put(myWord, new Integer(1));
                    } else {
                        wordFrequenciesOfFilteredSentences.put(myWord, new Integer(wordFrequenciesOfFilteredSentences.get(myWord).intValue() + 1));
                    }
                }
            }
            iCounter++;
        }

        // loop over filtered sentences, write to file, and check if 80% still holds
        System.out.println("There are " + filteredSentencesArray.size() + " sentences with 80% or more frequent words.");
        BufferedWriter out = new BufferedWriter(new FileWriter(Main.OUTPUT_DIRECTORY + "/" + "WSJ_lexical_filtered.txt"));
        // TEMP XXX 
        BufferedWriter out2 = new BufferedWriter(new FileWriter(Main.OUTPUT_DIRECTORY + "/" + "WSJ_lexical_filtered_labeled.txt"));
        int nrOfSentencesWithFrequentWords = 0;

        // TEMP XXX
        iCounter = 0;
        for (String[] mySentence : filteredSentencesArray) {
            out.write(arrayToSentence(mySentence, 0) + ".");
            out.newLine();

            // TEMP XXX
            out2.write(filteredLabeledSentencesArray.get(iCounter));
            out2.newLine();
            iCounter++;

            // double check
            nrOfFrequentWords = 0;
            for (String myWord : mySentence) {
                if (wordFrequenciesOfFilteredSentences.get(myWord).intValue() > 3) nrOfFrequentWords++;
            }
            if ((double) nrOfFrequentWords / mySentence.length > .70)
                nrOfSentencesWithFrequentWords++;
        }
        System.out.println("Of those " + nrOfSentencesWithFrequentWords + " sentences still have 80% or more frequent words after filtering.");
        out.flush();
        out.close();
        // TEMP XXX
        out2.flush();
        out2.close();
    }
}
