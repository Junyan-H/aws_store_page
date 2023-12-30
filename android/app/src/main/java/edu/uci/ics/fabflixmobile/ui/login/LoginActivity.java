package edu.uci.ics.fabflixmobile.ui.login;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.databinding.ActivityLoginBinding;
import edu.uci.ics.fabflixmobile.ui.mainPage.MainPageActivity;
import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {

    private EditText username;
    private EditText password;
    private TextView message;
//    private final String host = "10.0.2.2";
//    private final String port = "8080";
//    private final String domain = "cs122b_project1_api_example_war";
//    private final String baseURL = "http://" + host + ":" + port + "/" + domain;
    private final String host = "52.53.209.227";
    private final String port = "8443";
    private final String domain = "cs122b-project1-api-example";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        // upon creation, inflate and initialize the layout
        setContentView(binding.getRoot());

        username = binding.username;
        password = binding.password;
        message = binding.message;
        final Button loginButton = binding.login;

        //assign a listener to call a function to handle the user request when clicking a button
        loginButton.setOnClickListener(view -> login());
    }

    @SuppressLint("SetTextI18n")
    public void login() {
        message.setText("Logging in...");
        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        // request type is POST
        final StringRequest loginRequest = new StringRequest(

                // Grab post request from post method
                Request.Method.POST,
                baseURL + "/api/login",
                response -> {
                    // TODO: should parse the json response to redirect to appropriate functions
                    //  upon different response value.
                    Log.d("~~~~ LOGIN SUCCESS: ~~~~", response);
                    Log.d("USER", username.toString());
                    Log.d("pass", password.toString());

                    Gson gson = new Gson();
                    JsonObject js = gson.fromJson(response, JsonObject.class);
                    String status = js.get("status").getAsString();
                    Log.d("response to string", response);
                    Log.d("CHECKING STAT", String.valueOf(status.equals("customer_success")) );

                    if (status.equals("customer_success")){
                        //Complete and destroy login activity once successful
                        finish();
                        // initialize the activity(page)/destination
                        Intent MainPageActivity = new Intent(LoginActivity.this, MainPageActivity.class);
                        // activate the list page.
                        startActivity(MainPageActivity);
                    } else {
                        Log.d("~~~~ LOGIN ERROR: ~~~~", status);
                        message.setText("Incorrect Username/Password");
                    }
                },
                error -> {
                    // error
                    Log.d("~~~~ LOGIN ERROR: ~~~~", error.toString());
                }) {
            @Override
            protected Map<String, String> getParams() {
                // POST request form data
                final Map<String, String> params = new HashMap<>();
                params.put("username", username.getText().toString());
                params.put("password", password.getText().toString());
                params.put("android", "true");

                return params;
            }
        };
        // important: queue.add is where the login request is actually sent
        queue.add(loginRequest);
    }
}