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

function s_handlePrevNextResults()
{
	console.log("Search.js: HandlePrevNextResults!!!");
	var newHTML = '';
	if (getParameterByName('search_term') != null)
	{
//		var prev_pg = (parseInt(getParameterByName('page-num')) - 1).toString();
//		var next_pg = (parseInt(getParameterByName('page-num')) + 1).toString();
		var prevNextTableBodyElement = jQuery("#prev-next-forms");
		
		console.log("Search.js: using the advacned-search one!");
		var page_num = 1;
		if (getParameterByName('page-num') != null){
			page_num = parseInt(getParameterByName('page-num'));
		}
		//var page_num = parseInt(getParameterByName('page-num'));
		console.log(`Search.js page_num = ${page_num}`);
		
		if (page_num > 1)
		{
			// You do not want to include a prev button if you're on page 1
			// this runs if page num is (>=2)
			newHTML += '<form method = "get" action = "#">';
			newHTML += '<input type="hidden" id="movie-title" name="search_term" value="' + getParameterByName('search_term') + '">';
			newHTML += '<input type="hidden" id="page-num" name="page-num" value="' + (page_num - 1).toString() + '">';	
			newHTML += '<input type="submit" name="action" value="Prev">';
			newHTML += '</form>';
		}
		
		newHTML += '<form method = "get" action = "#">';
		newHTML += '<input type="hidden" id="movie-title" name="search_term" value="' + getParameterByName('search_term') + '">';
		newHTML += '<input type="hidden" id="page-num" name="page-num" value="' + (page_num + 1).toString() + '">';		
		newHTML += '<input type="submit" name="action" value="Next">';
		newHTML += '</form>';
		console.log("Search.js: appending html: " + newHTML);
		prevNextTableBodyElement.append(newHTML);
	}
	
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleSearchResult(resultData) {
	console.log("Search.js: handleSearchResult: populating movie table from resultData");

    // Populate the movie table
    // Find the empty table body by id "movie-table-body"
    let movieTableBodyElement = jQuery("#search-results-movie-table-body");

    // Iterate through resultData, no more than 20 entries
    let len = resultData.length;
    console.log(`Search.js: resultsData.length = ${len}`);
    //console.log(resultData);
    for (let i = 0; i < resultData.length; i++) {
    	
        // Concatenate the html tags with resultData jsonObject

/*    	let rowHTML = "";
//    	rowHTML += "<tr>";
//    	rowHTML += "<th>"+resultData[i]["movie_id"]+"</th>";
//    	rowHTML += "<th>"+resultData[i]["movie_title"]+"</th>";
//    	rowHTML += "<th>"+resultData[i]["movie_year"]+"</th>";
//    	rowHTML += "<th>"+resultData[i]["movie_director"]+"</th>";
//    	rowHTML += "<th>"+resultData[i]["genre_name"]+"</th>";
//    	rowHTML += "<th>"+resultData[i]["star_name"]+"</th>";
//    	rowHTML += "<th>"+resultData[i]["movie_rating"]+"</th>";
//    	rowHTML += "</tr>";
*/
    	
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>"+resultData[i]["movie_id"]+"</th>";
        rowHTML += "<th>" + '<a href = "single-movie.html?id=' + 
        	resultData[i]["movie_id"] + '">' + 
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

        rowHTML += "<th>" + resultData[i]["movie_rating"] + "</th>";
        rowHTML += "<th>" + '<button type= "button" onclick = "location.href=\'shopping-cart.html?movie_id=' + 
        	resultData[i]["movie_id"] + '\'"&action="add"\'">Add to Cart</button></th>';
        rowHTML += "</tr>";

        
        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
    console.log("Search.js: Finished populating table")
    s_handlePrevNextResults();
}

let searchTerm = getParameterByName('search_term'); 
console.log(`Search.js: Sending searchTerm = ${searchTerm} to api/search-results`);
let pageNumStr = getParameterByName("page-num"); // offset
let displayNumStr = getParameterByName("display-num"); // limit

let sortByStr = getParameterByName("sort-by");
let listStr = getParameterByName("list");



let sortFormElement = $("#search-sort-form-div");
let formHTML = "";


document.getElementById("sort-title").addEventListener("click", function(){
	  sortByStr = "title";

	  formHTML += "<input type=\"hidden\" name=\"search_term\" value=\""+ searchTerm +"\">";
	  formHTML += "<input type=\"hidden\" name=\"page-num\" value=\""+ pageNumStr +"\">";
	  formHTML += "<input type=\"hidden\" name=\"display-num\" value=\""+ displayNumStr +"\">";
	  formHTML += "<input type=\"hidden\" name=\"sort-by\" value=\""+ sortByStr +"\">";
	  formHTML += "<input type=\"hidden\" name=\"list\" value=\""+ listStr +"\">";
	  sortFormElement.append(formHTML);
});
document.getElementById("sort-rating").addEventListener("click", function(){
	  sortByStr = "rating";

	  formHTML += "<input type=\"hidden\" name=\"search_term\" value=\""+ searchTerm +"\">";
	  formHTML += "<input type=\"hidden\" name=\"page-num\" value=\""+ pageNumStr +"\">";
	  formHTML += "<input type=\"hidden\" name=\"display-num\" value=\""+ displayNumStr +"\">";
	  formHTML += "<input type=\"hidden\" name=\"sort-by\" value=\""+ sortByStr +"\">";
	  formHTML += "<input type=\"hidden\" name=\"list\" value=\""+ listStr +"\">";
	  sortFormElement.append(formHTML);
});
document.getElementById("sort-asc").addEventListener("click", function(){
	  listStr = "asc";

	  formHTML += "<input type=\"hidden\" name=\"search_term\" value=\""+ searchTerm +"\">";
	  formHTML += "<input type=\"hidden\" name=\"page-num\" value=\""+ pageNumStr +"\">";
	  formHTML += "<input type=\"hidden\" name=\"display-num\" value=\""+ displayNumStr +"\">";
	  formHTML += "<input type=\"hidden\" name=\"sort-by\" value=\""+ sortByStr +"\">";
	  formHTML += "<input type=\"hidden\" name=\"list\" value=\""+ listStr +"\">";
	  sortFormElement.append(formHTML);
});
document.getElementById("sort-desc").addEventListener("click", function(){
	  listStr = "desc";

	  formHTML += "<input type=\"hidden\" name=\"search_term\" value=\""+ searchTerm +"\">";
	  formHTML += "<input type=\"hidden\" name=\"page-num\" value=\""+ pageNumStr +"\">";
	  formHTML += "<input type=\"hidden\" name=\"display-num\" value=\""+ displayNumStr +"\">";
	  formHTML += "<input type=\"hidden\" name=\"sort-by\" value=\""+ sortByStr +"\">";
	  formHTML += "<input type=\"hidden\" name=\"list\" value=\""+ listStr +"\">";
	  sortFormElement.append(formHTML);
});

if ((pageNumStr == null) || (pageNumStr == ''))
{
	pageNumStr = "1";
}
if ((displayNumStr == null) || (displayNumStr == ''))
{
	displayNumStr = "10";
}
	// Makes the HTTP GET request and registers on success callback function handleIndexSearchResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/search-results?search_term=" + searchTerm + "&page-num=" + 
    pageNumStr + "&display-num=" + displayNumStr +
    "&sort-by=" + sortByStr + "&list=" + listStr,
    success: (resultData) => handleSearchResult(resultData)
});