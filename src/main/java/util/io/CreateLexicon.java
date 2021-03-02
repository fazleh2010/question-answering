/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author elahi
 */
public class CreateLexicon {
    private static String inputDir = "src/main/resources/test/input/";
    public static String lemonTtl = "test.ttl";
    private static String NounPPFrameTemplate = "@prefix :        <http://localhost:8080/lexicon#> .\n"
            + "\n"
            + "@prefix lexinfo: <http://www.lexinfo.net/ontology/2.0/lexinfo#> .\n"
            + "@prefix lemon:   <http://lemon-model.net/lemon#> .\n"
            + "\n"
            + "@base            <http://localhost:8080#> .\n"
            + "\n"
            + ":lexicon_en a    lemon:Lexicon ;\n"
            + "  lemon:language \"en\" ;\n"
            + "  lemon:entry    :capital_of ;\n"
            + "  lemon:entry    :of .\n"
            + "\n"
            + ":capital_of a          lemon:LexicalEntry ;\n"
            + "  lexinfo:partOfSpeech lexinfo:noun ;\n"
            + "  lemon:canonicalForm  :capital_form ;\n"
            + "  lemon:synBehavior    :capital_of_nounpp ;\n"
            + "  lemon:sense          :capital_sense_ontomap .\n"
            + "\n"
            + ":capital_form a    lemon:Form ;\n"
            + "  lemon:writtenRep \"WRITTENREP\"@en .\n"
            + "\n"
            + ":capital_of_nounpp a           lexinfo:NounPPFrame ;\n"
            + "  lexinfo:copulativeArg        :arg1 ;\n"
            + "  lexinfo:prepositionalAdjunct :arg2 .\n"
            + "\n"
            + ":capital_sense_ontomap a lemon:OntoMap, lemon:LexicalSense ;\n"
            + "  lemon:ontoMapping      :capital_sense_ontomap ;\n"
            + "  lemon:reference        REFERENCE ;\n"
            + "  lemon:subjOfProp       :arg2 ;\n"
            + "  lemon:objOfProp        :arg1 ;\n"
            + "  lemon:condition        :capital_condition .\n"
            + "\n"
            + ":capital_condition a   lemon:condition ;\n"
            + "  lemon:propertyDomain DOMAIN ;\n"
            + "  lemon:propertyRange  RANGE .\n"
            + "\n"
            + ":arg2 lemon:marker :of .\n"
            + "\n"
            + "## Prepositions ##\n"
            + "\n"
            + ":of a                  lemon:SynRoleMarker ;\n"
            + "  lemon:canonicalForm  [ lemon:writtenRep \"of\"@en ] ;\n"
            + "  lexinfo:partOfSpeech lexinfo:preposition .\n"
            + "";

    public CreateLexicon() {

    }

    private static String modifyString(String kb) {
        if (kb.contains("dbo:")) {
            kb = kb.replace("dbo:", "").trim();
            kb = "<http://dbpedia.org/ontology/" + kb + ">";
        }
        else if (kb.contains("xsd:double")) {
            kb = "<http://www.w3.org/2001/XMLSchema#" + kb + ">";
        }
        return kb;
    }
    
      private static void stringToFiles(String str, String fileName) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(str);
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(CreateLexicon.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
      
       public static void main(String args[]) {
        String WRITTENREP = "WRITTENREP";
        String REFERENCE = "REFERENCE";
        String DOMAIN = "DOMAIN";
        String RANGE = "RANGE";
        String[] kbs = {"discharge", "dbo:discharge", "dbo:Stream", "xsd:double"};
        String[] modifyKbs = new String[3];

        Integer index = 0;
        String str = NounPPFrameTemplate;
        String ttlFile=inputDir+lemonTtl;
        for (String kb : kbs) {
            String line = null;
            if (index == 0) {
                line = kbs[index].trim();
                str = str.replace(WRITTENREP, line);
            } else if (index == 1) {
                line = modifyString(kbs[index]);
                str = str.replace(REFERENCE, line);
            } else if (index == 2) {
                line = modifyString(kbs[index]);
                str = str.replace(DOMAIN, line);
            } else if (index == 3) {
                line = modifyString(kbs[index]);
                str = str.replace(RANGE, line);
            }
            index = index + 1;
        }
        stringToFiles(str,ttlFile);

    }


}
