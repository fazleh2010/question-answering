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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    public ReadAndWriteQuestions(String questionAnswerFile) throws Exception {
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
            for (GrammarEntryUnit grammarEntryUnit : grammarEntries.getGrammarEntries()) {
                sparql = grammarEntryUnit.getSparqlQuery();
                String returnVairable = grammarEntryUnit.getReturnVariable();
                Map<String, Pair<String, String>> uriAnswer = this.replaceVariables(grammarEntryUnit.getBindingList(), sparql, returnVairable);
                this.makeQuestionAnswer(grammarEntryUnit.getId(), grammarEntryUnit.getSentences(), uriAnswer, sparql);
                System.out.println("Id:" + grammarEntryUnit.getId() + " total:" + total + " example:" + grammarEntryUnit.getSentences().iterator().next());
            }
        }

    }

    private void makeQuestionAnswer(Integer id, List<String> questions, Map<String, Pair<String, String>> uriAnswer, String sparql) {
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
                    questionT = questionT.stripLeading().trim();
                    sparql = sparql.stripLeading().trim();
                    sparql = sparql.replace("\n", "");
                    sparql = sparql.replace(" ", "+");
                    sparql = sparql.replace("+", " ");
                    String[] record = {id.toString(), questionT, sparql, answer};
                    this.csvWriter.writeNext(record);
                } catch (Exception ex) {
                    System.err.println(ex.getMessage() + " " + id.toString() + " " + questionT + " " + sparql + " " + answer);
                }
            }
        }
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
            trie.insert(question);
            index = index + 1;
        }
    }

    private Map<String, Pair<String, String>> replaceVariables(List<UriLabel> uriLabels, String sparql, String frameType) {
        Map<String, Pair<String, String>> xValues = new TreeMap<String, Pair<String, String>>();
        for (UriLabel uriLabel : uriLabels) {
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
    
}
