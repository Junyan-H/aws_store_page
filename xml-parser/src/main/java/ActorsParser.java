import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import java.sql.DriverManager;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.xml.XMLConstants;
import org.xml.sax.InputSource;
import java.io.InputStreamReader;
import java.io.FileInputStream;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;


public class ActorsParser {
    String xmlFile;
    int numDuplicateActors = 0;
    HashMap<String, Actor> actorsMap = new HashMap<String, Actor>();
    Document dom;


    public HashMap<String, Actor> getActorMap() {
        return actorsMap;
    }

    public void runParser(){
        //parse xmpl file and get dom object
        parseXmlFile();
        //get each actor element and create a actor object
        parseDocument();
        //iterate through the list and print data
        printData();
        //add actor data to database
//        addDataToSQL();
    }

    private void parseXmlFile(){
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            InputSource inputSource = new InputSource(new InputStreamReader(new FileInputStream("stanford-movies/actors63.xml"), "ISO-8859-1"));
            dom = documentBuilder.parse(inputSource);
        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }


    private void parseDocument(){
        // get the document root Element
        Element documentElement = dom.getDocumentElement();
        // get a nodelist of actors elements parse each into actor objects
        NodeList nodelist = documentElement.getElementsByTagName("actor");

        // Clear output file first
        try (FileWriter writer = new FileWriter("StarsDuplicate.txt", false)) {
        } catch (IOException e) {
            System.out.println("No file, no need to clear.");
        }

        for(int i = 0; i < nodelist.getLength(); i++){
            //grab actor element
            Element element = (Element) nodelist.item(i);
            //grab actor object
            Actor actor = parserActor(element);

            if(!actorsMap.containsKey(actor.getName())) {
                this.actorsMap.put(actor.getName(), actor);
            } else {
                numDuplicateActors += 1;
                try (BufferedWriter writer = new BufferedWriter(new FileWriter("StarsDuplicate.txt", true))) {
                    writer.write(actor.toString());
                    writer.newLine();
                } catch (IOException e) {
                    System.out.println("An error occurred while writing to the file: " + e.getMessage());
                }
            }
        }
    }

    private Actor parserActor(Element element){
        //param needed for actors
        String name = getTextValue(element, "stagename");
        String birthYear;
        try {
            birthYear = getTextValue(element, "dob");
        } catch (NullPointerException e){
            birthYear = null;
        }

//        System.out.println("INSIDE CREATING ACTOR");
//        System.out.println(name);
//        System.out.println(birthYear);

        //filtering for birthyear
        try {
            birthYear = birthYear.substring(0,4);
            Integer.parseInt(birthYear);
        } catch(NumberFormatException e) {
            birthYear = null;
        } catch(StringIndexOutOfBoundsException e){
            birthYear = null;
        } catch (NullPointerException e){
            birthYear = null;
        }

        // id is generated when we add to list
        return new Actor(name, birthYear);
    }

    private String getTextValue(Element element, String tagName) {
        String textVal = null;
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            // here we expect only one <Name> would present in the <Employee>
            textVal = nodeList.item(0).getFirstChild().getNodeValue();
        }
        return textVal;
    }

    /**
     * Calls getTextValue and returns a int value
     */
    private int getIntValue(Element ele, String tagName) {
        // in production application you would catch the exception
        return Integer.parseInt(getTextValue(ele, tagName));
    }

    private void printData() {
//        for (Actor actor : actors) {
//            System.out.println("\t" + actor.toString());
//        }
        System.out.println("Total parsed actors: " + this.actorsMap.size());
        System.out.println("Num duplicate actors: " + this.numDuplicateActors);
    }

//    private void addDataToSQL() {
//        // prebuild sql statement add it all together
//        // use BatchInsert Technique
//        System.out.println("Adding data to SQL...(ADDING TO TEST TABLE)");
//        Connection connection = null;
//        try {
//            Class.forName("com.mysql.cj.jdbc.Driver");
//            connection = DriverManager.getConnection("jdbc:mysql:///moviedb?autoReconnect=true&useSSL=false",
//                    "mytestuser", "My6$Password");
//        } catch (Exception e) {
//            System.out.println("Exception: " + e);
//        }
//
//        PreparedStatement psInsertRecord = null;
//        String sqlInsertRecord = null;
//        int[] iNoRows = null;
//        sqlInsertRecord = "CALL add_star_test_version(?, ?)";
//        try {
//            connection.setAutoCommit(false);
//            psInsertRecord = connection.prepareStatement(sqlInsertRecord);
//
//            for (Map.Entry<String, Actor> entry : actorsMap.entrySet()) {
//                String key = entry.getKey();
//                String value = entry.getValue().getDob();
//                psInsertRecord.setString(1, key);
//                psInsertRecord.setString(2, value);
//                psInsertRecord.addBatch();
//            }
//
//            iNoRows = psInsertRecord.executeBatch();
//            connection.commit();
//            System.out.println("Done");
//        }
//        catch (Exception e) {
//            System.out.println("Exception: " + e);
//        }
//    }

//    public static void main(String[] args) {
//        ActorsParser actorsParser = new ActorsParser();
//        actorsParser.runParser();
//    }
}

//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.NodeList;
//import org.xml.sax.SAXException;
//
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//
//public class ActorsParser {
//
//    //url for xml file
//    String xmlFile;
//
//    //list of actors
//    List<Actor> actors = new ArrayList<>();
//
//    HashMap <String, Actor>  actorMap = new HashMap<String, Actor>();
//
//    //number of dups
//    int dups = 0;
//
//    //Dom doc
//    Document dom;
//
//    public void runParser(){
//        //parse xmpl file and get dom object
//        parseXmlFile();
//
//        //get each actor element and create a actor object
//        parseDocument();
//
//        //iterate through the list and print data
//        printData();
//    }
//
//    //
//    private void parseXmlFile(){
//        //document factory
//        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
//
//        try{
//            //document builder
//            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
//
//            //parse using builder to get Dom representation of the xml file
//            dom = documentBuilder.parse("actors63.xml");
//
//        }catch (ParserConfigurationException | SAXException | IOException error) {
//            error.printStackTrace();
//        }
//
//    }
//
//
//    private void parseDocument(){
//        // get the document root Element
//        Element documentElement = dom.getDocumentElement();
//
//        //get a nodelist of actors elements parse each into actor objects
//        NodeList nodelist = documentElement.getElementsByTagName("actor");
//        for(int i = 0; i < nodelist.getLength(); i++){
//
//            //grab actor element
//            Element element = (Element) nodelist.item(i);
//
//            //grab actor object
//            Actor actor = parserActor(element);
//
//            if (this.actorMap.containsKey(actor.getName())){
//                this.dups +=1;
//            } else {
//                this.actorMap.put(actor.getName(), actor);
//                this.actors.add(actor);
//            }
//
//            //add it to list
//
//        }
//    }
//
//    private Actor parserActor(Element element){
//        //param needed for actors
//        String name = getTextValue(element, "stagename");
//        String birthYear;
//        try{
//            birthYear = getTextValue(element, "dob");
//        }catch (NullPointerException e){
//            birthYear = null;
//        }
//
//
//        System.out.println("INSIDE CREATING ACTOR");
//        System.out.println(name);
//        System.out.println(birthYear);
//
//        //filtering for birthyear
//        try {
//            birthYear = birthYear.substring(0,4);
//            Integer.parseInt(birthYear);
//        }catch(NumberFormatException e) {
//            birthYear = null;
//        }catch(StringIndexOutOfBoundsException e){
//            birthYear = null;
//        }catch (NullPointerException e){
//            birthYear = null;
//        }
//
//        //id is generated when we add to list
//        return new Actor(name,birthYear);
//
//    }
//
//    /**
//     * It takes an XML element and the tag name, look for the tag and get
//     * the text content
//     * i.e for <Employee><Name>John</Name></Employee> xml snippet if
//     * the Element points to employee node and tagName is name it will return John
//     */
//    private String getTextValue(Element element, String tagName) {
//        String textVal = null;
//        NodeList nodeList = element.getElementsByTagName(tagName);
//        if (nodeList.getLength() > 0) {
//            // here we expect only one <Name> would present in the <Employee>
//            textVal = nodeList.item(0).getFirstChild().getNodeValue();
//        }
//        return textVal;
//    }
//
//    /**
//     * Iterate through the list and print the
//     * content to console
//     */
//    private void printData() {
//
//        System.out.println("Total parsed " + this.actorMap.size() + " actors");
//
//        for (Map.Entry<String,Actor> actor : actorMap.entrySet()) {
//            String key = actor.getKey();
//            Actor value = actor.getValue();
//            System.out.println("KEY = " + key + " Value = " + value.toString());
//
//        }
//        System.out.println("DUPS : "+ this.dups);
//    }
//
//    private void addData(){
////        prebuild sql statement add it all together
//    }
//
//
//
//    public static void main(String[] args) {
//        // create an instance
//        ActorsParser actorsParser = new ActorsParser();
//
//        // call run example
//        actorsParser.runParser();
//    }
//
//}
