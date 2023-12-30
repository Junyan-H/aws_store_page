
let movie_id;
function handleGetCartData(resultData) {
    console.log("Cart-pay get data")
    console.log(resultData);
    movie_id = resultData[0]["movie_id"];
    let totalPrice = 0;
    for(let i = 0; i < resultData.length; i++) {
        if (resultData[i]["quantity"] > 1) {
            totalPrice += (resultData[i]["price"] * resultData[i]["quantity"])
        } else {
            totalPrice += parseInt(resultData[i]["price"]);
        }
    }
    $("#total_price").append("<tr><th>$" + totalPrice + "</th></tr>");
}

function handlePaymentResult(resultDataString) {
    console.log("Handle Payment Result");
    let resultData = JSON.parse(resultDataString);
    console.log(resultData);
    console.log(resultData["status"]);

    if (resultData["status"] === "success") {
        console.log("Payment Success");
        $("#payment_error_message").text("");
        window.location.replace("confirm-pay.html?salesId=" + resultData["sale_id"]);
    } else {
        console.log("Payment Failed");
        $("#payment_error_message").text(resultData["message"]);
    }
}

let place_order_form = $("#place_order_form");
function handlePlaceOrder(orderEvent) {
    orderEvent.preventDefault();
    console.log(place_order_form.serialize() + "&movieId=" + movie_id);
    console.log(movie_id);
    $.ajax("api/pay", {
        method: "POST",
        data: place_order_form.serialize() + "&movieId=" + movie_id,
        success: handlePaymentResult
    });
}
place_order_form.submit(handlePlaceOrder);

$.ajax({
    dataType: "json",
    method: "GET",
    url: "api/cart",
    success: (resultData) => handleGetCartData(resultData)
});