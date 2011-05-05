/*
 * MergeBigram.java
 *
 * Created on 12 juni 2006, 16:36
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package BMM_labels;

/**
 *
 * @author peter
 * @author gideon
 */
import java.util.*;

import BMM_labels.Utils;

public class MergeBigram {

    private int totalSymbolsInLHSofOmega3;
    private int totalSymbolsInRHSofOmega3;
    private int totalSymbolsInLHSofOmega1;
    private int totalSymbolsInRHSofOmega1;
    private double totalPoisson;
    private double totalDirichlet;
    private double fourthTerm = 0d;
    // for each LHS there are one or several HashSets (in ArrayList) of dup rules
    private HashMap<String, HashMap<String, HashSet<ArrayList<String>>>> setOfSetsOfDuplicatesWithSameLHS;

    /**
     * Creates a new instance of MergeBigram 
     */
    public MergeBigram() {
        this.setOfSetsOfDuplicatesWithSameLHS = new HashMap<String, HashMap<String, HashSet<ArrayList<String>>>>();
    }

    public int getNrOfDuplicateRules() {
        int totalNrOfDupRules = 0;
        // for every set of duprules subtract one
         for (String myLHS : this.setOfSetsOfDuplicatesWithSameLHS.keySet()) {
            // key is HashMap<String (substitutedRule), HashSet<String>>
            for (String key : this.setOfSetsOfDuplicatesWithSameLHS.get(myLHS).keySet()) {
                totalNrOfDupRules += this.setOfSetsOfDuplicatesWithSameLHS.get(myLHS).get(key).size()-1;
            }
        }
        return totalNrOfDupRules;
    }

    public boolean addRedundantDuplicateRule(ArrayList<String> myDuplicateRule, String substitutedRule, String myLHS) {
        // also put the redundantRule into the set of redundant rules, indexed by substitutedRule
        HashMap<String, HashSet<ArrayList<String>>> mySetOfRedundantRulesWithSameLHS = null;
        HashSet<ArrayList<String>> mySetOfDuprules = null;

        if (this.setOfSetsOfDuplicatesWithSameLHS.get(myLHS)==null) {
            // create HashMap for LHS and add it to setOfSetsOfDuplicatesWithSameLHS
            mySetOfRedundantRulesWithSameLHS = new HashMap<String, HashSet<ArrayList<String>>>();
            this.setOfSetsOfDuplicatesWithSameLHS.put(myLHS, mySetOfRedundantRulesWithSameLHS);
        } else
            mySetOfRedundantRulesWithSameLHS = this.setOfSetsOfDuplicatesWithSameLHS.get(myLHS);

        if (this.setOfSetsOfDuplicatesWithSameLHS.get(myLHS).get(substitutedRule)==null) {
            //create HashSet of duprules
            mySetOfDuprules = new HashSet<ArrayList<String>>();
            mySetOfRedundantRulesWithSameLHS.put(substitutedRule,  mySetOfDuprules);
        }else
            mySetOfDuprules = this.setOfSetsOfDuplicatesWithSameLHS.get(myLHS).get(substitutedRule);

        // add duprule to the set
        return mySetOfDuprules.add(myDuplicateRule);
    }

    public boolean addRedundantDuplicateRuleSimple(ArrayList<String> myDuplicateRule, String substitutedRule, String myLHS) {
        // for the second redundantRule, you already know that HashMaps of LHS
        // and of substitutedRule exist
        HashMap<String, HashSet<ArrayList<String>>> mySetOfRedundantRulesWithSameLHS = this.setOfSetsOfDuplicatesWithSameLHS.get(myLHS);
        HashSet<ArrayList<String>> mySetOfDuprules = this.setOfSetsOfDuplicatesWithSameLHS.get(myLHS).get(substitutedRule);
        
        // add duprule to the set
        return mySetOfDuprules.add(myDuplicateRule);
    }

    public double getTotalPoisson() {
        return this.totalPoisson;
    }

    public double getTotalDirichlet() {
        return this.totalDirichlet;
    }

    public double getFourthTerm() {
        return this.fourthTerm;
    }

    public void dummy() {
    }

     public void computeTotalRemovedSymbolsOfMerge(String myBigramKey) {
        // redundant duplicates are grouped into sets (e.g. X A B, X B A, X A A)
        // to find dup rules eliminate all but one rule of each set
        // for these you must compute totalRemovedSymbols

        // first, create dupRules out of redundant dupRules
        // easiest is first to add all redundant rules, and then remove a single one from each set
        // find totalRemovedSymbols of duplicate rules
        // no poisson: so this is including STOP symbol, and including LHS, but not including LHS of TOP.
        int k;
        this.totalSymbolsInRHSofOmega3=0;
        this.totalSymbolsInLHSofOmega3=0;
        this.totalSymbolsInLHSofOmega1=0;
        this.totalSymbolsInRHSofOmega1=0;

        for (String myLHS : this.setOfSetsOfDuplicatesWithSameLHS.keySet()) {
            for (String substitutedRule : this.setOfSetsOfDuplicatesWithSameLHS.get(myLHS).keySet()) {
                // for every duplicate rule you need to know k = #symbols in RHS (was: k= myRuleString.split("#").length -1;)
                // determine # symbols : myRuleString = LHS#A#B#C#@count
                // NB!!! substitutedRule includes LHS, but ruleArray has NO LHS
                k = substitutedRule.split("#").length - 1;
                //  multiply by #dupRules = #rules in small set -1
                int nrDupRulesInSmallSet = this.setOfSetsOfDuplicatesWithSameLHS.get(myLHS).get(substitutedRule).size() - 1;
                 // determine whether LHS is S (TOP) or other nonT: +1 for STOP,
                 // -1 if LHS=TOP
                if (myLHS.equals("TOP")) {
                    this.totalSymbolsInLHSofOmega1 += nrDupRulesInSmallSet;
                    this.totalSymbolsInRHSofOmega1 += k*nrDupRulesInSmallSet;
                } else {
                    this.totalSymbolsInLHSofOmega3 += nrDupRulesInSmallSet;
                    this.totalSymbolsInRHSofOmega3 += k*nrDupRulesInSmallSet;
                }
            }
        }
    }

    public double computePoissonGainForMerge() {
        double totalPoisson = 0d;
        int k;
        for (String myLHS : this.setOfSetsOfDuplicatesWithSameLHS.keySet()) {
            // key is HashMap<String (substitutedRule), HashSet<String>>
            for (String substitutedRule : this.setOfSetsOfDuplicatesWithSameLHS.get(myLHS).keySet()) {
                //NB!!! substitutedRule includes LHS, but ruleArray has NO LHS
                // determine # symbols : myRuleString = LHS$A$B$C$
                k = substitutedRule.split("#").length -1;
                // multiply by #dupRules = #rules in small set -1
                int nrDupRulesInSmallSet = this.setOfSetsOfDuplicatesWithSameLHS.get(myLHS).get(substitutedRule).size()-1;
                // Poisson is function of #nonT in RHS, which is myHead
                totalPoisson += nrDupRulesInSmallSet*Utils.PoissonLookupTable.get(k);
            }
        }
        // store totalPoisson in mergeBigram; remove old value
        this.totalPoisson = totalPoisson;
        return totalPoisson;
    }

    public double computeDirichletGainForMerge(String myBigramKey, HashMap<String, HashMap<ArrayList<String>, Integer>> ruleCounts) {
        double totalDirichletGain = 0d;
        // identify merge pair
        String mergeA = myBigramKey.split("@")[0];
        String mergeB = myBigramKey.split("@")[1];
        nonTerminal Constituent1 = Induced_Grammar.inputGrammarLookupTable.get(mergeA);
        nonTerminal Constituent2 = Induced_Grammar.inputGrammarLookupTable.get(mergeB);
        nonTerminal originalNonTerminal;

        // Dirichlet Gain for merged nonT
        // create a new nonTerminal w/o any rules, and add to list of grammar
        Rule tempRule;
        nonTerminal mergedNonTerminal = new nonTerminal("dummy");

        // add the rules of the Constituent1 as well as Constituent2 to mergedNonTerminal
        for (Rule myRule : Constituent1.getRules()) {
            tempRule = (Rule) myRule.clone();
            // do substitution
            tempRule.substituteConstituent(mergeA, "dummy");
            tempRule.substituteConstituent(mergeB, "dummy" );
            // and add rules to new nonTerminal
            mergedNonTerminal.getRules().add(tempRule);
        }
        for (Rule myRule : Constituent2.getRules()) {
            tempRule = (Rule) myRule.clone();
            // do substitution
            tempRule.substituteConstituent(mergeA, "dummy" );
            tempRule.substituteConstituent(mergeB, "dummy" );
            // and add rules to new nonTerminal
            mergedNonTerminal.getRules().add(tempRule);
        }

        boolean blnRuleRemoved = mergedNonTerminal.removeDuplicateRules();
        totalDirichletGain += mergedNonTerminal.computelogDirichlet();
        totalDirichletGain -= Constituent1.getDirichlet();
        totalDirichletGain -= Constituent2.getDirichlet();
        
        // DUPLICATES
        // for each LHS there are one or several HashSets (in ArrayList) of dup rules
        // private HashMap<String, ArrayList<HashSet<String>>> setOfSetsOfDuplicatesWithSameLHS;
        int totalRules = 0;
        int totalCountOfRules = 0;
        int sumOfRuleCounts = 0, productOfRuleCounts = 1, ruleCount = 0;
        nonTerminal headNonTerminal = null;
        double newDirichlet = 0d;
        int totalDupRulesWithSameLHS = 0;

        for (String myLHS : this.setOfSetsOfDuplicatesWithSameLHS.keySet()) {
            if (!myLHS.equals(mergeA) && !myLHS.equals(mergeB)) {
                headNonTerminal = Induced_Grammar.inputGrammarLookupTable.get(myLHS);
                if (headNonTerminal == null) System.out.println("NullpointerException for " + myLHS);
                // find totalRules of LHS
                totalRules = headNonTerminal.getRules().size();
                totalDupRulesWithSameLHS = 0;
                // find totalCount of Rules of LHS
                totalCountOfRules = headNonTerminal.getTotalRuleCount();

                // divide by (alpha- 1)= (1-totalRules)/totalRules
                newDirichlet = headNonTerminal.getDirichlet() * ((double) totalRules / ((double) 1. - totalRules));

                // sets of dup rules with this LHS
                HashSet<ArrayList<String>> setOfDupRules = null;
                for (String key : this.setOfSetsOfDuplicatesWithSameLHS.get(myLHS).keySet()) {
                    setOfDupRules = this.setOfSetsOfDuplicatesWithSameLHS.get(myLHS).get(key);
                    totalDupRulesWithSameLHS += setOfDupRules.size() - 1;
                    sumOfRuleCounts = 0;
                    productOfRuleCounts = 1;
                    // loop over dup rules from one set
                    for (ArrayList<String> dupRule : setOfDupRules) {
                        // ruleCount is final part of ruleString behind @: XXX NIET MEER
                        // TODO check this!!!
                        ruleCount = ruleCounts.get(myLHS).get(dupRule).intValue();
                        sumOfRuleCounts += ruleCount;
                        productOfRuleCounts *= ruleCount;
                    }
                    // for each set of dup rules,
                    // Dirichlet/(alfa-1) = Dirichlet/(alfa-1) -log((count1*count2)/(count1+count2)*totCount)
                    newDirichlet -= Math.log(((double) productOfRuleCounts) / ((double) sumOfRuleCounts * Math.pow((double) totalCountOfRules, (double) setOfDupRules.size() - 1.))) / Math.log(2.);
                }
                // multiply by (alfa_new -1) = (1-(rules-dups))/(rules-dups)
                newDirichlet *= ((double) 1. - (totalRules - totalDupRulesWithSameLHS)) / ((double) totalRules - totalDupRulesWithSameLHS);
                // you are with a head nonTerminal that has dup rules; now subtract old Dir from new Dir
                totalDirichletGain += newDirichlet - headNonTerminal.getDirichlet();
            }
        }
        // store totalDirichletGain in mergeBigram; remove old value
        this.totalDirichlet = totalDirichletGain;
        return totalDirichletGain;
    }

    public double computeSecondTermOfStructurePriorGainOfMergeBigram(double logAunt, double logAuntForPoisson, boolean doPrint) {
        double MDLGain = 0d;
        int myTotalSymbolsInLHSofOmega1 = 0, myTotalSymbolsInRHSofOmega1 = 0;

        myTotalSymbolsInLHSofOmega1 = this.totalSymbolsInLHSofOmega1;
        myTotalSymbolsInRHSofOmega1 = this.totalSymbolsInRHSofOmega1;

        // based on grammar size AFTER merge, so everything -1
        // logAunt = log(A_{unt}) = Math.log((double) inputGrammar.size() - 1)/Math.log(2.);
        // including STOP, but not including TOP, because unique nonT of RHS alone
        // logAuntForPoisson = log(A_{unt} - 1) = Math.log((double) inputGrammar.size() - 2)/Math.log(2.); 
        // not including STOP and not including TOP
        if (doPrint) System.out.println("totalSymbolsInLHSofOmega1=" + myTotalSymbolsInLHSofOmega1 + "; totalSymbolsInLHSofOmega3="  + totalSymbolsInLHSofOmega3 + "; totalSymbolsInRHSofOmega1=" + myTotalSymbolsInRHSofOmega1 + "; totalSymbolsInRHSofOmega3=" + totalSymbolsInRHSofOmega3);

        // 2nd term
        // MergeBigram.getTotalRemovedSymbols = Sum(Lj + 1) or (Lj + 2) includes
        // LHS and also includes STOP, but not including LHS of TOP
        // A_{unt} = number of unique nonT, excluding S and the special STOP symbol

        if (!Main.INCLUDE_POISSON) {
            // (Sum_{Omega1} (L_{j} + 1) + Sum_{Omega3} (L_{j} + 2)) * logAunt
            MDLGain = -((double) myTotalSymbolsInLHSofOmega1 + 2 * this.totalSymbolsInLHSofOmega3 + myTotalSymbolsInRHSofOmega1 + this.totalSymbolsInRHSofOmega3) * logAunt;

        } else {
            // Poisson
            // for every duplicate rule remove the STOP symbol
            // (Sum_{Omega1} (L_{j}) + Sum_{Omega3} (L_{j} + 1)) * logAuntForPoisson
            // (1 symbol less in log)
            MDLGain = -((double) this.totalSymbolsInLHSofOmega3 + myTotalSymbolsInRHSofOmega1 + this.totalSymbolsInRHSofOmega3) * logAuntForPoisson;
            // getTotalPoisson is for all dup rules
            // totalPoisson is total contribution of dup rules to Poisson, so
            // you must subtract it
            MDLGain -= this.totalPoisson;
        }
        if (Main.INCLUDE_DIRICHLET) {
            // totalDirichletGain is already difference,
            // (Diri_{after merge} - Diri_{before merge}), so you must add it
            MDLGain += this.totalDirichlet;
        }
        return MDLGain;
    }

    public double computeThirdTermofLikelihoodGainOfMergeBigram(nonTerminal mergeA, nonTerminal mergeB) {
        double likeliHood = 0d, Fx, Fy, Fx_after, Fy_after, log1FxFy, log1FyFx;
        int occurrencesOfA = 0, occurrencesOfB = 0;

        // Sum_{R_{j} \in X} F_{j} * log(F_{j}/F_{Tot} - Sum_{R_{j} \in Y} F_{j} * log(F_{j}/F_{Tot}
        // compute F_{Tot}: F_Tot = Sum_{R_{j} \in X} F_{j} + Sum_{R_{myHead} \in Y} F_{myHead}
        int F_Tot = 0, F_A = 0, F_B = 0;
        for (Rule myRule : mergeA.getRules()) {
            F_Tot += myRule.getCount();
            F_A += myRule.getCount();
        }
        for (Rule myRule : mergeB.getRules()) {
            F_Tot += myRule.getCount();
            F_B += myRule.getCount();
        }
        if (Main.PRINT_DEBUG)
            System.out.println("ComputeThirdTerm: MergeBigram (" + mergeA.getName() + ", " + mergeB.getName() + "); F_tot=" +  F_Tot + "; ");

        // compute likelihood:
        for (Rule myRule : mergeA.getRules()) {
            if (Main.PRINT_DEBUG)
                System.out.println("MergeA count=" + myRule.getCount());
            likeliHood -= myRule.getCount()*Math.log((double) F_A/F_Tot)/Math.log(2.);
        }
        for (Rule myRule : mergeB.getRules()) {
            if (Main.PRINT_DEBUG)
                System.out.println("MergeB count=" + myRule.getCount());
            likeliHood -= myRule.getCount()*Math.log((double) F_B/F_Tot)/Math.log(2.);
        }
       return likeliHood;
    }

    public double computeFourthTermofLikelihoodGainOfMergeBigram(HashMap<String, Integer> frequencyOfNonTerminalsInRHS, HashMap<String, Integer> nrOfRulesOfNonTerminal, HashMap<String, Integer> totalRuleCountForLHSNonTerminal, int sumFRofTOP, HashMap<String, HashMap<ArrayList<String>, Integer>> ruleCounts, ArrayList<nonTerminal> inputGrammar, String myBigramKey, boolean doPrint) {
        double myFourthTerm = 0d;
        String myIndex;
        String mergeA = myBigramKey.split("@")[0];
        String mergeB = myBigramKey.split("@")[1];

        // loop over LHS for the redundant rules, for each LHS X, calculate F_{Tot_count, X}
        // loop over sets with LHS X, for each set calculate F_{Tot, setje}
        // calulate for each set:
        // - F_{set} log (F_{set}/F_{Tot}) + SUM_{Rules R in set} F_{R} log (F_{R}/F_{Tot})

        int F_tot = 0, F_set = 0, F_R = 0;
        nonTerminal headNonTerminal;

        for (String myLHS : this.setOfSetsOfDuplicatesWithSameLHS.keySet()) {
            F_tot = totalRuleCountForLHSNonTerminal.get(myLHS);
            // interaction term: mergeA is merged with mergeB, so F_tot is sum of both
            if (myLHS.equals(mergeA)) {
                F_tot += totalRuleCountForLHSNonTerminal.get(mergeB);
                if (doPrint)
                    System.out.println("XXXXXXFound dup rule of mergeA");
            }
            if (myLHS.equals(mergeB)) { //interaction term: mergeA is merged with mergeB, so F_tot is sum of both
                //for (Rule myRule : Induced_Grammar.inputGrammarLookupTable.get(mergeA).getRules()) {
                //    F_tot += myRule.getCount();
                //}
                F_tot += totalRuleCountForLHSNonTerminal.get(mergeA);
                if (doPrint)
                    System.out.println("XXXXXXFound dup rule of mergeB");
            }
            HashSet<ArrayList<String>> mySetOfDuplicates = null;
            for (String key : setOfSetsOfDuplicatesWithSameLHS.get(myLHS).keySet()) {
               mySetOfDuplicates = setOfSetsOfDuplicatesWithSameLHS.get(myLHS).get(key);
               F_set = 0;
               // - F_{set} log (F_{set}/F_{Tot}) + SUM_{Rules R in set} F_{R} log (F_{R}/F_{Tot})
               for (ArrayList<String> myDupRule : mySetOfDuplicates) {
                   if (doPrint)
                       System.out.println("substitutedRule=" + key + "; myDupRule=" + myDupRule + "; myLHS=" + myLHS);
                   F_R = ruleCounts.get(myLHS).get(myDupRule).intValue();
                   F_set += F_R;
                   // second term
                   myFourthTerm += ((double) F_R) * Math.log((double) F_R / F_tot) / Math.log(2.);
               }
               // for every set Omega, calculate the contribution to  myFourthTerm

               myFourthTerm -= ((double) F_set) * Math.log((double) F_set / F_tot) / Math.log(2.);
            }
        }
        // interaction term: duplicate rules for merged nonT M(A,B): LHS was part
        // of the merge, so there are additional rules
        // find dup rules for M(A,B): those that have LHS A or B: not simply the
        // sum, because merging A and B  may yield dups with different LHS!!!
        // these sets are included in mergeA, even though they have differing LHS
        // (both mergeA and mergeB)
        this.fourthTerm = myFourthTerm;
        return myFourthTerm;
    }

    public int getTotalSymbolsInRHSOfOmega1() {
        return this.totalSymbolsInRHSofOmega1;
    }

    public int getTotalSymbolsInLHSOfOmega1() {
        return this.totalSymbolsInLHSofOmega1;
    }

    
    public int getTotalSymbolsInLHSOfOmega3() {
        return this.totalSymbolsInLHSofOmega3;
    }

    public int getTotalSymbolsInRHSOfOmega3() {
        return this.totalSymbolsInRHSofOmega3;
    }

    public HashMap<String, HashMap<String, HashSet<ArrayList<String>>>> getSetOfSetsOfDuplicatesWithSameLHS() {
        return this.setOfSetsOfDuplicatesWithSameLHS;
    }

    public void setTotalSymbolsInRHSOfOmega1(int totalSymbolsInRHSofOmega1) {
        this.totalSymbolsInRHSofOmega1 = totalSymbolsInRHSofOmega1;
    }

    public void setTotalSymbolsInLHSOfOmega1(int totalSymbolsInLHSofOmega1) {
        this.totalSymbolsInLHSofOmega1 = totalSymbolsInLHSofOmega1;
    }

    public void setTotalSymbolsInLHSOfOmega3(int totalSymbolsInLHSofOmega3) {
        this.totalSymbolsInLHSofOmega3 = totalSymbolsInLHSofOmega3;
    }

    public void setTotalSymbolsInRHSOfOmega3(int totalSymbolsInRHSofOmega3) {
        this.totalSymbolsInRHSofOmega3 = totalSymbolsInRHSofOmega3;
    }

    public void setTotalPoisson(double Poisson) {
        this.totalPoisson = Poisson;
    }

    public void setTotalDirichlet(double Dirichlet) {
        this.totalDirichlet = Dirichlet;
    }

    public void setFourthTerm(double fourthTerm) {
        this.fourthTerm = fourthTerm;
    }

    public void dummy(double fourthTerm) {
    }
}
    