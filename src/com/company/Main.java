package com.company;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;

public class Main {

    public static void main(String[] args) {

        //These are for testing purposes, when not in IDE mode, I take the args
        String card = "ASYMMETRIK LTD\n" +
                "Mike Smith\n" +
                "Senior Software Engineer\n" +
                "(410)555-1234\n" +
                "msmith@asymmetrik.com\n";

        String card2 = "Foobar Technologies\n" +
                "Analytic Developer\n" +
                "Lisa Haung\n" +
                "1234 Sentry Road\n" +
                "Columbia, MD 12345\n" +
                "Phone: 410-555-1234\n" +
                "Fax: 410-555-4321\n" +
                "lisa.haung@foobartech.com";

        String card3 = "Arthur Wilson\n" +
                "Software Engineer\n" +
                "Decision & Security Technologies\n" +
                "ABC Technologies\n" +
                "123 North 11th Street\n" +
                "Suite 229\n" +
                "Arlington, VA 22209\n" +
                "Tel: +1 (703) 555-1259\n" +
                "Fax: +1 (703) 555-1200\n" +
                "awilson@abctech.com";

        ContactInfo parsed_card = new BusinessCardParser().getContactInfo(args[0]);
        //ContactInfo parsed_card = new BusinessCardParser().getContactInfo(card);

        //I get the values if I wanted to write test, but for this project, I just print it out
        System.out.println("\n==> \n\n"+parsed_card.getPrettyFormat());
    }

}

/**
 * factory class to create ContactInfo class
 */
class BusinessCardParser {

    ContactInfo getContactInfo(String document) {
        ContactInfo card =  new ContactInfo();
        card.setCard(document);
        return card;

    }
}

class ContactInfo {

    String card = null;
    TokenNameFinderModel model = null;


    String getPrettyFormat() {
        return
        "Name: "+getName()+"\n" +
        "Phone: "+getPhoneNumber()+"\n" +
        "Email: "+getEmailAddress()+"\n";

    }

    /**
     * Used to load apache opennlp
     */
    void nlp() {
        try {
            model = new TokenNameFinderModel(new File("en-ner-person.bin"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * make each object into
     */
     void setCard(String document) {
        card = document;
    }

    /**
     * returns the full name of the individual (eg. John Smith, Susan Malick)
     */
    String getName() {
        if(card == null) {
            return "";
        }

        nlp();  //Load the Natual language stuff
        int x;

        NameFinderME name_finder = new NameFinderME(model);

        Tokenizer tokenizer = SimpleTokenizer.INSTANCE;
        String[] lines = card.split("\n");  //I split this so we don't get weird mixtures of the line above or below.  If you give single string, then you get what you deserve
        for(x=0;x<lines.length;x++) {
            String[] tokens = tokenizer.tokenize(lines[x]);
            Span[] nameSpans = name_finder.find(tokens);
            String[] spanns = Span.spansToStrings(nameSpans, tokens);

            if (spanns.length > 0) {  //If we have anything, then the head will work.
                return spanns[0].trim();
            }

        }

        return "";
    }

    //returns the phone number formatted as a sequence of digits
    String getPhoneNumber() {
        if(card != null) { //Simple regex matching, yes I used the internet for this because Google is our friend
            final String PHONE_REGEX = "(?:^|\\D)(\\d{3})[)\\-. ]*?(\\d{3})[\\-. ]*?(\\d{4})(?:$|\\D)";
            Pattern p = Pattern.compile(PHONE_REGEX, Pattern.MULTILINE);
            Matcher m = p.matcher(card);
            if (m.find()) {
                return m.group().trim();
            }
        }
        return "";
    }

    //returns the email address
    String getEmailAddress() {
        if(card != null) {  //Simple regex matching, yes I used the internet because google is our friend
            final String EMAIL_REGEX = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
            Pattern p = Pattern.compile(EMAIL_REGEX, Pattern.MULTILINE);
            Matcher m = p.matcher(card);
            if(m.find()){
                return m.group().trim();
            }
        }

        return "";

    }
}