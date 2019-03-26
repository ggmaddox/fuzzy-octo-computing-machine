// calls api/index

/**
 * Handle the data returned by IndexServlet
 * @param resultDataString jsonObject, consists of session info
 */
function handleSessionData(resultDataString) {

    //resultDataJson = JSON.parse(resultDataString);
	resultDataJson = resultDataString;

    console.log("Index.js: handle session response");
    console.log(resultDataJson);
    console.log(resultDataJson["sessionID"]);

    // show the session information 
    $("#sessionID").text("Session ID: " + resultDataJson["sessionID"]);
    $("#lastAccessTime").text("Last access time: " + resultDataJson["lastAccessTime"]);
}

/**
 * Handle the items in item list 
 * @param resultDataString jsonObject, needs to be parsed to html 
 */
function handleCartArray(resultDataString) {
	// PLAN TO DELETE
    const resultArray = resultDataString.split(",");
    console.log(resultArray);
    console.log("Index.js: in handleCartArray");
    
    // change it to html list
    let res = "<ul>";
    for(let i = 0; i < resultArray.length; i++) {
        // each item will be in a bullet point
        res += "<li>" + resultArray[i] + "</li>";   
    }
    res += "</ul>";
    
    // clear the old array and show the new array in the frontend
    $("#item_list").html("");
    $("#item_list").append(res);
}

/**
 * Submit form content with POST method
 * @param cartEvent
 */
function handleCartInfo(cartEvent) {
	// PLAN TO DELETE
    console.log("Index.js: submit cart form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    cartEvent.preventDefault();

    $.get(
        "api/index",
        // Serialize the cart form to the data sent by POST request
        $("#cart").serialize(),
        (resultDataString) => handleCartArray(resultDataString)
    );
}

$.ajax({
    type: "POST",
    url: "api/index",
    success: (resultDataString) => handleSessionData(resultDataString)
});

// Bind the submit action of the form to a event handler function
$("#cart").submit((event) => handleCartInfo(event));