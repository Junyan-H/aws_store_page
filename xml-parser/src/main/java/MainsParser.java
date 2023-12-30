import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import org.xml.sax.InputSource;
import java.io.InputStreamReader;
import java.io.FileInputStream;

import java.util.HashMap;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class MainsParser {
    //url for xml file
    String xmlFile;

    //list of movies
//    ArrayList<Movie> movies = new ArrayList<>();
    HashMap<String, String> Genre_hmap = new HashMap<>();

    HashMap<String, Movie> movieMap = new HashMap<>();
    int numDuplicateMovies = 0;
    int numInconsistentMovies = 0;

    //Dom doc
    Document dom;
    public static class InvalidInput extends Exception{
        public InvalidInput(String errorMessage){
            super(errorMessage);
        }
    }

    public void runParser(){
        Genre_hmap.put("Actn","Action");
        Genre_hmap.put("Porn","Adult");
        Genre_hmap.put("Advt","Adventure");
        Genre_hmap.put("Susp","Thriller");
        Genre_hmap.put("Dram","Drama" );
        Genre_hmap.put("Comd","Comedy");
        Genre_hmap.put("West","Western" );
        Genre_hmap.put("Docu","Documentary");
        Genre_hmap.put("BioP","Biography");
        Genre_hmap.put("Horror","Horr");
        Genre_hmap.put("Faml","Family");
        Genre_hmap.put("Myst","Mystery");
        Genre_hmap.put("Romt","Romance");
        Genre_hmap.put("ScFi","Sci-Fi");
        Genre_hmap.put("Fant","Fantasy");
        Genre_hmap.put("Musc","Music");

        parseXmlFile();
        parseDocument();
        printData();
    }

    public HashMap<String, Movie> getMovieMap() {
        return movieMap;
    }

    private void parseXmlFile(){
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            InputSource inputSource = new InputSource(new InputStreamReader(new FileInputStream("stanford-movies/mains243.xml"), "ISO-8859-1"));
            dom = documentBuilder.parse(inputSource);
        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }

    private void parseDocument(){
        Element documentElement = dom.getDocumentElement();
        NodeList nodelist = documentElement.getElementsByTagName("film");
        // Clear output file first
        try (FileWriter writer = new FileWriter("MoviesDuplicate.txt", false)) {
        } catch (IOException e) {
            System.out.println("No file, no need to clear.");
        }

        try (FileWriter writer = new FileWriter("MoviesInconsistent.txt", false)) {
        } catch (IOException e) {
            System.out.println("No file, no need to clear.");
        }

        for(int i = 0; i < nodelist.getLength(); i++){
            //grab movie element
            Element element = (Element) nodelist.item(i);
            //grab movie object
            try {
                Movie movie = parserMovie(element);
                this.numInconsistentMovies += 1;
                if (movie.getGenre() == null || movie.getGenre().isEmpty()){
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter("MoviesInconsistent.txt", true))) {
                        writer.write(movie.toString());
                        writer.newLine();
                    } catch (IOException e) {
                        System.out.println("An error occurred while writing to the file: " + e.getMessage());
                    }
                    throw new InvalidInput("SKIPPING: " + movie.getTitle() + " because invalid genres");
                }
                if (movie.getDirector() == null || movie.getDirector().isEmpty()){
                    this.numInconsistentMovies += 1;
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter("MoviesInconsistent.txt", true))) {
                        writer.write(movie.toString());
                        writer.newLine();
                    } catch (IOException e) {
                        System.out.println("An error occurred while writing to the file: " + e.getMessage());
                    }
                    throw new InvalidInput("SKIPPING: " +movie.getTitle() + " because invalid Director");
                }

                if (this.movieMap.containsKey(movie.getID()) ){
                    this.numDuplicateMovies += 1;
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter("MoviesDuplicate.txt", true))) {
                        writer.write(movie.toString());
                        writer.newLine();
                    } catch (IOException e) {
                        System.out.println("An error occurred while writing to the file: " + e.getMessage());
                    }
                } else {
                    this.movieMap.put(movie.getID(), movie);
//                    this.movies.add(movie);
                }

            } catch(InvalidInput e){
//                System.out.println(e);
            }

        }
    }

    private Movie parserMovie(Element element){
//        System.out.println("PARSING MOVE ***********************************************");
        //grabbing id
        String id = getTextValue(element,"fid");
        //grabbing title
        String title = getTextValue(element, "t");
        //grabbing year
        String year = getTextValue(element,"year");

        //if null skip
        ArrayList<String> directors = getListValue(element,"dirn");
        String director;

        if(!directors.isEmpty()){
            director = directors.get(0);
        } else {
            director = null;
        }



        //if null skip
        ArrayList<String> genres = new ArrayList<>();
        for (int i = 0; i < getListValue(element,"cat").size();i++){
            genres.add(Genre_hmap.get(getListValue(element,"cat").get(i)));
        }

        return new Movie(id, title, year, director, genres);
    }

    private String getTextValue(Element element, String tagName) {
        String textVal = null;
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            try {
                // here we expect only one <Name> would present in the <Employee>
                textVal = nodeList.item(0).getFirstChild().getNodeValue();
            } catch (NullPointerException e){

            }

        }
        return textVal;
    }


    private ArrayList<String> getListValue(Element element, String tagName){
        ArrayList<String> val = new ArrayList<>();
        String textVal = null;

        NodeList nodeList = element.getElementsByTagName(tagName);

        for(int i = 0; i < nodeList.getLength(); i++){
            try {
                textVal = nodeList.item(i).getFirstChild().getNodeValue();
                if(textVal != null && !toString().isEmpty()){
//                    System.out.println("Parsing through list vals for "+ tagName+" :"+textVal);
                    val.add(textVal);
                }

            } catch (NullPointerException e) {

            }
        }
        return val;
    }

    private void printData() {
//        for (Movie movie : movies) {
//            System.out.println("\t" + movie.toString());
//        }
        System.out.println("Total parsed movies (mains): " + this.movieMap.size());
        System.out.println("Num duplicate movies: " + this.numDuplicateMovies);
        System.out.println("Num inconsistent movies: " + this.numInconsistentMovies);

    }

//    public static void main(String[] args) {
//        MainsParser mainsParser = new MainsParser();
//        mainsParser.runParser();
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
//import java.lang.reflect.Array;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//
//public class MainsParser {
//    //url for xml file
//    String xmlFile;
//
//    //list of movies
//
//    List<Movie> movies = new ArrayList<>();
//
//
//    //hashmpa of movies
//    HashMap<String, Movie> movieMap = new HashMap<>();
//
//    //
//    int dups = 0;
//
//    //Dom doc
//    Document dom;
//    public static class InvalidInput extends Exception{
//        public InvalidInput(String errorMessage){
//            super(errorMessage);
//        }
//    }
//
//    public void runParser(){
//
//
//        //parse xmpl file and get dom object
//        parseXmlFile();
//
//        //get each movie element and create a movie object
//        parseDocument();
//
//        //iterate through the list and print data
//        printData();
//    }
//
//    private void parseXmlFile(){
//        //document factory
//        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
//
//        try{
//            //document builder
//            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
//
//            //parse using builder to get Dom representation of the xml file
//            dom = documentBuilder.parse("mains243.xml");
//
//        }catch (ParserConfigurationException | SAXException | IOException error) {
//            error.printStackTrace();
//        }
//
//    }
//
//    private  void parseDocument(){
//        // get the document root Element
//        Element documentElement = dom.getDocumentElement();
//
//        //get a nodelist of movies elements parse each into film objects
//        NodeList nodelist = documentElement.getElementsByTagName("film");
//        for(int i = 0; i< nodelist.getLength(); i++){
//
//            //grab movie element
//            Element element = (Element) nodelist.item(i);
//
//            //grab movie object
//            try{
//                Movie movie = parserMovie(element);
//                if(movie.getGenre() == null|| movie.getGenre().isEmpty() ){
//                    throw new InvalidInput("SKIPPING: "+movie.getTitle() +" because invalid genres");
//                }
//                if( movie.getDirector() == null || movie.getDirector().isEmpty() ){
//                    throw new InvalidInput("SKIPPING: "+movie.getTitle() +" because invalid Director");
//                }
//
//
//                if (this.movieMap.containsKey(movie.getID()) ){
//                    this.dups+=1;
//                }else{
//                    this.movieMap.put(movie.getID(), movie);
//                    this.movies.add(movie);
//                }
//            }catch(InvalidInput e){
//                System.out.println(e);
//            }
//
//
//
//        }
//    }
//
//    private Movie parserMovie(Element element){
//        System.out.println("PARSING MOVE ***********************************************");
//        //grabbing id
//        String id = getTextValue(element,"fid");
//        //grabbing title
//        String title = getTextValue(element, "t");
//        //grabbing year
//        String year = getTextValue(element,"year");
//
//        //if null skip
//        ArrayList<String> directors = getListValue(element,"dirn");
//        String director;
//
//        if(!directors.isEmpty()){
//            director = directors.get(0);
//        }else{
//            director = null;
//        }
//
//
//        //if null skip
//        ArrayList<String> genres = getListValue(element,"cat");
//
//
//        System.out.println("INSIDE CREATING Movie");
//        System.out.print("TITLE :");
//
//        System.out.println(title);
//        System.out.print("Year :");
//
//        System.out.println(year);
//        System.out.print("Director :");
//
//        System.out.println(director);
//        System.out.print("genre : ");
//        for(String genre : genres){
//            System.out.print(genre+ " ");
//        }
//        System.out.println(" ");
//        return new Movie(id, title,year,director,genres);
//    }
//
//
//    private String getTextValue(Element element, String tagName) {
//        String textVal = null;
//        NodeList nodeList = element.getElementsByTagName(tagName);
//        if (nodeList.getLength() > 0) {
//            try{
//                // here we expect only one <Name> would present in the <Employee>
//                textVal = nodeList.item(0).getFirstChild().getNodeValue();
//            }catch (NullPointerException e){
//
//            }
//
//        }
//        return textVal;
//    }
//
//
//    private ArrayList<String> getListValue(Element element, String tagName){
//        ArrayList<String> val = new ArrayList<>();
//        String textVal = null;
//
//        NodeList nodeList = element.getElementsByTagName(tagName);
//
//        for(int i = 0; i < nodeList.getLength(); i++){
//            try{
//                textVal = nodeList.item(i).getFirstChild().getNodeValue();
//                if(textVal != null && !toString().isEmpty()){
//                    System.out.println("Parsing through list vals for "+ tagName+" :"+textVal);
//                    val.add(textVal);
//                }
//
//            }catch (NullPointerException e){
//
//            }
//        }
//        return val;
//    }
//
//    private void printData() {
//
//        System.out.println("Total parsed " + this.movieMap.size() + " movies");
//
//        for (Map.Entry<String,Movie> movie : movieMap.entrySet()){
//            String key = movie.getKey();
//            Movie value = movie.getValue();
//            System.out.println("KEY = " + key + " Value = " + value.toString());
//        }
//
//        System.out.println("DUPS : "+ this.dups);
//    }
//
//    public static void main(String[] args) {
//        // create an instance
//        MainsParser mainsParser = new MainsParser();
//
//        // call run example
//        mainsParser.runParser();
//    }
//
//
//
//}