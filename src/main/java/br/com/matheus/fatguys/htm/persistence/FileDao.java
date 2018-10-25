/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.matheus.fatguys.htm.persistence;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author y2gh
 */
public class FileDao {

    private File outputFile;
    private File inputFile;
    private PrintWriter pw;
    private BufferedReader bufferedReader;
    private String outputFileName;
    private String inputFileName;
    private boolean verbose;

    public FileDao(String outputFileName, String inputFileName) {
        try {
            this.outputFileName = outputFileName;
            if (outputFileName != null) {
                this.outputFileName = outputFileName;
                outputFile = new File(this.outputFileName);
                pw = new PrintWriter(new FileWriter(outputFile));
            }

            if (inputFileName != null) {
                this.inputFileName = inputFileName;
                if (inputFileName != null) {
                    inputFile = new File(this.inputFileName);
                    FileReader fileReader = new FileReader(inputFile);
                    bufferedReader = new BufferedReader(fileReader);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public String getInputFileName() {
        return inputFileName;
    }

    public void closeFiles() throws IOException {
        if(pw!=null){
            pw.flush();
            pw.close();            
        }
        if (bufferedReader != null) {
            bufferedReader.close();
        }
    }

    public String nextLine() throws IOException {
        return bufferedReader.readLine();
    }

    public void persistLine(String line) {
        pw.println(line);
        pw.flush();
    }

}
