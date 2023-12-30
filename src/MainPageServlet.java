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

@WebServlet(name = "MainPageServlet", urlPatterns = "/api/main-page")
public class MainPageServlet extends HttpServlet {
    private static final long serialVersionUID = 4L;

    // Create a dataSource which registered in web.
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
        System.out.println("Inside GET of MainPageServlet");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonArray returnAry = new JsonArray();
        JsonArray acArray = new JsonArray();
        String acQuery = request.getParameter("acQuery");
        System.out.println("acQuery: " + acQuery);

        try (Connection conn = dataSource.getConnection()) {
            if (acQuery != null && !acQuery.trim().isEmpty()) {
                String fullTextQuery = "";
                String[] splitTitle = acQuery.split(" ");
                for (int i = 0; i < splitTitle.length; i++) {
                    fullTextQuery += "+" + splitTitle[i] + "* ";
                }

//                String query = String.format("SELECT id, title FROM movies AS m " +
//                        "WHERE MATCH (m.title) AGAINST ('%s' IN BOOLEAN MODE);", fullTextQuery);
//                if(acQuery.length() > 7) {
//                    query = String.format("SELECT id, title FROM movies AS m " +
//                            "WHERE MATCH (m.title) AGAINST ('%s' IN BOOLEAN MODE) " +
//                            "OR (m.title LIKE \"%s\" \n" +
//                            "OR (edth(m.title, \"%s\", 5) = 1));", fullTextQuery, acQuery + "%", acQuery);
//                }
                String query = String.format("SELECT id, title FROM movies AS m JOIN ratings AS r ON m.id = r.movieId " +
                        "WHERE MATCH (m.title) AGAINST ('%s' IN BOOLEAN MODE) ORDER BY title ASC, rating ASC;", fullTextQuery);
                if(acQuery.length() > 7) {
                    query = String.format("SELECT id, title FROM movies AS m JOIN ratings AS r ON m.id = r.movieId " +
                            "WHERE MATCH (m.title) AGAINST ('%s' IN BOOLEAN MODE) " +
                            "OR (m.title LIKE \"%s\" \n" +
                            "OR (edth(m.title, \"%s\", 5) = 1)) ORDER BY title ASC, rating ASC;", fullTextQuery, acQuery + "%", acQuery);
                }

                System.out.println("PREPARED ACQUERY: " + query);

                PreparedStatement pStatement = conn.prepareStatement(query);
                ResultSet rs = pStatement.executeQuery();
                while(rs.next()) {
                    String movieId = rs.getString("id");
                    String movieTitle = rs.getString("title");
                    acArray.add(generateJsonObject(movieId, movieTitle));
                }
                rs.close();
                pStatement.close();
                returnAry.add(acArray.toString());
                System.out.println("return array(1) if: " + returnAry);
            } else {
                returnAry.add(acArray.toString());
                System.out.println("return array(1) else: " + returnAry);
            }

            ///////////////////////////////////////////////////////////////////////
            // ALL BROWSING GENRES FOR BROWSING QUERY /////////////////////////////
            String query = "SELECT name FROM genres;";
            PreparedStatement pStatement = conn.prepareStatement(query);
            ResultSet rs = pStatement.executeQuery();
            JsonArray genreArray = new JsonArray();
            while(rs.next()){
                String genre = rs.getString("name");
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("genreName", genre);
                genreArray.add(jsonObject);
            }
            rs.close();
            pStatement.close();
            returnAry.add(genreArray.toString());
            out.write(returnAry.toString());
            response.setStatus(200);
            // END ALL GENRES FOR BROWSING QUERY //////////////////////////////////
            ///////////////////////////////////////////////////////////////////////

        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());
            response.setStatus(500);
        } finally {
            out.close();
        }
    }

    private static JsonObject generateJsonObject(String movieId, String movieTitle) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", movieTitle);

        JsonObject jsonObject2 = new JsonObject();
        jsonObject2.addProperty("movieId", movieId);

        jsonObject.add("data", jsonObject2);
        return jsonObject;
    }
}
