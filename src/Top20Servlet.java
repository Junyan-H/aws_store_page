import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;


// Declaring a WebServlet called StarsServlet, which maps to url "/api/stars"
@WebServlet(name = "Top20Servlet", urlPatterns = "/api/top-20")
public class Top20Servlet extends HttpServlet {
    private static final long serialVersionUID = 5L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Response mime type
        PrintWriter out = response.getWriter();
        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT * FROM movies AS m \n" +
                    "JOIN ratings AS r ON m.id = r.movieId ORDER BY rating DESC\n" +
                    "LIMIT 20";
            PreparedStatement pStatement = conn.prepareStatement(query);
            ResultSet rs = pStatement.executeQuery();
            JsonArray jsonArray = new JsonArray();
            while (rs.next()) {
                String movie_id = rs.getString("movieId");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_rating = rs.getString("rating");

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_rating", movie_rating);
                jsonArray.add(jsonObject);
            }
            rs.close();
            pStatement.close();

            /////////////////////////////////////////////////////////////////////
            // STARS QUERY //////////////////////////////////////////////////////
            // Loop through each JSON object
            for(int i = 0; i < jsonArray.size(); i++){
                JsonObject je = jsonArray.get(i).getAsJsonObject();
                String mt = je.get("movie_title").getAsString();
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
                PreparedStatement pStatement2 = conn.prepareStatement(star_query);
                pStatement2.setString(1, mt);
                ResultSet star_rs = pStatement2.executeQuery();
                JsonArray starsArray = new JsonArray();
                while(star_rs.next()) {
                    JsonObject starObj = new JsonObject();
                    String name = star_rs.getString("name");
                    String id = star_rs.getString("starId");
                    starObj.addProperty("star_name",name);
                    starObj.addProperty("star_id",id);
                    starsArray.add(starObj);
                }
                star_rs.close();
                pStatement2.close();
                je.add("star_list", starsArray);
                jsonArray.set(i, je);
            }
            // END STARS QUERY //////////////////////////////////////////////////
            /////////////////////////////////////////////////////////////////////

            /////////////////////////////////////////////////////////////////////
            // GENRE QUERY //////////////////////////////////////////////////////
            for(int i = 0; i < jsonArray.size(); i++){
                JsonObject je2 = jsonArray.get(i).getAsJsonObject();
                String mt2 = je2.get("movie_title").getAsString();
                String genre_query = "SELECT g.name AS genre_name \n" +
                        "FROM movies AS m\n" +
                        "JOIN genres_in_movies AS gim ON m.id = gim.movieId\n" +
                        "JOIN genres AS g ON g.id = gim.genreId\n" +
                        "WHERE title LIKE ? ORDER BY g.name;";
                PreparedStatement pStatement3 = conn.prepareStatement(genre_query);
                pStatement3.setString(1, mt2);
                ResultSet genre_rs = pStatement3.executeQuery();
                JsonArray starsArray = new JsonArray();
                while(genre_rs.next()){
                    String genre = genre_rs.getString("genre_name");
                    starsArray.add(genre);
                }
                genre_rs.close();
                pStatement3.close();
                je2.add("movie_genres", starsArray);
                jsonArray.set(i, je2);
            }

            // END GENRE QUERY //////////////////////////////////////////////////
            /////////////////////////////////////////////////////////////////////
            out.write(jsonArray.toString());
            response.setStatus(200);
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}
