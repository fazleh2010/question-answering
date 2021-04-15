package grammar.read.questions;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import util.io.FileUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.andrewoma.dexx.collection.Pair;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author elahi
 */
public class ReadAndWriteQuestions {

    private Trie trie = new Trie();
    public String[] header = new String[]{id, question, sparql, answer};
    public static String FRAMETYPE_NPP = "NPP";
    public static final String id = "id";
    public static final String question = "question";
    public static final String sparql = "sparql";
    public static final String answer = "answer";
    public CSVWriter csvWriter;
    public String questionAnswerFile=null;
    private Set<String> excludes=new HashSet<String>();
    private String type;
    public static String ATTRIBUTE_ADJECTIVE = "ATTRIBUTE_ADJECTIVE";
    public static String GRADABLE_ADJECTIVE = "GRADABLE_ADJECTIVE";


   
    public ReadAndWriteQuestions(String questionAnswerFile, String type) throws Exception {
        this.initialExcluded();
        this.questionAnswerFile = questionAnswerFile;
        this.type = type;
    }


    public void readQuestionAnswers(List<File> fileList,String entityDir) throws Exception {
        String sparql = null;
        Integer index = 0;

            //this.csvWriter = new CSVWriter(new FileWriter(questionAnswerFile));
            this.csvWriter = new CSVWriter(new FileWriter(questionAnswerFile, true));
            //this.csvWriter.writeNext(header);
        

        for (File file : fileList) {
            index = index + 1;
            ObjectMapper mapper = new ObjectMapper();
            GrammarEntries grammarEntries = mapper.readValue(file, GrammarEntries.class);
            Integer total = grammarEntries.getGrammarEntries().size();
            Integer idIndex = 0, noIndex = 0;
            for (GrammarEntryUnit grammarEntryUnit : grammarEntries.getGrammarEntries()) {
                 /*if (idIndex > 1) {
                    break;
                }*/
                if (grammarEntryUnit.getSentences().iterator().next().contains("Where is $x located?"))
                    continue;
                
                sparql = grammarEntryUnit.getSparqlQuery();
                String returnVairable = grammarEntryUnit.getReturnVariable();
                String retunrStr=grammarEntryUnit.getBindingType();
                String entityFileName=entityDir+"ENTITY_LABEL_LIST"+"_"+retunrStr.toLowerCase()+".txt";
                File entityFile=new File(entityFileName);
                 List<UriLabel> bindingList=new ArrayList<UriLabel>();
                if (type.contains(ATTRIBUTE_ADJECTIVE)) {
                    sparql = SparqlQuery.ATTRIBUTE_ADJECTIVE_sparql(sparql);
                    bindingList = new ArrayList<UriLabel>();
                }  
                else if (type.contains(GRADABLE_ADJECTIVE)) {
                    sparql = SparqlQuery.GRADABLE_ADJECTIVE_sparql(sparql);
                    bindingList = new ArrayList<UriLabel>();
                } else {
                    //bindingList = this.getExtendedBindingList(grammarEntryUnit.getBindingList(), entityFile);
                     bindingList=new ArrayList<UriLabel>();
                }
                    
                noIndex =this.replaceVariables(bindingList, sparql, returnVairable,grammarEntryUnit.getSentences(),noIndex);
                noIndex = noIndex + 1;
                System.out.println("index:" + index + " Id:" + grammarEntryUnit.getId() + " total:" + total + " example:" + grammarEntryUnit.getSentences().iterator().next());
                idIndex = idIndex + 1;
            }
        }

    }
    
    private Integer replaceVariables(List<UriLabel> uriLabels, String sparqlOrg, String frameType, List<String> questions, Integer rowIndex) {
        Integer index = 0;
        List< String[]> rows = new ArrayList<String[]>();

        if (type.contains(ATTRIBUTE_ADJECTIVE)||type.contains(GRADABLE_ADJECTIVE)) {
            Pair<String, String> pair = this.checkAnswerFromWikipedia(sparqlOrg);
            rowIndex = rowIndex + 1;
            String sparql = pair.component1();
            String answer = pair.component2();
            for (String question : questions) {
                String id = rowIndex.toString();
                String questionT = question.stripLeading().trim();
                String[] record = {id, questionT, sparql, answer,};
                System.out.println("id::" + id + " questionT::" +questionT + " questionForShow::" + questionT + " sparql::" + sparql + " answer::" + answer);
                this.csvWriter.writeNext(record);
                rowIndex = rowIndex + 1;
            }
            return rowIndex;
        }

        for (UriLabel uriLabel : uriLabels) {
            if (!isKbValid(uriLabel)) {
                continue;
            }
            //System.out.println("index: " + index + " size:" + uriLabels.size() + " uriLabel:::" + uriLabel.getUri() + " labe::" + uriLabel.getLabel());
            String questionForShow = questions.iterator().next();
            if (questionForShow.contains("Where is $x located?")) {
                continue;
            }

            Pair<String, String> pair = this.getAnswerFromWikipedia(uriLabel.getUri(), sparqlOrg, frameType);
            String sparql = pair.component1();
            String answer = pair.component2();
            index = index + 1;
            sparql = this.modifySparql(sparql);

            System.out.println("index::" + index + " uriLabel::" + uriLabel.getLabel() + " questionForShow::" + questionForShow + " sparql::" + sparql + " answer::" + answer);

            try {
                if (answer.isEmpty() || answer.contains("no answer found")) {
                    continue;
                } else {
                    for (String question : questions) {
                        if (question.contains("(") && question.contains(")")) {
                            String result = StringUtils.substringBetween(question, "(", ")");
                            question = question.replace(result, "X");
                        } else if (question.contains("$x")) {
                            //System.out.println(question);

                        }

                        String id = rowIndex.toString();
                        //question = modifyQuestion(question, uriLabel);
                        String questionT = question.replaceAll("(X)", uriLabel.getLabel());
                        questionT = questionT.replace("(", "");
                        questionT = questionT.replace(")", "");
                        questionT = questionT.replace("$x", uriLabel.getLabel());
                        questionT = questionT.replace(",", "");
                        questionT = questionT.stripLeading().trim();
                        String[] record = {id, questionT, sparql, answer,};
                        this.csvWriter.writeNext(record);
                        rowIndex = rowIndex + 1;
                    }
                }

            } catch (Exception ex) {
                System.err.println(ex.getMessage() + " " + sparql + " " + answer);
            }
        }

        return rowIndex;
    }
    
    public void createTrieCsv() {
        List<String[]> rows = new ArrayList<String[]>();
        CSVReader reader;
        Integer index = 0;
        try {
            reader = new CSVReader(new FileReader(this.questionAnswerFile));
            rows = reader.readAll();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ReadAndWriteQuestions.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ReadAndWriteQuestions.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CsvException ex) {
            Logger.getLogger(ReadAndWriteQuestions.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (String[] row : rows) {
            String question = null;
            if (index == 0) {
                index = index + 1;
                continue;
            }
            question = row[1].trim().strip();
            trie.insertNode(question);
            index = index + 1;
        }
    }

    
    public Pair<String, String> getAnswerFromWikipedia(String subjProp, String sparql, String returnType) {
        String property = null;
        String answer = null;
        SparqlQuery sparqlQuery = null;
        property = StringUtils.substringBetween(sparql, "<", ">");
        
        sparqlQuery = new SparqlQuery(subjProp, property, SparqlQuery.FIND_ANY_ANSWER, returnType);
        //System.out.println("original sparql:: "+sparql);
        //System.out.println("sparqlQuery:: "+sparqlQuery.getSparqlQuery());
        answer = sparqlQuery.getObject();
        if (answer != null) {
            if (answer.contains("http:")) {
                //System.out.println(answer);
                SparqlQuery sparqlQueryLabel = new SparqlQuery(answer, property, SparqlQuery.FIND_LABEL, null);
                answer = sparqlQueryLabel.getObject();
                //System.out.println(answer);

            }
            return new Pair<String, String>(sparqlQuery.sparqlQuery, answer);
        } else {
            return new Pair<String, String>(sparqlQuery.sparqlQuery, "no answer found");
        }
    }
    
     
    public Pair<String, String> checkAnswerFromWikipedia(String sparql) {
        SparqlQuery sparqlQueryLabel = new SparqlQuery(sparql);
        String answer = sparqlQueryLabel.getObject();
        System.out.println("object:" + sparqlQueryLabel.getObject());
       /* if (!answer.isEmpty()) {
            answer = "no answer found";
        }*/
       
          sparqlQueryLabel = new SparqlQuery(answer, "", SparqlQuery.FIND_LABEL, null);
          answer = sparqlQueryLabel.getObject();
 
            
        return new Pair<String, String>(sparql, answer);
    }


    

    public Trie getTrie() {
        return trie;
    }

    private void initialExcluded() {
        this.excludes.add("2013_Santa_Monica_shooting");
        this.excludes.add("2014_Fort_Hood_shooting");
        this.excludes.add("2014_killings_of_NYPD_officers");
        this.excludes.add("2015_Chattanooga_shootings");
        this.excludes.add("2015_Lafayette_shooting");
        this.excludes.add("2015_Parramatta_shooting");
        this.excludes.add("2015_Sousse_attacks");
        this.excludes.add("2016_Berlin_truck_attack");
        this.excludes.add("2016_New_York_and_New_Jersey_bombings");
        this.excludes.add("2016_UCLA_shooting");
        this.excludes.add("2016_shooting_of_Almaty_police_officers");
        this.excludes.add("2016_shooting_of_Dallas_police_officers");
        this.excludes.add("2017_Fresno_shooting_spree");
        this.excludes.add("7669_(group)");
        this.excludes.add("2013_Hialeah_shooting");
        this.excludes.add("2014_Isla_Vista_killings");
        this.excludes.add("2014_Las_Vegas_shootings");
        this.excludes.add("2014_shootings_at_Parliament_Hill,_Ottawa");
        this.excludes.add("2016_Munich_shooting");
        this.excludes.add("2016_shooting_of_Baton_Rouge_police_officers");
        this.excludes.add("2017_New_York_City_truck_attack");
    }

    private boolean isKbValid(UriLabel uriLabel) {
        String kb = uriLabel.getUri().replace("http://dbpedia.org/resource/", "");
        if (this.excludes.contains(kb)) {
            return false;
        }
        return true;
    }

    private String modifyQuestion(String questionT,String uriLabel) {
        questionT = questionT.replaceAll("(X)", uriLabel);
        questionT = questionT.replace("(", "");
        questionT = questionT.replace(")", "");
        questionT = questionT.replace("$x", uriLabel);
        questionT = questionT.replace(",", "");
        questionT = questionT.stripLeading().trim();
        return questionT;
    }

    private Integer makeCsvRow(List<String> questions, List<String[]> rows,  Integer rowIndex) {
        for (String question : questions) {
            if (question.contains("(") && question.contains(")")) {
                String result = StringUtils.substringBetween(question, "(", ")");
                question = question.replace(result, "X");
            } else if (question.contains("$x")) {
                //System.out.println(question);

            }
            for (String[] row : rows) {
                String id = rowIndex.toString();
                String uriLabel = row[0];
                //question = modifyQuestion(question, uriLabel);
                String questionT = question.replaceAll("(X)", uriLabel);
                questionT = questionT.replace("(", "");
                questionT = questionT.replace(")", "");
                questionT = questionT.replace("$x", uriLabel);
                questionT = questionT.replace(",", "");
                questionT = questionT.stripLeading().trim();
                String sparql = row[1];
                String answer = row[2];
                //System.out.println("id::" + id + " uriLabel::" + uriLabel + " question::" + questionT + " sparql::" + sparql + " answer::" + answer);
                String[] record = {id, questionT, sparql, answer};
                this.csvWriter.writeNext(record);
                rowIndex = rowIndex + 1;
            }
        }
        return rowIndex;
    }

    private List<UriLabel> getExtendedBindingList(List<UriLabel> bindingList, File classFile) {
        List<UriLabel> modifyLabels = new ArrayList<UriLabel>();
        for (UriLabel uriLabel : bindingList) {
            if (isKbValid(uriLabel)) {
                modifyLabels.add(uriLabel);
            }
        }
        modifyLabels.addAll(FileUtils.getUriLabels(classFile));
        return modifyLabels;
    }

    private String modifySparql(String sparql) {
        sparql = sparql.stripLeading().trim();
        sparql = sparql.replace("\n", "");
        sparql = sparql.replace(" ", "+");
        sparql = sparql.replace("+", " ");
        return sparql;
    }

    

}
