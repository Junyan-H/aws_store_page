

function handleTop20Result(resultData) {
    // console.log("handleStarResult: populating star table from resultData");
    // console.log(resultData);
    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let top20TableBodyElement = jQuery("#top20_table_body");

    for (let i = 0; i < Math.min(20, resultData.length); i++) {
        let rowHTML = "<tr>";
        rowHTML += "<th>★ " + resultData[i]["movie_rating"] + "</th>";
        rowHTML +=
            '<th><a href="single-movie.html?id=' + resultData[i]["movie_id"] + '">'
            + resultData[i]["movie_title"] + '</a></th>';
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";

        // TOP 3 STARS
        rowHTML += "<th><ul> ";
        for (let j = 0; j < resultData[i]["star_list"].length; j++){
            rowHTML +=
                // Add a link to single-star.html with id passed with GET url parameter
                '<li><a href="single-star.html?id=' + resultData[i]["star_list"][j]["star_id"] + '">'
                + resultData[i]["star_list"][j]["star_name"] +     // display star_name for the link text
                '</a></li>';
        }
        rowHTML+= "</ul></th>";

        // TOP 3 GENRE
        rowHTML += "<th><ul>"
        for (let j = 0; j < resultData[i]["movie_genres"].length; j++){
            // rowHTML += '<li>' + resultData[i]["movie_genres"][j] + '</li>';
            rowHTML +=
                '<li><a href="movie-list.html?genre=' + resultData[i]["movie_genres"][j] + '">'
                + resultData[i]["movie_genres"][j] + '</a></li>';
        }
        rowHTML += "</ul></th>"

        // Add cart button
        rowHTML += "<th>"
        rowHTML += "<form id=\"shopping_cart\" METHOD=\"post\">\n" +
            "<input type=\"hidden\" name=\"item\" value=\"" + resultData[i]["movie_id"] + "\">\n" +
            "<input type=\"submit\" value=\"Add To Cart\">\n" +
            "</form>"
        rowHTML += "</th>"
        rowHTML += "</tr>";

        top20TableBodyElement.append(rowHTML);
    }
}

$(document).on('click', 'form input[type="submit"][value="Add To Cart"]', function(event) {
    event.preventDefault(); // Prevent form submission
    let itemId = $(this).closest('form').find('input[name="item"]').val();
    console.log("ID OF MOVIE CLICKED:" + itemId);
    $.ajax("api/cart", {
        method: "POST",
        data: {action: "Add", item: itemId, price: Math.floor((Math.random() * 100))},
        success: () => {
            alert("Movie has been added to your shopping cart!");
        }
    });
});

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/top-20", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleTop20Result(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});