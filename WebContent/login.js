let login_form = $("#login_form");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleLoginResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);
    // let resultDataJson = resultDataString;
    console.log("Handle Login Result (Customer)");
    console.log(resultDataJson);
    console.log("PRINTING STATUS: " + resultDataJson["status"]);

    // If login succeeds, it will redirect the user to index.html
    if (resultDataJson["status"] === "customer_success") {
        // ************ need to transfer to main page instead of index.html *********
        window.location.replace("index.html");
    } else {
        // console.log("show error message");
        // console.log(resultDataJson["message"]);
        if(resultDataJson["message"] === "success") {
            $("#login_error_message").text("The username/password is not valid!");
        } else {
            $("#login_error_message").text(resultDataJson["message"]);
        }
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginForm(formSubmitEvent) {
    console.log("submit login form");
    formSubmitEvent.preventDefault();
    $.ajax(
        "api/login", {
            method: "POST",
            // Serialize the login form to the data sent by POST request
            data: login_form.serialize(),
            success: handleLoginResult
        }
    );
    console.log("end submit login form");
}

// Bind the submit action of the form to a handler function
login_form.submit(submitLoginForm);

