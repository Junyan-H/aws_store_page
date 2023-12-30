import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "EmployeeDashboardServlet", urlPatterns = {"/api/employee_dashboard", "/_dashboard/api/employee_dashboard"})
public class EmployeeDashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 8L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;
    private DataSource dataSource2;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
            dataSource2 = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb2");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
//        System.out.println("Inside Employee Dashboard Get Request");
        try (Connection conn = dataSource.getConnection()) {
            String tablesQuery = "show tables";
            PreparedStatement pStatement = conn.prepareStatement(tablesQuery);
            ResultSet rs = pStatement.executeQuery();
            JsonArray metadataArray = new JsonArray();
            while(rs.next()){
                JsonObject jsonObject = new JsonObject();
                String table = rs.getString("Tables_in_moviedb");
                jsonObject.addProperty("table_name", table);
                metadataArray.add(jsonObject);
            }
            rs.close();
            pStatement.close();

            for(int i = 0; i < metadataArray.size(); i++) {
                JsonObject table = metadataArray.get(i).getAsJsonObject();
                String tn = table.get("table_name").getAsString();
                String tnQuery = "SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS " +
                        "WHERE TABLE_NAME = ? AND TABLE_SCHEMA = 'moviedb'";
                PreparedStatement pStatement2 = conn.prepareStatement(tnQuery);
                pStatement2.setString(1, tn);
                ResultSet rs2 = pStatement2.executeQuery();
                JsonArray fieldTypeArray = new JsonArray();
                while(rs2.next()) {
                    JsonObject jsonObj = new JsonObject();
                    String table_field = rs2.getString("COLUMN_NAME");
                    String table_type = rs2.getString("DATA_TYPE");
                    jsonObj.addProperty("table_field", table_field);
                    jsonObj.addProperty("table_type", table_type);
                    fieldTypeArray.add(jsonObj);
                }
                rs2.close();
                pStatement2.close();
                table.add("fieldTypeArray", fieldTypeArray);
                metadataArray.set(i, table);
            }
//            System.out.println("META DATA ARRAY: " + metadataArray);
            response.getWriter().write(metadataArray.toString());
            response.setStatus(200);
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            response.getWriter().write(jsonObject.toString());
            response.setStatus(500);
        } finally {
            response.getWriter().close();
        }
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("POST, CONNECTING TO MASTER DB");
        String nameString = request.getParameter("starName");
        String birthYearString = request.getParameter("starBirthYear");
        String movieTitleString = request.getParameter("movieTitle");
        String movieYearString = request.getParameter("movieYear");
        String movieDirectorString = request.getParameter("movieDirector");
        String movieStarNameString = request.getParameter("movieStarName");
        String movieStarBirthYearString = request.getParameter("movieStarBirthYear");
        String genreNameString = request.getParameter("genreName");

//        System.out.println("Star name: " + nameString);
//        System.out.println("Birth Year: " + birthYearString);
//        System.out.println("Movie title: " + movieTitleString);
//        System.out.println("Movie year: " + movieYearString);
//        System.out.println("Movie director: " + movieDirectorString);
//        System.out.println("Movie star name: " + movieStarNameString);
//        System.out.println("Movie star birth year: " + movieStarBirthYearString);
//        System.out.println("Genre name: " + genreNameString);

        JsonObject responseJsonObject = new JsonObject();
        try (Connection conn = dataSource2.getConnection()) {
            // Add star with null birth year
            System.out.println("1");
            System.out.println("2");
            if (nameString != null) {
                System.out.println("3");
                if(birthYearString.isEmpty()) {
                    birthYearString = null;
                }
                String insertStarsQuery = "CALL add_star(?, ?)";
                PreparedStatement pStatement = conn.prepareStatement(insertStarsQuery);
                pStatement.setString(1, nameString);
                pStatement.setString(2, birthYearString);
                boolean rsFound = pStatement.execute();
                if (rsFound) {
                    ResultSet rsAddStar = pStatement.getResultSet();
                    System.out.println("Outside while");
                    while (rsAddStar.next()) {
                        System.out.println("Inside while");
                        System.out.println(rsAddStar.getString(1));
                        responseJsonObject.addProperty("message", rsAddStar.getString(1));
                    }
                }
                responseJsonObject.addProperty("status", "success");
                pStatement.close();
            } else if (movieTitleString != null) {
                if(movieStarBirthYearString.isEmpty()) {
                    movieStarBirthYearString = null;
                }
                String insertMovieQuery = "CALL add_movie(?, ?, ?, ?, ?, ?)";
                PreparedStatement pStatement2 = conn.prepareStatement(insertMovieQuery);
                pStatement2.setString(1, movieTitleString);
                pStatement2.setString(2, movieYearString);
                pStatement2.setString(3, movieDirectorString);
                pStatement2.setString(4, movieStarNameString);
                pStatement2.setString(5, movieStarBirthYearString);
                pStatement2.setString(6, genreNameString);
                boolean rsFound2 = pStatement2.execute();
                if (rsFound2) {
                    ResultSet rsAddMovie = pStatement2.getResultSet();
                    while (rsAddMovie.next()) {
                        System.out.println(rsAddMovie.getString(1));
                        responseJsonObject.addProperty("message", rsAddMovie.getString(1));
                    }
                }
                responseJsonObject.addProperty("status", "success");
                pStatement2.close();
            }
            response.getWriter().write(responseJsonObject.toString());
            response.setStatus(200);
        }  catch (Exception e) {
            System.out.println(e);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            response.getWriter().write(jsonObject.toString());
            response.setStatus(500);
        } finally {
            response.getWriter().close();
        }
    }
}