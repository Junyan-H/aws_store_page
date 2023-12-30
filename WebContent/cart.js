
function handleGetCartData(resultData) {
    console.log("GETTING CART DATA:");
    console.log(resultData);
    let shoppingCartElement = $("#shopping_cart_table");
    let totalPrice = 0;
    for(let i = 0; i < resultData.length; i++) {
        if(resultData[i]["quantity"] > 1) {
            totalPrice += (resultData[i]["price"] * resultData[i]["quantity"])
        } else {
            totalPrice += parseInt(resultData[i]["price"]);
        }
        let rowHTML = "<tr>";
        rowHTML += "<th>$" + resultData[i]["price"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_title"] + "</th>";
        rowHTML += "<th>" + resultData[i]["quantity"] +
            ' <input name="' + resultData[i]["movie_id"] + '" type="submit" value="-"/> ' +
            ' <input name="' + resultData[i]["movie_id"] + '" type="submit" value="+"/></th>';

        rowHTML += '<th><form id="deleteButtonForm" METHOD=\"post\">' +
            '<input type="hidden" name="item" value="' + resultData[i]["movie_id"] + '">' +
            '<input type="submit" value="Delete"' +
            '</form></th>';

        rowHTML += "</tr>";
        shoppingCartElement.append(rowHTML);
    }
    $("#total_price").append("<tr><th>$" + totalPrice + "</th></tr>");
}

function handleDeleteMovieInCart(resultData) {
    console.log("Handle Delete Movie In Cart");
    console.log(resultData);
    window.location.reload();
}

$(document).on('click', 'form input[type="submit"][value="Delete"]', function(event) {
    event.preventDefault();
    let itemId = $(this).closest('form').find('input[name="item"]').val();
    console.log("DELETE CLICKED!");
    console.log(itemId);
    $.ajax({
        method: "POST",
        url: "api/cart",
        data: {
            action: "Delete",
            movie_id_to_delete: itemId
        },
        success: (resultData) => handleDeleteMovieInCart(resultData)
    });
});

$(document).on('click', 'input[type="submit"][value="+"]', function(event) {
    event.preventDefault();
    console.log("incr quantity clicked");
    let action = $(this).val();
    let itemId = $(this).attr("name");
    console.log("action: " + action);
    console.log("itemId: " + itemId);
    $.ajax({
        method: "POST",
        url: "api/cart",
        data: {action: "Add", item: itemId, price: Math.floor((Math.random() * 100))},
        success: (resultData) => handleDeleteMovieInCart(resultData)
    });
});

$(document).on('click', 'input[type="submit"][value="-"]', function(event) {
    event.preventDefault();
    console.log("decr quantity clicked");
    let itemId = $(this).attr("name");
    $.ajax({
        method: "POST",
        url: "api/cart",
        data: {action: "Decrease", item: itemId},
        success: (resultData) => handleDeleteMovieInCart(resultData)
    });
});

$.ajax({
    dataType: "json",
    method: "GET",
    url: "api/cart",
    success: (resultData) => handleGetCartData(resultData)
});
