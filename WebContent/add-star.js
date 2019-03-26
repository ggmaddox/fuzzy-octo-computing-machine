/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    // Replaces [ ] w/ "\\[insides]&"
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    // [?&] matches any occurance of ? and & 
    // (=([^&#]*)|&|#|$) matches for "=The+Terminator&" or "=The+Terminator#"
    // $ assures that it's the end of the string
    //this regez is basically 
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    // \+ matches "+" exactly.
    // SO, it's replacing any + with spaces, 
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function handleResult(resultData) {

    console.log("handleResult: add-star resultData");

    // empty <div> to populate in add-movie.html
    var errorElement = jQuery("#error-message");
    var successElement = jQuery("#error-message");

    // append error message, if any.
    errorElement.append(resultData[resultData.length-1]["error-message"]);
    successElement.append(resultData[resultData.length-2]["success-message"]);
    console.log("handleResult: populating error-message div");
    
    showMetaData(resultData);
}

function showMetaData(resultData) {
	var metaDataElement = jQuery("#star-table-body");

	let rowHTML = "";
	for (let i = 0; i < resultData.length - 2; i++) {
	    
	    rowHTML += "<tr>";
	    rowHTML += "<th>" + resultData[i]["column-name"] + "</th>";
	    rowHTML += "<th>" + resultData[i]["column-type"] + "</th>";
	    rowHTML += "<th>" + resultData[i]["column-constraint"] + "</th>";
	    rowHTML += "</tr>";
	}

	metaDataElement.append(rowHTML);
	console.log("handleResult: populating metadata-message div");
}

// Get id from URL
var starName = getParameterByName('star-name');
var starDob = getParameterByName('star-dob');
// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/add-star?star-name=" + starName + "&star-dob=" + starDob, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});