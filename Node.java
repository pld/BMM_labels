/*
 * Node.java
 *
 * Created on 1 december 2005, 9:10
 *
 */

package BMM_labels;

/**
 *
 * @author peter
 * @author gideon
 */

import java.util.*;

public class Node {
    private ArrayList<Node> childNodes;
    private String leftHandSide;
    private Node parentNode;
    // 1 = TERMINAL
    // 2=NONTERMINAL
    private int nodeType;
    private int depthOfNode;
    private int nodeLabel;
    private int spanLeft;
    private int spanRight;
    public int processedChildren =0;
    
    /**
     * Creates a new instance of Node 
     */
    public Node(String myLHS, Node myParentNode) {
        this.leftHandSide = myLHS;
        this.childNodes = new ArrayList<Node>();
        this.parentNode = myParentNode;
    }

    public void addChildNode(Node myNode) {
        this.childNodes.add(myNode);
    }

    public Node getParentNode() {
        return this.parentNode;
    }

    public String getName() {
        return this.leftHandSide;
    }

    public void setName(String myName) {
        this.leftHandSide = myName;
    }

    public ArrayList<Node> getChildNodes() {
        return this.childNodes;
    }

    public void removeAllChildNodes () {
        this.childNodes.clear();
    }

    public void setType(int myType) {
       this.nodeType = myType;
    }

    public int getType() {
       return this.nodeType;
    }

    public void replaceChildNode(Node oldNode, Node newNode) {
        for ( Node myNode : this.childNodes) {
            if (oldNode.getName().equals(myNode.getName())) {
                int k = this.childNodes.indexOf(myNode);
                this.childNodes.set(k,  newNode);
            }
        }
    }

    public void replaceParentNode(Node myparentNode) {
        this.parentNode = myparentNode;
    }

    public void setDepth(int myDepth) {
        this.depthOfNode = myDepth;
    }

    public int getDepth() {
        return this.depthOfNode;
    }

    public void setLabel(int myLabel) {
        this.nodeLabel = myLabel;
    }

    public int getLabel() {
        return this.nodeLabel;
    }

    public void replaceLHS(String myLHS) {
        this.leftHandSide = myLHS;
    }

    public int getLeftSpan() {
        return this.spanLeft;
    }

    public void setLeftSpan(int myLeftSpan) {
        this.spanLeft = myLeftSpan;
    }

    public int getRightSpan() {
        return this.spanRight;
    }

    public void setRightSpan(int myRightSpan) {
        this.spanRight = myRightSpan;
    }

    // only for sake of comparing parseTrees (arrays of nodes) after CYK parse
    public boolean equals(Object obj) {
        // so you are not interested in children
        if (!(obj instanceof Node)) {
            return false;
        }
        Node other = (Node) obj;
        if (!(other.leftHandSide.equals(this.leftHandSide))) {
            return false;
        }
        if (!(other.getLeftSpan() == this.getLeftSpan())) {
            return false;
        }
        if (!(other.getRightSpan() == this.getRightSpan())) {
            return false;
        }
        if (!(other.childNodes.equals(this.childNodes))) {
            return false;
        }
        return true;
    }
}
