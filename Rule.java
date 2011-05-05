package BMM_labels;

/*
 * Rule.java
 *
 * Created on 24 november 2005, 21:24
 *
 */

/**
 *
 * @author peter
 * @author gideon
 */
import java.util.*;

public class Rule implements Cloneable {
    private ArrayList<String> rightHandSide;
    private int countOfUse = 1;

    /**
     * Creates a new instance of Rule of the form X -> a
     * @param cat1 LeftHandSide of the rule.
     */

    public Rule(String RHS) {
        this.rightHandSide = new ArrayList<String>();
        this.rightHandSide.add(RHS);
    }

    /**
     *  Creates a new instance of Rule of the form X -> YZ
     *  @param catX LeftHandSide of the rule.
     *  @param catY RightHandSide of the rule.
     *  @param catZ RightHandSide of the rule.
     */
    public Rule(String catY, String catZ) {
        this.rightHandSide = new ArrayList<String>();
        this.rightHandSide.add(catY);
        this.rightHandSide.add(catZ);
    }

    // X -> ArrayList van Constituents (free, for artificial grammar)
    public Rule(ArrayList<String> constituentArray) {
        this.rightHandSide = constituentArray;
    }

    public ArrayList<String> getRightHandSide() {
        return this.rightHandSide;
    }

    public boolean containsConstituent(String myConstituent) {
        if (this.rightHandSide.indexOf(myConstituent) >= 0) return true;
        return false;
    }

    public void substituteConstituent(String oldConstituent, String newConstituent) {
        int k;
        for (int i =0; i< this.rightHandSide.size(); i++) {
            if (this.rightHandSide.get(i).equals(oldConstituent))
                this.rightHandSide.set(i,  newConstituent);
        }
    }

    public int findAndReplaceChunks(String sequenceToBeReplacedByChunk , String newChunkName) {
        // the RHS of the rule is changed in this method, so do it only for cloned rules
        String wordsToBeReplacedByChunk = new String("@" + sequenceToBeReplacedByChunk + "@");
        String beginningOfRule = null;
        String endOfRule = null;
        newChunkName = "@" + newChunkName + "@";
        // make a string out of rule, separated by @
        StringBuffer buffRule = new StringBuffer();
        buffRule.append("@");
        for (String myConstituent : this.rightHandSide) {
            buffRule.append(myConstituent).append("@");
        }
        String ruleString = buffRule.toString();
        // count
        int nrOfReplacements = 0;
        while (ruleString.contains(wordsToBeReplacedByChunk)) {
            nrOfReplacements++;
            // TODO: REPLACE DOESN'T WORK!
            // SO LOOK UP THE FIRST OCCURRENCE OF SUBSTRING, REMOVE IT MANUALLY
            // AND INSERT MANUALLY!
            beginningOfRule = ruleString.substring(0, ruleString.indexOf(wordsToBeReplacedByChunk));
            endOfRule = ruleString.substring(ruleString.indexOf(wordsToBeReplacedByChunk) + wordsToBeReplacedByChunk.length());
            ruleString = beginningOfRule + newChunkName + endOfRule;
        }

        // if there were replacements, then clear the array of the RHS of the rule
        // and create a new RHS array from the string with replacements
        if (nrOfReplacements > 0) {
            // replace all occurrences of the sequenceToBeReplacedByChunk by the newChunkName, and keep count
            this.rightHandSide.clear();
            // take first @ off from ruleString
            ruleString = ruleString.substring(1);
            // split the string back to its constituents
            String[] rhsArray = ruleString.split("@");
            // recreate the RHS of the rule
            for (String myConstit : rhsArray) {
                this.rightHandSide.add(myConstit);
            }
        }
        return nrOfReplacements;
    }

    public double getRuleProbability(nonTerminal associatedNonTerminal) {
        int totalCount = 0;
        // find NonTerminal
        for (Rule myRule : associatedNonTerminal.getRules()) {
            totalCount += myRule.getCount();
        }
        return (double) this.getCount()/totalCount;
    }

    /**
     * returns count of RHS symbols
     */
    public int countSymbolsinRHS() {
        return this.rightHandSide.size();
    }

    public int getCount() {
        return this.countOfUse;
    }

    public void increaseCount() {
         this.countOfUse++;
    }

    public void increaseCount(int step) {
        this.countOfUse += step;
    }

    public void setCount(int ruleCount) {
        this.countOfUse = ruleCount;
    }

    public boolean equals(Object obj) {
        if(!(obj instanceof Rule))
            return false;
        Rule otherRule = (Rule) obj;
        // compare rhs
        if (!(otherRule.rightHandSide.equals(this.rightHandSide)))
            return false;
        return true;
    }

    public Object clone() {
        // TODO check this over
        try {
            Rule aobj = (Rule) super.clone();
            aobj.rightHandSide = (ArrayList<String>) rightHandSide.clone();
            //aobj.leftHandSide = new String(leftHandSide);
            aobj.countOfUse = this.countOfUse;
            return aobj;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    public int hashCode() {
        return this.rightHandSide.hashCode();
    }

    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(" -->"); 
        for(String con : rightHandSide)
            result.append(' ').append(con);
        return result.toString();
    }
}
