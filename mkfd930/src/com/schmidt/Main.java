package com.schmidt;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;


public class Main {


    String eType;
    String bypass;

    public String geteType() {
        return eType;
    }

    public void seteType(String eType) {
        this.eType = eType;
    }


    public static void main(String[] args) throws FileNotFoundException, IOException {

        CliArgs cliArgs     = new CliArgs(args);
        ZipUtils zipUtils   = new ZipUtils();
        Long theEntityKey   = 0L;
        String ssnValue     = "";
        Main main = new Main();


        //Change to config file entries
        String zipFromDirectory = "/tmp/FD930docs/templates/";
        String zipToDirectory = "/tmp/FD930docs/zipFiles/";
        String entityDirectory = "/tmp/FD930docs/templates/sentinel_entities/";
        String entityTemplateFile = "src/resource/entitytemplate.xml";
        String caseDestination = "/tmp/FD930docs/templates/sentinel_cases/";
        String caseSource = "src/resource/";
        String[] caseXmlFileNames = {"1019.xml"};


        // main.copyCaseToDestination(caseSource, caseDestination,caseXmlFileNames);

        //process commandline arguments
        main.processCliArgs(cliArgs);
        //Are we generating new entity for a zip?

        //default is the same no mods
        switch (main.geteType()) {
            case "new":

                theEntityKey = main.getKeyValue() + 1;
                break;
            case "same":

                theEntityKey = main.getKeyValue();
                break;
            case "old":

                theEntityKey = main.getUserKeyValue();
                break;
            default:
                System.out.println("Entity Default Same");
                theEntityKey = main.getKeyValue();
                break;

        }
        //default is the same no mods
        switch (main.getBypass()) {
            case "mod":
                System.out.println(" Will not Bypass SSN changed");
                ssnValue="987-12-0987";
                break;
            case "nomod":
                System.out.println("Will Bypass");
                ssnValue="123-45-6789";
                break;
            default:
                System.out.println("Will Bypass");
                ssnValue="123-45-6789";
                break;

        }


        System.out.println(String.join(" ", "read Key:", theEntityKey.toString()));
        if(!main.geteType().equals("old")){
            main.setKeyValue(theEntityKey);
        }



        File file = main.getTemplate(entityTemplateFile);

        main.updatekeyValueInTemplate(file, theEntityKey.toString(),ssnValue,entityDirectory);

        //Get the files in the directory in case thereis more than one.
        //cleanDirectory(directory, savevalue);
        File[] files = main.getFiles(entityDirectory);

        //make a  method out of this
        for (File file1 : files) {
            if (!file1.getName().equals(theEntityKey + ".xml")) {
                main.deleteFile(file1);
            }
        }
        //zip up directories zip
//        File[] zipFiles = main.getFiles(zipToDirectory);
//        int version= main.gethighestVersionInZipDirectory(zipFiles,theEntityKey.toString());
        final String anEntity=theEntityKey.toString();

        File dir = new File(zipToDirectory);
        File[] zipFiles = dir.listFiles((d, name) -> name.startsWith(anEntity));
        //make a  method out of this
        for (File zipFile : zipFiles) {
            System.out.println(zipFile.getName().replaceAll(theEntityKey.toString(),"").replaceAll("_","").replaceAll(".zip",""));
        }

        //zipUtils.zipFiles(zipFromDirectory, zipToDirectory+theEntityKey+"_"+version+".zip");


    }
private    int gethighestVersionInZipDirectory(File[] files,String identifier) {
    int version=0;

    for (File file1 : files) {
        System.out.println(file1.getName());
        StringTokenizer str = new StringTokenizer(file1.getName(),"_");

        while (str.hasMoreTokens()) {
            String eval=str.nextToken();
            if(!eval.equals(identifier)){
                System.out.println("Toekn result:"+eval.replaceAll(".zip",""));
            }


            //if (str.nextToken().equals(identifier)) {
                //version = Integer.parseInt(str.nextToken());
                //System.out.println(version);
            //}
        }

    }
    return version;
}
    private Long getKeyValue() {
        Long result;
        File file = new File("src/resource/fd.txt");

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String keyId = bufferedReader.readLine();
            Long keyLong = Long.parseLong(keyId);
            result = keyLong;

        } catch (FileNotFoundException e) {
            result = 0L;
            System.out.println(e);
        } catch (IOException e) {
            result = 0L;
            System.out.println(e);
        }
        return result;
    }

    //Allows the user to generate a new FD using abn existing producer Id
    private Long getUserKeyValue() {
        Scanner keyboard = new Scanner(System.in);
        System.out.println("Enter the existing entity ID to generate a zip:");
        Long entityLongInput = keyboard.nextLong();
        return entityLongInput;
    }


    private File[] getFiles(String pathName) {
        File folder = new File(pathName);
        File[] listOfFiles = folder.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
//            if (listOfFiles[i].isFile()) {
//                System.out.println("File " + listOfFiles[i].getName());
//            } else if (listOfFiles[i].isDirectory()) {
//                System.out.println("Directory " + listOfFiles[i].getName());
//            }
            return listOfFiles;
        }
        return listOfFiles;
    }

    private void deleteFile(File file) {
        if (file.delete())
            System.out.println("Delete Success " + file.getName());
        else
            System.out.println("Delete Failed " + file.getName());


    }

    private void setKeyValue(Long keyValue) throws FileNotFoundException, IOException {
        try {
            String fileName = "src/resource/fd.txt";
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"));
            bufferedWriter.write(keyValue.toString());
            bufferedWriter.close();

        } catch (FileNotFoundException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        }

    }

    private void updatekeyValueInTemplate(File file, String keyValue,String ssnValue, String entityDirectory) throws
            IOException, FileNotFoundException {

        String newString = keyValue;
        String oldContent = "";
        String keyPattern = "##keyValue##";
        String ssnPattern = "##ssnValue##";
       String fileName= keyValue + ".xml";
        String newContent;
        String contentOut;
        try {


            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();

            while (line != null) {
                oldContent = oldContent + line + System.lineSeparator();
                line = reader.readLine();
            }
             newContent = oldContent.replaceAll(keyPattern, newString);
            String updatedContent = newContent;


            if(ssnValue!=null||!ssnValue.isEmpty()){
                contentOut = updatedContent.replaceAll(ssnPattern, ssnValue);
            }
            else
                contentOut=updatedContent;


            reader.close();

            writeTemplateFile(keyValue+".xml", entityDirectory,contentOut);

        } catch (FileNotFoundException nfe) {
            System.out.println(nfe);
        }

    }
    private void writeTemplateFile(String fileName, String entityDirectory,String content)throws IOException {

//        System.out.println(entityDirectory);
//        System.out.println(content);
        try {
            File out = new File(entityDirectory + fileName);
            FileWriter writer = new FileWriter(out);
            writer.write(content);
            writer.close();
        }catch(IOException io){
            System.out.println(io);

        }
    }

    private void processCliArgs(CliArgs cliArgs) {

        if (cliArgs.switchPresent("-etype")) {
            String eTypeValue;

            String cliEtypeValue = cliArgs.switchValue("-etype", "same");
            if (cliEtypeValue.contains("new") || cliEtypeValue.contains("same") || cliEtypeValue.contains("old")) {
                System.out.println("cliType:"+cliEtypeValue);
                seteType(cliEtypeValue);
            } else {
                System.out.println("else cliType:"+cliEtypeValue);
                seteType("same");
            }
        } else
            seteType("same");


        if (cliArgs.switchPresent("-bypass")) {
            String byPass;

            String cliEtypeValue = cliArgs.switchValue("-bypass", "nomod");
            if (cliEtypeValue.contains("mod") || cliEtypeValue.contains("nomod")) {
                setBypass(cliEtypeValue);
            } else {
                seteType("nomod");
            }
        } else
            setBypass("nomod");
    }


    private File getTemplate(String entityTemplateFile) {
        return new File("src/resource/entitytemplate.xml");
    }


    //Copies the cases from the source tempalte to the desitnation for processing
    private void copyCaseToDestination(String caseSource, String caseDestination, String[] caseXmlFileNames) {
        CopyOption[] options = new CopyOption[]{StandardCopyOption.REPLACE_EXISTING};
        Path from;
        Path to;
        for (String caseName : caseXmlFileNames) {
            from = Paths.get(caseSource + caseName);
            to = Paths.get(caseDestination + caseName);
            try {
                Files.copy(from, to, options);
            } catch (IOException io) {
                System.out.println(io);
            }
        }
    }


    //Method to allow changing case files...
    private File setCasesTemplate(File file) {
        try {
            PrintWriter printWriter = new PrintWriter("/tmp/FD930docs/Deploy/sentinel_cases", "UTF-8");

            File template = new File("/tmp/FD930docs/Deploy/sentinel_cases");
            String caseTemplate = " some text";
            printWriter.write(caseTemplate);
            printWriter.close();
            return template;
        } catch (Exception e) {
            System.out.println(e);

        }
        return null;

    }

//    private String setEntityTemplate(File file) {
//        try {
//            String fileName = "c:\\temp\\FD930\\entity";
//            PrintWriter printWriter = new PrintWriter(fileName, "UTF-8");
//            String caseTemplate = "entity_id:##entity##";
//            printWriter.write(caseTemplate);
//            printWriter.close();
//
//
//        } catch (Exception e) {
//            System.out.println(e);
//
//        }
//        return null;
//
//    }

    private void createDirectories() {
        Path p1 = null;
        Path p2 = null;
        Path p3 = null;
        Path p4 = null;
        Path p5 = null;
        Path p6 = null;
        Path p7 = null;
        Path p8 = null;
        try {
            System.out.println(System.getProperty("os.name"));
            if (System.getProperty("os.name").equalsIgnoreCase("Mac OS X")) {
                p1 = Paths.get("/tmp/FD930docs");
                p2 = Paths.get("/tmp/FD930docs/archive");
                p3 = Paths.get("/tmp/FD930docs/Deploy");
                p4 = Paths.get("/tmp/FD930docs/Deploy/cases");
                p5 = Paths.get("/tmp/FD930docs/Deploy/entity");
                p6 = Paths.get("/tmp/FD930docs/templates");
                p7 = Paths.get("/tmp/FD930docs/templates/entity");
                p8 = Paths.get("/tmp/FD930docs/templates/cases");

            } else {
                p1 = Paths.get("C:\\FD930docs");
                p2 = Paths.get("C:\\FD930docs\\archive");
                p3 = Paths.get("C:\\FD930docs\\Deploy");
                p4 = Paths.get("C:\\FD930docs\\Deploy\\cases");
                p5 = Paths.get("C:\\FD930docs\\Deploy\\entity");
                p6 = Paths.get("C:\\FD930docs\\templates");
                p7 = Paths.get("C:\\FD930docs\\templates\\entity");
                p8 = Paths.get("C:\\FD930docs\\templates\\cases");

            }

            Files.createDirectories(p1);
            Files.createDirectories(p2);
            Files.createDirectories(p3);
            Files.createDirectories(p4);
            Files.createDirectories(p5);
            Files.createDirectories(p6);
            Files.createDirectories(p7);
            Files.createDirectories(p8);


            //create template files
//           setEntityTemplate();
//
//           setCasesTemplate();


        } catch (IOException e) {
            System.out.println(e);
        }

    }

    private void deleteSetup() {
        try {
            Path p1 = Paths.get("C:\\FD930docs");
            Path p2 = Paths.get("C:\\FD930docs\\archive");
            Path p3 = Paths.get("C:\\FD930docs\\Deploy");
            Path p4 = Paths.get("C:\\FD930docs\\Deploy\\cases");
            Path p5 = Paths.get("C:\\FD930docs\\Deploy\\entity");
            Path p6 = Paths.get("C:\\FD930docs\\templates");
            Path p7 = Paths.get("C:\\FD930docs\\templates\\entity");
            Path p8 = Paths.get("C:\\FD930docs\\templates\\cases");
            Files.delete(p1);
            Files.delete(p2);
            Files.delete(p3);
            Files.delete(p4);
            Files.delete(p5);
            Files.delete(p6);
            Files.delete(p7);
            Files.delete(p8);


            //create template files


        } catch (IOException e) {
            System.out.println(e);
        }

    }

    private void setUp() {
        System.out.println((getKeyValue()));

    }


    public String getBypass() {
        return bypass;
    }

    public void setBypass(String bypass) {
        this.bypass = bypass;
    }
}