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
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


@WebServlet(name = "CartServlet", urlPatterns = "/api/cart")
public class CartServlet extends HttpServlet {
    private static final long serialVersionUID = 6L;

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
     * handles GET requests to store session information
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("At top of CartServlet GET");
        response.setContentType("application/json");
        HttpSession session = request.getSession();
        PrintWriter out = response.getWriter();

        JsonArray previousCartItems = (JsonArray) session.getAttribute("previousCartItems");
        System.out.println("(GET) CURRENT CART ARRAY: " + previousCartItems);

        if (previousCartItems == null) {
            previousCartItems = new JsonArray();
        } else {
            try (Connection conn = dataSource.getConnection()) {
                String query = "SELECT * FROM movies WHERE id = ?";
                PreparedStatement pStatement = conn.prepareStatement(query);
                JsonArray cartTitleArray = new JsonArray();
                for(int i = 0; i < previousCartItems.size(); i++) {
                    JsonObject je = previousCartItems.get(i).getAsJsonObject();
                    String id_of_movie = je.get("movie_id").getAsString();
                    pStatement.setString(1, id_of_movie);
                    ResultSet titles_rs = pStatement.executeQuery();
                    while(titles_rs.next()) {
                        JsonObject titleObj = new JsonObject();
                        String title = titles_rs.getString("title");
                        titleObj.addProperty("movie_id", id_of_movie);
                        titleObj.addProperty("movie_title", title);
                        titleObj.addProperty("quantity", je.get("quantity").getAsInt());
                        titleObj.addProperty("price", je.get("price").getAsString());
                        cartTitleArray.add(titleObj);
                    }
                    titles_rs.close();
                }
                pStatement.close();
                out.write(cartTitleArray.toString());
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

    /**
     * handles POST requests to add and show the item list information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String movie_id_item = request.getParameter("item");
        String price = request.getParameter("price");
        String action = request.getParameter("action");
        String movie_id_to_delete = request.getParameter("movie_id_to_delete");
        System.out.println("PRINTING INFO OF BOUGHT MOVIE:");
        System.out.println(movie_id_item);
        System.out.println(price);
        System.out.println(action);
        System.out.println(movie_id_to_delete);
        HttpSession session = request.getSession();
        JsonArray previousCartItems = (JsonArray) session.getAttribute("previousCartItems");
        if(action.equals("Delete")) {
            System.out.println("(POST) DELETE ITEM IN CART: " + previousCartItems);
            for (int i = 0; i < previousCartItems.size(); i++) {
                JsonObject je = previousCartItems.get(i).getAsJsonObject();
                String id_of_movie = je.get("movie_id").getAsString();
                if (id_of_movie.equals(movie_id_to_delete)) {
                    previousCartItems.remove(i);
                    session.setAttribute("previousCartItems", previousCartItems);
                    System.out.println("Item deleted from backend cart...returning");
                    response.getWriter().write(previousCartItems.toString());
                    return;
                }
            }
        } else if(action.equals("Add")) {
            // Get the previous items in a ArrayList
            if (previousCartItems == null) {
                previousCartItems = new JsonArray();
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id_item);
                jsonObject.addProperty("quantity", 1);
                jsonObject.addProperty("price", price);
                previousCartItems.add(jsonObject);
                session.setAttribute("previousCartItems", previousCartItems);
            } else {
                synchronized (previousCartItems) {
                    boolean movie_id_exists = false;
                    for (int i = 0; i < previousCartItems.size(); i++) {
                        JsonObject je = previousCartItems.get(i).getAsJsonObject();
                        String id_of_movie = je.get("movie_id").getAsString();
                        if (id_of_movie.equals(movie_id_item)) {
                            movie_id_exists = true;
                            int existingQuantity = je.get("quantity").getAsInt();
                            je.addProperty("quantity", existingQuantity + 1);
                            break;
                        }
                    }
                    if(!movie_id_exists) {
                        System.out.println("MOVIE DOES NOT EXIST");
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty("movie_id", movie_id_item);
                        jsonObject.addProperty("quantity", 1);
                        jsonObject.addProperty("price", price);
                        previousCartItems.add(jsonObject);
                    }
                    session.setAttribute("previousCartItems", previousCartItems);
                }
            }
        } else if(action.equals("Decrease")) {
            for (int i = 0; i < previousCartItems.size(); i++) {
                JsonObject je = previousCartItems.get(i).getAsJsonObject();
                String id_of_movie = je.get("movie_id").getAsString();
                if (id_of_movie.equals(movie_id_item)) {
                    int existingQuantity = je.get("quantity").getAsInt();
                    if(existingQuantity > 1) {
                        je.addProperty("quantity", existingQuantity - 1);
                    }
                    break;
                }
            }
            session.setAttribute("previousCartItems", previousCartItems);
        }
        System.out.println("(POST) CURRENT CART ARRAY: " + previousCartItems);
        response.getWriter().write(previousCartItems.toString());
    }
}
