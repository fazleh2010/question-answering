/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grammar.read.questions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author elahi
 */
public class SparqlQuery {
    //https://www.w3.org/TR/rdf-sparql-query/

    private static String endpoint = "https://dbpedia.org/sparql";

    private String objectOfProperty;
    public static String FIND_ANY_ANSWER = "FIND_ANY_ANSWER";
    public static String FIND_LABEL = "FIND_LABEL";
    public String sparqlQuery = null;
    public static String RETURN_TYPE_OBJECT = "objOfProp";
    public static String RETURN_TYPE_SUBJECT = "subjOfProp";
    private String resultSparql = null;
    private String ontology = "http://dbpedia.org/ontology/";

    public SparqlQuery(String entityUrl, String property, String type, String returnType) {
        if (type.contains(FIND_ANY_ANSWER)) {
            if (returnType.contains("objOfProp")) {
                sparqlQuery = this.setSparqlQueryPropertyObject(entityUrl, property);
            } else if (returnType.contains("subjOfProp")) {
                sparqlQuery = this.setSparqlQueryPropertyWithSubject(entityUrl, property);
            }

        } else if (type.contains(FIND_LABEL)) {
            sparqlQuery = this.setSparqlQueryForLabel(entityUrl);
        }
        this.resultSparql = executeSparqlQuery(sparqlQuery);
        parseResult(resultSparql);
    }

    public SparqlQuery(String sparqlQuery) {
        this.resultSparql = executeSparqlQuery(sparqlQuery);
        parseResult(resultSparql);
    }

    private String executeSparqlQuery(String query) {
        String result = null, resultUnicode = null, command = null;
        Process process = null;
        try {
            resultUnicode = this.stringToUrlUnicode(query);
            command = "curl " + endpoint + "?query=" + resultUnicode;
            process = Runtime.getRuntime().exec(command);
            //System.out.print(command);
        } catch (Exception ex) {
            Logger.getLogger(SparqlQuery.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("error in unicode in sparql query!" + ex.getMessage());
            ex.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }
            result = builder.toString();
        } catch (IOException ex) {
            Logger.getLogger(SparqlQuery.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("error in reading sparql query!" + ex.getMessage());
            ex.printStackTrace();
        }
        return result;
    }

    public void parseResult(String xmlStr) {
        Document doc = convertStringToXMLDocument(xmlStr);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
            this.parseResult(builder, xmlStr);
        } catch (Exception ex) {
            Logger.getLogger(SparqlQuery.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("error in parsing sparql in XML!" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private Document convertStringToXMLDocument(String xmlString) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
            return doc;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void parseResult(DocumentBuilder builder, String xmlStr) {

        try {
            Document document = builder.parse(new InputSource(new StringReader(
                    xmlStr)));
            NodeList results = document.getElementsByTagName("results");
            for (int i = 0; i < results.getLength(); i++) {
                NodeList childList = results.item(i).getChildNodes();
                for (int j = 0; j < childList.getLength(); j++) {
                    Node childNode = childList.item(j);
                    if ("result".equals(childNode.getNodeName())) {
                        this.objectOfProperty = childList.item(j).getTextContent().trim();

                    }
                }

            }
        } catch (SAXException ex) {
            Logger.getLogger(SparqlQuery.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("no result after sparql query!" + ex.getMessage());
            return;
        } catch (IOException ex) {
            Logger.getLogger(SparqlQuery.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("no result after sparql query!" + ex.getMessage());
            return;
        }

        //System.out.println("xmlStr!!!!!!!!!!!!!" + xmlStr);
    }

    public String setSparqlQueryPropertyObject(String entityUrl, String property) {
        return "select  ?o\n"
                + "    {\n"
                + "    " + "<" + entityUrl + ">" + " " + "<" + property + ">" + "  " + "?o" + "\n"
                + "    }";

    }

    /*public  String setSparqlQueryPropertyWithSubject(String entityUrl, String property) {
        return "select  ?s\n"
                + "    {\n"
                 + "   " + "?s" + " " + "<" + property + ">" + "  " + "<" +  "http://www.w3.org/2001/XMLSchema#"+entityUrl + ">" + "\n"
                + "    }";

    }*/
    public String setSparqlQueryPropertyWithSubject(String entityUrl, String property) {
        String sparql = null;
        if (entityUrl.contains("http:")) {
            sparql = "select  ?s\n"
                    + "    {\n"
                    + "   " + "?s" + " " + "<" + property + ">" + "  " + "<" + entityUrl + ">" + "\n"
                    + "    }";
        } else {
            sparql = "select  ?s\n"
                    + "    {\n"
                    + "   " + "?s" + " " + "<" + property + ">" + "  " + entityUrl + "\n"
                    + "    }";
        }
        return sparql;

    }

    public static String setSparqlQueryPropertyWithSubjectFilter(String entityUrl, String property) {
        String sparql = null;
        if (entityUrl.contains("http:")) {
            sparql = "select  ?s\n"
                    + "    {\n"
                    + "   " + "?s" + " " + "<" + property + ">" + "  " + "<" + entityUrl + ">" + "\n"
                    + "    }";
        } else {
            sparql = "select  ?s\n"
                    + "    {\n"
                    + "   " + "?s" + " " + "<" + property + ">" + "  " + entityUrl + "\n"
                    + "    }";
        }
        return sparql;

    }

    public static String setSparqlQueryForLabel(String entityUrl) {
        String sparql = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "   PREFIX dbo: <http://dbpedia.org/ontology/>\n"
                + "   PREFIX dbpedia: <http://dbpedia.org/resource/>\n"
                + "\n"
                + "   SELECT DISTINCT ?label \n"
                + "   WHERE {  \n"
                + "       <" + entityUrl + "> rdfs:label ?label .     \n"
                + "       filter(langMatches(lang(?label),\"EN\"))         \n"
                + "   }";

        return sparql;

    }

    public static String setSparqlQueryForTypes(String propertyUrl, String objectUrl) {
        String sparql = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "   PREFIX dbo: <http://dbpedia.org/ontology/>\n"
                + "   PREFIX dbpedia: <http://dbpedia.org/resource/>\n"
                + "\n"
                + "   SELECT DISTINCT ?label \n"
                + "   WHERE {  \n"
                + "   " + "?label" + " " + "<" + propertyUrl + ">" + " " + "<" + objectUrl + ">" + " .     \n"
                + "       filter(langMatches(lang(?label),\"EN\"))         \n"
                + "   }";

        return sparql;

    }

    public static String ATTRIBUTE_ADJECTIVE_sparql(String sparql) throws IOException {
        sparql = sparql.replace("<", "\n" + "<");
        sparql = sparql.replace(">", ">" + "\n");

        Integer index = 0;
        String[] lines = sparql.split(System.getProperty("line.separator"));
        String[] kbs = new String[4];
        for (String line : lines) {
            if (line.contains("<")) {
                line = line.replace("<", "");
                line = line.replace(">", "");
                line = line.replace("http://dbpedia.org/ontology/", "");
                line = line.replace("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "");
                line = line.replace("http://dbpedia.org/resource/", "");

                kbs[index] = line;
                index = index + 1;
            }

        }
        kbs[0] = "<http://dbpedia.org/ontology/" + kbs[0] + ">";
        kbs[1] = "<http://dbpedia.org/resource/" + kbs[1] + ">";
        kbs[2] = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#" + kbs[2] + ">";
        kbs[3] = "<http://dbpedia.org/ontology/" + kbs[3] + ">";

        String newSparqlQuery = "SELECT DISTINCT ?uri WHERE { ?uri " + kbs[2] + " " + kbs[3] + " ; "
                + kbs[0] + " " + kbs[1] + " }";
        return newSparqlQuery;
    }

    
    /*
    On 4/1/21 12:15 PM, Philipp Cimiano wrote:
> I would like the following things to work on the interface:
>
> german cities
>
> cities in germany
>
> What is the largest german city?
>
> What is the biggest german city?
should work if it has binding list Germany. There is property dbo:largestCity
>
> What is the longest german river?
>
> What is the highest spanish mountain?
>
> What is the highest mountain?
>
> What is the largest city in Germany
>
> What is the biggest city in Germany?
>
> What is the longest river in Germany?
>
> What is the highest mountain in Spain?
>
> What is the highest mountain?
The sparql query has to be added to the QueGG system.
>
> Let me know if you need to discuss this.
I think first i try it and then we can discuss.
>
> Best regards, */
    
    
    public static String GRADABLE_ADJECTIVE_sparql(String sparql) {
        sparql = sparql.replace("<", "\n" + "<");
        sparql = sparql.replace(">", ">" + "\n");

        Integer index = 0;
        String[] lines = sparql.split(System.getProperty("line.separator"));
        String[] kbs = new String[4];
        for (String line : lines) {
            if (line.contains("<")) {
                line = line.replace("<", "");
                line = line.replace(">", "");
                line = line.replace("http://dbpedia.org/ontology/", "");
                line = line.replace("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "");
                line = line.replace("http://dbpedia.org/resource/", "");

                kbs[index] = line;
                index = index + 1;
            }

        }
        System.out.println("kbs[0]:"+kbs[0]);
        System.out.println("kbs[1]:"+kbs[1]);
        System.out.println("kbs[2]:"+kbs[2]);
        kbs[0] = "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type/" + kbs[0] + ">";
        kbs[1] = "<http://dbpedia.org/ontology/" + kbs[1] + ">";
        kbs[2] = "<http://dbpedia.org/ontology/" + kbs[2] + ">";
        
        String newSparqlQuery = "SELECT DISTINCT ?uri WHERE {  ?uri <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Mountain> .  ?uri <http://dbpedia.org/ontology/height> ?num . } ORDER BY DESC(?num) OFFSET 0 LIMIT 1 ";
        //String  newSparqlQuery = "SELECT DISTINCT ?uri WHERE {  ?uri "+ kbs[0]+" "+kbs[1]+" .  ?uri "+kbs[2]+" ?num . } ORDER BY DESC(?num) OFFSET 0 LIMIT 1 ";
        return newSparqlQuery;
    }

    /*public static String setSparqlQueryForTypes(String classUrl) {
        String sparql = 
                  "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "   PREFIX dbo: <http://dbpedia.org/ontology/>\n"
                + "   PREFIX res: <http://dbpedia.org/resource/>\n"
                + "\n"
                + "   SELECT DISTINCT ?label \n"
                + "   WHERE {  \n"
                + "       ?label"+" rdf:type "+" dbo:"+classUrl+" "+" .     \n"
                + "       filter(langMatches(lang(?label),\"EN\"))         \n"
                + "   }";

        return sparql;

    }*/
    public String stringToUrlUnicode(String string) throws UnsupportedEncodingException {
        String encodedString = URLEncoder.encode(string, "UTF-8");
        return encodedString;
    }

    public String getObject() {
        return this.objectOfProperty;
    }

    public String getSparqlQuery() {
        return sparqlQuery;
    }

    public String getResultSparql() {
        return resultSparql;
    }

    @Override
    public String toString() {
        return "SparqlQuery{" + "objectOfProperty=" + objectOfProperty + ", sparqlQuery=" + sparqlQuery + '}';
    }

    public SparqlQuery() {

    }

    /*String newSparqlQuery = "SELECT DISTINCT ?uri WHERE { ?uri "+"<http://www.w3.org/1999/02/22-rdf-syntax-ns#" +kbs[2]+">"+  " "+"<http://dbpedia.org/ontology/"+ kbs[3]+">" + " ; "
                + "<http://dbpedia.org/ontology/" + kbs[0] +">"+ " "+"<http://dbpedia.org/resource/:" + kbs[1] +">"+ " }";*/
    //  System.out.println(newSparqlQuery);
    //  SparqlQuery sparqlGeneration = new SparqlQuery(newSparqlQuery);
    // System.out.println("object:"+sparqlGeneration.getObject());
    /*String newSparqlQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> "
                + "PREFIX res: <http://dbpedia.org/resource/> "
                + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                + "SELECT DISTINCT ?uri WHERE { ?uri rdf:type dbo:"+"City"+" ; "
                + "dbo:"+"country"+" res:" + "Denmark" + " }";*/
    public static void main(String[] args) {
        /*
        ‘Which are Spanish movies?’, 2)‘Which is a Spanish
         movie?’, 3) ‘Which was a Spanish movie?’, 4) ‘Which were Spanish movies?’,
         and 5) ‘a spanish movie’.
         */
        String objectUrl = "http://dbpedia.org/ontology/largestCity";
        String propertyUrl = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
        String subject = "http://dbpedia.org/resource/Province_of_Saxony";
        String object = "http://dbpedia.org/resource/Russia";
        String sparqlQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX res: <http://dbpedia.org/resource/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?uri WHERE { ?uri rdf:type dbo:City ; dbo:country res:" + "Denmark" + " }";
        //SparqlQuery sparqlGeneration = new SparqlQuery(sparqlQuery);
        //System.out.println(sparqlGeneration.objectOfProperty);

        String entityUrl = "http://dbpedia.org/resource/Colombo_Lighthouse";
        propertyUrl = "http://dbpedia.org/ontology/height";

        //String sparql=	"PREFIX dbo: <http://dbpedia.org/ontology/>\nPREFIX res: <http://dbpedia.org/resource/>\nPREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nSELECT DISTINCT ?uri \nWHERE {\n\t?uri rdf:type dbo:Mountain .\n        ?uri dbo:locatedInArea res:Australia .\n        ?uri dbo:elevation ?elevation .\n} \nORDER BY DESC(?elevation) LIMIT 1";
        String sparql = "SELECT DISTINCT ?uri WHERE {  ?uri <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Building> .  ?uri <http://dbpedia.org/ontology/height> ?num . } ORDER BY DESC(?num) OFFSET 0 LIMIT 1 ";

        //String sparql=	"PREFIX dbo: <http://dbpedia.org/ontology/>\nPREFIX res: <http://dbpedia.org/resource/>\nPREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nSELECT DISTINCT ?uri \nWHERE {\n\t?uri rdf:type dbo:Mountain .\n   ?uri dbo:elevation ?elevation .\n} \nORDER BY DESC(?elevation) LIMIT 1";
        //SparqlQuery gradableSparqlQuery = new SparqlQuery(entityUrl, propertyUrl, FIND_ANY_ANSWER, RETURN_TYPE_OBJECT);
        
        String sparqlT="SELECT DISTINCT ?uri WHERE { ?uri <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/SoccerClub> ; <http://dbpedia.org/ontology/colour> <http://dbpedia.org/resource/Red> }";
        
        
        SparqlQuery gradableSparqlQuery = new SparqlQuery(sparqlT);
        System.out.println(gradableSparqlQuery.getObject());
        

        /*SparqlQuery sparqlQuery = new SparqlQuery(subject, objectUrl, FIND_ANY_ANSWER, RETURN_TYPE_OBJECT);
        System.out.println(sparqlQuery.getSparqlQuery());
        System.out.println(sparqlQuery.getResultSparql());
         System.out.println(sparqlQuery.getObject());*/
 /*String entitieSparql = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
               + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
               + "SELECT ?subject ?label\n"
               + "WHERE {\n"
               + "    ?subject rdf:type <http://dbpedia.org/ontology/Country> .\n"
               + "    ?subject rdfs:label ?label .\n"
               + "       filter(langMatches(lang(?label),\"EN\"))         \n"
               + "} LIMIT 20000";*/
        ///<http://dbpedia.org/resource/Algeria> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Country> .
        /*SparqlQuery sparql=new SparqlQuery();
       String sparqlStr=SparqlQuery.setSparqlQueryPropertyWithSubjectFilter(objectUrl, propertyUrl);
       String resultSparql = sparql.executeSparqlQuery(entitieSparql);
       System.out.println("sparql:"+resultSparql);
       System.out.println("sparql:"+sparql.getObject());
         */
    }

}
