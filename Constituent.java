package BMM_labels;
/*
 * Constituent.java
 *
 * Created on 24 november 2005, 17:03
 *
 */

/**
 *
 * @author peter
 * @author gideon
 * the children of this class are Terminal and nonTerminal
 */
public abstract class Constituent {
    protected String symbolString;

    /**
     * Creates a new instance of Constituent
     */
    public Constituent() {
    }

    public abstract String getName();

    public abstract boolean equals(Object obj);

    public abstract int hashCode();

}
