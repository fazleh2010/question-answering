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

    public static void main(String[] args) throws IOException, FileNotFoundException, CsvException, Exception {
        File file = new File(inputDir + feedbackQAFile);
        CsvFile csvFile=new CsvFile(file);
        csvFile.readCsv();
    }

}
