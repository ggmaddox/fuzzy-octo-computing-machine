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

    console.log("handleResult: add-movie resultData");

    // empty <div> to populate in add-movie.html
    var errorElement = jQuery("#error-message");

    // append error message, if any.
    errorElement.append(resultData[resultData.length-1]["error-message"]);

    console.log("handleResult: populating error-message div");
    
    showMetaData(resultData);
    showStatus(resultData);
}

function showMetaData(resultData) {
	var metaDataElement = jQuery("#movie-table-body");

	let rowHTML = "";
	for (let i = 0; i < 4; i++) {
	    
	    rowHTML += "<tr>";
	    rowHTML += "<th>" + resultData[i]["column-name"] + "</th>";
	    rowHTML += "<th>" + resultData[i]["column-type"] + "</th>";
	    rowHTML += "<th>" + resultData[i]["column-constraint"] + "</th>";
	    rowHTML += "</tr>";
	}

	metaDataElement.append(rowHTML);
	
	var starMetadata = jQuery("#stars-table-body");
	rowHTML = "";
	for (let i = 4; i < 7; ++i) {
		rowHTML += "<tr>";
	    rowHTML += "<th>" + resultData[i]["column-name"] + "</th>";
	    rowHTML += "<th>" + resultData[i]["column-type"] + "</th>";
	    rowHTML += "<th>" + resultData[i]["column-constraint"] + "</th>";
	    rowHTML += "</tr>";
	}
	starMetadata.append(rowHTML);
	
	var genreMetadata = jQuery("#genres-table-body");
	rowHTML = "";
	for (let i = 7; i < 9; ++i) {
		rowHTML += "<tr>";
	    rowHTML += "<th>" + resultData[i]["column-name"] + "</th>";
	    rowHTML += "<th>" + resultData[i]["column-type"] + "</th>";
	    rowHTML += "<th>" + resultData[i]["column-constraint"] + "</th>";
	    rowHTML += "</tr>";
	}
	genreMetadata.append(rowHTML);
	console.log("handleResult: populating metadata-message divs");
}

function showStatus(resultData) {
	var statusElement = jQuery("#status");

	let rowHTML = "";
	for (let i = 9; i < resultData.length - 1; i++) {
	    
	    rowHTML += "<p>" + resultData[i]["msg"] + "</p><br>";
	}

	statusElement.append(rowHTML);
	console.log("handleResult: populating status-message div");
}



// Get id from URL
var title = getParameterByName('movie-title');
var year = getParameterByName('movie-year');
var director = getParameterByName('movie-director');
var star = getParameterByName('star-name');
var genre = getParameterByName('genre-name');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/add-movie?movie-title=" + title + "&movie-year=" + year +
    	"&movie-director=" + director + "&star-name=" + star +
    	"&genre-name=" + genre, // Setting request url, which is mapped by AddMovieServlet
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});