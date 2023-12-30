

function handleGetCartData(resultData) {
    console.log("CART DATA IN CONFIRMATION:");
    console.log(resultData);
    let orderSummaryTableElement = $("#order_summary_table");
    let totalPrice = 0;
    const urlParams = new URLSearchParams(window.location.search);
    const salesId = urlParams.get('salesId');
    for(let i = 0; i < resultData.length; i++) {
        if(resultData[i]["quantity"] > 1) {
            totalPrice += (resultData[i]["price"] * resultData[i]["quantity"])
        } else {
            totalPrice += parseInt(resultData[i]["price"]);
        }
        let rowHTML = "<tr>";
        rowHTML += "<th>$" + resultData[i]["price"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_title"] + "</th>";
        rowHTML += "<th>" + resultData[i]["quantity"] + "</th>";
        rowHTML += "<th>" + salesId + "</th>";
        rowHTML += "</tr>";
        orderSummaryTableElement.append(rowHTML);
    }
    $("#total_price").append("<tr><th>$" + totalPrice + "</th></tr>");
}


$.ajax({
    dataType: "json",
    method: "GET",
    url: "api/cart",
    success: (resultData) => handleGetCartData(resultData)
});