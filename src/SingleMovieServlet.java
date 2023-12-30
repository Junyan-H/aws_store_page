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

@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 3L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        String id = request.getParameter("id");
        PrintWriter out = response.getWriter();
        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT * FROM movies AS m " +
                    "JOIN ratings AS r ON r.movieId = m.id " +
                    "WHERE m.id = ?";

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();
            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_rating = rs.getString("rating");

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("title", movie_title);
                jsonObject.addProperty("year", movie_year);
                jsonObject.addProperty("director", movie_director);
                jsonObject.addProperty("rating", movie_rating);
                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            /////////////////////////////////////////////////////////////////////
            // STARS QUERY //////////////////////////////////////////////////////
            JsonObject je = jsonArray.get(0).getAsJsonObject();
            String mt = je.get("title").getAsString();
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
                    "ORDER BY num_movies DESC, s.name ASC;";
            PreparedStatement pStatement = conn.prepareStatement(star_query);
            pStatement.setString(1, mt);
            ResultSet star_rs = pStatement.executeQuery();
            JsonArray starsAry = new JsonArray();
            while(star_rs.next()){
                JsonObject starObj = new JsonObject();
                String name = star_rs.getString("name");
                String starId = star_rs.getString("starId");
                starObj.addProperty("star_name",name);
                starObj.addProperty("star_id", starId);
                starsAry.add(starObj);
            }
            star_rs.close();
            je.add("star_list", starsAry);
            jsonArray.set(0, je);
            pStatement.close();
            // END STARS QUERY //////////////////////////////////////////////////
            /////////////////////////////////////////////////////////////////////

            /////////////////////////////////////////////////////////////////////
            // GENRE QUERY //////////////////////////////////////////////////////
            JsonObject je2 = jsonArray.get(0).getAsJsonObject();
            String mt2 = je2.get("title").getAsString();
            String genre_query = "SELECT g.name AS genre_name\n" +
                    "FROM movies AS m\n" +
                    "JOIN genres_in_movies AS gim ON m.id = gim.movieId\n" +
                    "JOIN genres AS g ON g.id = gim.genreId\n" +
                    "WHERE title LIKE ? ORDER BY genre_name ASC;";
            PreparedStatement pStatement2 = conn.prepareStatement(genre_query);
            pStatement2.setString(1, mt2);
            ResultSet star_rs2 = pStatement2.executeQuery();

            // Create new JSON Array to store names
            JsonArray starsArray = new JsonArray();
            while(star_rs2.next()){
                String genre = star_rs2.getString("genre_name");
                starsArray.add(genre);
            }
            star_rs2.close();
            je2.add("movie_genres", starsArray);
            jsonArray.set(0, je2);
            pStatement2.close();
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
