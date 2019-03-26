// calls api/browse-results

function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
   // console.log("url in getParameterByName(): " + url);
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


function b_handlePrevNextResults()
{
	//title=&genre=3&page-num=1&display-num=10
	//getParameterByName("sort-by");
	//let listString = getParameterByName("list");
	
	console.log("Browse.js: HandlePrevNextResults");
	
	var newHTML = '';

	var prevNextTableBodyElement = jQuery("#prev-next-forms");
	console.log("Browse.js: using the advacned-search one!");
	// Prev form & button
	var page_num = parseInt(getParameterByName('page-num'));
	if (page_num > 1)
	{
		// You do not want to include a prev button if you're on page 1
		// this runs if page num is (>=2)
		newHTML += '<form method = "get" action = "#">';
		newHTML += '<input type="hidden" id="movie-title" name="title" value="' + getParameterByName('title') + '">';
		newHTML += '<input type="hidden" id="movie-year" name="genre" value="' + getParameterByName('genre') + '">';

		// subtract 1 from page num
		newHTML += '<input type="hidden" id="page-num" name="page-num" value="' + (page_num - 1).toString() + '">';
		newHTML += '<input type="hidden" id="movie-title" name="sort-by" value="' + getParameterByName('sort-by') + '">';
		newHTML += '<input type="hidden" id="movie-year" name="list" value="' + getParameterByName('list') + '">';
		newHTML += '<input type="submit" name="action" value="Prev">';
		newHTML += '</form>';
	}
	
	// NExt form & button
	newHTML += '<form method = "get" action = "#">';
	newHTML += '<input type="hidden" id="movie-title" name="title" value="' + getParameterByName('title') + '">';
	newHTML += '<input type="hidden" id="movie-year" name="genre" value="' + getParameterByName('genre') + '">';

	// Add 1 to page_num
	newHTML += '<input type="hidden" id="page-num" name="page-num" value="' + (page_num + 1).toString() + '">';
	newHTML += '<input type="submit" name="action" value="Next">';
	newHTML += '</form>'
	console.log("Browse-results.js: appending html: " + newHTML);
	prevNextTableBodyElement.append(newHTML);
	
}

/*
 * Handle the click of the next button
 */
/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
//function submitNextForm(formSubmitEvent) {
//    console.log("submit next form");
//    /**
//     * When users click the submit button, the browser will not direct
//     * users to the url defined in HTML form. Instead, it will call this
//     * event handler when the event is triggered.
//     */
//    //formSubmitEvent.preventDefault();
//
//    $.post(
//        "api/browse-results",
//        // Serialize the login form to the data sent by POST request
//        $("#next-button").serialize(),
//        (resultDataString) => handleResult(resultDataString)
//    );
//}

// Bind the submit action of the form to a handler function
//$("#next-button").submit((event) => submitNextForm(event));


//let pageNum = sessionStorage.getItem("page-num");
//console.log("page-num = " + pageNum);


/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) { 
    console.log("Browse-result.js: handleResult: populating browse results info from resultData");

    let tableBodyElement = jQuery("#browse-results-movie-table-body");

    let len = resultData.length;
    console.log(`Broswe-results.js: resultsData.length = ${len}`);
    // Iterate through resultData, no more than 20 entries
    for (let i = 0; i < resultData.length; i++) {
    	
        // Concatenate the html tags with resultData jsonObject

//    	let rowHTML = "";
//    	rowHTML += "<tr>";
//    	rowHTML += "<th>"+resultData[i]["movie_id"]+"</th>";
//    	rowHTML += "<th>"+resultData[i]["movie_title"]+"</th>";
//    	rowHTML += "<th>"+resultData[i]["movie_year"]+"</th>";
//    	rowHTML += "<th>"+resultData[i]["movie_director"]+"</th>";
//    	rowHTML += "<th>"+resultData[i]["genre_name"]+"</th>";
//    	rowHTML += "<th>"+resultData[i]["star_name"]+"</th>";
//    	rowHTML += "<th>"+resultData[i]["movie_rating"]+"</th>";
//    	rowHTML += "</tr>";
    	
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
        rowHTML += "<th>" + '<button type= "button" onclick = "location.href=\'shopping-cart.html?movie_id=' + resultData[i]["movie_id"] + '\'"&action="add"\'">Add to Cart</button></th>';
        rowHTML += "</tr>";

        
        // Append the row created to the table body, which will refresh the page
        tableBodyElement.append(rowHTML);
    }
    console.log("Browse-results.js: populated table");
    b_handlePrevNextResults();
}


console.log(window.location.href);

console.log("In Browse-result.js");
let titleString = getParameterByName("title");
let genreString = getParameterByName("genre");
let pageNumStr = getParameterByName("page-num"); // offset
let displayNumStr = getParameterByName("display-num"); // limit
let sortByStr = getParameterByName("sort-by");
let listStr = getParameterByName("list");


console.log("title: " + titleString);
console.log("genre: " + genreString);
console.log("page-num: " + pageNumStr);
console.log("display-num: " + displayNumStr);
console.log("sort-by: " + sortByStr);
console.log("list: " + listStr);

//var sortBy = "";
//console.log("calling document.getElementById() for sort-title");
//console.log(document.getElementById("sort-title"));
let sortFormElement = $("#sort-form-div");
let formHTML = "";


document.getElementById("sort-title").addEventListener("click", function(){
	  sortByStr = "title";

	  formHTML += "<input type=\"hidden\" name=\"title\" value=\""+ titleString +"\">";
	  formHTML += "<input type=\"hidden\" name=\"genre\" value=\""+ genreString +"\">";
	  formHTML += "<input type=\"hidden\" name=\"page-num\" value=\""+ pageNumStr +"\">";
	  formHTML += "<input type=\"hidden\" name=\"display-num\" value=\""+ displayNumStr +"\">";
	  formHTML += "<input type=\"hidden\" name=\"sort-by\" value=\""+ sortByStr +"\">";
	  formHTML += "<input type=\"hidden\" name=\"list\" value=\""+ listStr +"\">";
	  sortFormElement.append(formHTML);
});
document.getElementById("sort-rating").addEventListener("click", function(){
	  sortByStr = "rating";

	  formHTML += "<input type=\"hidden\" name=\"title\" value=\""+ titleString +"\">";
	  formHTML += "<input type=\"hidden\" name=\"genre\" value=\""+ genreString +"\">";
	  formHTML += "<input type=\"hidden\" name=\"page-num\" value=\""+ pageNumStr +"\">";
	  formHTML += "<input type=\"hidden\" name=\"display-num\" value=\""+ displayNumStr +"\">";
	  formHTML += "<input type=\"hidden\" name=\"sort-by\" value=\""+ sortByStr +"\">";
	  formHTML += "<input type=\"hidden\" name=\"list\" value=\""+ listStr +"\">";
	  sortFormElement.append(formHTML);
});
document.getElementById("sort-asc").addEventListener("click", function(){
	  listStr = "asc";

	  formHTML += "<input type=\"hidden\" name=\"title\" value=\""+ titleString +"\">";
	  formHTML += "<input type=\"hidden\" name=\"genre\" value=\""+ genreString +"\">";
	  formHTML += "<input type=\"hidden\" name=\"page-num\" value=\""+ pageNumStr +"\">";
	  formHTML += "<input type=\"hidden\" name=\"display-num\" value=\""+ displayNumStr +"\">";
	  formHTML += "<input type=\"hidden\" name=\"sort-by\" value=\""+ sortByStr +"\">";
	  formHTML += "<input type=\"hidden\" name=\"list\" value=\""+ listStr +"\">";
	  sortFormElement.append(formHTML);
});
document.getElementById("sort-desc").addEventListener("click", function(){
	  listStr = "desc";

	  formHTML += "<input type=\"hidden\" name=\"title\" value=\""+ titleString +"\">";
	  formHTML += "<input type=\"hidden\" name=\"genre\" value=\""+ genreString +"\">";
	  formHTML += "<input type=\"hidden\" name=\"page-num\" value=\""+ pageNumStr +"\">";
	  formHTML += "<input type=\"hidden\" name=\"display-num\" value=\""+ displayNumStr +"\">";
	  formHTML += "<input type=\"hidden\" name=\"sort-by\" value=\""+ sortByStr +"\">";
	  formHTML += "<input type=\"hidden\" name=\"list\" value=\""+ listStr +"\">";
	  sortFormElement.append(formHTML);
});


console.log(`Browse-results.js: Parameters title String = ${titleString}, genre String = ${genreString}, pageNumStr = ${pageNumStr}, displayNumStr = ${displayNumStr}`);
//Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    
    // Setting request url, which is mapped by SingleMovieServlet.java
    url: "api/browse-results?title=" + titleString + "&genre="+genreString +
    	"&page-num=" + pageNumStr + "&display-num=" + displayNumStr + 
    	"&sort-by=" + sortByStr +"&list=" + listStr, 
    
    // Setting callback function to handle data returned successfully by the SingleMovieServlet
    success: (resultData) => handleResult(resultData) 
});