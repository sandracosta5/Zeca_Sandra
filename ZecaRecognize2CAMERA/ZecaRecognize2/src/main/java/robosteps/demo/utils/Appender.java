/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/////////////////////////////preciso ver ainda. class em cru. será para unir todos os .csv que quiser
package robosteps.demo.utils;

import java.io.*;


// this class allows only listings of cvs files 
// when reading directory etc.
public class Appender {
    
    private BufferedReader console; // used to prompt users for input
    private File directory; // directory containing csv files
    private File[] files;     // all cvs files in a given directory
    private final String fileName = "appender_app.csv"; // name of the file containing final output
    
    public Appender() {
        // initialize console to start prompting user for input
        initConsole();
        // prompt user to enter directory containing files
        System.out.print("Please Enter Directory [empty for current directory]: ");
        String dir = this.getInput();
        // check user input
        if ("".equals(dir)) {
            // if user didn't enter anything...use current directory
            this.directory = new File(System.getProperty("user.dir"));
        } //-- end if block
        else {
            // use user input as directory
            this.directory = new File(dir);
        } //-- ends else block
        
        // check that user's input points to a directory that exists 
        // in the file system
        if (this.directory.exists() && this.directory.isDirectory()) {
            // check that the directory is both readable and writable
            if (!this.directory.canRead() && !this.directory.canWrite()){
                System.out.println("\n++++++++++++++++++++++++++++++++++++++++++++++++++");
                System.out.println("Error:\nApplication will now terminate because:");
                System.out.println("Directory: " + this.directory.getAbsolutePath());
                System.out.println("Either; NOT WRITABLE or NOT READABLE");
                System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++\n");
                System.exit(-100);
            } //-- end if block
            // read directory and check for any csv files
            this.readSelectedDirectory();
            // read all csv files and append data to fileName
            this.readAndAppend();
            // close console once done
            this.closeConsole();
        } //-- end if block
        else {
            System.out.println("\n++++++++++++++++++++++++++++++++++++++++++++++++++");
            System.out.println("Error:\nApplication will now terminate because:");
            System.out.println("Directory: " + this.directory.getAbsolutePath());
            System.out.println("Does not exist!!!!");
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++\n");
            System.exit(-100);
        } //-- ends else block
    } //-- ends class constructor
    
    public void readSelectedDirectory() {
        // inform the user of the working directory
        System.out.println("Now Working in: " + this.directory.getAbsolutePath());
        // get a list of all csv files in a given directory
        // Note: use of CVSFilter declared below this class
        this.files = this.directory.listFiles(new CVSFilter());
        // if a list of csv files exist, continue...otherwise terminate application
        if (files.length<2){
            System.out.println("\n++++++++++++++++++++++++++++++++++++++++++++++++++");
            System.out.println("Application will now terminate as the number of ");
            System.out.println("files to read is zero or below 2 files");
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++\n");
        } //-- end if block
        
    } //-- ends readSelectedDirectory
    
    private void readAndAppend() {
        File fileOut = new File(this.directory, fileName);
        // if file exists...delete it...so it is ready for a fresh input
        if (fileOut.exists()){
            fileOut.delete(); 
        } //-- end if block
        // now create it 
        try {
            fileOut.createNewFile();
            System.out.println("File: '" + fileOut.getAbsolutePath() + "' created successfully ;)" );
        } //-- ends try block
        catch (IOException err ) {
            err.printStackTrace(System.err);
        } //-- ends catch block
        
        // initialize buffer and start reading data
        BufferedWriter  out = null;
        try {
            out = new BufferedWriter(new FileWriter(fileOut,true));
            for(File input : this.files) {
                if (fileName.equalsIgnoreCase(input.getName())){
                    continue;
                } //-- end if block
                BufferedReader in = null;
                try {
                    in = new BufferedReader(new FileReader(input));
                    System.out.println("++ Now reading: '" + input.getName() + "'");
                    String txt = null;
                    while ((txt=in.readLine())!=null) {
                        //System.out.println(in.readLine());
                        out.write(txt);
                        out.newLine();
                        out.flush();
                    } //-- ends while loop
                    in.close();
                    System.out.println("-- Finished Writing: '" + input.getName() + "'");
                } //-- ends try
                catch (IOException e ) {
                    e.printStackTrace(System.err);
                } //-- ends catch block
            } //-- ends for loop
        } //-- ends try block
        catch (FileNotFoundException err ) {
            err.printStackTrace(System.err);
        } //-- ends catch block
        catch (IOException err ) {
            err.printStackTrace(System.err);
        } //-- ends catch block
        finally {
            try {
                if(out!=null) {
                    out.close();
                } //-- ends if block
            } //-- ends try block
            catch (IOException ignored ) {
                //ignored.printStackTrace(System.err);
            } //-- ends catch block
        } //-- ends finally block
        
    } //-- ends readAndAppend
    
    private void initConsole() {
        if (this.console==null){
            this.console = new BufferedReader(new InputStreamReader(System.in));
        } //-- end if block
    } //-- ends initConsole
    
    private void closeConsole() {
        if (this.console!=null){
            try {
                this.console.close();
            } //-- ends try block
            catch (IOException ignored ) {
                // ignored exception....but you can uncomment the line below
                // ignored.printStackTrace(System.err);
            } //-- ends catch block
        } //-- end if block
    } //-- ends close
    
    private String getInput() {
        String input = new String("");
        try {
            input = this.console.readLine();
        } //-- ends try block
        catch (IOException e ) {
            e.printStackTrace(System.err);
        } //-- ends catch block
        return input;
    } //-- ends getInput
    
    public static void main(String[] args) {
        new Appender();
    } //-- ends class method main
    
} //-- ends class definition
class CVSFilter implements FilenameFilter {
    public boolean accept(File dir, String name) {
        return (name.endsWith(".csv"));
    } //-- ends accept
} //-- ends class definition 