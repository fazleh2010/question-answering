
import grammar.read.questions.ReadAndWriteQuestions;
import grammar.read.questions.Trie;
import grammar.structure.component.Language;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.io.FileUtils;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author elahi
 */
public class AutoCompletion {

    //temporarly closed. becuase it does not work from command line
    //private static final Logger LOG = LogManager.getLogger(QueGG.class);
    private static String inputDir = "src/main/resources/test/input/";
    //this is a temporary solution. it will be removed later..
    //private static String BaseDir = "/var/www/html//question-answering/";
    private static String BaseDir = "";
    private static String outputDir = BaseDir + "src/main/resources/test/output/";

    public static String QUESTION_ANSWER_LOCATION = BaseDir + "src/main/resources/";
    public static String QUESTION_ANSWER_FILE = "questions.txt";
    public static String QUESTION_ANSWER_CSV_FILE = "questions.csv";


    public static void main(String[] args) {
        String BaseDir = "";
        QueGG queGG = new QueGG();
        Language language = Language.stringToLanguage("EN");
        String questionAnswerFile = QUESTION_ANSWER_LOCATION + File.separator + QUESTION_ANSWER_CSV_FILE;

        ReadAndWriteQuestions readAndWriteQuestions = null;
        Integer task = 2;
        String content = "";

        if (task.equals(1)) {
            try {
                queGG.init(language, inputDir, outputDir);
                //CreateTree createTree = new CreateTree(readAndWriteQuestions.getInputFileName());
                //content = output(createTree.getInputTupples());
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(QueGG.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else if (task.equals(2)) {
            try {
                readAndWriteQuestions = new ReadAndWriteQuestions(questionAnswerFile, outputDir, "grammar_FULL_DATASET_EN",".csv");
                //CreateTree createTree = new CreateTree(readAndWriteQuestions.getInputFileName());
                //content = output(createTree.getInputTupples());
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(QueGG.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else if (task.equals(3)) {
            //readAndWriteQuestions = new ReadAndWriteQuestions(questionAnswerFile);
            Trie trie = createTrie(questionAnswerFile);
            List autoCompletionList = trie.autocomplete("Give me");
            for (int i = 0; i < autoCompletionList.size(); i++) {
                System.out.println(i + " auto completion:" + autoCompletionList.get(i));
            }
        }
    }
     public static Trie createTrie(String fileName) {
            Trie trie = new Trie();
            Integer index=0;
        try {
            InputStream is = new FileInputStream(fileName);
            BufferedReader buf = new BufferedReader(new InputStreamReader(is));
            String line = buf.readLine();
            StringBuilder sb = new StringBuilder();
            while (line != null) {
                if (line.contains("=")) {
                    String[] info = line.split("=");
                    String question = info[0];
                     trie.insert(question);
                     line = buf.readLine();
                     index=index+1;
                     System.err.println("index:"+index+" line:"+line);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
      

        return trie;
    }

}
