/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author elahi
 */
public class CsvFile {

    public static Integer TIME_STAMP = 0;
    public static Integer NAME = 1;
    public static Integer Q1_ques = 2;
    public static Integer Q1_question_result = 3;
    public static Integer Q1_sparql = 4;
    public static Integer Q1_answer = 5;
    public static Integer Q1_answer_result = 6;
    private static String inputDir = "src/main/resources/";
    private static String feedbackQAFile = "FeedbackQA.xlsx";
    

    private File filename = null;
    public String[] header = null;
    private Map<String, List<String[]>> wordRows = new TreeMap<String, List<String[]>>();
    private List<String[]> rows = new ArrayList<String[]>();

    public CsvFile(File filename) {
        this.filename = filename;

    }

    public void readCsv() {
        List<String[]> rows = new ArrayList<String[]>();
        Stack<String> stack = new Stack<String>();
        CSVReader reader;
        try {
            reader = new CSVReader(new FileReader(filename));
            rows = reader.readAll();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CsvFile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CsvFile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CsvException ex) {
            Logger.getLogger(CsvFile.class.getName()).log(Level.SEVERE, null, ex);
        }

        Integer index = 0;
        String word = null;
        for (String[] row : rows) {
            if (index == 0) {
                this.header = row;
            } else {

                word = row[0].trim().strip();
                System.out.println("word:" + word);
            }

            index = index + 1;
        }
    }

    public void writeCSV(List<String[]> csvData) {
        if (csvData.isEmpty()) {
            System.out.println("writing csv file failed!!!");
            return;
        }
        try ( CSVWriter writer = new CSVWriter(new FileWriter(this.filename))) {
            writer.writeAll(csvData);
        } catch (IOException ex) {
            System.out.println("writing csv file failed!!!" + ex.getMessage());
        }
    }

    public File getFilename() {
        return filename;
    }

    public String[] getQaldHeader() {
        return this.header;
    }

    public Map<String, List<String[]>> getRow() {
        return wordRows;
    }
    
    public static Integer readAndAppendCsv(String filename, CSVWriter csvWriter,Boolean flag,Integer count) {
        List<String[]> rows = new ArrayList<String[]>();
        Stack<String> stack = new Stack<String>();
        CSVReader reader;
        try {
            reader = new CSVReader(new FileReader(filename));
            rows = reader.readAll();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CsvFile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CsvFile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CsvException ex) {
            Logger.getLogger(CsvFile.class.getName()).log(Level.SEVERE, null, ex);
        }

        Integer index = 1;
        String word = null;
        for (String[] row : rows) {
            String id=modify(row[0]);
                String question=modify(row[1]);
                String sparql=modify(row[2]);
                String answer=modify(row[3]);
                System.out.println("fileName:"+filename+" id:"+id+" question:"+question+" sparql:"+sparql+" answer:"+answer);
                if(answer.contains("no answer found"))
                    continue;
                if(answer.isBlank())
                    continue;
                 if(answer.isEmpty())
                    continue;
                String[] record = {id,question,sparql,answer,"nounPP"};
            if (index == 1&& flag) {
                 csvWriter.writeNext(record);
            }
            else if (index == 1&& !flag) {
                 ;
            }
            
            else {
                csvWriter.writeNext(record);
            }
            index = index + 1;
        }
        return count+rows.size();
    }
    
    private static String modify(String string) {
        string=string.stripTrailing();
        string=string.stripLeading();
        string="\""+string+"\"";
        return string;
    }

    public static void main(String[] args) throws IOException, FileNotFoundException, CsvException, Exception {
        String questionDir = "/home/elahi/grammar/new/questions/";
        String allCsvFile = "questions.csv";

        File file = new File(questionDir);
        String[] files = file.list();
        List<String> fileList = new ArrayList<String>();
        for (String fileStr : files) {
            fileList.add(fileStr);
        }
        CSVWriter csvWriter = new CSVWriter(new FileWriter(new File(questionDir + allCsvFile), true));
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
            String readFileName = questionDir + partCsv;
            System.out.println("readFileName:" + readFileName);
            if (fileNumber == 1) {
                flag = true;
            }

            count = readAndAppendCsv(readFileName, csvWriter, flag, count);
            fileNumber = fileNumber + 1;
        }
         System.out.println("count::" + count);
    }


}
