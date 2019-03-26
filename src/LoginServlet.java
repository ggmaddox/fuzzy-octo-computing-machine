import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.jasypt.util.password.StrongPasswordEncryptor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.jasypt.util.password.StrongPasswordEncryptor;

/**
 * This class is declared as LoginServlet in web annotation, 
 * which is mapped to the URL pattern /api/login
 */
@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

	// Create a dataSource which registered in web.xml
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;
		
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
    		throws IOException {
    	response.setContentType("application/json");
    	// get username and password from the login form 
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String encryptedPassword = "";
        String rsUsername = "";

        // New to Proj. 4: Vertification of either web or android device
        String userAgent = request.getHeader("User-Agent");
        System.out.println("recieved login request");
        System.out.println("userAgent: " + userAgent);
        
        
        
        // If the client is not coming from an Android device 
        if (userAgent != null && !userAgent.contains("Android"))
        {
        	String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
            System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);
            
            try {
				RecaptchaVerifyUtils.verify(gRecaptchaResponse);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("recaptcha success");
                JsonObject responseJsonObject = new JsonObject();
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", e.getMessage());
                response.getWriter().write(responseJsonObject.toString());
                return;
			}
        	System.out.println("LoginServlet: reCaptcha valid!");
        }
        
        
        // Rest runs whether the user is coming from an Android or not
        boolean success = false;
    	
        PrintWriter sout = response.getWriter();

        try {
        	
        	Context initCtx = new InitialContext();

            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            if (envCtx == null)
                System.out.println("envCtx is NULL");

            // Look up our data source
            DataSource ds = (DataSource) envCtx.lookup("jdbc/TestDB");

            if (ds == null)
                System.out.println("ds is null.");

            Connection dbcon = ds.getConnection();
            if (dbcon == null)
                System.out.println("dbcon is null.");
        	//Connection dbcon = dataSource.getConnection();
        	
        	// check username and password against the database
        	String query = "select email, `password` from customers \r\n" + 
          		"where ? = email;";
          

        	PreparedStatement statement = dbcon.prepareStatement(query);
        	statement.setString(1, username);
        	
        	System.out.println("username: " + username);
        	System.out.println("password: " + password);
          
        	ResultSet rs = statement.executeQuery();


        	if (rs.next()) {
        		rsUsername = rs.getString("email");
        		encryptedPassword = rs.getString("password");
        		success = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
        		
        		System.out.println("username in db: " + rsUsername);
        		System.out.println("password in db: " + encryptedPassword);
        		System.out.println("success: " + success);
        	}
        	
	          response.setStatus(200);
	          dbcon.close();
	          rs.close();
	        	
	        } catch(Exception e) {
				// write error message JSON object to output
	        	JsonObject jsonObject = new JsonObject();
	        	jsonObject.addProperty("error message", e.getMessage());
	        	System.out.println("Caught an exception: " + e.getMessage());
	        	Writer writer = new StringWriter();
	        	e.printStackTrace(new PrintWriter(writer));
	        	String s = writer.toString();
	        	System.out.println("Stack: " + s);
	
	        	sout.write(jsonObject.toString());
	        	
	        	response.setStatus(500);
	        }

            if (success) {
                // Login succeeds
                // Set this user into current session
                String sessionId = ((HttpServletRequest) request).getSession().getId();
                Long lastAccessTime = ((HttpServletRequest) request).getSession().getLastAccessedTime();
                request.getSession().setAttribute("user", new User(username));
                
                System.out.println(request.getAttribute("user"));

                JsonObject responseJsonObject = new JsonObject();
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");
                
                System.out.println("LoginServlet: Json is!: " + responseJsonObject.toString());
                response.getWriter().write(responseJsonObject.toString());
            } else {
                // Login fails
                JsonObject responseJsonObject = new JsonObject();
                responseJsonObject.addProperty("status", "fail");
                if (!username.equals(rsUsername)) {
                    responseJsonObject.addProperty("message", "user " + username + " doesn't exist");
                } else {
                    responseJsonObject.addProperty("message", "incorrect password");
                }
                System.out.println("LoginServlet: Json is: " + responseJsonObject.toString());
                response.getWriter().write(responseJsonObject.toString());
            }

    }
}