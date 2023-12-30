import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;

import java.sql.DriverManager;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.HashMap;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class ParserRunner {
    public HashMap <String, Actor>  actorMap = new HashMap<String, Actor>();
    public HashMap <String, Cast>  castMap = new HashMap<String, Cast>();
    public HashMap <String, Movie>  movieMap = new HashMap<String, Movie>();
    public Set<String> star_set = new HashSet<>();
    public ArrayList <HashMap<String,String>> sqlMovieParams = new ArrayList<>();
    public ArrayList <HashMap<String,String>> sqlStarParams = new ArrayList<>();
    public Set<String> genre_set = new HashSet<>();
    public HashMap<Movie, HashMap <String, ArrayList<String>>> starsGenreMovieMap = new HashMap<>();
    public ArrayList<HashMap<String, String>> sqlStars_in_MoviesParams = new ArrayList<>();
    public ArrayList<HashMap<String, String>> sqlGenre_in_MoviesParams = new ArrayList<>();

    public ParserRunner(){
        ActorsParser domParserActor = new ActorsParser();
        CastsParser domParserCast = new CastsParser();
        MainsParser domParserMovies = new MainsParser();

        domParserActor.runParser();
        domParserCast.runParser();
        domParserMovies.runParser();

        // grabbing each of their hashmaps
        this.actorMap = domParserActor.getActorMap();
        this.castMap = domParserCast.getCastMap();
        this.movieMap = domParserMovies.getMovieMap();
    }

    public void addMovieProcedure(){
        HashMap<String,String> param = new HashMap<>();
//        for
    }
    public void runParsers(){
        sqlMoviesParamInit();
        sqlStarParamsInit();
        addMoviesToSQL();
        addStarsToSQL();
        sqlStarGenresInMoviesParamsInit();

    }

    private void sqlStarGenresInMoviesParamsInit() {
        System.out.println("Adding corresponding stars/genres to database....");
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql:///moviedb?autoReconnect=true&useSSL=false",
                    "mytestuser", "My6$Password");
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }

        PreparedStatement psInsertRecord=null;
        String sqlInsertRecord=null;
        int[] iNoRows=null;
        sqlInsertRecord = "CALL add_starsANDgenres_in_movies(?, ?, ?, ?, ?)";

        for (Map.Entry<Movie, HashMap<String,ArrayList<String>>> entry : starsGenreMovieMap.entrySet()) {
            Movie movie = entry.getKey();
            HashMap<String,ArrayList<String>> mapStarsGenreArray= entry.getValue();

            String title = movie.getTitle();
            String year = movie.getYear();
            String director = movie.getDirector();

            ArrayList<String> castArray = mapStarsGenreArray.get("CastArray");
            ArrayList<String> genreArray = mapStarsGenreArray.get("GenreArray");

            try {
            while (castArray.size() > 0 || genreArray.size() > 0){
                String starName = null;
                String genreName = null;
                if(castArray.size() > 0){
                    starName = castArray.remove(0);
                }
                if(genreArray.size() > 0){
                    genreName = genreArray.remove(0);
                }
                    connection.setAutoCommit(false);
                    psInsertRecord = connection.prepareStatement(sqlInsertRecord);
                    psInsertRecord.setString(1, title);
                    psInsertRecord.setString(2, year);
                    psInsertRecord.setString(3, director);
                    psInsertRecord.setString(4, starName);
                    psInsertRecord.setString(5, genreName);
                    psInsertRecord.addBatch();
                }
                iNoRows = psInsertRecord.executeBatch();
                connection.commit();
                // Call the procedure here
//                System.out.println("QUERY FOR ADDING IN SIM AND GIM *");
            } catch (Exception e) {
//                    System.out.println("ADD ALL MOVIES Exception: " + e);
                try {
                    iNoRows = psInsertRecord.executeBatch();
                    connection.commit();
                } catch (Exception z) {
                }
            }
        }
    }

    private void sqlStarParamsInit() {
        Iterator <String> star_itr = this.star_set.iterator();
        while(star_itr.hasNext()) {
            HashMap <String, String> params = new HashMap<>();
            Actor actor = actorMap.get(star_itr.next());
            params.put("Actor", actor.getName());
            params.put("Dob", actor.getDob());
            sqlStarParams.add(params);
        }
    }

    private void sqlMoviesParamInit(){
        // loop through cast movies id _>
        try (FileWriter writer = new FileWriter("CastNotFound.txt", false)) {
        } catch (IOException e) {
            System.out.println("No file, no need to clear.");
        }
        for (Map.Entry<String, Cast> itr: castMap.entrySet()) {
            String id = itr.getKey();
            Cast cast = itr.getValue();
            HashMap <String,String> params = new HashMap<>();

            // find if movie exists in movie hashmap -> t
            if(movieMap.containsKey(id) && !cast.getTitle().isEmpty()){
                Movie m = movieMap.get(id);
                // remove all stars not present in actor list
                ArrayList<String> movieCast = new ArrayList<>();
                for(String castMember: cast.getActor()) {
                    if(actorMap.containsKey(castMember)){
                        movieCast.add(castMember);
                    } else {
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter("CastNotFound.txt", true))) {
                            writer.write("Actor: " + castMember);
                            writer.newLine();
                        } catch (IOException e) {
                            System.out.println("An error occurred while writing to the file: " + e.getMessage());
                        }
                    }
                }
                // checks for genres > 0
                if(movieCast.size() > 0 && m.getGenre().size() > 0) {
                    // add movie procedure with first star ->
                    String actorName = movieCast.get(0);

                    //GENRE
                    ArrayList<String> genreList = m.getGenre();
                    String genre;

                    //grabbing genres
                    genre = genreList.get(0);
                    params.put("Title", m.getTitle());
                    params.put("Year", m.getYear());
                    params.put("Director", m.getDirector());
                    params.put("Genre",genre);
                    params.put("Actor", actorName);
                    // grab dob if not null
                    params.put("Dob", actorMap.get(actorName).getDob());

//                    System.out.println("TESTING PARAM HASHMAP*********************************");
//                    System.out.println("Title: " + params.get("Title"));
//                    System.out.println("Year: " + params.get("Year"));
//                    System.out.println("Director: " + params.get("Director"));
//                    System.out.println("Actor: " + params.get("Actor"));
//                    System.out.println("DOB: "+ params.get("Dob"));
//                    System.out.println("Genre: "+ params.get("Genre"));

                    movieCast.remove(0);
                    genreList.remove(0);

//                    System.out.println("size param");
//                    System.out.println(movieCast.size());
//                    System.out.println(genreList.size());

                    //link movie to remaining cast member and genres
                    HashMap<String, ArrayList<String>> sgmParam = new HashMap<>();
                    sgmParam.put("CastArray", movieCast);
                    sgmParam.put("GenreArray", genreList);
                    starsGenreMovieMap.put(m,sgmParam);

                    // add remaining stars to -> set
                    star_set.addAll(movieCast);
                    genre_set.addAll(genreList);
                    sqlMovieParams.add(params);
                }
            }
        }

    }

    private void addMoviesToSQL() {
        System.out.println("Adding movies to database....");
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql:///moviedb?autoReconnect=true&useSSL=false",
                    "mytestuser", "My6$Password");
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }

        PreparedStatement psInsertRecord=null;
        String sqlInsertRecord=null;
        int[] iNoRows=null;
        sqlInsertRecord = "CALL add_movie(?, ?, ?, ?, ?, ?)";
        try {
            connection.setAutoCommit(false);
            psInsertRecord = connection.prepareStatement(sqlInsertRecord);
            for (HashMap<String, String> map: sqlMovieParams) {
                psInsertRecord.setString(1, map.get("Title"));
                psInsertRecord.setString(2, map.get("Year"));
                psInsertRecord.setString(3, map.get("Director"));
                psInsertRecord.setString(4, map.get("Actor"));
                psInsertRecord.setString(5, map.get("Dob"));
                psInsertRecord.setString(6, map.get("Genre"));
                psInsertRecord.addBatch();
            }
            iNoRows = psInsertRecord.executeBatch();
            connection.commit();
        } catch (Exception e) {
            System.out.println("ADD ALL MOVIES Exception: " + e);
            try {
                iNoRows = psInsertRecord.executeBatch();
                connection.commit();
            } catch (Exception z) {
            }
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                }
            }
            System.out.println("DONE W ADD MOVIES TO SQL!!");
        }
    }

    private void addStarsToSQL() {
        System.out.println("Adding stars to database....");
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql:///moviedb?autoReconnect=true&useSSL=false",
                    "mytestuser", "My6$Password");
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }

        PreparedStatement psInsertRecord=null;
        String sqlInsertRecord=null;
        int[] iNoRows= null;
        sqlInsertRecord = "CALL add_star(?, ?)";
        try {
            connection.setAutoCommit(false);
            psInsertRecord = connection.prepareStatement(sqlInsertRecord);
            for (HashMap<String, String> map: sqlStarParams) {
                psInsertRecord.setString(1, map.get("Actor"));
                psInsertRecord.setString(2, map.get("Dob"));
                psInsertRecord.addBatch();
            }
            iNoRows = psInsertRecord.executeBatch();
            connection.commit();
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }


    private void addStarsGenresInMoviesToSQL() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql:///moviedb?autoReconnect=true&useSSL=false",
                    "mytestuser", "My6$Password");
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }

        PreparedStatement psInsertRecord=null;
        String sqlInsertRecord=null;
        int[] iNoRows=null;
        sqlInsertRecord = "CALL add_star(?, ?)";
        try {
            connection.setAutoCommit(false);
            psInsertRecord = connection.prepareStatement(sqlInsertRecord);
            for (HashMap<String, String> map: sqlStarParams) {
                psInsertRecord.setString(1, map.get("Actor"));
                psInsertRecord.setString(2, map.get("Dob"));
                psInsertRecord.addBatch();
            }
            iNoRows = psInsertRecord.executeBatch();
            connection.commit();
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }


    public static void main(String[] args) {
        ParserRunner parserRunner = new ParserRunner();
        parserRunner.runParsers();
    }
}