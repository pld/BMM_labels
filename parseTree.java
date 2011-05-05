/*
 * parseTree.java
 *
 * Created on 1 december 2005, 9:35
 *
 */

package BMM_labels;

/**
 *
 * @author peter
 * @author gideon
 */

import java.util.*;

public class parseTree {
    private ArrayList<Node> associatedNodes;
    public int deepestLevel = 0;
    protected double parseProbability;

    /**
     * Creates a new instance of parseTree 
     */
    public parseTree() {
        this.associatedNodes = new ArrayList<Node>();
        // add TOP rule
        Node topNode = new Node("TOP", null);
        topNode.setType(Main.NONTERMINAL);
        this.associatedNodes.add(topNode);
    }

    // this constructor is for reading in rules from an external grammar and
    // converting them to CNF
    public parseTree(String myLHS) {
        this.associatedNodes = new ArrayList<Node>();
        // add TOP rule
        Node topNode = new Node(myLHS, null);
        topNode.setType(Main.NONTERMINAL);
        this.associatedNodes.add(topNode);
    }

    /**
     * add a rule of the form X->YZ to nonTerminal
     */
    public void addNode(Node myNode) {
        this.associatedNodes.add(myNode);
    }

    public void removeNode(int nrNode) {
        this.associatedNodes.remove(nrNode);
    }

    public Node getNode(int nrNode) {
        // TODO error if nrNode > totalRules
        return this.associatedNodes.get(nrNode);
    }

    public ArrayList<Node> getNodes() {
        return this.associatedNodes;
    }

    /**
     * go counter-clockwise, start with TOP, find left child if no child, then
     * go to sister, if no more sister, then go up one level, go to next child
     * do this until you encounter again the TOP node from the right side, and
     * after you have treated all its children
     */
    public String printToLatex(boolean blnPrintNrSubtrees, boolean blnPrintNodeSpans, String strText) {
        // first set processedChildren of all nodes to 0
        for (Node myNode : this.associatedNodes) {
            myNode.processedChildren = 0;
        }
        StringBuffer parseLatexFmt = new StringBuffer();
        // right of nonterminal: space ]
        // everything except for spaces must be nonterminals.
        // multiwords in Latex commands as \ldots must be in {}

        Node currentNode = this.associatedNodes.get(0);
        boolean allChildrenOfTOPProcessed = false;

        while (allChildrenOfTOPProcessed ==false) {
            // only write [. first time you enter node
            if (currentNode.processedChildren==0){
                if (currentNode.getType()==Main.NONTERMINAL)
                    parseLatexFmt.append(" [.{").append(currentNode.getName());
                else {
                    parseLatexFmt.append(" {");
                    String terminalName = currentNode.getName();
                    // remove _ from name, because Latex doesn't like it
                    if (!Main.INDUCE_FROM_POSTAGS) {
                        terminalName = currentNode.getName().substring(0, currentNode.getName().length()-1);
                    }
                    parseLatexFmt.append(terminalName);
                }
                // {} for multiwords
                if (blnPrintNodeSpans && currentNode.getType()==Main.NONTERMINAL)
                    parseLatexFmt.append(" (" + currentNode.getLeftSpan() + "-" + currentNode.getRightSpan() + ")");
                parseLatexFmt.append("}");
            }
            // (s,[(per,[(ik_,[])]),(vp,[(v,[(wil_,[])]),(up,[(mp,[(mp,[(p,[(van_,[])]),(np,[(voorschoten_,[])])]),(mp,[(p,[(naar_,[])]),(np,[(np,[(den,[(den_,[])]),(haag,[(haag_,[])])]),(np,[(np,[(centraal_,[])]),(n,[(station_,[])])])])]),(zp,[(hallo_,[]),(jij_,[]),(rp,[(daar_,[]),(auto_,[])])])])])])]).
            // if there is an unprocessed child, make it current node and return to start of while-loop
            if (currentNode.getChildNodes().size()>currentNode.processedChildren)
                currentNode = currentNode.getChildNodes().get(currentNode.processedChildren);
            // if there are no more unprocessed children, write closing bracket,
            // and mark node as processed with parent
            else {
                if (currentNode.getType()==Main.NONTERMINAL) parseLatexFmt.append(" ]");
                currentNode.getParentNode().processedChildren++;
                // go to parent node
                currentNode = currentNode.getParentNode();
            }
            // check allChildrenOfTOPProcessed
            if (currentNode.getName().equals("TOP") && currentNode.getChildNodes().size() == currentNode.processedChildren)
                allChildrenOfTOPProcessed = true;
        }
        parseLatexFmt.append(" ]");

        System.out.println("The " + strText + " parse tree is: " + parseLatexFmt.toString());
        return parseLatexFmt.toString();
    }

    /**
     * go counter-clockwise, start with TOP, find left child if no child, then
     * go to sister, if no more sister, then go up one level, go to next child
     * do this until you encounter again the TOP node from the right side, and
     * after you have treated all its children
     */
    public String printOVISFormat(boolean blnWithUnderScore) {
        // first set processedChildren of all nodes to 0
        for (Node myNode : this.associatedNodes) {
            myNode.processedChildren = 0;
        }
        StringBuffer parseOVISFmt = new StringBuffer();

        // (A,[comma-list])
        // (mp,[(morgenavond_,[])]).

        Node currentNode = this.associatedNodes.get(0);
        boolean allChildrenOfTOPProcessed = false;

        while (allChildrenOfTOPProcessed ==false) {
            // only write [. first time you enter node
            if (currentNode.processedChildren==0){
                if (currentNode.getType()==Main.NONTERMINAL) parseOVISFmt.append("(").append(currentNode.getName()).append(",[");
                else //terminal
                    parseOVISFmt.append("(").append(currentNode.getName()).append(blnWithUnderScore?"_":"").append(",[])");
            }
            // OVIS: (s,[(per,[(ik_,[])]),(vp,[(v,[(wil_,[])]),(up,[(mp,[(mp,[(p,[(van_,[])]),(np,[(voorschoten_,[])])]),(mp,[(p,[(naar_,[])]),(np,[(np,[(den,[(den_,[])]),(haag,[(haag_,[])])]),(np,[(np,[(centraal_,[])]),(n,[(station_,[])])])])]),(zp,[(hallo_,[]),(jij_,[]),(rp,[(daar_,[]),(auto_,[])])])])])])]).

            // if there is an unprocessed child, make it current node and return
            // to start of while-loop
            if (currentNode.getChildNodes().size()>currentNode.processedChildren) {
                if (currentNode.processedChildren > 0) parseOVISFmt.append(",");
                currentNode = currentNode.getChildNodes().get(currentNode.processedChildren);
            }
            // if there are no more unprocessed children, write closing bracket,
            // and mark node as processed with parent
            else {
                if (currentNode.getType()==Main.NONTERMINAL) parseOVISFmt.append("])");
                currentNode.getParentNode().processedChildren++;
                // go to parent node
                currentNode = currentNode.getParentNode();
            }

            // check allChildrenOfTOPProcessed
            if (currentNode.getName().equals("TOP") && currentNode.getChildNodes().size() == currentNode.processedChildren)
                allChildrenOfTOPProcessed = true;
        }
        parseOVISFmt.append(")");
        return parseOVISFmt.toString();
    }

    /**
     * go counter-clockwise, start with TOP, find left child if no child, then
     * go to sister, if no more sister, then go up one level, go to next child
     * do this until you encounter again the TOP node from the right side, and
     * after you have treated all its children
     */
    public String printWSJFormat() {
        // first set processedChildren of all nodes to 0
        for (Node myNode : this.associatedNodes) {
            myNode.processedChildren = 0;
        }
        StringBuffer parseWSJFmt = new StringBuffer();

        // (A,[comma-list])
        // (mp,[(morgenavond_,[])]).
        // wordt: (mp (POSTAG morgenavond) )  
        // dus: , --> spatie ; [] niet schrijven, [] wordt herhaling

        Node currentNode = this.associatedNodes.get(0); 
        boolean allChildrenOfTOPProcessed = false;

        while (allChildrenOfTOPProcessed == false) {
            //only write [. first time you enter node
            if (currentNode.processedChildren == 0){
                if (currentNode.getType() == Main.NONTERMINAL)
                    parseWSJFmt.append("(").append(currentNode.getName()).append(" ");
                else //terminal
                    parseWSJFmt.append("(").append(currentNode.getName()).append(")");
            }
            // if there is an unprocessed child, make it current node and return
            // to start of while-loop
            if (currentNode.getChildNodes().size()>currentNode.processedChildren) {
                if (currentNode.processedChildren > 0) parseWSJFmt.append(" ");
                currentNode = currentNode.getChildNodes().get(currentNode.processedChildren);
            }
            // if there are no more unprocessed children, write closing bracket,
            // and mark node as processed with parent
            else {
                if (currentNode.getType()==Main.NONTERMINAL) parseWSJFmt.append(")");
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

    public void printSimple(boolean blnPrintNrSubtrees, boolean blnPrintNodeSpans, String strText) {
        StringBuffer parseSimpleFmt = new StringBuffer();
        parseSimpleFmt.append(strText + ": ");
        for (Node myNode : this.associatedNodes) {
            if (myNode.getType()==Main.NONTERMINAL && (myNode.getRightSpan()-myNode.getLeftSpan() > 1))
                parseSimpleFmt.append(" (" + myNode.getLeftSpan() + "-" + myNode.getRightSpan() + ")  ");
        }
        System.out.println(parseSimpleFmt.toString());
    }

    public String printSpans() {
        StringBuffer parseSpans = new StringBuffer();
        for (Node myNode : this.associatedNodes) {
            if (myNode.getType()==Main.NONTERMINAL && (myNode.getRightSpan()-myNode.getLeftSpan() > 1))
                parseSpans.append(myNode.getLeftSpan() + "-" + myNode.getRightSpan() + " ");
        }
        System.out.println(parseSpans);
        return parseSpans.toString();
    }

    public void calculateNodeDepth() {
        int myDeepestLevel = 0;
        int iLabel = 0;

        for (Node myNode : this.associatedNodes) {
            iLabel++;
            // recursively find parent node, until you encounter TOP
            Node tempNode = myNode;
            int depth = 0;
            while (!(tempNode.getName().equals("TOP"))) {
                depth++;
                tempNode = tempNode.getParentNode();
            }
            myNode.setDepth(depth);
            myNode.setLabel(iLabel);
            if (depth>myDeepestLevel) myDeepestLevel = depth;
        }
        this.deepestLevel = myDeepestLevel;
    }

    public void calculateNodeSpans() {
        // start with deepest level and climb up the tree
        for (int currentDepth = this.deepestLevel; currentDepth >= 0; currentDepth--) {
            for (Node myNode : this.associatedNodes) {
                if (myNode.getDepth() == currentDepth) {
                    // if node is terminal then span corresponds to word position
                    // if node is nonterminal, then left span is same as for
                    // leftmost child, and right span same as for rightmost child
                    if (myNode.getType() == Main.NONTERMINAL) {
                        if (myNode.getChildNodes().size() == 0)
                            System.out.println("index out of bounds: " + myNode.getName());
                        myNode.setLeftSpan(myNode.getChildNodes().get(0).getLeftSpan());
                        myNode.setRightSpan(myNode.getChildNodes().get(myNode.getChildNodes().size()-1).getRightSpan());
                    }
                    // the span of TERMINALS is already entered with the
                    // initiation of the Terminal nodes 
                }
            }
        }
    }

    public void renameNodes() {
        // start with deepest level and climb up the tree
        for (int currentDepth = this.deepestLevel; currentDepth >=0; currentDepth--) {
            for (Node myNode : this.associatedNodes) {
                if (myNode.getDepth() == currentDepth) {
                    if (myNode.getType() == Main.NONTERMINAL) {
                        if (currentDepth > 0) {
                            StringBuffer newLabel = new StringBuffer();
                            newLabel.append("[");
                            for (Node childNode : myNode.getChildNodes()){
                                newLabel.append(childNode.getName());
                            }
                            newLabel.append("]");
                            myNode.setName(newLabel.toString());
                        }
                        else myNode.setName("TOP");
                    }
                }
            }
        }
    }
}
