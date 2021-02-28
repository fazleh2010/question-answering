/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util.io;

import com.opencsv.CSVWriter;
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
import java.util.ArrayList;
import java.util.List;
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
        InputStream is;String fileAsString=null;
        Integer index=0;
        try {
            is = new FileInputStream(fileName);
            BufferedReader buf = new BufferedReader(new InputStreamReader(is));
            String line = buf.readLine();
            StringBuilder sb = new StringBuilder();
            while (line != null) {
                System.out.println("line:"+line);
                sb.append(line).append("\n");
                line = buf.readLine();
                index=index+1;
            }
            fileAsString = sb.toString();
        } catch (Exception ex) {
            Logger.getLogger(FileUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("index:"+index);

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
    

}
