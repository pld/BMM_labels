package BMM_labels;

import BMM_labels.Constituent;

/*
 * Terminal.java
 *
 * Created on 24 november 2005, 17:24
 *
 */

/**
 *
 * @author peter
 * @author gideon
 */
public class Terminal extends Constituent {

    private String myString;

    /**
     * Creates a new instance of Terminal
     */
    public Terminal(String myWord) {
        this.myString = myWord;
    }

     public String getName() {
        return this.myString;
    }

    public boolean equals(Object obj) {
        if(!(obj instanceof Terminal)) {
            return false;
        }
        Terminal other = (Terminal) obj;
        return this.myString.equals(other.myString);
    }

    public int hashCode() {
        return myString.hashCode();
    }
}
