import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;


// Declaring a WebServlet called StarsServlet, which maps to url "/api/stars"
@WebServlet(name = "MovieListServlet", urlPatterns = "/api/movie-list")
public class MovieListServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        double msJDBC = -1.0;
        long servletStartTime = System.nanoTime();
        String logFilePath = request.getSession().getServletContext().getRealPath("/logs") + "/TESTLOG.txt";
//        System.out.println("LOG FILE PATH: " + logFilePath);
        FileWriter writer = new FileWriter(logFilePath, true);

        response.setContentType("application/json"); // Response mime type
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        JsonArray previousItems = (JsonArray) session.getAttribute("previousItems");
        String previousQuery = (String) session.getAttribute("previousQuery");
        ArrayList<String> previousQueryList = (ArrayList<String>) session.getAttribute("previousQueryList");

//        System.out.println("TOP OF PAGE - PREVIOUS ITEMS: " + previousItems);

        String str_title = request.getParameter("title");
        String str_year = request.getParameter("year");
        String str_director = request.getParameter("director");
        String str_starName = request.getParameter("starName");
        String str_genre = request.getParameter("genre");
        String str_n = request.getParameter("n");
        String str_sortOption = request.getParameter("sortOption");
        String str_arrow = request.getParameter("arrow");
        String queryString = request.getQueryString();
//
//        System.out.println("Query String: " + queryString);
//        System.out.println("Query Title: " + str_title);
//        System.out.println("Query Year: " + str_year);
//        System.out.println("Query Director: " + str_director);
//        System.out.println("Query StarName: " + str_starName);
//        System.out.println("Query Genre: " + str_genre);
//        System.out.println("Query n: " + str_n);
//        System.out.println("Query sortOption: " + str_sortOption);
//        System.out.println("Query arrow: " + str_arrow);

        if (str_title.equals("null") && str_year.equals("null") && str_director.equals("null") && str_starName.equals("null")) {
            if(str_genre.equals("null") && str_n.equals("null") && str_sortOption.equals("null") && str_arrow.equals("null")) {
//                System.out.println("~~~~~ SESSION RESTORED, LOADING PREVIOUS RESULTS ~~");
                out.write(previousItems.toString());
                response.setStatus(200);
                out.close();
                return;
            }
            if(str_genre.equals("null") && !str_n.equals("null") && !str_sortOption.equals("null")) {
//                System.out.println("~~~~~ SESSION RESTORED, NEW ROWS / SORT OPTION REQUESTED~~");
//                System.out.println("PREVIOUS QUERY:\n" + previousQuery);

                JsonArray jsonArray = new JsonArray();
                int index_of_order = previousQuery.indexOf("ORDER");
                String modifiedQuery = previousQuery.substring(0, index_of_order);
                JsonObject n_sortBy_jsonObject = new JsonObject();

                // HANDLE SORT BY
                if (str_sortOption.equals("truu")) {
                    modifiedQuery += " ORDER BY title ASC, rating ASC";
                    n_sortBy_jsonObject.addProperty("sort_option", "truu");
                } else if (str_sortOption.equals("trud")) {
                    modifiedQuery += " ORDER BY title ASC, rating DESC";
                    n_sortBy_jsonObject.addProperty("sort_option", "trud");
                } else if (str_sortOption.equals("trdu")) {
                    modifiedQuery += " ORDER BY title DESC, rating ASC";
                    n_sortBy_jsonObject.addProperty("sort_option", "trdu");
                } else if (str_sortOption.equals("trdd")) {
                    modifiedQuery += " ORDER BY title DESC, rating DESC";
                    n_sortBy_jsonObject.addProperty("sort_option", "trdd");
                } else if (str_sortOption.equals("rtuu")) {
                    modifiedQuery += " ORDER BY rating ASC, title ASC";
                    n_sortBy_jsonObject.addProperty("sort_option", "rtuu");
                } else if (str_sortOption.equals("rtud")) {
                    modifiedQuery += " ORDER BY rating ASC, title DESC";
                    n_sortBy_jsonObject.addProperty("sort_option", "rtud");
                } else if (str_sortOption.equals("rtdu")) {
                    modifiedQuery += " ORDER BY rating DESC, title ASC";
                    n_sortBy_jsonObject.addProperty("sort_option", "rtdu");
                } else if (str_sortOption.equals("rtdd")) {
                    modifiedQuery += " ORDER BY rating DESC, title DESC";
                    n_sortBy_jsonObject.addProperty("sort_option", "rtdd");
                }

                // HANDLE ROWS PER PAGE
                if(str_n.equals("10")) {
                    modifiedQuery += " LIMIT 10";
                    n_sortBy_jsonObject.addProperty("n", 10);
                } else if (str_n.equals("25")) {
                    modifiedQuery += " LIMIT 25";
                    n_sortBy_jsonObject.addProperty("n", 25);
                } else if (str_n.equals("50")) {
                    modifiedQuery += " LIMIT 50";
                    n_sortBy_jsonObject.addProperty("n", 50);
                } else if (str_n.equals("100")) {
                    modifiedQuery += " LIMIT 100";
                    n_sortBy_jsonObject.addProperty("n", 100);
                }

                // HANDLE NEXT / PREV
                if(str_arrow.equals("next")) {
                    boolean canGoToNext = (boolean) session.getAttribute("canGoToNext");
                    int pageNum = (int) session.getAttribute("pageNum");
                    // If next does not go out of bounds
                    if (canGoToNext) {
                        int rowsPerPage_option = n_sortBy_jsonObject.get("n").getAsInt();
                        int calcOffset = rowsPerPage_option * pageNum;
                        modifiedQuery += " OFFSET " + calcOffset + ";";
                        pageNum += 1;
                    }
                    // Set session attribute page num and add property page num to json object
                    session.setAttribute("pageNum", pageNum);
                    n_sortBy_jsonObject.addProperty("pageNum", pageNum);
                }
                else if(str_arrow.equals("prev")) {
                    int pageNum = (int) session.getAttribute("pageNum");
                    if (pageNum == 1) {
                        session.setAttribute("pageNum", 1);
                        n_sortBy_jsonObject.addProperty("pageNum", 1);
                    }
                    else {
                        pageNum -= 1;
                        int rowsPerPage_option = n_sortBy_jsonObject.get("n").getAsInt();
                        int calcOffset = rowsPerPage_option * (pageNum - 1);
                        modifiedQuery += " OFFSET " + calcOffset + ";";
                        session.setAttribute("pageNum", pageNum);
                        n_sortBy_jsonObject.addProperty("pageNum", pageNum);
                    }
                }
                else {
                    n_sortBy_jsonObject.addProperty("pageNum", 1);
                }
                ////////////////////////////////////////////////////////////////////////
                ////////////////////////////////////////////////////////////////////////
                ////////////////////////////////////////////////////////////////////////
                ////////////////////////////////////////////////////////////////////////
                try (Connection conn = dataSource.getConnection()) {
                    PreparedStatement pStatement1 = conn.prepareStatement(modifiedQuery);
                    for(int i = 0; i < previousQueryList.size(); i++) {
                        pStatement1.setString(i + 1, "%" + previousQueryList.get(i) + "%");
                    }
                    ResultSet rs = pStatement1.executeQuery();
                    while (rs.next()) {
                        String movieId = rs.getString("id");
                        String movieTitle = rs.getString("title");
                        String movieYear = rs.getString("year");
                        String movieDirector = rs.getString("director");
                        String movieRating = rs.getString("rating");
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("movie_id", movieId);
                        jsonObject.addProperty("movie_title", movieTitle);
                        jsonObject.addProperty("movie_year", movieYear);
                        jsonObject.addProperty("movie_director", movieDirector);
                        jsonObject.addProperty("movie_rating", movieRating);
                        jsonArray.add(jsonObject);
                    }
                    rs.close();
                    pStatement1.close();

                    /////////////////////////////////////////////////////////////////////
                    // STARS QUERY //////////////////////////////////////////////////////
                    String star_query = "SELECT s.name, s.id AS starId, COUNT(*) AS num_movies\n" +
                            "FROM stars AS s\n" +
                            "JOIN stars_in_movies AS sim ON s.id = sim.starId\n" +
                            "WHERE EXISTS (\n" +
                            "  SELECT *\n" +
                            "  FROM movies AS m\n" +
                            "  JOIN stars_in_movies AS sim2 ON m.id = sim2.movieId\n" +
                            "  WHERE sim2.starId = s.id AND m.title = ?\n" +
                            ")\n" +
                            "GROUP BY s.name, s.id\n" +
                            "ORDER BY num_movies DESC, s.name ASC\n" +
                            "LIMIT 3;";
                    PreparedStatement starsStatement = conn.prepareStatement(star_query);
                    for(int i = 0; i < jsonArray.size(); i++){
                        JsonObject je = jsonArray.get(i).getAsJsonObject();
                        String mt = je.get("movie_title").getAsString();
                        starsStatement.setString(1, mt);
                        ResultSet star_rs = starsStatement.executeQuery();
                        JsonArray starsArray = new JsonArray();
                        while (star_rs.next()) {
                            JsonObject starObj = new JsonObject();
                            String name = star_rs.getString("name");
                            String id = star_rs.getString("starId");
                            starObj.addProperty("star_name", name);
                            starObj.addProperty("star_id", id);
                            starsArray.add(starObj);
                        }
                        star_rs.close();
                        je.add("star_list", starsArray);
                        jsonArray.set(i, je);
                    }
                    starsStatement.close();
                    // END STARS QUERY //////////////////////////////////////////////////
                    /////////////////////////////////////////////////////////////////////

                    /////////////////////////////////////////////////////////////////////
                    // GENRE QUERY //////////////////////////////////////////////////////
                    String genre_query = "SELECT g.name AS genre_name\n" +
                            "FROM movies AS m\n" +
                            "JOIN genres_in_movies AS gim ON m.id = gim.movieId\n" +
                            "JOIN genres AS g ON g.id = gim.genreId\n" +
                            "WHERE title LIKE ? ORDER BY g.name;";
                    PreparedStatement genreStatement = conn.prepareStatement(genre_query);
                    for(int i = 0; i < jsonArray.size(); i++) {
                        JsonObject je2 = jsonArray.get(i).getAsJsonObject();
                        String mt2 = je2.get("movie_title").getAsString();
                        genreStatement.setString(1, mt2);
                        ResultSet genre_rs = genreStatement.executeQuery();
                        JsonArray genreArray = new JsonArray();
                        while(genre_rs.next()){
                            String genre = genre_rs.getString("genre_name");
                            genreArray.add(genre);
                        }
                        genre_rs.close();
                        je2.add("movie_genres", genreArray);
                        jsonArray.set(i, je2);
                    }
                    genreStatement.close();
                    // END GENRE QUERY //////////////////////////////////////////////////
                    /////////////////////////////////////////////////////////////////////
                    jsonArray.add(n_sortBy_jsonObject);

                    // SESSION
                    if (previousItems == null) {
                        previousItems = jsonArray;
                        previousQuery = modifiedQuery;
                        session.setAttribute("previousItems", previousItems);
                        session.setAttribute("previousQuery", previousQuery);
                        session.setAttribute("pageNum", 1);
                        if(previousItems.size() < 10) {
                            session.setAttribute("canGoToNext", false);
                        } else {
                            session.setAttribute("canGoToNext", true);
                        }
//                        System.out.println("PREVIOUS ITEMS INITIALIZATION (NULL)! AT THE TOP");
//                        System.out.println(previousItems);
                        out.write(previousItems.toString());
                    } else {
                        session.setAttribute("previousItems", jsonArray);
                        session.setAttribute("previousQuery", modifiedQuery);
                        if(jsonArray.size() < 10) {
                            session.setAttribute("canGoToNext", false);
                        } else {
                            session.setAttribute("canGoToNext", true);
                        }
//                        System.out.println("PREVIOUS ITEMS ELSE STATEMENT! AT THE TOP");
//                        System.out.println(jsonArray);
                        out.write(jsonArray.toString());
                    }
                    response.setStatus(200);
                }
                catch (Exception e) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("errorMessage", e.getMessage());
                    out.write(jsonObject.toString());

                    // Log error to localhost log
                    request.getServletContext().log("Error:", e);
                    // Set response status to 500 (Internal Server Error)
                    response.setStatus(500);
                }
                finally {
                    out.close();
//                    System.out.println("Done with result table update.");
                    return;
                }
                ////////////////////////////////////////////////////////////////////////
                ////////////////////////////////////////////////////////////////////////
                ////////////////////////////////////////////////////////////////////////
                ////////////////////////////////////////////////////////////////////////
            }
            else {
                str_title = "";
                str_year = "";
                str_director = "";
                str_starName = "";
            }
        }

        // BROWSING/SEARCHING FROM MAIN PAGE:
        try (Connection conn = dataSource.getConnection()) {
            JsonArray jsonArray = new JsonArray();
            ArrayList<String> queryStringList = new ArrayList<String>();

            String fullTextString = "";
            String[] splitTitle = str_title.split(" ");
            for (int i = 0; i < splitTitle.length; i++) {
                fullTextString += "+" + splitTitle[i] + "* ";
            }

            String query = "SELECT * FROM movies AS m \n" +
                    "JOIN ratings AS r ON m.id = r.movieId \n";

            if (str_starName.length() != 0) {
                query += "JOIN stars_in_movies AS sim ON m.id = sim.movieID \n";
                query += "JOIN stars AS s ON sim.starID = s.id \n";
                queryStringList.add(str_starName);
                query += "WHERE s.name LIKE ? AND ";
            } else if (str_genre.equals("null")) {
                query += "JOIN genres_in_movies AS gim ON m.id = gim.movieId \n";
                query += "JOIN genres AS g ON g.id = gim.genreId \n";
                query += "WHERE g.name LIKE '%' AND ";
            } else if (str_genre.length() != 0){
                query += "JOIN genres_in_movies AS gim ON m.id = gim.movieId \n";
                query += "JOIN genres AS g ON g.id = gim.genreId \n";
                queryStringList.add(str_genre);
                query += "WHERE g.name LIKE ? AND ";
            } else {
                query += "WHERE ";
            }

            // MODIFYING QUERY:
            // TITLE
            if (str_title.length() != 0 && !str_title.equals("*")) {
                if(str_title.length() > 7) {
                    query += String.format(" MATCH (m.title) AGAINST ('%s' IN BOOLEAN MODE) OR (m.title LIKE \"%s\" OR (edth(m.title, \"%s\", 5) = 1)) AND", fullTextString, str_title + "%", str_title);
                } else {
                    query += String.format(" MATCH (m.title) AGAINST ('%s' IN BOOLEAN MODE) AND", fullTextString);
                }
            } else if (str_title.length() != 0 && str_title.equals("*")){
                query += " m.title REGEXP '^[^[:alnum:]]' AND";
            } else {
                query += " m.title LIKE '%' AND";
            }

            // YEAR
            if (str_year.length() != 0) {
                queryStringList.add(str_year);
                query += " m.year LIKE ? AND";
            } else {
                query += " m.year LIKE '%' AND";
            }

            // DIRECTOR
            if (str_director.length() != 0) {
                queryStringList.add(str_director);
                query += " m.director LIKE ?";
            } else {
                query += " m.director LIKE '%'";
            }

            // SORT OPTION: DEFAULT !!!!!, SET DEFAULT TO LIMIT 10 AS WELL
            if (str_sortOption.equals("null") && str_n.equals("null")) {
                query += " ORDER BY title ASC, rating ASC LIMIT 10;";
            }

            // PRINT FINAL QUERY
//            System.out.println("~~ FINAL QUERY: ~~");
//            System.out.println(query);
            long jdbcPartsStartTime = System.nanoTime();
            PreparedStatement pQueryInitialStatement = conn.prepareStatement(query);
            for(int i = 0; i < queryStringList.size(); i++) {
                pQueryInitialStatement.setString(i + 1, "%" + queryStringList.get(i) + "%");
            }
//            System.out.println(pQueryInitialStatement);
            // Execute Query
            ResultSet rs = pQueryInitialStatement.executeQuery();
            while(rs.next()) {
                String movieId = rs.getString("id");
                String movieTitle = rs.getString("title");
                String movieYear = rs.getString("year");
                String movieDirector = rs.getString("director");
                String movieRating = rs.getString("rating");
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movieId);
                jsonObject.addProperty("movie_title", movieTitle);
                jsonObject.addProperty("movie_year", movieYear);
                jsonObject.addProperty("movie_director", movieDirector);
                jsonObject.addProperty("movie_rating", movieRating);
                jsonArray.add(jsonObject);
            }
            rs.close();
            pQueryInitialStatement.close();
            long jdbcPartsEndTime = System.nanoTime();
            long elapsedJDBCTime = jdbcPartsEndTime - jdbcPartsStartTime; // elapsed time in nano seconds. Note: print the values in nanoseconds
            msJDBC = elapsedJDBCTime / 1000000.0;

            /////////////////////////////////////////////////////////////////////
            // STARS QUERY //////////////////////////////////////////////////////
            String star_query = "SELECT s.name, s.id AS starId, COUNT(*) AS num_movies\n" +
                    "FROM stars AS s\n" +
                    "JOIN stars_in_movies AS sim ON s.id = sim.starId\n" +
                    "WHERE EXISTS (\n" +
                    "  SELECT *\n" +
                    "  FROM movies AS m\n" +
                    "  JOIN stars_in_movies AS sim2 ON m.id = sim2.movieId\n" +
                    "  WHERE sim2.starId = s.id AND m.title = ?\n" +
                    ")\n" +
                    "GROUP BY s.name, s.id\n" +
                    "ORDER BY num_movies DESC, s.name ASC\n" +
                    "LIMIT 3;";
            PreparedStatement preparedStarsStatement = conn.prepareStatement(star_query);
            // Loop through each JSON object
            for(int i = 0; i < jsonArray.size(); i++){
                JsonObject je = jsonArray.get(i).getAsJsonObject();
                String mt = je.get("movie_title").getAsString();
                preparedStarsStatement.setString(1, mt);
                ResultSet star_rs = preparedStarsStatement.executeQuery();
                JsonArray starsArray = new JsonArray();

                while(star_rs.next()){
                    JsonObject starObj = new JsonObject();
                    String name = star_rs.getString("name");
                    String id = star_rs.getString("starId");
                    starObj.addProperty("star_name",name);
                    starObj.addProperty("star_id",id);
                    starsArray.add(starObj);
                }
                star_rs.close();
                je.add("star_list", starsArray);
                jsonArray.set(i, je);
            }
            preparedStarsStatement.close();
            // END STARS QUERY //////////////////////////////////////////////////
            /////////////////////////////////////////////////////////////////////

            /////////////////////////////////////////////////////////////////////
            // GENRE QUERY //////////////////////////////////////////////////////
            String genre_query = "SELECT g.name AS genre_name\n" +
                    "FROM movies AS m\n" +
                    "JOIN genres_in_movies AS gim ON m.id = gim.movieId\n" +
                    "JOIN genres AS g ON g.id = gim.genreId\n" +
                    "WHERE title LIKE ? ORDER BY g.name;";
            PreparedStatement genrePreparedStatement = conn.prepareStatement(genre_query);
            for(int i = 0; i < jsonArray.size(); i++) {
                JsonObject je2 = jsonArray.get(i).getAsJsonObject();
                String mt2 = je2.get("movie_title").getAsString();
                genrePreparedStatement.setString(1, mt2);
                ResultSet genre_rs = genrePreparedStatement.executeQuery();
                JsonArray genreArray = new JsonArray();
                while(genre_rs.next()){
                    String genre = genre_rs.getString("genre_name");
                    genreArray.add(genre);
                }
                genre_rs.close();
                je2.add("movie_genres", genreArray);
                jsonArray.set(i, je2);
            }
            genrePreparedStatement.close();
            // END GENRE QUERY //////////////////////////////////////////////////
            /////////////////////////////////////////////////////////////////////

            // SESSION: FIRST TIME EVER QUERYING
            if (previousItems == null) {
                previousItems = jsonArray;
                previousQuery = query;
                session.setAttribute("previousItems", previousItems);
                session.setAttribute("previousQuery", previousQuery);
                session.setAttribute("previousQueryList", queryStringList);
                session.setAttribute("pageNum", 1);
                if(previousItems.size() < 10) {
                    session.setAttribute("canGoToNext", false);
                } else {
                    session.setAttribute("canGoToNext", true);
                }
//                System.out.println("PREVIOUS ITEMS INITIALIZATION (NULL)! AT THE BOTTOM");
//                System.out.println(previousItems);
                out.write(previousItems.toString());
            }
            // QUERYING ANOTHER TIME IN SAME SESSION
            else {
                session.setAttribute("previousItems", jsonArray);
                session.setAttribute("previousQuery", query);
                session.setAttribute("previousQueryList", queryStringList);
                session.setAttribute("pageNum", 1);
                if(jsonArray.size() < 10) {
                    session.setAttribute("canGoToNext", false);
                } else {
                    session.setAttribute("canGoToNext", true);
                }
//                System.out.println("PREVIOUS ITEMS ELSE STATEMENT! AT THE BOTTOM");
//                System.out.println(jsonArray);
                out.write(jsonArray.toString());
            }
            response.setStatus(200);
        }
        catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500);
        }
        finally {
            out.close();
        }

        long servletEndTime = System.nanoTime();
        long elapsedServletTime = servletEndTime - servletStartTime; // elapsed time in nano seconds. Note: print the values in nanoseconds
        double msServlet = elapsedServletTime / 1000000.0;
//        String formattedServletMs = String.format("%.4f", msServlet);
//        System.out.println("Elapsed time (nanoseconds): " + elapsedTime);
        System.out.println("Elapsed time (milliseconds): " + msServlet);
        try {
            writer.write("TS," + msServlet + ",TJ," + msJDBC);
            writer.write(System.lineSeparator());
            writer.close();
        } catch (IOException e) {
            System.out.println("Write error: " + e);
        }
    }
}
