import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.jasypt.util.password.StrongPasswordEncryptor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.jasypt.util.password.StrongPasswordEncryptor;

/**
 * This class is declared as DashboardServlet in web annotation, 
 * which is mapped to the URL pattern /api/login
 */
@WebServlet(name = "EmployeeLoginServlet", urlPatterns = "/api/employee-login")
public class EmployeeLoginServlet extends HttpServlet {
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
        	String query = "select email, `password` from employees \r\n" + 
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
	        	
	        } catch(Exception ex) {
				// write error message JSON object to output
	        	JsonObject jsonObject = new JsonObject();
	        	jsonObject.addProperty("error message", ex.getMessage());
	        	System.out.println("Caught an exception: " + ex.getMessage());
	        	Writer writer = new StringWriter();
	        	ex.printStackTrace(new PrintWriter(writer));
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
                request.getSession().setAttribute("employee", new User(username));

                JsonObject responseJsonObject = new JsonObject();
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");
                
                System.out.println("EmployeeLoginServlet: Json is!: " + responseJsonObject.toString());
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
                System.out.println("EmployeeLoginServlet: Json is: " + responseJsonObject.toString());
                response.getWriter().write(responseJsonObject.toString());
            }
    }
}