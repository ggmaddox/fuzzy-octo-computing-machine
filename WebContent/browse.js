/* 
 * JavaScript file for /browse.html
 * 
 */

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleTablesResult(resultData) {
	
	// Populating Titles table
	console.log("Browse.js - handleGenresResult: populating title table");

	// Define where the HTML code should be inserted to
    let characterTableBodyElement = jQuery("#character_table_body");

    // Start of creating HTML code string
    let titleHTML = "";
    titleHTML += "<tr>";
    
    // Filling in alphanumeric table (36 characters 0-9, A-Z)
    for (let i = 0; i < 36; i++)
    {
    	// Creates rows of 9 elements
    	if (i % 10 == 9)
    	{
    		// If this is the 9th element, append </tr> code to indicate the end of the row
    		titleHTML += "<td align = 'center'><a href = 'browse-results.html?title=" + 
    		resultData["title_characters"][i] + "&genre=&page-num=1&display-num=10&sort-by=&list='>" + 
    		resultData["title_characters"][i] + "</a></td>" + "</tr><tr>";
    	}
    	else
    	{
    		titleHTML += "<td align = 'center'><a href = 'browse-results.html?title=" + 
    		resultData["title_characters"][i] + "&genre=&page-num=1&display-num=10&sort-by=&list='>" + resultData["title_characters"][i] + "</a></td>";
    	}
    }
    titleHTML += "</tr>";
    //console.log("Current titleHTML:" + titleHTML);
    //for (let i = 0; i <)
        
//        rowHTML += "<tr>";
//        rowHTML += "<th>" + '<a href = "single-movie.html?id=' + resultData[i]["movie_id"] + '">' + resultData[i]["movie_title"] + "</a> </th>";
//        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
//        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
//        rowHTML += "<th>";
//        
//        // Genres column
//        for (let j = 0; j < resultData[i]["genres"].length-1; j++)
//        	{
//        		rowHTML += '<a href="single-genre.html?id=' + resultData[i]["genres"][j]["genre_id"] +'">' + resultData[i]["genres"][j]["genre_name"] + '</a>, ';
//        	}
//        rowHTML += '<a href="single-genre.html?id=' + resultData[i]["genres"][resultData[i]["genres"].length-1]["genre_id"] +'">' + resultData[i]["genres"][resultData[i]["genres"].length-1]["genre_name"] + '</a>';
//        rowHTML += "</th>";
//        
//        // Stars column
//        rowHTML += "<th>";
//        for (let k = 0; k < resultData[i]["stars"].length - 1; k++)
//    	{
//    		rowHTML += '<a href="single-star.html?id=' + resultData[i]["stars"][k]["star_id"] +'">' + resultData[i]["stars"][k]["star_name"] + '</a>, ';
//    	}
//        rowHTML += '<a href="single-star.html?id=' + resultData[i]["stars"][resultData[i]["stars"].length - 1]["star_id"] +'">' + resultData[i]["stars"][resultData[i]["stars"].length - 1]["star_name"] + '</a>';
//        rowHTML += "</th>";
//
//        rowHTML += "<th>" + resultData[i]["movie_rating"] + "</th>";
//        rowHTML += "</tr>";

    	// Append HTML code for alphanumeric numbers into the body
        characterTableBodyElement.append(titleHTML);
        
        // ------------------------------------------------------
        // Populating Genre table
        console.log("Browse.js - handleGenresResult: populating genre table");
        
        let genreTableBodyElement = jQuery("#genre_table_body")
        
        // Start creating genre HTML code string
        let genreHTML = "";
        genreHTML += "<tr>";
        
        // For every genre from the result set
        for (let k = 0; k < resultData["genres"].length; k++)
        {
        	// Create rows of 5 genres 
        	if (k % 6 == 5)
        	{
        		// If you're at the 5th element of that row, append </tr> tags
        		genreHTML += "<td align = 'center'><a href = 'browse-results.html?title=&genre=" + 
        		resultData["genres"][k]["genre_id"].toString() + "&page-num=1&display-num=10&sort-by=&list='>" + 
        		resultData["genres"][k]["genre_name"] + "</a></td>" + "</tr><tr>";
        	}
        	else
        	{
        		genreHTML += "<td align = 'center'><a href = 'browse-results.html?title=&genre=" + 
        		resultData["genres"][k]["genre_id"].toString() + "&page-num=1&display-num=10&sort-by=&list='>" + 
        		resultData["genres"][k]["genre_name"] + "</a></td>";
        	}
        }
        genreHTML += "</tr>";
        // Append genreHTML code into table
        genreTableBodyElement.append(genreHTML);
}

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/browse", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleTablesResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});