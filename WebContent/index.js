let browseByGenreElement = $("#browseByGenre");
function handleBrowsingGenres(data) {
    console.log("Handling browsing Genres")
    let resultData = JSON.parse(data);
    for (let i = 0; i < resultData.length; i++) {
        let liElement = $("<li><a href='movie-list.html?genre=" + resultData[i]["genreName"] + "' >" + resultData[i]["genreName"] + "</a></li>");
        browseByGenreElement.append(liElement);
    }
}

// Generate A-Z
let browseByTitleElement = $("#browseByTitle");
for (let i = 65; i <= 90; i++) {
    let letter = String.fromCharCode(i);
    let liElement = $("<li><a href='movie-list.html?title=" + letter + "&year=&director=&starName=&genre='>" + letter + "</a></li>");
    browseByTitleElement.append(liElement);
}

// Generate 0-9 & *
let browseByNumElement = $("#browseByTitleNum");
// Loop through A to Z and generate links

for (let i = 0; i < 10; i++) {
    let liElement = $("<li><a href='movie-list.html?title=" + i + "&year=&director=&starName=&genre='>" + i + "</a></li>");
    browseByNumElement.append(liElement);
    if(i === 9) {
        let liStarElement = $("<li><a href='movie-list.html?title=*&year=&director=&starName=&genre='>*</a></li>");
        browseByNumElement.append(liStarElement);
    }
}

sessionStorage.clear();

// Autocomplete /////////////////////////////////////////////
function handleLookup(query, doneCallback) {
    console.log("Autocomplete initiated (after delay)")
    let queryInCache = sessionStorage.getItem(query);
    if(queryInCache === null) {
        console.log("Sending AJAX request to server...")
        jQuery.ajax({
            "method": "GET",
            "url": "api/main-page?acQuery=" + query,
            "success": function (data) {
                // pass the data, query, and doneCallback function into the success handler
                handleLookupAjaxSuccess(data[0], query, doneCallback)
            },
            "error": function (errorData) {
                console.log("lookup ajax error")
                console.log(errorData)
            }
        })
    } else {
        console.log("Query exists, loading from cache...");
        let parsedObject = JSON.parse(queryInCache);
        console.log(parsedObject);
        doneCallback( { suggestions: parsedObject } );
    }
}

function handleLookupAjaxSuccess(data, query, doneCallback) {
    // parse the string into JSON
    let jsonData = JSON.parse(data).slice(0, 10);
    console.log(jsonData);
    let jsonDataString = JSON.stringify(jsonData);

    // Cache to session
    sessionStorage.setItem(query, jsonDataString);
    doneCallback( { suggestions: jsonData } );
}

function handleSelectSuggestion(suggestion) {
    // console.log("Selected: " + suggestion["value"] + " ID: " + suggestion["data"]["movieId"])
    window.location.href = "single-movie.html?id=" + suggestion["data"]["movieId"];
}

$('#autocomplete').autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        if (query.length < 3) {
            return;
        }
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },
    deferRequestBy: 300,
});

function handleNormalSearch(query) {
    window.location.href = "movie-list.html?title=" + query + "&year=&director=&starName=&genre=";
}

$('#acSearch').on('click', function(event) {
    event.preventDefault(); // Prevent form submission (if the button is inside a form)
    handleNormalSearch($('#autocomplete').val())
    console.log("ac search button clicked");
})
$('#autocomplete').keypress(function(event) {
    if (event.keyCode == 13) {
        handleNormalSearch($('#autocomplete').val())
    }
})

// End Autocomplete /////////////////////////////////////////////

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/main-page",
    success: (resultData) => handleBrowsingGenres(resultData[1])
});