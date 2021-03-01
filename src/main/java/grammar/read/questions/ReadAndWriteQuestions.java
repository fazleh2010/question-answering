package grammar.read.questions;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import util.io.FileUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.andrewoma.dexx.collection.Pair;
import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author elahi
 */
public class ReadAndWriteQuestions {

    private LinkedHashMap<String, String> questionAnswers = new LinkedHashMap<String, String>();
    private String content = null;
    private Trie trie = new Trie();
    public String[] header = new String[]{id, question, sparql, answer};
    private List<String[]> csvRows = new ArrayList<String[]>();
    public static String FRAMETYPE_NPP = "NPP";
    public static final String id = "id";
    public static final String question = "question";
    public static final String sparql = "sparql";
    public static final String answer = "answer";
    public String questionAnswerFile;

    public ReadAndWriteQuestions(String questionAnswerFile) {
        this.content = FileUtils.fileToString(questionAnswerFile);

    }

    public ReadAndWriteQuestions(String questionAnswerFile, String inputFileDir, String inputFile, String type) throws Exception {
        this.questionAnswerFile = questionAnswerFile;
        List<File> list = FileUtils.getFiles(inputFileDir, inputFile, ".json");
        if (list.isEmpty()) {
            throw new Exception("No files to process for question answering system!!");
        } else {
            this.readQuestionAnswers(list);
        }
        if (type.contains(".txt")) {
            this.content = this.prepareQuestionAnswerStr();
            FileUtils.stringToFile(this.content, questionAnswerFile);
        } else if (type.contains(".csv")) {
            FileUtils.stringToCSVFile(this.csvRows, questionAnswerFile);
        }

    }

    private void readQuestionAnswers(List<File> fileList) throws Exception {
        String sparql = null;
        Integer index = 0;
        csvRows.add(header);
        for (File file : fileList) {
            index = index + 1;
            ObjectMapper mapper = new ObjectMapper();
            GrammarEntries grammarEntries = mapper.readValue(file, GrammarEntries.class);
            Integer total = grammarEntries.getGrammarEntries().size();
            for (GrammarEntryUnit grammarEntryUnit : grammarEntries.getGrammarEntries()) {
                sparql = grammarEntryUnit.getSparqlQuery();
                Map<String, Pair<String, String>> uriAnswer = this.replaceVariables(grammarEntryUnit.getBindingList(), sparql, grammarEntryUnit.getFrameType());
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
                    csvRows.add(record);
                    questionAnswers.put(questionT, answer);
                } catch (Exception ex) {
                    System.err.println(id.toString() + " " + questionT + " " + sparql + " " + answer);
                }

            }
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

    public Pair<String, String> getAnswerFromWikipedia(String subjProp, String sparql, String syntacticFrame) {
        String property = null;
        String answer = null;
        SparqlQuery sparqlQuery = null;
        property = StringUtils.substringBetween(sparql, "<", ">");
        sparqlQuery = new SparqlQuery(subjProp, property, SparqlQuery.FIND_ANY_ANSWER);
        //System.out.println("original sparql:: "+sparql);
        //System.out.println("sparqlQuery:: "+sparqlQuery.getSparqlQuery());
        answer = sparqlQuery.getObject();
        if (answer != null) {
            if (answer.contains("http:")) {
                //System.out.println(answer);
                SparqlQuery sparqlQueryLabel = new SparqlQuery(answer, property, SparqlQuery.FIND_LABEL);
                answer = sparqlQueryLabel.getObject();
                //System.out.println(answer);

            }
            return new Pair<String, String>(sparqlQuery.sparqlQuery, answer);
        } else {
            return new Pair<String, String>(sparqlQuery.sparqlQuery, "no answer found");
        }

        //return new SparqlQuery(subjProp, property,SparqlQuery.FIND_ANY_ANSWER).getObject();
    }

    private String prepareQuestionAnswerStr() {
        String quesAnsStr = "";
        Integer totalLimit = questionAnswers.size();
        for (String question : questionAnswers.keySet()) {
            String line = question + "=" + questionAnswers.get(question) + "\n";
            quesAnsStr += line;
        }
        return quesAnsStr;
    }

    public String getContent() {
        return content;
    }

    /*private void replaceVariables(String question, List<UriLabel> uriLabels, String sparql, String frameType) {
        String result = null;
        if (question.contains("(") && question.contains(")")) {
            result = StringUtils.substringBetween(question, "(", ")");
            question = question.replace(result, "X");
        } else if (question.contains("$x")) {
            //System.out.println(question);

        }
        Integer index = 0;
        for (UriLabel uriLabel : uriLabels) {
            Boolean flag = false;
            index = index + 1;
            String questionT = question.replaceAll("(X)", uriLabel.getLabel());
            questionT = questionT.replace("(", "");
            questionT = questionT.replace(")", "");
            questionT = questionT.replace("$x", uriLabel.getLabel());
            String answer = this.getAnswerFromWikipedia(uriLabel.getUri(), sparql, frameType);
            System.out.println("questionT:" + questionT + " answer:" + answer);
            questionAnswers.put(questionT, answer);
        }

    }*/
}
