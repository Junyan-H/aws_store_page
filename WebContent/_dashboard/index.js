let add_star_form = $("#add_star_form");
let add_movie_form = $("#add_movie_form");

function handleAddResponse(resultDataString) {
    let resultData = JSON.parse(resultDataString);
    alert(resultData["message"]);
}

// Handle adding new stars to db
function handleAddStarForm(event) {
    console.log("handle add star form");
    event.preventDefault();
    $.ajax("api/employee_dashboard", {
        method: "POST",
        data: add_star_form.serialize(),
        success: handleAddResponse
    });
}

// Handle adding new movies to db
function handleAddMovieForm(event) {
    console.log("handle add movie form");
    event.preventDefault();
    $.ajax("api/employee_dashboard", {
        method: "POST",
        data: add_movie_form.serialize(),
        success: handleAddResponse
    });
}

// Get metadata
function handleMetaData(resultData) {
    let metadata_table = jQuery("#metadata_table");

    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "<tr>";
        rowHTML += "<th>" + resultData[i]["table_name"] + "</th>";
        rowHTML += "<th><ul style='list-style: none;'>";
        for (let j = 0; j < resultData[i]["fieldTypeArray"].length; j++){
            rowHTML += '<li>' + resultData[i]["fieldTypeArray"][j]["table_field"] + '</li>';
        }
        rowHTML+= "</ul></th>";

        rowHTML += "<th><ul style='list-style: none;'>";
        for (let j = 0; j < resultData[i]["fieldTypeArray"].length; j++){
            rowHTML += '<li>' + resultData[i]["fieldTypeArray"][j]["table_type"] + '</li>';
        }
        rowHTML+= "</ul></th>";
        rowHTML += "</tr>";
        metadata_table.append(rowHTML);
    }
}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/employee_dashboard",
    success: (resultData) => handleMetaData(resultData)
});

add_star_form.submit(handleAddStarForm);
add_movie_form.submit(handleAddMovieForm);