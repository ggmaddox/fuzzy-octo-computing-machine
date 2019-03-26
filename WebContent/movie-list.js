// calls api/movie-list


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
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

function handleMovieResult(resultData) {
	console.log("Movie-list.js: handleMovieResult: populating movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    // TODO change star name to movie
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Iterate through resultData, no more than 20 entries
    let len = resultData.length;
    console.log(`Movie-list.js: resultsData.length = ${len}`)
    for (let i = 0; i < 20; i++) {

        // Concatenate the html tags with resultData jsonObject
    	// TODO fix dict/json keys
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" + '<a href = "single-movie.html?id=' + resultData[i]["movie_id"] + '">' + 
        resultData[i]["movie_title"] + "</a> </th>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        rowHTML += "<th>";
        
        // Genres column
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
//        rowHTML += "<th>" +
//            // Add a link to single-star.html with id passed with GET url parameter
//            // TODO Fix URLs for genres and stars. Use below as an example for genres. And make sure to print them correctly!
//            '<a href="single-star.html?id=' + resultData[i]['star_id'] + '">'
//            + resultData[i]["star_name"] +     // display star_name for the link text
//            '</a>' +
//            "</th>";
        rowHTML += "<th>" + resultData[i]["movie_rating"] + "</th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        // TODO change star name to movie
        movieTableBodyElement.append(rowHTML);
    }
    console.log("Movie-list.js: Done populating table");
}


let movieTitle = getParameterByName('movie-title');
let movieYear = getParameterByName('movie-year');
let movieDirector  = getParameterByName("movie-director");
let starName = getParameterByName("star-name");

console.log(`Movie-list.js: Sending parameters to api/movie-list: movieTitle = ${movieTitle}, movieYear = ${movieYear}, movieDirector = ${movieDirector}, starName = ${starName}`);
//Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movie-list?movie-title=" + movieTitle+"&movie-year="+
    	movieYear+"&movie-director="+movieDirector+"&star-name="+starName,
    success: (resultData) => handleMovieResult(resultData)
});