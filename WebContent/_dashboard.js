// calls api/_dashboard

/**
 * Handle the data returned by IndexServlet
 * @param resultDataString jsonObject, consists of session info
 */
function handleSessionData(resultDataString) {

    //resultDataJson = JSON.parse(resultDataString);
	resultDataJson = resultDataString;

    console.log("_dashboard.js: handle session response");
    console.log(resultDataJson);
    console.log(resultDataJson["sessionID"]);

    // show the session information 
    //$("#sessionID").text("Session ID: " + resultDataJson["sessionID"]);
    //$("#lastAccessTime").text("Last access time: " + resultDataJson["lastAccessTime"]);
}

$.ajax({
    type: "POST",
    url: "api/_dashboard",
    success: (resultDataString) => handleSessionData(resultDataString)
});