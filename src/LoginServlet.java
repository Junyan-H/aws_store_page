import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

@WebServlet(name = "LoginServlet", urlPatterns = {"/api/login", "/_dashboard/api/login"})
public class LoginServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    private static DataSource dataSource;
    private boolean userIsValid = false;
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    private static ArrayList<Object> verifyCredentials(String email, String password) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            System.out.println("1");
            ArrayList<Object> success = new ArrayList<>();
            // HANDLE CUSTOMER LOGIN
            String query = "SELECT * from customers where email = ?";
            PreparedStatement pStatement = conn.prepareStatement(query);
            pStatement.setString(1, email);
            ResultSet rs = pStatement.executeQuery();
            System.out.println("2");
            if (rs.next()) {
                String encryptedPassword = rs.getString("password");
                success.add("customer");
                success.add(new StrongPasswordEncryptor().checkPassword(password, encryptedPassword));
                rs.close();
                pStatement.close();
                conn.close();
                System.out.println("Verify customer email: " + email + " - " + password);
                System.out.println(success);
                return success;
            }
            System.out.println("3");
            // HANDLE EMPLOYEE LOGIN
            String employeeQuery = "SELECT * from employees where email = ?";
            PreparedStatement pStatementEmployee = conn.prepareStatement(employeeQuery);
            pStatementEmployee.setString(1, email);
            ResultSet employee_rs = pStatementEmployee.executeQuery();
            if (employee_rs.next()) {
                String encryptedPassword = employee_rs.getString("password");
                success.add("employee");
                success.add(new StrongPasswordEncryptor().checkPassword(password, encryptedPassword));
                employee_rs.close();
                pStatementEmployee.close();
                conn.close();
                System.out.println(success);
                System.out.println("Verified employee email: " + email);
            }

            if (success.size() == 0){
                success.add("customer");
                success.add(false);

            }
            return success;

        } catch (Exception e) {
            System.out.println("Verify Credentials Error: " + e);
            ArrayList<Object> error = new ArrayList<Object>(); error.add("Error");
            return error;
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String android = request.getParameter("android");

        System.out.println(username);
        System.out.println(password);
        System.out.println(android);

        if(android == null || android.equals("null")) {
            // FIRST, VERIFY RECAPTCHA
            // response.setContentType("application/json");
            String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
            System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);
            try {
                System.out.println("reCaptcha: Verifying");
                RecaptchaVerifyUtils.verify(gRecaptchaResponse);
                userIsValid = true;
            } catch (Exception e) {
                if(!userIsValid) {
                    System.out.println("reCaptcha: Catch");
                    System.out.println("PLEASE VERIFY YOU ARE HUMAN!");
                    System.out.println(e.getMessage());
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("status", "fail");
                    jsonObject.addProperty("message", "Please verify that you are human!");
                    response.getWriter().write(jsonObject.toString());
                    response.setStatus(200);
                    return;
                }
            }
            // END RECAPTCHA //////////////////////////////
            ///////////////////////////////////////////////
        }

        JsonObject responseJsonObject = new JsonObject();
        // Verify user/pass
        try {
            ArrayList<Object> verifiedArray = verifyCredentials(username, password);
            String userType = (String) verifiedArray.get(0);
            boolean isVerified = (Boolean) verifiedArray.get(1);
            System.out.print(" SEEING UERTYPE AND VERIFICATION");
            System.out.println(userType);
            System.out.println(isVerified);
            if(userType.equals("customer") && isVerified) {
                request.getSession().setAttribute("user", new User(username, "customer"));
                responseJsonObject.addProperty("status", "customer_success");
                responseJsonObject.addProperty("message", "success");
            } else if (userType.equals("employee") && isVerified){
                request.getSession().setAttribute("user", new User(username, "employee"));
                responseJsonObject.addProperty("status", "employee_success");
                responseJsonObject.addProperty("message", "success");
            } else {
                responseJsonObject.addProperty("status", "fail");
                request.getServletContext().log("Login failed");
                responseJsonObject.addProperty("message", "The username/password is not valid!");
            }
            response.getWriter().write(responseJsonObject.toString());
            response.setStatus(200);
        } catch (Exception e) {
            System.out.print("error : "+ e);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            response.getWriter().write(jsonObject.toString());
            response.setStatus(500);
        } finally {
            System.out.print("FINALLY :");
            System.out.println(responseJsonObject.get("status"));

            response.getWriter().close();
        }
    }
}
