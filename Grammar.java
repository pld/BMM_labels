package BMM_labels;
/*
 * Grammar.java
 *
 * Created on 24 november 2005, 16:50
 *
 */

/**
 *
 * @author gideon
 */

import java.util.*;

import BMM_labels.Terminal;



/** 
 * a grammar has an arraylist of Terminals
 * and a HashMap of nonTerminals and their names as hashcode (which contain the production rules inside them
 * it has methods for parsing, generate_all, generate random string
 */
public abstract class Grammar {
    
    protected  HashMap<String, Terminal> Terminals; // = new HashMap<String, Terminal>();
    protected  HashMap<String, nonTerminal> nonTerminals ;
    //protected HashMap<String, HashSet<Rule>> rulesIndexedByRHS = new HashMap<String, HashSet<Rule>>();
    public static int nrTerminalsInGrammar = 0;
    
    /** Creates a new instance of Grammar */
    public Grammar() {
        this.Terminals = new HashMap<String, Terminal>();
        this.nonTerminals = new HashMap<String, nonTerminal>();
        //this.rulesIndexedByRHS = new HashMap<String, HashSet<Rule>>();

        //create TOP (S), and add to list
        nonTerminal myNonTerminal = new nonTerminal("TOP");
        this.nonTerminals.put("TOP", myNonTerminal);
    }
    
   
    
    public String[] GenerateRandomSentence(int maxDepth, int minLength, int maxLength) {
        
        ArrayList<Constituent> mySentence = new ArrayList<Constituent>();
        int depth = 0;
        int nrOfTerminals = 0;
        int nrOfTrials = 0;
        
        while ((nrOfTerminals < minLength || nrOfTerminals > maxLength) && nrOfTrials < 50){
            nrOfTrials++;
              
            //start with S
            mySentence.clear();
            mySentence.add(this.nonTerminals.get("TOP") );

            while(depth <= maxDepth && nrOfTerminals == 0){
                depth++;
                mySentence = GetRandomSuccessor(mySentence);
                nrOfTerminals = OnlyTerminalsInSentence(mySentence);
            }
 
        }
        //check again if sentence contains only terminals (in case thrown out because exceeds depth
            if (OnlyTerminalsInSentence(mySentence)>0) {        
                //convert terminals to strings
                ArrayList<String> outputsentence = new ArrayList<String>();
                //??? is declareren van size probleem?
                //int k = 0;
                for (Constituent myConstituent : mySentence){

                    Terminal tempTerminal = (Terminal) myConstituent;
                    outputsentence.add(tempTerminal.getName());
                    //outputsentence[k] = tempTerminal.getName();
                    //k++;
                    }
               return outputsentence.toArray(new String[0]) ; 
           }
            else return null;
    }
    
    /* if only Terminals in random sentence then returns nr of Terminals
     * otherwise returns 0\
     */
    
    public int OnlyTerminalsInSentence(ArrayList<Constituent> inputsentence){
        int nrOfTerminals = 0;
        for (Constituent myConstituent : inputsentence){
            if(myConstituent instanceof nonTerminal) return 0;
            else nrOfTerminals++;
        }
        return nrOfTerminals ;
    }
    
    /* input ArrayList<Constituent> inputsentence, returns ArrayList<Constituent> outputSentence,
     * which is constructed from the inputsentence by replacing all nonTerminals by their RHS 
     */
    public ArrayList<Constituent> GetRandomSuccessor(ArrayList<Constituent> inputsentence){
        ArrayList<Constituent> outputSentence = new ArrayList<Constituent>();

        //iterate over constituents and find nonTerminals, replaces every nonTerminal in the array
        //with its RHS
        for (Constituent myConstituent : inputsentence){
            
            if(myConstituent instanceof nonTerminal){

                //find out how many branches/rules nonTerminal has
                nonTerminal tempConstituent  = (nonTerminal) myConstituent;
                //pick a random nr between 0 and total nr of rules of nonTerminal
                //??? klopt dit?
                //picks at random one of the rule emanating from LHS nonTerminal
                
                //int iCheck = tempConstituent.countRules();
                //get the rule
                //assert(randomRule < tempConstituent.countRules());
                //System.out.print(tempConstituent.getName() + "; #rules=" + tempConstituent.getRules().size());
                for (String newConstituent : tempConstituent.getRandomRule().getRightHandSide() ){
                    //convert string to either nonterminal or terminal
                    if (this.Terminals.get(newConstituent) != null) {
                        outputSentence.add(this.Terminals.get(newConstituent));
                    }
                    if (this.nonTerminals.get(newConstituent) != null) {
                        outputSentence.add(this.nonTerminals.get(newConstituent));
                    }
                   
                }
            }
            else outputSentence.add(myConstituent);
            
        }
        return outputSentence;
    }
    
    public String[] GetListofTerminals(){
        //String[] myTerminalArray = new String[100];
        ArrayList<String> myTerminalArrayList = new ArrayList<String>();
        //int i = 0;
        for(String key : this.Terminals.keySet()) {
            myTerminalArrayList.add(key);
        }
        
        
        return myTerminalArrayList.toArray(new String[0]);
    }
    
    public String[] GetListofNonTerminals(){
        //String[] myNonTerminalArray = new String[100];
        ArrayList<String> myNonTerminalArrayList = new ArrayList<String>();
        //int i = 0;
        for(String key : this.nonTerminals.keySet()) {
           //myNonTerminalArray[i] =  key;
           //i++;
            myNonTerminalArrayList.add(key);
        }
        return myNonTerminalArrayList.toArray(new String[0]) ;
    }
    
     public ArrayList<ArrayList<String>> GetListofRulesPlusCounts(String myLHS){
        
        ArrayList<ArrayList<String>> ArrayListofRules = new ArrayList<ArrayList<String>>();
        //??? er zijn misschien nonTerminals zonder rule: maar dat mag niet
        ArrayList<Rule> myAssociatedRules = this.nonTerminals.get(myLHS).getRules();
        
        for (Rule myRule : myAssociatedRules){
            ArrayList<String> RuleArray = new ArrayList<String>();
            ArrayListofRules.add(RuleArray);
            
            //populate list of rules
            RuleArray.add(myLHS);
            //add RHS
            for (String myConstituent : myRule.getRightHandSide() ){
                RuleArray.add(myConstituent);
            }
            //add count
            String mycount = "(" + myRule.getCount() + ")";
            RuleArray.add(mycount);
        }
        return ArrayListofRules;
        
    }
     
     /*
    public ArrayList<ArrayList<String>> GetListofRules(String myLHS){
        
        ArrayList<ArrayList<String>> ArrayListofRules = new ArrayList<ArrayList<String>>();
        //??? er zijn misschien nonTerminals zonder rule: maar dat mag niet
        HashSet<Rule> myAssociatedRules = this.nonTerminals.get(myLHS).getRules();
        
        for (Rule myRule : myAssociatedRules){
            ArrayList<String> RuleArray = new ArrayList<String>();
            ArrayListofRules.add(RuleArray);
            
            //populate list of rules
            RuleArray.add(myLHS);
            for (Constituent myConstituent : myRule.getRightHandSide() ){
                RuleArray.add(myConstituent.getName());
                }
        }
        return ArrayListofRules;
        
    }
    */
     
  
    
    public HashSet<Rule> getAllRules(){
        //Set of all rules
        HashSet<Rule> allRules = new HashSet<Rule>();
        
        //loop over all nonTerminals of the grammar, add all the rules from each
        for(nonTerminal nt : nonTerminals.values()){    //from key-value pair
            allRules.addAll(nt.getRules()); //getRules returns ArrayList<Rule>
        }
        
        return allRules;
    }
    
    /*
    public HashSet<Rule> getRulesFromRHS(String myRHS){
        return this.rulesIndexedByRHS.get(myRHS);
    }
    
    
    public String[][] GetListofRules(String myLHS){
        ArrayList<ArrayList<String>> ArrayListofRules = new ArrayList<ArrayList<String>>();
        //String[][] ListofRules = new String[50][];  //array of length 10 of arrayobjects of undefined length
        //??? er zijn misschien nonTerminals zonder rule
        ArrayList<Rule> myAssociatedRules = this.nonTerminals.get(myLHS).getRules();
        /*
         int[][] aMatrix = new int[4][];

	//populate matrix
        for (int i = 0; i < aMatrix.length; i++) {
	    aMatrix[i] = new int[5];	//create sub-array
	    for (int j = 0; j < aMatrix[i].length; j++) {
	        aMatrix[i][j] = i + j;
	    }
        }
       ster/
        int i = 0;
        for (Rule myRule : myAssociatedRules){
            //ListofRules[i] = new String[10];
            ArrayList<String> RuleArray = new ArrayList<String>();
            ArrayListofRules.add(RuleArray);
            
            //populate list of rules
            //ListofRules[i][0]  = myLHS;
            RuleArray.add(myLHS);
            int j = 1;
            for (Constituent myConstituent : myRule.getRightHandSide() ){
                /*
                if (myConstituent instanceof Terminal) {
                    Terminal myTerm = (Terminal) myConstituent;
                    ListofRules[i][j]  = myTerm.getName();
                }
                else {
                    nonTerminal myNonTerm = (nonTerminal) myConstituent;
                    ListofRules[i][j]  = myNonTerm.getName();
                } 
                ster/
                //ListofRules[i][j]  = myConstituent.getName();
                RuleArray.add(myConstituent.getName());
                j++;
                }
            i++;
        }
        //return ListofRules;
        ArrayList<String>[] ListofRules = ArrayListofRules.toArray(new ArrayList<String>()[0]) ;
        return myNonTerminalArrayList.toArray(new String[0]) ;
    }
    */
        
    /*
    public String[][] GenerateEntireLanguage(int depth){
        
    }
    
    public ArrayList<Constituent>[] GenerateAllSuccessors(ArrayList<Constituent>[] inputsentences){
        //iterate over constituents and find nonTerminals
        
    }
    */
    
}
