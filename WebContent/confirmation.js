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

function handleSessionData(resultDataString) 
	{
	//console.log(alert(resultDataString));
    resultDataJson = JSON.parse(JSON.stringify(resultDataString));

    console.log("Confirmation.js: Got json data");
    
//    console.log("handle session response");
//    console.log(resultDataJson);
//    console.log(resultDataJson["sessionID"]);
//
//    // show the session information 
//    $("#sessionID").text("Session ID: " + resultDataJson["sessionID"]);
//    $("#lastAccessTime").text("Last access time: " + resultDataJson["lastAccessTime"]);
    
    let cartTableBodyElement = jQuery("#checked_out_cart_table_body");

    // Iterate through resultData, no more than 20 entries
    for (let i = 0; i < resultDataJson.length; i++) {


        let rowHTML = "";
        rowHTML += "<tr>";
        // Sales ID
        rowHTML += "<th>" + resultDataJson[i]["sales_id"].toString() + "</th>";
        // Movie link
        rowHTML += "<th>" + '<a href = "single-movie.html?id=' + resultDataJson[i]["movie_id"] + '">' + resultDataJson[i]["movie_title"] + "</a> </th>";
        // Quantity
        rowHTML += "<th>" + resultDataJson[i]["count"].toString() + "</th>";
        rowHTML += "</tr>";
        
        console.log(rowHTML);
        cartTableBodyElement.append(rowHTML);
    }
    
    console.log("Confirmation.js: Populated Table");
}

//let movieid = getParameterByName("movie_id");
//let quantity = getParameterByName("q");
//let action = getParameterByName("action");
//
//let cur_url = "api/shopping-cart?";
//console.log("Cur url 1: " + cur_url)
//if (movieid !== null)
//{
//	cur_url += "movie_id=" + movieid;
//}
//console.log("Cur url 2: " + cur_url);
//if (quantity !== null)
//{
//	cur_url += "&q=" + quantity;
//}
//console.log("Cur url 3: " + cur_url);
//if (action !== null)
//{
//	cur_url += "&action=" + action;
//}

//console.log("Displaying parameters: " + movieid + quantity + action);
//console.log("Displaying url: " + cur_url);

console.log("Confirmation.js: sending request")
$.ajax({
	dataType : "json",
    type: "GET",
    url: "api/confirmation",
    success: (resultDataString) => handleSessionData(resultDataString)
});


