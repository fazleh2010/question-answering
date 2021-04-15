/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util.io;

import com.opencsv.CSVWriter;
import grammar.read.questions.SparqlQuery;
import grammar.read.questions.UriLabel;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author elahi
 */
public class FileUtils {

    public static void stringToFile(String content, String fileName)
            throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write(content);
        writer.close();

    }

    public static String fileToString(String fileName) {
        String fileAsString = null;
        Integer index = 0;
        try {
            InputStream is = new FileInputStream(fileName);
            BufferedReader buf = new BufferedReader(new InputStreamReader(is));
            String line = buf.readLine();
            StringBuilder sb = new StringBuilder();
            while (line != null) {
                System.out.println("line:" + line);
                sb.append(line).append("\n");
                line = buf.readLine();
                index = index + 1;
            }
            fileAsString = sb.toString();
        } catch (Exception ex) {
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("index:" + index);

        return fileAsString;
    }

    public static List<File> getFiles(String fileDir, String category, String extension) {
        String[] files = new File(fileDir).list();
        List<File> selectedFiles = new ArrayList<File>();
        for (String fileName : files) {
            if (fileName.contains(category) && fileName.contains(extension)) {
                selectedFiles.add(new File(fileDir + fileName));
            }
        }

        return selectedFiles;

    }

    public static void stringToCSVFile(List<String[]> csvRows, String questionAnswerFile) {

        if (csvRows.isEmpty()) {
            System.out.println("writing csv file failed!!!");
            return;
        }
        try ( CSVWriter writer = new CSVWriter(new FileWriter(questionAnswerFile))) {
            writer.writeAll(csvRows);
        } catch (IOException ex) {
            System.out.println("writing csv file failed!!!" + ex.getMessage());
        }
    }

    public static void stringToAppendCSVFile(CSVWriter writer, String[] nextLine, String questionAnswerFile) {
        if (nextLine.length < 1) {
            System.out.println("appending csv file failed!!!");
            return;
        }
        writer.writeNext(nextLine);
    }

    public static void createEntityRdfsLevel(String inputFileName, String outputFileName,String className) {
        Set<String> set = new TreeSet<String>();
        BufferedReader reader;
        String line = "";
        Integer index=0;
        try {
            reader = new BufferedReader(new FileReader(inputFileName));
            line = reader.readLine();
            while (line != null) {
                line = reader.readLine();
                if (line != null) {
                    line = line.strip().trim();
                    line = line.replace("<", "");
                    line = line.replace(">", "");
                    line = line.strip().trim();
                    String entityUrl = line;
                    String sparqlQueryStr = SparqlQuery.setSparqlQueryForLabel(entityUrl);
                    SparqlQuery sparqlQuery = new SparqlQuery(sparqlQueryStr);
                    if (sparqlQuery.getObject() != null) {
                        System.out.println("className:"+className+" "+index+" entityUrl:" + entityUrl + " object:" + sparqlQuery.getObject());
                        appendtoFile(outputFileName, entityUrl + "=" + sparqlQuery.getObject());
                    }
                }
               index=index+1;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    
    public static void appendtoFile(String fileName,String textToAppend) throws IOException {
        FileWriter fileWriter = new FileWriter(fileName, true); //Set true for append mode
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println(textToAppend); 
        printWriter.close();
    }

    public static List<UriLabel> getUriLabels(File classFile) {
        List<UriLabel> uriLabels = new ArrayList<UriLabel>();
        Set<String> set = new TreeSet<String>();
        BufferedReader reader;
        String line = "";
        try {
            reader = new BufferedReader(new FileReader(classFile));
            line = reader.readLine();
            while (line != null) {
                line = reader.readLine();
                if (line != null) {
                    line = line.strip().trim();
                    if (line.contains("=")) {
                        String uri = line.split("=")[0];
                        String label = line.split("=")[1];
                        UriLabel uriLabel = new UriLabel(uri, label);
                        uriLabels.add(uriLabel);
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return uriLabels;
    }

}
