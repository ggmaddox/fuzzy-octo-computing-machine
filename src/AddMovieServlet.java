import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
//import java.util.ArrayList;
//import java.util.Date;

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

@WebServlet(name = "AddMovieServlet", urlPatterns = "/api/add-movie")
public class AddMovieServlet  extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public AddMovieServlet() {
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
		
		response.setContentType("application/json");
		
		String title = request.getParameter("movie-title");
		String year = request.getParameter("movie-year");
		String director = request.getParameter("movie-director");
		String star = request.getParameter("star-name");
		String genre = request.getParameter("genre-name");
		
		if (title == null) { title = ""; }
		if (title.equals("null")) { title = ""; }
		
		if (year == null) { year = "0"; }
		if (year.equals("null")) { year = "0"; }
		if (year.isEmpty()) { year = "0"; }
		int yearInt = Integer.parseInt(year);
		
		if (director == null) { director = ""; }
		if (director.equals("null")) { director = ""; }
		
		if (star == null) { star = ""; }
		if (star.equals("null")) { star = ""; }
		
		if (genre == null) { genre = ""; }
		if (genre.equals("null")) { genre = ""; }
		
		System.out.println("title: " + title);
		System.out.println("year: " + year);
		System.out.println("director: " + director);
		System.out.println("star: " + star);
		System.out.println("genre: " + genre);
		
		String sql = "CALL add_movie(?,?,?,?,?);";
		String sqlTable = "desc movies;";
		String sqlStarTable = "desc stars;";
		String sqlGenreTable = "desc genres;";

		try {
			// Get a connection from dataSource
			Connection dbcon = dataSource.getConnection();
			PrintWriter out = response.getWriter();
			System.out.println("Connected to database.");
			
			// json object will contain metadata about movies table
			JsonArray jsonArray = new JsonArray();
			Statement tableStatement = dbcon.createStatement();
			ResultSet rsTable = tableStatement.executeQuery(sqlTable);
			
			// json object will contain metadata about stars table
			Statement starTableStatement = dbcon.createStatement();
			ResultSet rsStarTable = starTableStatement.executeQuery(sqlStarTable);
			
			// json object will contain metadata about genres table
			Statement genreTableStatement = dbcon.createStatement();
			ResultSet rsGenreTable = genreTableStatement.executeQuery(sqlGenreTable);
			
			ResultSet rs = null;
			
			JsonObject errObject = new JsonObject();
			errObject.addProperty("error-message", "");
			
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
			
			while (rsStarTable.next()) {
				System.out.println("ResultSet for metadata.");
				JsonObject jsonTableData = new JsonObject();
				String columnName = rsStarTable.getString("Field");
				String columnType = rsStarTable.getString("Type");
				String columnConstraint = rsStarTable.getString("NULL");
				
				System.out.println("column name: " + columnName);
				System.out.println("column type: " + columnType);
				System.out.println("column constraint: " + columnConstraint);
				
				jsonTableData.addProperty("column-name", columnName);
				jsonTableData.addProperty("column-type", columnType);
				jsonTableData.addProperty("column-constraint", columnConstraint);
				
				jsonArray.add(jsonTableData);
			}
			
			while (rsGenreTable.next()) {
				System.out.println("ResultSet for metadata.");
				JsonObject jsonTableData = new JsonObject();
				String columnName = rsGenreTable.getString("Field");
				String columnType = rsGenreTable.getString("Type");
				String columnConstraint = rsGenreTable.getString("NULL");
				
				System.out.println("column name: " + columnName);
				System.out.println("column type: " + columnType);
				System.out.println("column constraint: " + columnConstraint);
				
				jsonTableData.addProperty("column-name", columnName);
				jsonTableData.addProperty("column-type", columnType);
				jsonTableData.addProperty("column-constraint", columnConstraint);
				
				jsonArray.add(jsonTableData);
			}
			
			if (!title.isEmpty() && !director.isEmpty() &&
					!star.isEmpty() && !genre.isEmpty() && yearInt != 0) {
				
				PreparedStatement statement = dbcon.prepareStatement(sql);
				statement.setString(1, title);
				statement.setInt(2, yearInt);
				statement.setString(3, director);
				statement.setString(4, star);
				statement.setString(5, genre);
				
				boolean results = statement.execute();
				
				while (results) {
					rs = statement.getResultSet();
					
					while (rs.next()) {
						System.out.println("processing result sets of add_movie.");
						String msg = rs.getString(1);
						System.out.println(msg);
						JsonObject statusMsg = new JsonObject();
						statusMsg.addProperty("msg", msg);
						jsonArray.add(statusMsg);
					}
					
					results = statement.getMoreResults();
				}
				jsonArray.add(errObject);
			}
			else {
				/*
				 * print error message:
				 * all fields must be filled out.
				 */
				errObject.addProperty("error-message", "All fields must be completed.");
				jsonArray.add(errObject);					
			}
			out.write(jsonArray.toString());
			response.setStatus(200);
	        if (rs != null) {
				rs.close();
	        }
	        rsTable.close();
	        dbcon.close();   
			
		} catch (Exception e) {
			System.out.println(e);
			// set reponse status to 500 (Internal Server Error)
			response.setStatus(500);
		}	
	}
}

