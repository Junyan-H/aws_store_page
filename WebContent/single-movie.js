/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */

function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
let movieId = getParameterByName('id');
function handleResult(resultData) {
    console.log("handleResult: populating star info from resultData");
    // populate the star info h3
    // find the empty h3 body by id "star_info"
    let movieInfoElement = jQuery("#movie_info");
    console.log("RESULTS:");
    console.log(resultData);
    // append two html <p> created to the h3 body, which will refresh the page
    movieInfoElement.append("<p>Movie Title: " + resultData[0]["title"] + "</p>" +
        "<p>Director: " + resultData[0]["director"] + "</p>" +
        "<p>Year: " + resultData[0]["year"] + "</p>" +
        "<p>Rating: ★ " + resultData[0]["rating"] + "</p>");

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Frontend should not cache more than 100 records
    for (let i = 0; i < Math.min(100, resultData.length); i++) {
        let rowHTML = "";
        rowHTML += "<tr>";

        // MOVIE STARS
        rowHTML += "<th><ul> ";
        for (let j = 0; j < resultData[i]["star_list"].length; j++){
            rowHTML +=
                '<li><a href="single-star.html?id=' + resultData[i]["star_list"][j]["star_id"] + '">'
                + resultData[i]["star_list"][j]["star_name"] +
                '</a></li>';
        }
        rowHTML+= "</ul></th>";

        // GENRES
        rowHTML += "<th><ul>"
        for (let j = 0; j < resultData[i]["movie_genres"].length; j++){
            rowHTML +=
                '<li><a href="movie-list.html?genre=' + resultData[i]["movie_genres"][j] + '">'
                + resultData[i]["movie_genres"][j] + '</a></li>';
        }
        rowHTML += "</ul></th>"

        // Add cart button
        rowHTML += "<th>"
        rowHTML += "<form id=\"shopping_cart\" METHOD=\"post\">\n" +
            "<input type=\"hidden\" name=\"item\" value=\"" + movieId + "\">\n" +
            "<input type=\"submit\" value=\"Add To Cart\">\n" +
            "</form>"
        rowHTML += "</th>"
        rowHTML += "</tr>";
        movieTableBodyElement.append(rowHTML);
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */
$(document).on('click', 'form input[type="submit"][value="Add To Cart"]', function(event) {
    event.preventDefault(); // Prevent form submission
    console.log("ID OF SINGLE MOVIE:" + movieId);
    $.ajax("api/cart", {
        method: "POST",
        data: {action: "Add", item: movieId, price: Math.floor((Math.random() * 100))},
        success: () => {
            alert("Movie has been added to your shopping cart!");
        }
    });
});

jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});