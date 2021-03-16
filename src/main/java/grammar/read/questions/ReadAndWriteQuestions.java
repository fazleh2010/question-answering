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
   
    public ReadAndWriteQuestions(String questionAnswerFile) throws Exception {
        this.initialExcluded();
       this.questionAnswerFile=questionAnswerFile;
    }

    public void readQuestionAnswers(String inputFileDir, String inputFile) throws Exception {
        String sparql = null;
        Integer index = 0;

            this.csvWriter = new CSVWriter(new FileWriter(questionAnswerFile));
            this.csvWriter.writeNext(header);
        
        
        List<File> fileList = FileUtils.getFiles(inputFileDir, inputFile, ".json");
        if (fileList.isEmpty()) {
            throw new Exception("No files to process for question answering system!!");
        }

        for (File file : fileList) {
            index = index + 1;
            ObjectMapper mapper = new ObjectMapper();
            GrammarEntries grammarEntries = mapper.readValue(file, GrammarEntries.class);
            Integer total = grammarEntries.getGrammarEntries().size();
            Integer idIndex = 0, noIndex = 0;
            for (GrammarEntryUnit grammarEntryUnit : grammarEntries.getGrammarEntries()) {
                 if (idIndex > 1) {
                    break;
                }
                
                sparql = grammarEntryUnit.getSparqlQuery();
                String returnVairable = grammarEntryUnit.getReturnVariable();
                Map<String, Pair<String, String>> uriAnswer = this.replaceVariables(grammarEntryUnit.getBindingList(), sparql, returnVairable);
                List< String[]> rows = this.makeQuestionAnswer(grammarEntryUnit.getId(), uriAnswer, sparql);
                noIndex = this.makeCsvRow(grammarEntryUnit.getSentences(), rows, grammarEntryUnit.getFrameType(), noIndex);
                noIndex = noIndex + 1;
                System.out.println("index:" + index + " Id:" + grammarEntryUnit.getId() + " total:" + total + " example:" + grammarEntryUnit.getSentences().iterator().next());
                idIndex = idIndex + 1;
            }
        }

    }
    
    private List< String[]> makeQuestionAnswer(Integer id, Map<String, Pair<String, String>> uriAnswer, String sparql) {
        List< String[]> rows=new ArrayList<String[]> ();
        String result = null;
        
        for (String uriLabel : uriAnswer.keySet()) {
            id = id + 1;
            String answer = "no answer found";
            Pair<String, String> pair = uriAnswer.get(uriLabel);
            sparql = pair.component1();
            answer = pair.component2();
            //String questionT = null;
            try {
                //questionT=this.modifyQuestion(questionT,uriLabel);
                sparql = sparql.stripLeading().trim();
                sparql = sparql.replace("\n", "");
                sparql = sparql.replace(" ", "+");
                sparql = sparql.replace("+", " ");
                if (answer.isEmpty()) {
                    System.out.println("answer:" + answer);
                } else {
                    String[] record = {uriLabel, sparql, answer};
                    rows.add(record);
                    //System.out.println("id::"+id+" uriLabel::"+uriLabel+" sparql::"+sparql+" answer::"+answer);

                    //this.csvWriter.writeNext(record);
                }

            } catch (Exception ex) {
                System.err.println(ex.getMessage() + " " + id.toString() +  " " + sparql + " " + answer);
            }
        }
        return rows;

    }

    /*private void makeQuestionAnswer(Integer id, List<String> questions, Map<String, Pair<String, String>> uriAnswer, String sparql) {
        for (String question : questions) {
            String result = null;
            if (question.contains("(") && question.contains(")")) {
                result = StringUtils.substringBetween(question, "(", ")");
                question = question.replace(result, "X");
            } else if (question.contains("$x")) {
                //System.out.println(question);

            }
            for (String uriLabel : uriAnswer.keySet()) {
                id = id + 1;
                String answer = "no answer found";
                Pair<String, String> pair = uriAnswer.get(uriLabel);
                sparql = pair.component1();
                answer = pair.component2();
                String questionT = null;
                try {
                    questionT = question.replaceAll("(X)", uriLabel);
                    questionT = questionT.replace("(", "");
                    questionT = questionT.replace(")", "");
                    questionT = questionT.replace("$x", uriLabel);
                    questionT=questionT.replace(",", "");
                    questionT = questionT.stripLeading().trim();
                    sparql = sparql.stripLeading().trim();
                    sparql = sparql.replace("\n", "");
                    sparql = sparql.replace(" ", "+");
                    sparql = sparql.replace("+", " ");
                    if (answer.isEmpty())
                        System.out.println("answer:" + answer);
                    else {
                        String[] record = {id.toString(), questionT, sparql, answer};
                        this.csvWriter.writeNext(record);
                    }
                   
                } catch (Exception ex) {
                    System.err.println(ex.getMessage() + " " + id.toString() + " " + questionT + " " + sparql + " " + answer);
                }
            }
        }
    }*/
    
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

    private Map<String, Pair<String, String>> replaceVariables(List<UriLabel> uriLabels, String sparql, String frameType) {
        Map<String, Pair<String, String>> xValues = new TreeMap<String, Pair<String, String>>();
        for (UriLabel uriLabel : uriLabels) {
            if(!isKbValid(uriLabel)){
                continue;
            }  
            System.out.println("uriLabel:::"+uriLabel.getUri()+" labe::"+uriLabel.getLabel());
            Pair<String, String> pair = this.getAnswerFromWikipedia(uriLabel.getUri(), sparql, frameType);
            String sparqlQuery = pair.component1();
            String answer = pair.component2();
            xValues.put(uriLabel.getLabel(), pair);
        }
        return xValues;
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

    

    public Trie getTrie() {
        return trie;
    }

    private void initialExcluded() {
        this.excludes.add("2013_Hialeah_shooting");
        this.excludes.add("2014_Isla_Vista_killings");
        this.excludes.add("2014_Las_Vegas_shootings");
        this.excludes.add("2014_shootings_at_Parliament_Hill,_Ottawa");
        this.excludes.add("2016_Munich_shooting");
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

    private Integer makeCsvRow(List<String> questions, List<String[]> rows, String frameType, Integer rowIndex) {
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

}
