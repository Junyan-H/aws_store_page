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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


@WebServlet(name = "PayServlet", urlPatterns = "/api/pay")
public class PayServlet extends HttpServlet {
    private static final long serialVersionUID = 7L;

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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String ccNum = request.getParameter("ccNum");
        String expDate = request.getParameter("expDate");
        String movieId = request.getParameter("movieId");

        System.out.println(firstName);
        System.out.println(lastName);
        System.out.println(ccNum);
        System.out.println(expDate);
        System.out.println(movieId);
        JsonObject responseJsonObject = new JsonObject();

        try (Connection conn = dataSource2.getConnection()) {
            String query = "SELECT * FROM creditcards WHERE id = ? AND firstName = ? AND lastName = ? AND expiration = ?";
            PreparedStatement pStatement = conn.prepareStatement(query);
            pStatement.setString(1, ccNum);
            pStatement.setString(2, firstName);
            pStatement.setString(3, lastName);
            pStatement.setString(4, expDate);
            ResultSet rs = pStatement.executeQuery();

            // Verification Success
            boolean paymentIsVerified = false;
            if (rs.next()) {
                System.out.println("Verification Success");
                responseJsonObject.addProperty("status", "success");
                paymentIsVerified = true;
            }
            // Verification Fail
            else {
                System.out.println("Verification Failed");
                responseJsonObject.addProperty("status", "failed");
                responseJsonObject.addProperty("message", "Payment Verification Failed. Please try again!");
            }
            rs.close();
            pStatement.close();
            if(paymentIsVerified) {
                // GET CUSTOMER ID FROM NAME
                String getCustomerIdQuery = "SELECT * FROM customers WHERE firstName = ? AND lastName = ?";
                PreparedStatement pStatement2 = conn.prepareStatement(getCustomerIdQuery);
                pStatement2.setString(1, firstName);
                pStatement2.setString(2, lastName);
                ResultSet rs2 = pStatement2.executeQuery();
                String customerId = "";
                while (rs2.next()) {
                    customerId = rs2.getString("id");
                }
                rs2.close();
                pStatement2.close();

                // UPDATE SALES TABLE
                LocalDate currentDate = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                String currDate = currentDate.format(formatter);

                String updateSalesQuery = "INSERT INTO sales(customerId, movieId, saleDate) VALUES (?, ?, ?)";
                PreparedStatement pStatement3 = conn.prepareStatement(updateSalesQuery);
                pStatement3.setString(1, customerId);
                pStatement3.setString(2, movieId);
                pStatement3.setDate(3, java.sql.Date.valueOf(currDate));
                int rowsAffected = pStatement3.executeUpdate();
                System.out.println(rowsAffected + " row(s) inserted.");
                System.out.println(currDate);
                System.out.println(java.sql.Date.valueOf(currDate));
                pStatement3.close();

                // GET NEW SALES ID
                String getSalesIDQuery = "SELECT id FROM sales ORDER BY ID DESC LIMIT 1;";
                PreparedStatement pStatement4 = conn.prepareStatement(getSalesIDQuery);
                ResultSet rs4 = pStatement4.executeQuery();
                while (rs4.next()) {
                    String sale_id = rs4.getString("id");
                    responseJsonObject.addProperty("sale_id", sale_id);
                }
                rs4.close();
                pStatement4.close();
            }
            response.getWriter().write(responseJsonObject.toString());
            response.setStatus(200);
        }  catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            response.getWriter().write(jsonObject.toString());
            response.setStatus(500);
        } finally {
            response.getWriter().close();
        }
    }
}