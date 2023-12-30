
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

function handleMovieListResult(resultData) {
    console.log("HANDLING GET REQUEST IN MOVIE-LIST.js");
    console.log(resultData);
    // $("#search_results_length").text("(" + resultData.length + " results found)");
    let movieTableBodyElement = $("#movie_table_body");
    for (let i = 0; i < resultData.length; i++) {
        if (i === resultData.length - 1 && "sort_option" in resultData[resultData.length - 1]) {
            $("#dropdown").val(resultData[resultData.length - 1]["n"]);
            $("#dropdown2").val(resultData[resultData.length - 1]["sort_option"]);
            $("#pageNumParagraph").text("Page " + resultData[resultData.length - 1]["pageNum"]);
            break;
        }

        let rowHTML = "<tr>";
        if(resultData[i]["movie_rating"] === "-1.0") {
            rowHTML += "<th>★ N/A</th>";
        } else {
            rowHTML += "<th>★ " + resultData[i]["movie_rating"] + "</th>";
        }
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

        movieTableBodyElement.append(rowHTML);
    }
}

let movieGenre = getParameterByName('genre');
let movieTitle = getParameterByName('title');
let movieYear = getParameterByName('year');
let movieDirector = getParameterByName('director');
let movieStarName = getParameterByName('starName');
let movieN = getParameterByName('n');
let movieSortOption = getParameterByName('sortOption');
let movieArrow = getParameterByName('arrow');

$.when(
    $.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/movie-list?title=" + encodeURIComponent(movieTitle)
            + "&year=" + encodeURIComponent(movieYear)
            + "&director=" + encodeURIComponent(movieDirector)
            + "&starName=" + encodeURIComponent(movieStarName) // Setting request url, which is mapped by StarsServlet in Stars.java
            + "&genre=" + encodeURIComponent(movieGenre)
            + "&n=" + encodeURIComponent(movieN)
            + "&sortOption=" + encodeURIComponent(movieSortOption)
            + "&arrow=" + encodeURIComponent(movieArrow),
        success: (resultData) => handleMovieListResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
    })
).done(function() {
        console.log("done");
        $(document).on('click', 'form input[type="submit"][value="Add To Cart"]', function(event) {
            event.preventDefault(); // Prevent form submission
            let itemId = $(this).closest('form').find('input[name="item"]').val();
            // console.log("ID OF MOVIE CLICKED:" + itemId);
            $.ajax("api/cart", {
                method: "POST",
                data: {action: "Add", item: itemId, price: Math.floor((Math.random() * 100))},
                success: () => {
                    alert("Movie has been added to your shopping cart!");
                }
            });
        });
});


