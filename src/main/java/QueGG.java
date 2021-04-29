import com.opencsv.CSVWriter;
import eu.monnetproject.lemon.LemonModel;
import grammar.generator.BindingResolver;
import grammar.generator.GrammarRuleGeneratorRoot;
import grammar.generator.GrammarRuleGeneratorRootImpl;
import grammar.read.questions.ReadAndWriteQuestions;
import static grammar.read.questions.ReadAndWriteQuestions.ATTRIBUTE_ADJECTIVE;
import static grammar.read.questions.ReadAndWriteQuestions.GRADABLE_ADJECTIVE_SUPERLATIVE;
import grammar.read.questions.SparqlQuery;
import grammar.read.questions.Trie;
import grammar.structure.component.DomainOrRangeType;
import grammar.structure.component.FrameType;
import grammar.structure.component.GrammarEntry;
import grammar.structure.component.GrammarWrapper;
import grammar.structure.component.Language;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import java.util.Set;
import java.util.logging.Level;
import util.io.FileUtils;
import static grammar.read.questions.ReadAndWriteQuestions.GRADABLE_ADJECTIVE_ALL;

//index location /var/www/html/
@NoArgsConstructor
public class QueGG implements ParameterConstant{
    //private static final Logger LOG = LogManager.getLogger(QueGG.class);
    private static String inputDir = "src/main/resources/test/input/";

    private static String BaseDir = "";
    private static String outputDir = BaseDir + "src/main/resources/test/output/";
    //main source location :"src/main/resources/"
    public static String QUESTION_ANSWER_LOCATION = BaseDir + "questions/";
    public static String QUESTION_ANSWER_FILE = "questions.txt";
    public static String QUESTION_ANSWER_CSV_FILE = "questions.csv";
    public static String GENERATE_JSON = "GENERATE_JSON";
    public static String CREATE_CSV = "CreateCsv";
    public static String SEARCH = "SEARCH";
    public static String ENTITY_LABEL_LIST = "ENTITY_LABEL_LIST";
    public static String COMBINED_CSV = "COMBINED_CSV";
    //private static String entityDir = "src/main/resources/test/entity/";
    public static String combinedCsvFilesDir = "/home/elahi/grammar/new/questions/";
    public static String allCsvFile = "all.csv";


   
    //private static List<String> classNames = Arrays.asList("actor","country","actor","architecturalstructure","person");;

    public static void main(String[] args) throws IOException {
        QueGG queGG = new QueGG();
        Language language = Language.stringToLanguage("EN");
        String questionAnswerFile = QUESTION_ANSWER_LOCATION + File.separator + QUESTION_ANSWER_CSV_FILE;
        Integer task = 1;
        String content = "";
        String search = null;
        search=GENERATE_JSON+CREATE_CSV;
        String lexicalEntry=null;
        //search=COMBINED_CSV;
        //search = CreateCsv;
        //search = ENTITY_LABEL_LIST;
        //search = CreateCsv;
        search=CREATE_CSV;
        // search=ATTRIBUTE_ADJECTIVE;
        //search=GENERATE_JSON+ATTRIBUTE_ADJECTIVE;
        search=COMBINED_CSV;
        //search=GRADABLE_ADJECTIVE_SUPERLATIVE;
        
     
        if (search.equals(ENTITY_LABEL_LIST)) {
            String[] files = new File(inputDir).list();
             Arrays.sort(files);
            for (String fileName : files) {
                if (fileName.contains(ENTITY_LABEL_LIST)||fileName.contains(".ttl")) {
                    continue;
                }
                String inputFileName = inputDir + fileName;
                String outputFileName = inputDir + ENTITY_LABEL_LIST + "_" + fileName;

                FileUtils.createEntityRdfsLevel(inputFileName, outputFileName, fileName);
            }

        }
        else if (search.equals(GENERATE_JSON)) {
            try {
                queGG.init(language, inputDir, outputDir);
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(QueGG.class.getName()).log(Level.SEVERE, null, ex);
            }

        }else if (search.equals(GENERATE_JSON+CREATE_CSV)) {
            try {
                queGG.init(language, inputDir, outputDir);
                List<File> fileList = FileUtils.getFiles(outputDir, "grammar_FULL_DATASET_EN", ".json");
                if (fileList.isEmpty()) {
                    throw new Exception("No files to process for question answering system!!");
                }
                ReadAndWriteQuestions readAndWriteQuestions = new ReadAndWriteQuestions(questionAnswerFile,search);
                readAndWriteQuestions.readQuestionAnswers(fileList,inputDir);
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(QueGG.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        else if (search.equals(CREATE_CSV)) {
            try {
                List<File> fileList = FileUtils.getFiles(outputDir, "grammar_FULL_DATASET_EN", ".json");
                if (fileList.isEmpty()) {
                    throw new Exception("No files to process for question answering system!!");
                }
                ReadAndWriteQuestions readAndWriteQuestions = new ReadAndWriteQuestions(questionAnswerFile,search);
                readAndWriteQuestions.readQuestionAnswers(fileList,inputDir);
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(QueGG.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        else if (search.equals(ATTRIBUTE_ADJECTIVE)||search.equals(GRADABLE_ADJECTIVE_SUPERLATIVE)) {
            try {
                List<File> fileList = FileUtils.getFiles(outputDir, "grammar_FULL_DATASET_EN", ".json");
                if (fileList.isEmpty()) {
                    throw new Exception("No files to process for question answering system!!");
                }
                ReadAndWriteQuestions readAndWriteQuestions = new ReadAndWriteQuestions(questionAnswerFile,search);
                readAndWriteQuestions.readQuestionAnswers(fileList,inputDir);
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(QueGG.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
         else if (search.contains(GRADABLE_ADJECTIVE_SUPERLATIVE)) {
            try {
                List<File> fileList = FileUtils.getFiles(outputDir, "grammar_FULL_DATASET_EN", ".json");
                if (fileList.isEmpty()) {
                    throw new Exception("No files to process for question answering system!!");
                }
                ReadAndWriteQuestions readAndWriteQuestions = new ReadAndWriteQuestions(questionAnswerFile,search);
                readAndWriteQuestions.readQuestionAnswers(fileList,inputDir);
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(QueGG.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
         else if (search.equals(GENERATE_JSON+ATTRIBUTE_ADJECTIVE)) {
             queGG.init(language, inputDir, outputDir);
            try {
                List<File> fileList = FileUtils.getFiles(outputDir, "grammar_FULL_DATASET_EN", ".json");
                if (fileList.isEmpty()) {
                    throw new Exception("No files to process for question answering system!!");
                }
                ReadAndWriteQuestions readAndWriteQuestions = new ReadAndWriteQuestions(questionAnswerFile,ATTRIBUTE_ADJECTIVE);
                readAndWriteQuestions.readQuestionAnswers(fileList,inputDir);
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(QueGG.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        /*else if (search.equals(COMBINED_CSV)) {
            File file = new File(combinedCsvFilesDir);
            String[] files = file.list();
            List<String> fileList = new ArrayList<String>();
            for (String fileStr : files) {
                fileList.add(fileStr);
            }
            CSVWriter csvWriter = new CSVWriter(new FileWriter(new File(combinedCsvFilesDir + allCsvFile), true));
            Integer fileNumber = 1;
            Collections.sort(fileList);
            for (String partCsv : fileList) {
                Boolean flag = false;

                if (partCsv.equals(allCsvFile)) {
                    continue;
                }
                if (!partCsv.contains(".csv")) {
                    continue;
                }
                String readFileName = combinedCsvFilesDir + partCsv;
                System.out.println("readFileName:" + readFileName);
                if (fileNumber == 1) {
                    flag = true;
                }

                CsvFile.readAndAppendCsv(readFileName, csvWriter, flag);
                fileNumber = fileNumber + 1;
            }
        } */
        else if (search.equals(COMBINED_CSV)) {
            File file = new File(combinedCsvFilesDir);
            String[] files = file.list();
            List<String> fileList = new ArrayList<String>();
            for (String fileStr : files) {
                fileList.add(fileStr);
            }
            CSVWriter csvWriter = new CSVWriter(new FileWriter(new File(combinedCsvFilesDir + allCsvFile), true));
            Integer fileNumber = 1;
            Collections.sort(fileList);
             Integer count = 0;
            for (String partCsv : fileList) {
                Boolean flag = false;

                if (partCsv.equals(allCsvFile)) {
                    continue;
                }
                if (!partCsv.contains(".csv")) {
                    continue;
                }
                String readFileName = combinedCsvFilesDir + partCsv;
                System.out.println("readFileName:" + readFileName);
                if (fileNumber == 1) {
                    flag = true;
                }

                count=CsvFile.readAndAppendCsv(readFileName, csvWriter, flag,count);
                fileNumber = fileNumber + 1;
            }
            System.out.println("count::"+count);
        } 
        else if (search.contains(SEARCH)) {
            if (args.length < 3) {
                System.out.println("less number of parameters!!");
            } else {
                language = Language.stringToLanguage(args[1]);
                questionAnswerFile = args[2];
                String tokenStr = args[3];
                ReadAndWriteQuestions readAndWriteQuestions;
                try {
                    readAndWriteQuestions = new ReadAndWriteQuestions(questionAnswerFile,search);
                    readAndWriteQuestions.createTrieCsv();
                    /*List autoCompletionList = readAndWriteQuestions.getTrie().performAutocomplete("Give me");
                    for (int i = 0; i < autoCompletionList.size(); i++) {
                        System.out.println(i + " " + autoCompletionList.get(i));
                    }*/
                } catch (Exception ex) {
                    java.util.logging.Logger.getLogger(QueGG.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
            /*try {
                ReadAndWriteQuestions readAndWriteQuestions = new ReadAndWriteQuestions(questionAnswerFile);
                readAndWriteQuestions.createTrieCsv();
                List autoCompletionList = readAndWriteQuestions.getTrie().autocomplete("Give me");
                for (int i = 0; i < autoCompletionList.size(); i++) {
                    System.out.println(i + " " +autoCompletionList.get(i));
                }
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(QueGG.class.getName()).log(Level.SEVERE, null, ex);
            }*/


    }
    
    /* String objectUrl = "http://dbpedia.org/ontology/largestCity";
        String propertyUrl = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
        String subject = "http://dbpedia.org/resource/Province_of_Saxony";
        String object = "http://dbpedia.org/resource/Russia";
        String sparqlQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX res: <http://dbpedia.org/resource/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?uri WHERE { ?uri rdf:type dbo:City ; dbo:country res:" + "Denmark" + " }";
        SparqlQuery sparqlGeneration = new SparqlQuery(sparqlQuery);
        System.out.println(sparqlGeneration.objectOfProperty);
    */

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
//1000000
//1060233
//554818
    
    //recent data
    //1797116
    
    //noun 1060234
    //585845
    //151040
    
}
