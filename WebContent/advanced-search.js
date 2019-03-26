


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

function handlePrevNextResults()
{
	console.log("Advanced-search.js: HandlePrevNextResults");
	
	var newHTML = '';
	if (getParameterByName('search_term') == null)
	{
		var prevNextTableBodyElement = jQuery("#prev-next-forms");
		console.log("Advanced-search.js: using the advacned-search one!");
		// Prev form & button
		var page_num = parseInt(getParameterByName('page-num'));
		if (page_num > 1)
		{
			// You do not want to include a prev button if you're on page 1
			// this runs if page num is (>=2)
			newHTML += '<form method = "get" action = "#">';
			newHTML += '<input type="hidden" id="movie-title" name="movie-title" value="' + getParameterByName('movie-title') + '">';
			newHTML += '<input type="hidden" id="movie-year" name="movie-year" value="' + getParameterByName('movie-year') + '">';
			newHTML += '<input type="hidden" id="movie-director" name="movie-director" value="' + getParameterByName('movie-director') +'">';
			newHTML += '<input type="hidden" id="star-name" name="star-name" value="' + getParameterByName('star-name') +'">';
			// subtract 1 from page num
			newHTML += '<input type="hidden" id="page-num" name="page-num" value="' + (page_num - 1).toString() + '">';
			//newHTML += '<input type="hidden" id="display-num" name="display-num" value="' + getParameterByName('display-num'); + '">';
			//newHTML += "<input type=\"hidden\" name=\"sort-by\" value=\""+ getParameterByName('sort-by') +"\">";
			//newHTML += "<input type=\"hidden\" name=\"list\" value=\""+ getParameterByName('list') +"\">";
			newHTML += '<input type="submit" name="action" value="Prev">';
			newHTML += '</form>';
		}
		
		// NExt form & button
		newHTML += '<form method = "get" action = "#">';
		newHTML += '<input type="hidden" id="movie-title" name="movie-title" value="' + getParameterByName('movie-title') + '">';
		newHTML += '<input type="hidden" id="movie-year" name="movie-year" value="' + getParameterByName('movie-year') + '">';
		newHTML += '<input type="hidden" id="movie-director" name="movie-director" value="' + getParameterByName('movie-director') +'">';
		newHTML += '<input type="hidden" id="star-name" name="star-name" value="' + getParameterByName('star-name') +'">';
		// Add 1 to page_num
		newHTML += '<input type="hidden" id="page-num" name="page-num" value="' + (page_num + 1).toString() + '">';
		//newHTML += '<input type="hidden" id="display-num" name="display-num" value="' + getParameterByName('display-num'); + '">';
		//newHTML += "<input type=\"hidden\" name=\"sort-by\" value=\""+ getParameterByName('sort-by') +"\">";
		//newHTML += "<input type=\"hidden\" name=\"list\" value=\""+ getParameterByName('list') +"\">";
		newHTML += '<input type="submit" name="action" value="Next">';
		newHTML += '</form>'
		console.log("Advanced-search.js: appending html: " + newHTML);
		prevNextTableBodyElement.append(newHTML);
	}
	else
	{
//		var prev_pg = (parseInt(getParameterByName('page-num')) - 1).toString();
////		var next_pg = (parseInt(getParameterByName('page-num')) + 1).toString();
//		var prevNextTableBodyElement = jQuery("#prev-next-forms");
//		
//		console.log("AdvSearch.js: using the advacned-search one?????");
//		var page_num = 1;
//		if (getParameterByName('page-num') != null){
//			page_num = parseInt(getParameterByName('page-num'));
//		}
//		
//		console.log(`Search.js page_num = ${page_num}`);
//		
//		if (page_num > 1)
//		{
//			// You do not want to include a prev button if you're on page 1
//			// this runs if page num is (>=2)
//			newHTML += '<form method = "get" action = "#">';
//			newHTML += '<input type="hidden" id="movie-title" name="search_term" value="' + getParameterByName('search_term') + '">';
//			newHTML += '<input type="hidden" id="page-num" name="page-num" value="' + (page_num - 1).toString() + '">';	
//			newHTML += '<input type="submit" name="action" value="Prev">';
//			newHTML += '</form>';
//		}
//		
//		newHTML += '<form method = "get" action = "#">';
//		newHTML += '<input type="hidden" id="movie-title" name="search_term" value="' + getParameterByName('search_term') + '">';
//		newHTML += '<input type="hidden" id="page-num" name="page-num" value="' + (page_num + 1).toString() + '">';		
//		newHTML += '<input type="submit" name="action" value="Next">';
//		newHTML += '</form>';
//		console.log("AdvSearch.js: appending html: " + newHTML);
//		prevNextTableBodyElement.append(newHTML);
	}
	
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleAdvancedSearchResult(resultData) {
	console.log("Advanced-Search.js: handleAdvancedSearchResult: populating movie table from resultData");
    //console.log("Result: " +resultData);
    // Populate the movie table
    // Find the empty table body by id "movie-table-body"
    let movieTableBodyElement = jQuery("#advanced-search-results-movie-table-body");

    // Iterate through resultData, no more than 20 entries
    let len = resultData.length;
    console.log(`Advanced-search.js: resultsData.length = ${len}`);
    for (let i = 0; i < resultData.length; i++) {

        // Concatenate the html tags with resultData jsonObject
               
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
        //"location.href='http://www.example.com'"
        rowHTML += "<th>" + '<button type= "button" onclick = "location.href=\'shopping-cart.html?movie_id=' + resultData[i]["movie_id"] + '\'"&action="add"\'">Add to Cart</button></th>';
        rowHTML += "</tr>";

        
        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
    handlePrevNextResults();
}


//function handleCartArray(resultDataString) {
//    const resultArray = resultDataString.split(",");
//    console.log(resultArray);
//    
//    // change it to html list
//    let res = "<ul>";
//    for(let i = 0; i < resultArray.length; i++) {
//        // each item will be in a bullet point
//        res += "<li>" + resultArray[i] + "</li>";   
//    }
//    res += "</ul>";
//    
//    // clear the old array and show the new array in the frontend
//    $("#item_list").html("");
//    $("#item_list").append(res);
//}
//
//function handleCartInfo(cartEvent) {
//    console.log("submit cart form");
//    /**
//     * When users click the submit button, the browser will not direct
//     * users to the url defined in HTML form. Instead, it will call this
//     * event handler when the event is triggered.
//     */
//    cartEvent.preventDefault();
//
//    $.post(
//        "api/advanced-search",
//        // Serialize the cart form to the data sent by POST request
//        $("#add_cart").serialize(),
//        (resultDataString) => handleCartArray(resultDataString)
//    );
//}

let movieTitle = getParameterByName('movie-title');
let movieYear = getParameterByName('movie-year');
let movieDirector  = getParameterByName("movie-director");
let starName = getParameterByName("star-name");
let pageNum = getParameterByName("page-num"); // offset
let displayNum = getParameterByName("display-num"); // limit


/*
 * sorting button additions
 */
let sortByStr = getParameterByName("sort-by");
let listStr = getParameterByName("list");


let sortFormElement = $("#adv-sort-form-div");
let formHTML = "";


document.getElementById("sort-title").addEventListener("click", function(){
	  sortByStr = "title";

	  formHTML += "<input type=\"hidden\" name=\"movie-title\" value=\""+ movieTitle +"\">";
	  formHTML += "<input type=\"hidden\" name=\"movie-year\" value=\""+ movieYear +"\">";
	  formHTML += "<input type=\"hidden\" name=\"movie-director\" value=\""+ movieDirector +"\">";
	  formHTML += "<input type=\"hidden\" name=\"star-name\" value=\""+ starName +"\">";
	  formHTML += "<input type=\"hidden\" name=\"page-num\" value=\""+ pageNum +"\">";
	  formHTML += "<input type=\"hidden\" name=\"display-num\" value=\""+ displayNum +"\">";
	  formHTML += "<input type=\"hidden\" name=\"sort-by\" value=\""+ sortByStr +"\">";
	  formHTML += "<input type=\"hidden\" name=\"list\" value=\""+ listStr +"\">";
	  sortFormElement.append(formHTML);
});
document.getElementById("sort-rating").addEventListener("click", function(){
	  sortByStr = "rating";

	  formHTML += "<input type=\"hidden\" name=\"movie-title\" value=\""+ movieTitle +"\">";
	  formHTML += "<input type=\"hidden\" name=\"movie-year\" value=\""+ movieYear +"\">";
	  formHTML += "<input type=\"hidden\" name=\"movie-director\" value=\""+ movieDirector +"\">";
	  formHTML += "<input type=\"hidden\" name=\"star-name\" value=\""+ starName +"\">";
	  formHTML += "<input type=\"hidden\" name=\"page-num\" value=\""+ pageNum +"\">";
	  formHTML += "<input type=\"hidden\" name=\"display-num\" value=\""+ displayNum +"\">";
	  formHTML += "<input type=\"hidden\" name=\"sort-by\" value=\""+ sortByStr +"\">";
	  formHTML += "<input type=\"hidden\" name=\"list\" value=\""+ listStr +"\">";
	  sortFormElement.append(formHTML);
});
document.getElementById("sort-asc").addEventListener("click", function(){
	  listStr = "asc";

	  formHTML += "<input type=\"hidden\" name=\"movie-title\" value=\""+ movieTitle +"\">";
	  formHTML += "<input type=\"hidden\" name=\"movie-year\" value=\""+ movieYear +"\">";
	  formHTML += "<input type=\"hidden\" name=\"movie-director\" value=\""+ movieDirector +"\">";
	  formHTML += "<input type=\"hidden\" name=\"star-name\" value=\""+ starName +"\">";
	  formHTML += "<input type=\"hidden\" name=\"page-num\" value=\""+ pageNum +"\">";
	  formHTML += "<input type=\"hidden\" name=\"display-num\" value=\""+ displayNum +"\">";
	  formHTML += "<input type=\"hidden\" name=\"sort-by\" value=\""+ sortByStr +"\">";
	  formHTML += "<input type=\"hidden\" name=\"list\" value=\""+ listStr +"\">";
	  sortFormElement.append(formHTML);
});
document.getElementById("sort-desc").addEventListener("click", function(){
	  listStr = "desc";

	  formHTML += "<input type=\"hidden\" name=\"movie-title\" value=\""+ movieTitle +"\">";
	  formHTML += "<input type=\"hidden\" name=\"movie-year\" value=\""+ movieYear +"\">";
	  formHTML += "<input type=\"hidden\" name=\"movie-director\" value=\""+ movieDirector +"\">";
	  formHTML += "<input type=\"hidden\" name=\"star-name\" value=\""+ starName +"\">";
	  formHTML += "<input type=\"hidden\" name=\"page-num\" value=\""+ pageNum +"\">";
	  formHTML += "<input type=\"hidden\" name=\"display-num\" value=\""+ displayNum +"\">";
	  formHTML += "<input type=\"hidden\" name=\"sort-by\" value=\""+ sortByStr +"\">";
	  formHTML += "<input type=\"hidden\" name=\"list\" value=\""+ listStr +"\">";
	  sortFormElement.append(formHTML);
});


console.log(`Advanced-search.js: movie Title = ${movieTitle}, movie Year = ${movieYear}, movie Director = ${movieDirector}, star Name = ${starName}, page num =  ${pageNum}, display num = ${displayNum}`);
// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/advanced-search?movie-title=" + movieTitle+"&movie-year="+
    	movieYear+"&movie-director="+movieDirector+"&star-name="+starName + 
    	"&page-num=" + pageNum + "&display-num=" + displayNum +
    	"&sort-by=" + sortByStr + "&list=" + listStr,
    success: (resultData) => handleAdvancedSearchResult(resultData)
});
console.log("It's printing once?????");



