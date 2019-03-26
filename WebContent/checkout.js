/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleCheckoutResult(resultDataString) {
    //resultDataJson = JSON.parse(resultDataString);
	resultDataJson = JSON.parse(JSON.stringify(resultDataString));

    console.log("Checkout.js - handleCheckoutResult: handle checkout response");
    console.log(resultDataJson);
    console.log("Checkout.js: success??: " + resultDataJson["status"] );

    // If login succeeds, it will redirect the user to index.html
    if (resultDataJson["status"] === "success") 
    {
    	console.log("Checkout.js: Successful purchase. Directing to confirmation.html");
        window.location.replace("confirmation.html");
    } 
    else 
    {
        // If login fails, the web page will display 
        // error messages on <div> with id "login_error_message"
        console.log("Checkout.js: showing error message");
        console.log(resultDataJson["message"]);
        $("#checkout_error_message").text(resultDataJson["message"]);
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitCheckoutForm(formSubmitEvent) {
    console.log("Checkout.js - - handleCheckoutForm : submit checkout form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();

    $.post(
        "api/checkout",
        // Serialize the login form to the data sent by POST request
        $("#checkout-form").serialize(),
        (resultDataString) => handleCheckoutResult(resultDataString)
    );
    console.log("Checkout.js -  POST to api/checkout finished");
}

// Bind the submit action of the form to a handler function
console.log("Checkout.js -  Starting");
$("#checkout-form").submit((event) => submitCheckoutForm(event));

