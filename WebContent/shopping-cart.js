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

    console.log("Filler, but handleSessionData is passing thru");
    
//    console.log("handle session response");
//    console.log(resultDataJson);
//    console.log(resultDataJson["sessionID"]);
//
//    // show the session information 
//    $("#sessionID").text("Session ID: " + resultDataJson["sessionID"]);
//    $("#lastAccessTime").text("Last access time: " + resultDataJson["lastAccessTime"]);
    
    let cartTableBodyElement = jQuery("#cart_table_body");

    // Iterate through resultData, no more than 20 entries
    for (let i = 0; i < resultDataJson.length; i++) {

        // Concatenate the html tags with resultData jsonObject
    	// TODO fix dict/json keys
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" + '<a href = "single-movie.html?id=' + resultDataJson[i]["movie_id"] + '">' + resultDataJson[i]["movie_title"] + "</a> </th>";
        // Form type
        rowHTML += "<th> <form method = \"GET\" action=\"shopping-cart.html\">";
        // Hidden movie id box
        rowHTML += '<input type="hidden" name="movie_id" value="' +resultDataJson[i]["movie_id"] + '">';
        // Quantity text box
        rowHTML += "<input type=\"number\" min = \"0\" name=\"q\" value=\"" + resultDataJson[i]["quantity"]+"\">";
        // Update Button
        rowHTML += "<input type=\"submit\" name=\"action\" value=\"Update\">";
        // Delete Button
        rowHTML += "<input type=\"submit\" name=\"action\" value=\"Remove\">";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        // TODO change star name to movie
        //console.log(rowHTML);
        cartTableBodyElement.append(rowHTML);
    }
}

let movieid = getParameterByName("movie_id");
let quantity = getParameterByName("q");
let action = getParameterByName("action");

let cur_url = "api/shopping-cart?";
console.log("Cur url 1: " + cur_url)
if (movieid !== null)
{
	cur_url += "movie_id=" + movieid;
}
console.log("Cur url 2: " + cur_url);
if (quantity !== null)
{
	cur_url += "&q=" + quantity;
}
console.log("Cur url 3: " + cur_url);
if (action !== null)
{
	cur_url += "&action=" + action;
}

console.log("Displaying parameters: " + movieid + quantity + action);
console.log("Displaying url: " + cur_url);
$.ajax({
    type: "GET",
    url: cur_url,
    success: (resultDataString) => handleSessionData(resultDataString)
});

