
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
    //private static final Logger LOG = LogManager.getLogger(QueGG.class);
    private static String inputDir = "src/main/resources/test/input/";
    private static String BaseDir = "";
    private static String outputDir = BaseDir + "src/main/resources/test/output/";
    public static String QUESTION_ANSWER_LOCATION = BaseDir + "src/main/resources/";
    public static String QUESTION_ANSWER_FILE = "questions.txt";
    public static String QUESTION_ANSWER_CSV_FILE = "questions.csv";

    public static void main(String[] args) {
        QueGG queGG = new QueGG();
        Language language = Language.stringToLanguage("EN");
        String questionAnswerFile = QUESTION_ANSWER_LOCATION + File.separator + QUESTION_ANSWER_CSV_FILE;
        Integer task = 3;
        String content = "";

        if (task.equals(1)) {
            try {
                queGG.init(language, inputDir, outputDir);
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(QueGG.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else if (task.equals(2)) {
            try {
                ReadAndWriteQuestions readAndWriteQuestions = new ReadAndWriteQuestions(questionAnswerFile);
                readAndWriteQuestions.readQuestionAnswers(outputDir, "grammar_FULL_DATASET_EN");
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(QueGG.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else if (task.equals(3)) {
            try {
                ReadAndWriteQuestions readAndWriteQuestions = new ReadAndWriteQuestions(questionAnswerFile);
                readAndWriteQuestions.createTrieCsv();
                List autoCompletionList = readAndWriteQuestions.getTrie().autocomplete("Give me");
                for (int i = 0; i < autoCompletionList.size(); i++) {
                    System.out.println(i + " " +autoCompletionList.get(i));
                }
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(QueGG.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

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
