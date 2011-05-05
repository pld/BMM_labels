package BMM_labels;
/*
 * nonTerminal.java
 *
 * Created on 24 november 2005, 19:45
 */

/**
 *
 * @author peter
 * @author gideon
 */
import java.util.*;

import BMM_labels.Utils;

/** 
 * a nonTerminal has a name, and a set of rules associated with it
 * the rules have a fixed order, according to which the nonTerminal branches
 * (expands) in parsing
 * extends Constituent, so you can combine nonTerminals with Terminals
 */
public class nonTerminal extends Constituent implements Cloneable {
    private String nonTerminalName;
    private ArrayList<Rule> associatedRules;
    private double logLikelihood = 0d;
    private double logDirichletPrior = 0d;
    private double logPoissonPart = 0d;
    private int symbolsInRHS = 0;
    private int nonTerminalsInRHS = 0;
    private int totalRuleCount = 0;

    /**
     * Creates a new instance of nonTerminal with given capitalized name
     * (at initialization of grammar: every word gets a category) 
     */
    public nonTerminal(String myName) {
        this.nonTerminalName = myName;
        this.associatedRules = new ArrayList<Rule>();
    }

    /**
     * add a rule of the form X->YZ to nonTerminal, after chunking two nonTerminals together 
     */
    public Rule addRule(String cat1, String cat2) {
        Rule mynewRule = new Rule(cat1, cat2);
        this.associatedRules.add(mynewRule );
        return mynewRule;
    }

    /**
     * add a rule of the form X->a to nonTerminal
     */
    public void addRule(String word) {
        this.associatedRules.add(new Rule(word));
    }

    /**
     * add a rule of the form X->any ArrayList of Constituents to nonTerminal 
     */
    public Rule addRule(ArrayList<String> constituentArray) {
        Rule mynewRule = new Rule(constituentArray);
        this.associatedRules.add(mynewRule);
        return mynewRule;
    }

    /**
     * iterates over all associated rules and substitutes constituents 
     */
    public void substituteConstituentsinRules(String oldConstituent, String newConstituent) {
        for (Rule myRule : this.associatedRules) {
            myRule.substituteConstituent( oldConstituent,  newConstituent);
        }
    }

    /**
     * iterates over all associated rules and returns count of rules 
     */
    public int countRules() {
        return this.associatedRules.size();
    }

    public void computeTotalRuleCount() {
        int totalCountOfRules = 0;
        for (Rule myRule : this.associatedRules) {
            totalCountOfRules += myRule.getCount();
        }
        this.totalRuleCount = totalCountOfRules;
    }

    public int getTotalRuleCount() {
        return this.totalRuleCount;
    }

    public Rule getRandomRule() {
        // TODO: error if nrRule > totalRules
        int randomRule = (int)(Math.random() * this.associatedRules.size());
        return this.associatedRules.get(randomRule);
    }

    public ArrayList<Rule> getRules() {
        return this.associatedRules;
    }

    public boolean removeDuplicateRules() {
        HashSet<ArrayList<String>> mySet = new HashSet<ArrayList<String>>();
        Rule tempRule = null;
        ArrayList<Rule> duplicateRules = new ArrayList<Rule>();
        int keepTrackOfHowManyRemoved = 0;
        boolean blnRuleRemoved = false;
        int ruleCount = 0;
        for (ListIterator<Rule> it = this.associatedRules.listIterator(); it.hasNext();) {
            tempRule = (Rule) it.next();
            if (!mySet.add(tempRule.getRightHandSide())){
                duplicateRules.add(tempRule);
                blnRuleRemoved =true;
                keepTrackOfHowManyRemoved ++;
                // remove the rule from arraylist
                it.remove();
            }
        }

        if (Main.PRINT_REMOVED_RULES_TO_SCREEN && keepTrackOfHowManyRemoved > 0)
            System.out.println("Removed " + keepTrackOfHowManyRemoved + " rules in nonT: " + this.getName());
        // update te count of remaining rule in arraylist
        StringBuffer rhsBuff ;
        for (Rule myRule : this.associatedRules) {
            // add myRule
            for (Rule myDuplicateRule : duplicateRules) {
                if (myRule.getRightHandSide().equals(myDuplicateRule.getRightHandSide())) {
                    myRule.increaseCount(myDuplicateRule.getCount());
                    // print myDuplicateRule
                    if (Main.PRINT_REMOVED_RULES_TO_SCREEN) {
                        rhsBuff = new StringBuffer();
                        rhsBuff.append(this.nonTerminalName).append("#");
                        for (String myWord : myRule.getRightHandSide()){
                            rhsBuff.append(myWord).append("#");
                        }
                        System.out.println(rhsBuff.toString().substring(0, rhsBuff.toString().length()-1) + "@Stolcke");
                    }
                } 
            }
        }
        return blnRuleRemoved;
    }

    public int getCountOfSymbolsinRHS() {
        return this.symbolsInRHS;
    }

    public int getCountOfNonTerminalsinRHS() {
        return this.nonTerminalsInRHS;
    }
    public String getName() {
        return this.nonTerminalName;
    }

    public void computePriorsAndLikelihoodForNonTerminal(Grammar myGrammar) {
        this.computenrOfSymbolsinRHS();
        this.computenrOfNonTerminalsInRHS(myGrammar);
        if (Main.INCLUDE_POISSON) this.computePoissonPartOfStructurePrior();
        if (Main.INCLUDE_DIRICHLET) this.computelogDirichlet();
        this.computelogLikelihood();
    }
    
    public int computenrOfSymbolsinRHS() {
        int totalConstituents = 0;
        for(Rule myRule : this.associatedRules) {
            totalConstituents += myRule.getRightHandSide().size();
        }
        this.symbolsInRHS = totalConstituents;
        return totalConstituents;
    }

    public int computenrOfNonTerminalsInRHS(Grammar myGrammar) {
        int totalNonTerminalsInRHS = 0;
        for (Rule myRule : this.associatedRules) {
            // check for every symbol of rule
            for (String mySymbol : myRule.getRightHandSide()) {
                if (!myGrammar.Terminals.containsKey(mySymbol)) totalNonTerminalsInRHS++;
            }
            // if size of rule = 1 you may assume it is terminal, except for
            // when LHS=TOP, in which RHS of size 1 is nonT
        }
        this.nonTerminalsInRHS = totalNonTerminalsInRHS;
        return totalNonTerminalsInRHS;
    }

    /**
     * for every rule with k nonTerminals in RHS:
     * 2log(Poisson(k;mu)) + k*2log(nrNonTerminalsInGrammar)
     * for every lexical rule (only terminals in RHS):
     * 2log(Grammar.nrTerminalsInGrammar)
     * 2logX = lnX/ln2; Poisson(k,MU) = epow(-MU)*MUpow(k)/k!
     */
    public double computePoissonPartOfStructurePrior() {
        double logPoisson = 0d;
        double ePOW_mu = Math.exp(-Main.MU);
        double kMin1, k_fac, poisson;
        int k = 0;
        for (Rule myRule : this.getRules()) {
            k = myRule.getRightHandSide().size();
            logPoisson += Utils.PoissonLookupTable.get(new Integer(k));
        }
        this.logPoissonPart = logPoisson;
        // if only lexical rules it should return 0
        return logPoisson;
    }

    public double getPoissonPartOfStructurePrior() {
        return this.logPoissonPart;
    }

    public double computelogLikelihood(){
        // enumerate rules
        int totalCount = 0;
        double logLikelihood = 0d;

        for (Rule myRule : this.getRules()) {
            totalCount += myRule.getCount();
        }

        // temp e-grids approach

        for (Rule myRule : this.getRules())
            logLikelihood += ((double) myRule.getCount()) * (Math.log((double) myRule.getCount())/Math.log(2.) - Math.log((double) totalCount)/Math.log(2.));
        this.logLikelihood = -logLikelihood;
        return -logLikelihood;
    }

    public double getLikelihood() {
        return this.logLikelihood;
    }

    public void setLikelihoodZero() {
        this.logLikelihood=0d;
    }

    /*
     * log P(Theta) = 1/Beta * Sum_i(alfa_i - 1)log Theta_i 
     * Theta_i = rule probability = myRule.getCount()/totalCount
     */
    public double computelogDirichlet() {
        // enumerate rules
        int totalCount = 0;
        int totalRules = this.getRules().size();

        if (totalRules == 1) { 
            this.logDirichletPrior = 0d;
            return 0d;
        }
        double logDirichlet = 0d;

        for (Rule myRule : this.getRules()) {
            totalCount += myRule.getCount();
        }

        for (Rule myRule : this.getRules()) {
            logDirichlet += (Math.log((double) myRule.getCount()) - Math.log((double) totalCount))/Math.log(2.);
        }

        // multiply by alfa_i-1;alfa_i = 1/#rules
        this.logDirichletPrior =  logDirichlet * (1d - ((double) totalRules)) / ((double) totalRules);
        return this.logDirichletPrior;
    }

    public double getDirichlet() {
        return this.logDirichletPrior;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof nonTerminal)) {
            return false;
        }

        nonTerminal other = (nonTerminal) obj;
        if (this.nonTerminalName.equals(other.nonTerminalName) && this.associatedRules.equals(other.associatedRules))
            return true;
        // TODO is this correct?
        return false;
    }

    public int hashCode() {
        return nonTerminalName.hashCode();
    }

    public Object clone() {
        try {
            nonTerminal aobj = (nonTerminal) super.clone();
            aobj.associatedRules = (ArrayList<Rule>) associatedRules.clone();
            return aobj;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }
}
