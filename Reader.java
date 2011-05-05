package BMM_labels;

/**
 *
 * @author peter
 * @author gideon
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;

public class Reader {
    protected static String[][] randomSentences;
    protected static ArrayList<String[]> spanArray = new ArrayList<String[]>();
    protected static ArrayList<String> markedForManualAnnotationArray = new ArrayList<String>();

    // constructor
    public Reader() {
    }

    /**
     * Creates a new instance of Reading 
     */
    public void readSentencesFromTextFile(String myFile, String mySpanFile) throws Exception {
        BufferedReader buffUnlabeled = new BufferedReader(new FileReader(myFile));
        ArrayList<String[]> randomSentencesArray = new ArrayList<String[]>();

        // remember fileName for title
        Induced_Grammar.inputFile = myFile.substring(0,  myFile.length()-4);

        // TEMP XXX !!!
        BufferedReader buffLabeled = null;
        BufferedReader buffSpans = null;
        buffSpans = new BufferedReader(new FileReader(mySpanFile));

        String mySentence, myParsedSentence, mySpanSentence = null;
        boolean blnParseable;
        int myCounter = 0, myCounter2 = 0;
        HashSet<String> distinctWords = null;

        while ((mySentence = buffUnlabeled.readLine()) !=null){
            myCounter++;
            mySpanSentence = buffSpans.readLine();

            // TEMP!!!XXX CUT OFF THE FIRST UNARY SPAN
            if (Main.INDUCE_FROM_POSTAGS) {
               if (!mySpanSentence.equals(""))  {
                    int lengthOfFirstSpan = mySpanSentence.split(" ")[0].length();
                    mySpanSentence = mySpanSentence.substring(lengthOfFirstSpan + 1);
               }
            }

            // cut of the . at the end
            mySentence = mySentence.trim();
            mySentence = mySentence.substring(0, mySentence.length() - 2);

            if (Main.PRINT_DEBUG) {
                System.out.println(mySentence);
            }

            String[] tempSentence = mySentence.split(" ");
            // only accept sentences that have more than one word
            // and not only the same words (like "jumping jumping"
            if (!mySentence.equals("") && !mySpanSentence.equals("") && tempSentence.length > 1) {
                // check if at least 2 different words
                distinctWords = new HashSet<String>();
                for (int k = 0; k < tempSentence.length; k++ ) {
                    distinctWords.add(tempSentence[k]);
                }
                if (distinctWords.size() > 1) {
                    myCounter2++;
                    randomSentencesArray.add(tempSentence);
                    if (Main.PRINT_DEBUG) {
                        System.out.println("reading sentence...>>" + mySentence + "<<");
                        System.out.println("reading spans...>>" + mySpanSentence + "<<");
                    }
                    if (mySpanSentence.equals("")) spanArray.add(null);
                    else {
                        mySpanSentence.trim();
                        spanArray.add(mySpanSentence.split(" "));
                    }
                } else if (Main.PRINT_DEBUG)
                    System.out.println("*****single word sentence: " + mySentence);
            }
        }
        System.out.println("There were " + myCounter + " sentences of which " + myCounter2 + " with two or more distinct words");

        // copy to array String[][]
        randomSentences = randomSentencesArray.toArray(new String[0][]);
    }

    public String[][] getSentences() {
        return randomSentences;
    }

    public static ArrayList<String[]> getSpanArray() {
        return spanArray;
    }
    
    /**
     * this method for reading from .csv is not used presently, but perhaps in future when integrated
     * with bracketer
     * @param myFile
     * @throws Exception
     */
    public void readSentencesAndSpansFromCSVFile(String myFile) throws Exception {
            BufferedReader buffUnlabeled = new BufferedReader(new FileReader(myFile));
            ArrayList<String[]> randomSentencesArray = new ArrayList<String[]>();

            // remember fileName for title
            Induced_Grammar.inputFile = myFile.substring(0,  myFile.length() - 4);

            String mySentence, myUnLabeledSentence, myParsedSentence, mySpanSentence = null;
            boolean blnParseable;
            boolean manualAnnotation;

            int myCounter = 0, myCounter2 = 0;
            HashSet<String> distinctWordsInSentence = null;
            HashSet<String> distinctWordsInCorpus = new HashSet<String>();

            while ((mySentence = buffUnlabeled.readLine()) != null){
                myCounter++;

                myUnLabeledSentence = mySentence.split(";")[0];
                mySpanSentence = mySentence.split(";")[1];

                // cut of the . at the end
                myUnLabeledSentence = myUnLabeledSentence.substring(0, myUnLabeledSentence.length() - 2);

                String[] tempSentence = myUnLabeledSentence.split(" ");
                // only accept sentences that have more than one word
                // and not only the same words (like "jumping jumping"
                if(!(myUnLabeledSentence.equals("") || mySpanSentence.equals("")) && tempSentence.length > 1) {
                    //check if at least 2 differen words
                    distinctWordsInSentence = new HashSet<String>();
                    for (int k=0; k < tempSentence.length; k++ ) {
                        distinctWordsInSentence.add(tempSentence[k]);
                        distinctWordsInCorpus.add(tempSentence[k]);
                    }
                    if (distinctWordsInSentence.size()>1) {
                        myCounter2++;
                        randomSentencesArray.add(tempSentence);

                        // check whether the sentences was marked with  "T" in the third column of .csv file
                        // indicating that is was  annotated manually (outside algo), and therefore must be skipped in bracketing_correction
                        if (mySentence.split(";").length == 3) {  
                            markedForManualAnnotationArray.add(mySentence.split(";")[2]);
                        }
                        if (Main.PRINT_DEBUG) {
                           System.out.println("reading sentence...>>" + myUnLabeledSentence + "<<");
                           System.out.println("reading spans...>>" + mySpanSentence + "<<");
                        }
                        if (mySpanSentence.equals("")) spanArray.add(null);
                        else {
                            // every span of the sentence is suffixed by T (approved) of F (not)
                            if (Main.SPAN_IMPORT_FORMAT_MANUAL) {
                                // the first time spanfile is imported it is formatted as spans -- v: v if manually checked
                                String sentenceChecked = "";
                                if (mySpanSentence.split(";").length==2) {
                                    sentenceChecked = mySpanSentence.split(";")[1];
                                    sentenceChecked.trim();
                                }
                                String spans = mySpanSentence.split(";")[0];

                                spans.trim();
                                String[] singleSpanArray = spans.split(" ");
                                if (sentenceChecked.equals("v")) {
                                    // add T after every span
                                    int i=0;
                                    for (String span : singleSpanArray) {
                                        if (!singleSpanArray[i].equals(""))
                                            singleSpanArray[i] = "T" + singleSpanArray[i];
                                        else System.out.println("the span contains null");
                                        i++;
                                    }
                                }
                                else {
                                    // temp: add F after every span
                                    int i = 0;
                                    for (String span : singleSpanArray) {
                                        if (!singleSpanArray[i].equals(""))
                                            singleSpanArray[i] = "F" + singleSpanArray[i];
                                        else System.out.println("the span contains null");
                                        i++;
                                    }
                                }
                                spanArray.add(singleSpanArray);
                            }
                            else {
                                // the format is T0-2  F4-7 etc
                                // spanArray is global var
                                spanArray.add(mySpanSentence.split(" "));
                            }
                        }
                    }
                    else if (Main.PRINT_DEBUG) {
                        System.out.println("*****single word sentence: " + myUnLabeledSentence);
                    }
                }
            }
            System.out.println("There were " + myCounter + " sentences of which " + myCounter2 + " with two or more distinct words");
            // copy to array String[][]
            randomSentences =  randomSentencesArray.toArray(new String[0][]);
    }
}
