/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs three steps:
 *      1. Get parameter from request URL so it know which id to look for
 *      2. Use jQuery to talk to backend API to get the json data.
 *      3. Populate the data to correct html elements.
 */

/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
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

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating star info from resultData");

    // populate the star info h3
    // find the empty h3 body by id "single_movie_info"
    let singleMovieInfoElement = jQuery("#single_movie_info");

    // append three html <p> created to the h3 body, which will refresh the page
    // "movie_title, etc. created in SingleMovieServlet.java (json object)
    singleMovieInfoElement.append("<p>Movie Title: " + resultData[0]["movie_title"] + "</p>" +
        "<p>Year: " + resultData[0]["movie_year"] + "</p>" +
        "<p>Director: " + resultData[0]["movie_director"] + "</p>");

    console.log("handleResult: populating movie table from resultData");

    // Populate the single movie table
    // Find the empty table body by id "single_movie_table_body"
    let singleMovieTableBodyElement = jQuery("#single_movie_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" + resultData[i]["movie_id"] + "</th>";
        
        // Genres column
        rowHTML += "<th>";
        for (let j = 0; j < resultData[i]["genres"].length-1; j++)
        	{
        		rowHTML += '<a href="single-genre.html?id=' + 
        		resultData[i]["genres"][j]["genre_id"] +'">' + 
        		resultData[i]["genres"][j]["genre_name"] + '</a>, ';
        	}
        rowHTML += '<a href="single-genre.html?id=' + 
        resultData[i]["genres"][resultData[i]["genres"].length-1]["genre_id"] +
        '">' + resultData[i]["genres"][resultData[i]["genres"].length-1]["genre_name"] + '</a>';
        rowHTML += "</th>";
        
        // Stars column
        rowHTML += "<th>";
        for (let k = 0; k < resultData[i]["stars"].length - 1; k++)
    	{
    		rowHTML += '<a href="single-star.html?id=' + 
    		resultData[i]["stars"][k]["star_id"] +'">' +
    		resultData[i]["stars"][k]["star_name"] + '</a>, ';
    	}
        rowHTML += '<a href="single-star.html?id=' + 
        resultData[i]["stars"][resultData[i]["stars"].length - 1]["star_id"] +
        '">' + resultData[i]["stars"][resultData[i]["stars"].length - 1]["star_name"] + '</a>';
        rowHTML += "</th>";
        
        rowHTML += "<th>" + resultData[i]["movie_rating"] + "</th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        singleMovieTableBodyElement.append(rowHTML);
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    
    // Setting request url, which is mapped by SingleMovieServlet.java
    url: "api/single-movie?id=" + movieId, 
    
    // Setting callback function to handle data returned successfully by the SingleMovieServlet
    success: (resultData) => handleResult(resultData) 

});