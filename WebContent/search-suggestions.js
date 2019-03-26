/*
 * CS 122B Project 4. Autocomplete Example.
 * 
 * This Javascript code uses this library: https://github.com/devbridge/jQuery-Autocomplete
 * 
 * This example implements the basic features of the autocomplete search, features that are 
 *   not implemented are mostly marked as "TODO" in the codebase as a suggestion of how to implement them.
 * 
 * To read this code, start from the line "$('#autocomplete').autocomplete" and follow the callback functions.
 * 
 */

/*
 * store json data with the search term as the key, so 
 * we don't have to query the server for results we already have
 */
let map = new Map();
let globalJsonData = [];
let globalQuery = [];

/*
 * This function is called by the library when it needs to lookup a query.
 * 
 * The parameter query is the query string.
 * The doneCallback is a callback function provided by the library, after you get the
 *   suggestion list from AJAX, you need to call this function to let the library know.
 */
function handleLookup(query, doneCallback) {
	console.log("autocomplete initiated")
	
	// TODO: if you want to check past query results first, you can do it here
	var cached = false;
	for (var key of map.keys()) {
		  console.log("key in map:" + key);
		  if (key == query) {
			  cached = true;
			  console.log("using cached results");
			  break;
		  }
	}
	if (cached) {
		// get the json data (suggestions) from map, not from server
		handleLookupAjaxSuccess(map.get(query), query, doneCallback);
	}
	else {
		console.log("query not cached, sending an ajax request to the server.");
		
		// ajax get method
		// sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
		// with the query data
		jQuery.ajax({
			"method": "GET",
			// generate the request url from the query.
			// escape the query string to avoid errors caused by special characters 
			
			//index?query
			"url": "index?query=" + escape(query),
			"success": function(data) {
				// pass the data, query, and doneCallback function into the success handler
				handleLookupAjaxSuccess(data, query, doneCallback) 
			},
			"error": function(errorData) {
				console.log("lookup ajax error")
				console.log(errorData)
			}
		})
	}
	
	
}


/*
 * This function is used to handle the ajax success callback function.
 * It is called by our own code upon the success of the AJAX request
 * 
 * data is the JSON data string you get from your Java Servlet
 * 
 */
function handleLookupAjaxSuccess(data, query, doneCallback) {
	var jsonDataFull = [];
	
	if (isParsable(data)) {
		jsonDataFull = JSON.parse(data);
	}
	else {
		jsonDataFull = data;
	}
	
	console.log("suggested list (json data):");
	console.log(jsonDataFull);
	
	var jsonData = jsonDataFull.slice(0, 10);
	
	// TODO: if you want to cache the result into a global variable you can do it here
	map.set(query, jsonDataFull);
	globalQuery.push(query);

	// call the callback function provided by the autocomplete library
	// add "{suggestions: jsonData}" to satisfy the library response format according to
	//   the "Response Format" section in documentation
	doneCallback( { suggestions: jsonData } );
}

/*
 * function to tell if we can parse a json object
 */
function isParsable(data) {
	var result = true;
	try {
		JSON.parse(data);
	} catch(exception) {
		result = false;
	}
	return result;
}

/*
 * This function is the select suggestion handler function. 
 * When a suggestion is selected, this function is called by the library.
 * 
 * You can redirect to the page you want using the suggestion data.
 */
function handleSelectSuggestion(suggestion) {
	// TODO: jump to the specific result page based on the selected suggestion
	var pageURL = $(location).attr("href");

	//https://localhost:8443/fabflix/index.html
	var basePage = pageURL.substring(0, pageURL.lastIndexOf("/"));
	
	window.location.replace(basePage + "/single-movie.html?id=" + suggestion["data"]["movie_id"]);
	
	/*
	// Populate the movie table
    // Find the empty table body by id "movie-table-body
    var resultsElement = jQuery("#results");
    // suggestion contenet(only one movie selected)
	var rowHTML = "";
    // Concatenate the html tags with resultData jsonObject
	rowHTML += "<tr>";
	rowHTML += "<th>"+suggestion["data"]["movie_id"]+"</th>";
	rowHTML += "<th>"+suggestion["data"]["movie_title"]+"</th>";
	rowHTML += "<th>"+suggestion["data"]["movie_year"]+"</th>";
	rowHTML += "<th>"+suggestion["data"]["movie_director"]+"</th>";
	rowHTML += "<th>";
	for (let i = 0; i < suggestion["data"]["genres"].length-1; ++i) {	
		rowHTML += suggestion["data"]["genres"][i]["genre_name"]+", " ;
	}
	rowHTML += suggestion["data"]["genres"][suggestion["data"]["genres"].length-1]["genre_name"]+"</th>";
	rowHTML += "<th>"
	for (let i = 0; i < suggestion["data"]["stars"].length-1; ++i) {
		
		rowHTML += suggestion["data"]["stars"][i]["star_name"]+", " ;
	}
	rowHTML += suggestion["data"]["stars"][suggestion["data"]["stars"].length-1]["star_name"]+"</th>";
	//rowHTML += "<th>"+suggestion["data"]["star_name"]+"</th>";
	rowHTML += "<th>"+suggestion["data"]["movie_rating"]+"</th>";
	rowHTML += "</tr>";
	console.log("you select " + suggestion["value"] + " with ID " + suggestion["data"]["movie_id"])
    resultsElement.append(rowHTML);
	*/

}


/*
 * This statement binds the autocomplete library with the input box element and 
 *   sets necessary parameters of the library.
 * 
 * The library documentation can be find here: 
 *   https://github.com/devbridge/jQuery-Autocomplete
 *   https://www.devbridge.com/sourcery/components/jquery-autocomplete/
 * 
 */
// $('#autocomplete') is to find element by the ID "autocomplete"
$('#autocomplete').autocomplete({
	// documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
    		handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
    		handleSelectSuggestion(suggestion)
    },
    //source: function(request, response)
    // set delay time
    deferRequestBy: 300,
    // there are some other parameters that you might want to use to satisfy all the requirements
    // TODO: add other parameters, such as minimum characters
    minChars: 3, // don't query the server until >= 3 characters are typed
    lookupLimit: 10 // number of results to suggest
});


/*
 * do normal full text search if no suggestion is selected 
 */
function handleNormalSearch(query) {
	console.log("doing normal search with query: " + query);
	
	// TODO: you should do normal search here
	var pageURL = $(location).attr("href");

	//https://localhost:8443/fabflix/index.html
	var basePage = pageURL.substring(0, pageURL.lastIndexOf("/"));

	window.location.replace(basePage + "/search-results.html?search_term=" + escape(query));
	
	/*
	var jsonData;
	$.get( "index?query=" + escape(query), function( data ) {
		  alert( "Data Loaded: " + data );
		  // now we have the json object in the same format of suggestion
		  // parse it 
		  jsonData = JSON.parse(data);
		  console.log(jsonData);
		  var resultsElement = jQuery("#results");
		    // suggestion contenet(only one movie selected)
			var rowHTML = "";
			for (let k = 0; k < jsonData.length; ++k) {							
			    // Concatenate the html tags with resultData jsonObject
				rowHTML += "<tr>";
				rowHTML += "<th>"+jsonData[k]["data"]["movie_id"]+"</th>";
				rowHTML += "<th>"+jsonData[k]["data"]["movie_title"]+"</th>";
				rowHTML += "<th>"+jsonData[k]["data"]["movie_year"]+"</th>";
				rowHTML += "<th>"+jsonData[k]["data"]["movie_director"]+"</th>";				
				rowHTML += "<th>";
				for (let i = 0; i < jsonData[k]["data"]["genres"].length-1; ++i) {					
					rowHTML += jsonData[k]["data"]["genres"][i]["genre_name"]+", " ;
				}
				rowHTML += jsonData[k]["data"]["genres"][jsonData[k]["data"]["genres"].length-1]["genre_name"]+"</th>";								
				rowHTML += "<th>"
				for (let i = 0; i < jsonData[k]["data"]["stars"].length-1; ++i) {
					
					rowHTML += jsonData[k]["data"]["stars"][i]["star_name"]+", " ;
				}
				rowHTML += jsonData[k]["data"]["stars"][jsonData[k]["data"]["stars"].length-1]["star_name"]+"</th>";				
				//rowHTML += "<th>"+jsonData[k]["data"]["star_name"]+"</th>";
				rowHTML += "<th>"+jsonData[k]["data"]["movie_rating"]+"</th>";
				rowHTML += "</tr>";				
				console.log("you select " + jsonData[k]["value"] + " with ID " + jsonData[k]["data"]["movie_id"]);
				resultsElement.append(rowHTML);
			}		  
		});
	
//		"error": function(errorData) {
//			console.log("lookup ajax error")
//			console.log(errorData)
//		}
//	})
	*/
}

// bind pressing enter key to a handler function
$('#autocomplete').keypress(function(event) {
	// keyCode 13 is the enter key
	if (event.keyCode == 13) {
		// pass the value of the input box to the handler function
		handleNormalSearch($('#autocomplete').val())
	}
})

// TODO: if you have a "search" button, you may want to bind the onClick event as well of that button
