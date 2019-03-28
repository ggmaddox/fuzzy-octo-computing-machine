import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.*;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
//import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@WebServlet(name = "AddStarServlet", urlPatterns = "/api/add-star")
public class AddStarServlet  extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public AddStarServlet() {
		super();
	}
	// Create a dataSource which registered in web.xml
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource; 
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		
		String starName = request.getParameter("star-name");
		String starDob = request.getParameter("star-dob");
		
		if (starName == null) { starName = ""; }
		if (starName.equals("null")) { starName = ""; }
		
		if (starDob == null) { starDob = "0"; }
		if (starDob.equals("null")) { starDob = "0"; }
		if (starDob.isEmpty()) { starDob = "0"; }
		
		// error checking for starDob
		String str = "";
		String dobErrMsg = "";
        for (int i = 0; i < starDob.length(); ++i) {
            char c = starDob.charAt(i);
            
            if (Character.isDigit(c)) {
                str += c;
            }
            else {
                // add error to json object
            	dobErrMsg = "Date of birth contains non-integer characters.";
            }
        }
        int starDobInt = 0;
        if (dobErrMsg.isEmpty()) {
        	starDobInt = Integer.parseInt(str);
        }
        else {
        	starName = "";
        }
		
		System.out.println("starName: " + starName);
		System.out.println("starDob: " + starDob);
		
		Statement statement = null;
		String maxIdQuery = "select max(id) from stars;";	
		String sqlTable = "desc stars;";
	
		try {
			// Get a connection from dataSource
			Connection dbcon = dataSource.getConnection();
			
			System.out.println("Connected to database.");
			
			JsonArray jsonArray = new JsonArray();
			Statement tableStatement = dbcon.createStatement();
			ResultSet rsTable = tableStatement.executeQuery(sqlTable);
			
			// error and success messages
			JsonObject errObject = new JsonObject();
			errObject.addProperty("error-message", "");
			JsonObject successObject = new JsonObject();
			successObject.addProperty("success-message", "");	
			
			while (rsTable.next()) {
				System.out.println("ResultSet for metadata.");
				JsonObject jsonTableData = new JsonObject();
				String columnName = rsTable.getString("Field");
				String columnType = rsTable.getString("Type");
				String columnConstraint = rsTable.getString("NULL");
				
				System.out.println("column name: " + columnName);
				System.out.println("column type: " + columnType);
				System.out.println("column constraint: " + columnConstraint);
				
				jsonTableData.addProperty("column-name", columnName);
				jsonTableData.addProperty("column-type", columnType);
				jsonTableData.addProperty("column-constraint", columnConstraint);
				
				jsonArray.add(jsonTableData);
			}
			
			statement = dbcon.createStatement();
			ResultSet rs = statement.executeQuery(maxIdQuery);
			
			// get largest id and increment for new star entry
			String newId = "";
			while (rs.next()) {
				String id = rs.getString("max(id)");
				id = id.substring(2); // all ids begin with 'nm'
				int i = Integer.parseInt(id);
				++i;
				newId = "nm" + Integer.toString(i);
				
				System.out.println("newId: " + newId);
			}
			
			/*
			 * Depending on whether birthYear is provided, the statements
			 * will be either:
			 * 
			 * INSERT INTO stars (id, name) VALUES('nm2179380','Derek Slaton');
			 * INSERT INTO stars (id, name, birthYear) VALUES('nm2179436','Wan Lee',1984);
			 */
			String insertQuery = "";
			PreparedStatement insertStatement = null;
			if (!starName.isEmpty()) {
				if (starDobInt != 0) { // insert all fields
					
					insertQuery = "INSERT INTO stars (id, name, birthYear) VALUES(?, ?, ?);";
					insertStatement = dbcon.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
					
					insertStatement.setString(1, newId);
					insertStatement.setString(2, starName);
					insertStatement.setInt(3, starDobInt);
				}
				else { // insert id, name 
		
					insertQuery = "INSERT INTO stars (id, name) VALUES(?, ?);";
					insertStatement = dbcon.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
					
					insertStatement.setString(1, newId);
					insertStatement.setString(2, starName);
				}
				int rows_affected = insertStatement.executeUpdate();
				System.out.println("Rows affected by insert: " + rows_affected);
				successObject.addProperty("success-message", "Star successfully inserted into the database.");
				jsonArray.add(successObject);
				jsonArray.add(errObject);
				insertStatement.close();
			}
			else {
				/*
				 * star name is empty and need to put error 
				 * message in json object
				 */
				errObject.addProperty("error-message", "Name field is required. " + dobErrMsg);
				jsonArray.add(successObject);
				jsonArray.add(errObject);
			}
			
			out.write(jsonArray.toString());
			response.setStatus(200);
	        rs.close();
	        dbcon.close();   
	        
		} catch (Exception e) {
			System.out.println(e);
			// set reponse status to 500 (Internal Server Error)
			response.setStatus(500);
		}
	}
}

