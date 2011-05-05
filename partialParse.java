/*
 * partialParse.java
 *
 * Created on 29 augustus 2006, 13:40
 *
 */

package BMM_labels;

/**
 *
 * @author peter
 * @author gideon
 */
import java.util.*;


public class partialParse {
    protected HashMap<String, Span> originalSpans;
    protected parseTree myParseTree;
    protected ArrayList<String> originalSentence;
    protected int nrConstituentsOfGivenParse =0;
    protected int nrConstituentsOfComputedParse =0;
    protected int nrMatchingConstituents =0;
    protected boolean manualAnnotation = false;
    protected HashMap<String, Integer> inconsistentMergeInformation = new HashMap<String, Integer>();
    protected int countOfOccurrences = 1;
    protected static int conflictNumber =0;

    /** Creates a new instance of partialParse */
    public partialParse(parseTree myParseTree, ArrayList<String> originalRHS) {
        this.myParseTree = myParseTree;
        this.originalSentence = new ArrayList<String>();
        this.originalSentence.addAll(originalRHS);
    }

    // inner class
    class Span {
        public boolean spanProcessed = false;
        public boolean frontSpan = false;
        public boolean spanApproved = false;
        public String spanName = null;
        public int leftSpan = 0;
        public int rightSpan = 0;

        public Span(int leftSpan, int rightSpan, String spanPrefix) {
            this.leftSpan = leftSpan;
            this.rightSpan = rightSpan;
            if (spanPrefix.equals("T")) this.spanApproved = true;
            else this.spanApproved = false;
        }

        public String toString() {
            return "" + this.leftSpan + "-" + this.rightSpan;
        }

        public boolean equals(Object obj){
            if (!(obj instanceof Span)) {
                return false;
            }
            Span other = (Span) obj;
            // distinguish spans in HashSet by their LeftSpan and RightSpan
            if (!(other.leftSpan == this.leftSpan && other.rightSpan == this.rightSpan)) {
                return false;
            }
            return true;
        }

        public int hashCode() {
            return this.leftSpan + 31 * this.rightSpan;
        }
    }

    //inner comparator class
    public class SpanComparator implements Comparator<Span> {
        /** 
         * Creates a new instance of parseTreeComparator
         */
        public SpanComparator() {
        }

        public int compare(Span a, Span b) {
            if (a.leftSpan < b.leftSpan) return -1;
            if (a.leftSpan == b.leftSpan) {
                if (a.rightSpan < b.rightSpan) return -1;
                if (a.rightSpan == b.rightSpan) return 0;
                else return 1;
            }
            return 1;
        }
    }

    public void setSpanName(String whichSpan, String myName) {
        this.originalSpans.get(whichSpan).spanName = myName;
    }

    public void setSpanProcessed(String whichSpan) {
        this.originalSpans.get(whichSpan).spanProcessed = true;
    }

    public void setFrontSpan(String whichSpan) {
        this.originalSpans.get(whichSpan).frontSpan = true;
    }

    public void unsetFrontSpan(String whichSpan) {
        this.originalSpans.get(whichSpan).frontSpan = false;
    }

    public void setApprovalOfSpan(String whichSpan) {
        this.originalSpans.get(whichSpan).spanApproved = true;
    }

    public boolean getApprovalOfSpan(String whichSpan) {
        return this.originalSpans.get(whichSpan).spanApproved;
    }

    public void addNode(Node myNode) {
        this.myParseTree.addNode(myNode);
    }

    public void removeNode(int nrNode) {
        this.myParseTree.removeNode(nrNode);
    }

    public Node getNode(int nrNode) { 
        return this.myParseTree.getNode(nrNode); 
    }

    public ArrayList<Node> getNodes() {
        return this.myParseTree.getNodes();
    }

    public parseTree getTree() {
        return this.myParseTree;
    }

    public ArrayList<String> getSpans() {
        ArrayList<String> mySpans = new ArrayList<String>();
        for (String spanString: this.originalSpans.keySet()) {
            // only return spans with width >= 2
            if (this.originalSpans.get(spanString).rightSpan - this.originalSpans.get(spanString).leftSpan >= 2) {
                mySpans.add(spanString);
            }
        }
        return mySpans;
    }

    public ArrayList<String> getSpansPlusApproval() {
        ArrayList<String> mySpans = new ArrayList<String>();
        for (String spanString: this.originalSpans.keySet()) {
            // only return spans with width >= 2
            if (this.originalSpans.get(spanString).rightSpan - this.originalSpans.get(spanString).leftSpan >= 2) {
                if (this.originalSpans.get(spanString).spanApproved)
                    mySpans.add("T" + spanString);
                else mySpans.add("F" + spanString);
            }
        }
        return mySpans;
    }

    public ArrayList<String> getOriginalSentence() {
        return this.originalSentence;
    }

    public String reconstructOriginalSentence() {
        StringBuffer mySentence = new StringBuffer();
        for (String aWord : this.originalSentence) {
            mySentence.append(aWord.substring(1).toLowerCase() + " ");
        }
        mySentence.append(".");
        return mySentence.toString();
    }

    public String reconstructOriginalSpans() {
        // sort the spans
        int leftSpan, rightSpan;
        ArrayList<String> sortedSpans = new ArrayList<String>();
        for (String aSpan : this.originalSpans.keySet()) {
            // don't print the preterminal spans
            leftSpan = java.lang.Integer.parseInt(aSpan.split("-")[0]);
            rightSpan = java.lang.Integer.parseInt(aSpan.split("-")[1]);
            if ((rightSpan-leftSpan)>1) sortedSpans.add(aSpan);
        }
        Collections.sort(sortedSpans);
        StringBuffer mySpans = new StringBuffer();
        for (String aSpan : sortedSpans) {
            mySpans.append(aSpan + " ");
        }
        return mySpans.toString();
    }

    public HashMap<String, Integer> getInconsistentMergeInformation() {
        return this.inconsistentMergeInformation;
    }

    public void setInconsistentMergeInformation(String myRule1, String myRule2, boolean addOne) {
        // remove null entry
        this.inconsistentMergeInformation.remove("");
        if (addOne)
            this.inconsistentMergeInformation.put(myRule1, ++conflictNumber);
        else this.inconsistentMergeInformation.put(myRule1, conflictNumber);
    }

    public int getNrConstituentsOfGivenParse() {
        return this.nrConstituentsOfGivenParse;
    }

    public int getNrConstituentsOfComputedParse() {
        return this.nrConstituentsOfComputedParse;
    }

    public int getNrMatchingConstituents() {
        return this.nrMatchingConstituents;
    }

    public void setNrConstituentsOfGivenParse(int myInt) {
        this.nrConstituentsOfGivenParse = myInt;
    }

    public void setNrConstituentsOfComputedParse(int myInt) {
        this.nrConstituentsOfComputedParse = myInt;
    }

    public void setNrMatchingConstituents(int myInt) {
        this.nrMatchingConstituents = myInt;
    }

    public int getCount() {
        return this.countOfOccurrences;
    }

    public void setCount(int myInt) {
        this.countOfOccurrences = myInt;
    }

    public void setMarkForManualAnnotation() {
        this.manualAnnotation = true;
    }

    public boolean getMarkForManualAnnotation() {
        return this.manualAnnotation;
    }

    /* this method returns the number of matching constituents between the two
     * trees LP and LR are not calculated here
     */
    public int doUPARSEVAL() {
        HashSet<String> listOfUniqueBrackets = new HashSet<String>();
        String computedSpan =null;
        // iterate over nodes of both trees
        for(Node nodeOfTree1 : this.getNodes()) {
            computedSpan = nodeOfTree1.getLeftSpan() + "-" + nodeOfTree1.getRightSpan();
            // skip terminals, include TOP 
            // if (nodeOfTree1.getChildNodes().size()>1 ) { //WRONG! : that excludes TOP!!!
            if (nodeOfTree1.getType() == Main.NONTERMINAL && (nodeOfTree1.getRightSpan() - nodeOfTree1.getLeftSpan()) > 1) {
                for(String givenSpan : this.originalSpans.keySet()) {
                    // check spans
                    if (computedSpan.equals(givenSpan)) {
                        listOfUniqueBrackets.add(computedSpan);
                        break;
                    }
                }
            }
        }
        this.nrMatchingConstituents = listOfUniqueBrackets.size();
        return listOfUniqueBrackets.size();
    }

    /**
     * update from CF (list of exposed bigrams): per PP: for each 
     * unprocessed span in originalSpans, find combination of 2 or more
     * processed spans in old CF where rightSpan is equal to another leftSpan
     * and min(leftSpan) - max(rightSpan) = unprocessed span
     * in method (in: original, old CF, from: new CF), of TEST method!
     * PROBLEM because Stolcke can only be binary: use closest bigram
     * warn on cross-bracketing error
     */
    public String computeNewCF() {
        int leftSpan1 = 0, rightSpan1 = 0, leftSpan2 = 0, rightSpan2 = 0;
        boolean blnCrossBracketing = false;
        for (String mySpan1 : this.originalSpans.keySet() ) {
            leftSpan1 = this.originalSpans.get(mySpan1).leftSpan;
            rightSpan1 = this.originalSpans.get(mySpan1).rightSpan;
            for (String mySpan2 : this.originalSpans.keySet() ) {
                leftSpan2 = this.originalSpans.get(mySpan2).leftSpan;
                rightSpan2 = this.originalSpans.get(mySpan2).rightSpan;
                if (leftSpan2<leftSpan1 && rightSpan2>leftSpan1 && rightSpan2<rightSpan1) blnCrossBracketing=true;
                if (leftSpan2>leftSpan1 && leftSpan2<rightSpan1 && rightSpan2>rightSpan1 ) blnCrossBracketing=true;
            }
        }
        if (blnCrossBracketing)
            System.out.println(">>>>BUG!!!! CROSS_BRACKETING FOR SPANS " + this.getSpansPlusApproval());
        StringBuffer constituentFrontBuffer = new StringBuffer();

        // put front spans in array and sort it by means of comparator interface
        ArrayList<Span> frontSpans = new ArrayList<Span>();
        for (String frontSpanKey : this.originalSpans.keySet() ) {
           // if it is a frontspan: the spans that correspond to constituents of
           // current S-rule
           if (this.originalSpans.get(frontSpanKey).frontSpan)
               frontSpans.add(this.originalSpans.get(frontSpanKey));
        }

        Collections.sort(frontSpans, new SpanComparator());
        // same for unprocessed spans
        ArrayList<Span> unprocessedSpans = new ArrayList<Span>();
        for (String unprocessedSpanKey : this.originalSpans.keySet() ) {
           if (!this.originalSpans.get(unprocessedSpanKey).spanProcessed)
               unprocessedSpans.add(this.originalSpans.get(unprocessedSpanKey));
        }
        Collections.sort(unprocessedSpans, new SpanComparator());
        if (Main.PRINT_DEBUG) {
            System.out.println("ordered front spans are:" + frontSpans);
            System.out.println("ordered unprocessed spans are:" + unprocessedSpans);
        }

        // move through front, and find Span of two neighbours in front
        // for example, front is {<0,2>, <2,5>, <5,6>, <6,9>} : combine any two neighbours
        // but now also combine any 3 neighbors, or 4 neighbors.
        // then check if there is any unprocessed span that fits to leftSpan of
        // L neighbor - righSpan of R nb

        // findBigrams: add 2, find triGrams, findQuartograms.
        for (int frontPosition = 0; frontPosition < frontSpans.size()-1; frontPosition++) {
            boolean matchFound = false;
            boolean cancelMatch = false;
            int leftFront=0, rightFront=0;
            // only consider unprocessed
            for (Span unprocessedSpan : unprocessedSpans) {
                if (unprocessedSpan.leftSpan==frontSpans.get(frontPosition).leftSpan && unprocessedSpan.rightSpan==frontSpans.get(frontPosition+1).rightSpan)
                    matchFound = true;
            }
            if (matchFound) constituentFrontBuffer.append("2");
            else {
                // look for trigrams (starting on same position)
                // trigrams: consider 3 consecutive neighbors: there must be
                // space for them from current position
                if (Main.INDUCE_MULTIGRAMS && frontPosition < frontSpans.size() - 2) {
                    leftFront = frontSpans.get(frontPosition).leftSpan;
                    rightFront = frontSpans.get(frontPosition+2).rightSpan;
                    // only consider unprocessed
                    for (Span unprocessedSpan : unprocessedSpans) {
                        // the middle frontspan, at position frontPosition+1 is
                        // always consecutive to the left and right frontspan
                        if (unprocessedSpan.leftSpan==leftFront && unprocessedSpan.rightSpan==rightFront)
                            matchFound = true;
                        // but if there is another unprocessed span whose either
                        // left- or right-span is between frontSpans.get(frontPosition).leftSpan and frontSpans.get(frontPosition+3).rightSpan
                        // then it must be done first, so it destroys the match
                        if ((unprocessedSpan.leftSpan>leftFront && unprocessedSpan.leftSpan<rightFront) || (unprocessedSpan.rightSpan>leftFront && unprocessedSpan.rightSpan<rightFront))
                            cancelMatch = true;
                    }
                    if (matchFound && !cancelMatch)
                        constituentFrontBuffer.append("3");
                    else {
                        // look for quartograms
                        matchFound = false;
                        // quartograms: consider 4 consecutive neigbors: there
                        // must be space for them from current position
                        if (frontPosition < frontSpans.size() - 3) {
                            if (Main.PRINT_DEBUG)
                                System.out.println("frontSpan in position " + frontPosition + ": " + frontSpans.get(frontPosition).leftSpan + "-" + frontSpans.get(frontPosition).rightSpan + "; frontSpan in position " + (frontPosition+3) + ": " + frontSpans.get(frontPosition+3).leftSpan + "-" + frontSpans.get(frontPosition+3).rightSpan);
                            leftFront = frontSpans.get(frontPosition).leftSpan;
                            rightFront = frontSpans.get(frontPosition+3).rightSpan;
                            // only consider unprocessed
                            cancelMatch = false;
                            for (Span unprocessedSpan : unprocessedSpans) {
                                if (Main.PRINT_DEBUG)
                                    System.out.println("unprocessedSpan : " + unprocessedSpan.leftSpan + "-" + unprocessedSpan.rightSpan);
                                //t he middle frontspan, at position
                                // frontPosition + 1 is always consecutive to
                                // the left and right frontspan
                                if (unprocessedSpan.leftSpan == leftFront && unprocessedSpan.rightSpan == rightFront) {
                                    matchFound = true;
                                }
                                // but if there is another unprocessed span
                                // whose either left- or right-span is between
                                // frontSpans.get(frontPosition).leftSpan and frontSpans.get(frontPosition+3).rightSpan
                                // then it must be done first, so it destroys the match
                                if ((unprocessedSpan.leftSpan>leftFront && unprocessedSpan.leftSpan<rightFront) || (unprocessedSpan.rightSpan>leftFront && unprocessedSpan.rightSpan<rightFront))
                                    cancelMatch = true;
                            }
                            if (Main.PRINT_DEBUG)
                                System.out.println("matchFound=" + matchFound + "; cancelMatch=" + cancelMatch);
                            if (matchFound && !cancelMatch) constituentFrontBuffer.append("4");
                            else matchFound = false;
                        }
                    }
                }
            }
            if (!matchFound) constituentFrontBuffer.append("0");
        }
        if (Main.PRINT_DEBUG)
            System.out.println(">>>>>>>the new constituent front=" + constituentFrontBuffer.toString());
        return constituentFrontBuffer.toString();
    }

    /**
     * go counter-clockwise, start with TOP, find left child if no child, then
     * go to sister, if no more sister, then go up one level, go to next child
     * do this until you encounter again the TOP node from the right side, and
     * after you have treated all its children
     */
    public String printWSJFormat() {
        // first set processedChildren of all nodes to 0
        for (Node myNode : this.myParseTree.getNodes()) {
            myNode.processedChildren = 0;
        }
        StringBuffer parseWSJFmt = new StringBuffer();
        Node currentNode = this.myParseTree.getNode(0);
        boolean allChildrenOfTOPProcessed = false;

        while (allChildrenOfTOPProcessed ==false) {
            // only write [. first time you enter node
            if (currentNode.processedChildren==0){
                // if (currentNode.getType()==Main.NONTERMINAL): there are only nonT, also in OVIS
                parseWSJFmt.append("(").append(currentNode.getName()).append(" ");
            }

            // if there is an unprocessed child, make it current node and return
            // to start of while-loop
            if (currentNode.getChildNodes().size()>currentNode.processedChildren) {
                if (currentNode.processedChildren > 0) parseWSJFmt.append(" ");
                currentNode = currentNode.getChildNodes().get(currentNode.processedChildren);
            } else {
                // if there are no more unprocessed children, write closing bracket,
                // and mark node as processed with parent
                parseWSJFmt.append(")");
                currentNode.getParentNode().processedChildren++;
                // go to parent node
                currentNode = currentNode.getParentNode();
            }
            // check allChildrenOfTOPProcessed
            if (currentNode.getName().equals("TOP") && currentNode.getChildNodes().size() == currentNode.processedChildren)
                allChildrenOfTOPProcessed = true;
        }
        parseWSJFmt.append(")");
        return parseWSJFmt.toString();
    }

    /**
     * only for sake of comparing parseTrees (arrays of nodes) after CYK parse
     */
    public boolean equals(Object obj) {
        // so you are not interested in children
        if(!(obj instanceof partialParse)) {
            return false;
        }
        partialParse other = (partialParse) obj;
        // only criterion is original sentence
        if(!(other.originalSentence.equals(this.originalSentence))) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return originalSentence.hashCode();
    }
}
