
import eu.monnetproject.lemon.LemonModel;
import grammar.generator.BindingResolver;
import grammar.generator.GrammarRuleGeneratorRoot;
import grammar.generator.GrammarRuleGeneratorRootImpl;
import grammar.read.questions.ReadAndWriteQuestions;
import grammar.read.questions.Trie;
import grammar.structure.component.DomainOrRangeType;
import grammar.structure.component.FrameType;
import grammar.structure.component.GrammarEntry;
import grammar.structure.component.GrammarWrapper;
import grammar.structure.component.Language;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import lexicon.LexiconImporter;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import java.util.logging.Level;
import util.io.FileUtils;

//index location /var/www/html/

@NoArgsConstructor
public class QueGG {
    //temporarly closed. becuase it does not work from command line
    //private static final Logger LOG = LogManager.getLogger(QueGG.class);

    private static String inputDir = "src/main/resources/lexicon/en/nouns/input/";
    //this is a temporary solution. it will be removed later..
    //private static String BaseDir = "/var/www/html//question-answering/";
    private static String BaseDir = "";
    private static String outputDir = BaseDir + "src/main/resources/lexicon/en/nouns/new/output/";

    public static String QUESTION_ANSWER_LOCATION = BaseDir + "src/main/resources";
    public static String QUESTION_ANSWER_FILE = "questions.txt";

    //GENERATE_QUESTION_ANSWER_FROM_GRAMMAR=1
    //PREPARE_QUESTION_ANSWER =1
    //QUESTIONS_ANSWERS=2;
    public static void main(String[] args) {
        
        String BaseDir = "";
        outputDir = BaseDir + "src/main/resources/lexicon/en/nouns/new/output/";
        QUESTION_ANSWER_LOCATION = BaseDir + "src/main/resources";
        String questionAnswerFile = QUESTION_ANSWER_LOCATION + File.separator + QUESTION_ANSWER_FILE;

        ReadAndWriteQuestions readAndWriteQuestions = null;
        Integer task = 3;
        String content = "";
        QueGG queGG = new QueGG();
        Language language = Language.stringToLanguage("EN");
       

        try {
            System.out.println("inputDir:"+inputDir);
             System.out.println("outputDir:"+outputDir);
            queGG.init(language, inputDir, outputDir);

           /* if (args.length > 3) {
                String search = args[0];
                if (search.contains("search")) {
                    language = Language.stringToLanguage(args[1]);
                    questionAnswerFile = args[2];
                    String tokenStr = args[3];
                    Trie trie = createTrie(questionAnswerFile);
                    List autoCompletionList = trie.autocomplete(tokenStr);
                    for (int i = 0; i < autoCompletionList.size(); i++) {
                        content += autoCompletionList.get(i) + "=" + "answer" + "\n";
                    }
                    System.out.println(content);
                } else {
                    language = Language.stringToLanguage(args[0]);
                    inputDir = Path.of(args[1]).toString();
                    outputDir = Path.of(args[2]).toString();
                }

            } else if (args.length == 4) {
                String search = args[0];
                language = Language.stringToLanguage(args[1]);
                questionAnswerFile = args[2];
                String tokenStr = args[3];

                Trie trie = createTrie(questionAnswerFile);
                List autoCompletionList = trie.autocomplete(tokenStr);
                for (int i = 0; i < autoCompletionList.size(); i++) {
                    content += autoCompletionList.get(i) + "\n";
                }
                System.out.println(content);
            } else if (args.length < 3) {
                System.out.println("provide correct parameter!!!!");
            } else {
                queGG.init(language, inputDir, outputDir);

            }*/

        } catch (IllegalArgumentException | IOException e) {
            System.err.printf("%s: %s%n", e.getClass().getSimpleName(), e.getMessage());
            System.err.printf("Usage: <%s> <input directory> <output directory>%n", Arrays.toString(Language.values()));
        }
       
    }

    public static Trie createTrie(String fileName) {
        Trie trie = new Trie();
        Integer index = 0;
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
                    index = index + 1;
                    //System.err.println("index:" + index + " line:" + line);
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
        }

        return trie;
    }


    private static void questionAnsweringInterface(String[] args, QueGG queGG) {
        String questionAnswerFile = QUESTION_ANSWER_LOCATION + File.separator + QUESTION_ANSWER_FILE;

        ReadAndWriteQuestions readAndWriteQuestions = null;
        Integer task = 3;
        String content = "";

        if (task.equals(1)) {
            generateQuestions(args, queGG);
        } else if (task.equals(2)) {
            try {
                readAndWriteQuestions = new ReadAndWriteQuestions(questionAnswerFile, outputDir, "grammar_FULL_DATASET_EN");
                //CreateTree createTree = new CreateTree(readAndWriteQuestions.getInputFileName());
                //content = output(createTree.getInputTupples());
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(QueGG.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else if (task.equals(3)) {
            readAndWriteQuestions = new ReadAndWriteQuestions(questionAnswerFile);
            System.out.println(readAndWriteQuestions.getContent());
        }
    }

    private static void generateQuestions(String[] args, QueGG queGG) {

    }

    public void init(Language language, String inputDir, String outputDir) throws IOException {
        try {
            loadInputAndGenerate(language, inputDir, outputDir);
        } catch (URISyntaxException | InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            //LOG.error("Could not create grammar: {}", e.getMessage());
        }
    }

    private void loadInputAndGenerate(Language lang, String inputDir, String outputDir) throws
            IOException,
            InvocationTargetException,
            NoSuchMethodException,
            InstantiationException,
            IllegalAccessException,
            URISyntaxException {
        LexiconImporter lexiconImporter = new LexiconImporter();
        LemonModel lemonModel = lexiconImporter.loadModelFromDir(inputDir, lang.toString().toLowerCase());
        generateByFrameType(lang, lemonModel, outputDir);
    }

    private void generateByFrameType(Language language, LemonModel lemonModel, String outputDir) throws
            IOException,
            NoSuchMethodException,
            IllegalAccessException,
            InvocationTargetException,
            InstantiationException {
        GrammarWrapper grammarWrapper = new GrammarWrapper();
        for (FrameType frameType : FrameType.values()) {
            if (!isNull(frameType.getImplementingClass())) {
                GrammarWrapper gw = generateGrammarGeneric(
                        lemonModel,
                        (GrammarRuleGeneratorRoot) frameType.getImplementingClass()
                                .getDeclaredConstructor(Language.class)
                                .newInstance(language)
                );
                grammarWrapper.merge(gw);
            }
        }
        // Make a GrammarRuleGeneratorRoot instance to use the combination function
        GrammarRuleGeneratorRoot generatorRoot = new GrammarRuleGeneratorRootImpl(language);
        //LOG.info("Start generation of combined entries");
        grammarWrapper.getGrammarEntries().addAll(generatorRoot.generateCombinations(grammarWrapper.getGrammarEntries()));

        for (GrammarEntry grammarEntry : grammarWrapper.getGrammarEntries()) {
            grammarEntry.setId(String.valueOf(grammarWrapper.getGrammarEntries().indexOf(grammarEntry) + 1));
        }

        // Output file is too big, make two files
        GrammarWrapper regularEntries = new GrammarWrapper();
        regularEntries.setGrammarEntries(
                grammarWrapper.getGrammarEntries()
                        .stream()
                        .filter(grammarEntry -> !grammarEntry.isCombination())
                        .collect(Collectors.toList())
        );
        GrammarWrapper combinedEntries = new GrammarWrapper();
        combinedEntries.setGrammarEntries(
                grammarWrapper.getGrammarEntries().stream().filter(GrammarEntry::isCombination).collect(Collectors.toList())
        );

        // Generate bindings
        //LOG.info("Start generation of bindings");
        grammarWrapper.getGrammarEntries().forEach(generatorRoot::generateBindings);

        generatorRoot.dumpToJSON(
                Path.of(outputDir,
                        "grammar_" + generatorRoot.getFrameType().getName() + "_" + language + ".json").toString(),
                regularEntries
        );
        generatorRoot.dumpToJSON(Path.of(outputDir, "grammar_COMBINATIONS" + "_" + language + ".json").toString(), combinedEntries);

        // Insert those bindings and write new files
        //LOG.info("Start resolving bindings");
        BindingResolver bindingResolver = new BindingResolver(grammarWrapper.getGrammarEntries());
        grammarWrapper = bindingResolver.resolve();
        generatorRoot.dumpToJSON(Path.of(outputDir, "grammar_FULL_WITH_BINDINGS_" + language + ".json").toString(), grammarWrapper);

    }

    private GrammarWrapper generateGrammarGeneric(LemonModel lemonModel, GrammarRuleGeneratorRoot grammarRuleGenerator) {
        GrammarWrapper grammarWrapper = new GrammarWrapper();
        lemonModel.getLexica().forEach(lexicon -> {
            //LOG.info("Start generation for FrameType {}", grammarRuleGenerator.getFrameType().getName());
            grammarRuleGenerator.setLexicon(lexicon);
            grammarWrapper.setGrammarEntries(grammarRuleGenerator.generate(lexicon));
        });
        return grammarWrapper;
    }

}
