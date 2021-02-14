package grammar.read.questions;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import util.io.FileUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
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
    public static String FRAMETYPE_NPP = "NPP";

    public ReadAndWriteQuestions(String questionAnswerFile) {
        this.content = FileUtils.fileToString(questionAnswerFile);

    }

    public ReadAndWriteQuestions(String questionAnswerFile, String inputFileDir, String inputFile) throws Exception {
        List<File> list = FileUtils.getFiles(inputFileDir, inputFile, ".json");
        if (list.isEmpty()) {
            throw new Exception("No files to process for question answering system!!");
        } else {
            this.readQuestionAnswers(list);
        }
        this.content = this.prepareQuestionAnswerStr();
        FileUtils.stringToFile(this.content, questionAnswerFile);
    }

    private void readQuestionAnswers(List<File> fileList) throws Exception {
        String sparql = null;
        Integer index = 0;
        for (File file : fileList) {
            index = index + 1;
            ObjectMapper mapper = new ObjectMapper();
            GrammarEntries grammarEntries = mapper.readValue(file, GrammarEntries.class);
            Integer total = grammarEntries.getGrammarEntries().size();
            for (GrammarEntryUnit grammarEntryUnit : grammarEntries.getGrammarEntries()) {
                sparql = grammarEntryUnit.getSparqlQuery();
                Map<String, String> uriAnswer = this.replaceVariables(grammarEntryUnit.getBindingList(), sparql, grammarEntryUnit.getFrameType());
                this.makeQuestionAnswer(grammarEntryUnit.getSentences(), uriAnswer);
                System.out.println("Id:" + grammarEntryUnit.getId() + " total:" + total+" example:"+grammarEntryUnit.getSentences().iterator().next());
            }

        }
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
    
    private void makeQuestionAnswer(List<String> questions, Map<String, String> uriAnswer) {

        for (String question : questions) {
            String result = null;
            if (question.contains("(") && question.contains(")")) {
                result = StringUtils.substringBetween(question, "(", ")");
                question = question.replace(result, "X");
            } else if (question.contains("$x")) {
                //System.out.println(question);

            }
            for (String uriLabel : uriAnswer.keySet()) {
                String answer = uriAnswer.get(uriLabel);
                String questionT = question.replaceAll("(X)", uriLabel);
                questionT = questionT.replace("(", "");
                questionT = questionT.replace(")", "");
                questionT = questionT.replace("$x", uriLabel);
                questionAnswers.put(questionT, answer);
            }
        }

    }
    
    private Map<String,String> replaceVariables(List<UriLabel> uriLabels, String sparql, String frameType) {
        Map<String,String> xValues = new TreeMap<String,String>();
        for (UriLabel uriLabel : uriLabels) {
            String answer = this.getAnswerFromWikipedia(uriLabel.getUri(), sparql, frameType);
            //System.out.println("uriLabel:" + uriLabel + " answer:" + answer);
            xValues.put(uriLabel.getUri(),answer);
        }
        return xValues;
    }

    public String getAnswerFromWikipedia(String subjProp, String sparql, String syntacticFrame) {
        String property = null;
        String answer = null;
        if (syntacticFrame.contains(FRAMETYPE_NPP)) {
            property = StringUtils.substringBetween(sparql, "<", ">");
        }
        SparqlQuery sparqlQuery = new SparqlQuery(subjProp, property, SparqlQuery.FIND_ANY_ANSWER);
        answer = sparqlQuery.getObject();
        if (answer != null) {
            if (answer.contains("http:")) {
                //System.out.println(answer);
                SparqlQuery sparqlQueryLabel = new SparqlQuery(answer, property, SparqlQuery.FIND_LABEL);
                answer = sparqlQueryLabel.getObject();
                //System.out.println(answer);
            }
            return answer;
        } else {
            return "No answer found for this question!!";
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

    
}
