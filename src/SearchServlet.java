import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;



@WebServlet(name = "SearchServlet", urlPatterns = "/api/search-results")
public class SearchServlet  extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public SearchServlet() {
		super();
	}
////	// Create a dataSource which registered in web.xml
//		@Resource(name = "jdbc/moviedb")
//		private DataSource dataSource; 
//////		
////		/**
////		 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
////		 *      response)
////		 */
		protected void doGet(HttpServletRequest request, HttpServletResponse response)
				throws ServletException, IOException {
			
//			// Time an event in a program to nanosecond precision
			long startTimeTS = System.nanoTime();
			
////			/********************************************************
////			 * writing TS, TJ to file
////			 *******************************************************/
			String contextPath = getServletContext().getRealPath("/");
			String filePath = contextPath+"search-log";
			System.out.println(filePath);
			
			File file = null;
			try {
				file = new File(filePath);
			
				// returns boolean
				// true if file created, false if file already exists
				file.createNewFile(); 
				
			} catch (Exception e) {
				e.printStackTrace();
			} 

			response.setContentType("application/json"); // Response mime type

			String userAgent = request.getHeader("User-Agent");
			String searchTermRaw;
			String pageNum;
			String displayNum;
			String sortBy;
			String listOrder;
			if (userAgent != null && !userAgent.contains("Android"))
			{
				
////			// get the parameters in the url
				searchTermRaw = request.getParameter("search_term");
				pageNum = request.getParameter("page-num");
				displayNum = request.getParameter("display-num");
				sortBy = request.getParameter("sort-by");
				listOrder = request.getParameter("list");
			}
			else
			{
				searchTermRaw = request.getHeader("search_term");
				pageNum = request.getHeader("page-num");
				displayNum = request.getHeader("display-num");
				sortBy = request.getHeader("sort-by");
				listOrder = request.getHeader("list");
			}
			if (displayNum == null || displayNum.isEmpty()) {
				displayNum = "10";
			}
			if (pageNum == null || pageNum.isEmpty()) {
				pageNum = "1";
			}
			System.out.println("SearchTerm: " + searchTermRaw);
			System.out.println("PageNum: " + pageNum);
			System.out.println("DisplayNum: " + displayNum);
////			// default query, no ORDER BY, no ASC/DESC
			String base_query = "select \r\n" + 
					"movies.id, title, movies.`year`, \r\n" + 
					"director, rating\r\n" + 
					"from \r\n" + 
					"movies\r\n" + 
					"left join ratings \r\n" + 
					"	on ratings.movieId = movies.id \r\n" + 
					"where \r\n" ;
			String middle_query = 
					"union\r\n" +
					"select \r\n" + 
					"movies.id, title, movies.`year`, \r\n" + 
					"director, rating\r\n" + 
					"from \r\n" + 
					"movies\r\n" + 
					"right join ratings \r\n" + 
					"	on ratings.movieId = movies.id \r\n" + 
					"where \r\n" 
					;
			String order_query =
					"limit ?\r\n" + 
					"offset ?;";
////			/*
////			 * create order by statement to insert into sql query.
////			 */
			String orderByClause = "";	
			if (sortBy != null) {
				if (!sortBy.isEmpty() && sortBy.equals("title")) {
					orderByClause = "title";
				} 
				else if (!sortBy.isEmpty() && sortBy.equals("rating")) {
					orderByClause = "rating";
				}
			}
			
////			// query with ORDER BY but no ASC/DESC
			if (!orderByClause.isEmpty()) {
				if (orderByClause.equals("rating")) {
					order_query = 
							"order by rating\r\n" +
							"limit ?\r\n" + 
							"offset ?;";
				}
				else {
					order_query = 
							
							"order by title\r\n" +
							"limit ?\r\n" + 
							"offset ?;";
				}
			}
			
			String listClause = "";
			if (listOrder != null) {
				if (!listOrder.isEmpty() && listOrder.equals("asc") && 
						!orderByClause.isEmpty()) {
					listClause = "asc";
					if (orderByClause.equals("rating")) {
						order_query = 
								
								"order by rating asc\r\n" +
								"limit ?\r\n" + 
								"offset ?;";
					}
					else {
						order_query = 
								
								"order by title asc\r\n" +
								"limit ?\r\n" + 
								"offset ?;";
					}
				}
				else if (!listOrder.isEmpty() && listOrder.equals("desc") && 
						!orderByClause.isEmpty()) {
					listClause = "desc";
					if (orderByClause.equals("rating")) {
						order_query = 
								
								"order by rating desc\r\n" +
								"limit ?\r\n" + 
								"offset ?;";
					}
					else {
						order_query = 
								
								"order by title desc\r\n" +
								"limit ?\r\n" + 
								"offset ?;";
					}
				}
			}
						
			System.out.printf("SearchServlet: offset (pagenum) = %s, limit (displayNum) = %s %n", pageNum, displayNum);
			
////			// Determine whether you're going to the next or prev page.
			int int_pg_num = Integer.parseInt(pageNum);
		
			
////			// none of the if statements above will execute if you're just loading the current pg
			System.out.println("SearchServlet: int_pg_num =  " + Integer.toString(int_pg_num));
			int int_offset = Integer.parseInt(displayNum) * (int_pg_num - 1);
			System.out.println("SearchServlet: int_offset = " + Integer.toString(int_offset));
			
			if (searchTermRaw != null) {
////				//String searchTerm = "%" + searchTermRaw + "%";
////				
////				//System.out.println("SearchServlet search term: " + searchTerm);
////				
////				// Output stream to STDOUT
				PrintWriter sout = response.getWriter();
				try {

					// Time an event in a program to nanosecond precision
					long startTimeTJ = System.nanoTime();
////					
////		            // the following few lines are for connection pooling
////		            // Obtain our environment naming context

					
		            Context initCtx = new InitialContext();

		            Context envCtx = (Context) initCtx.lookup("java:comp/env");
		            if (envCtx == null)
		                sout.println("envCtx is NULL");

		            // Look up our data source
		            DataSource ds = (DataSource) envCtx.lookup("jdbc/TestDB");
		            
		            if (ds == null)
		                sout.println("ds is null.");

		            Connection dbcon = ds.getConnection();
		            if (dbcon == null)
		                sout.println("dbcon is null.");
//					
					
//					// Get a connection from dataSource
					//Connection dbcon = dataSource.getConnection();
////					
////					/***********************************************
////					 * START fuzzy query
////					 **********************************************/
					String fuzzyQuery = "select title from movies where " +
							"ed(upper(?), upper(title)) <= 1;";
					PreparedStatement fuzzyStmt = dbcon.prepareStatement(fuzzyQuery);
					fuzzyStmt.setString(1, searchTermRaw);
					ResultSet fuzzyRS = fuzzyStmt.executeQuery();
					
					
					String temp = "";
					String first = "";
					ArrayList<String> fuzzy_terms  = new ArrayList<String>();
					while (fuzzyRS.next()) {
						String searchTerm = "";
						first = fuzzyRS.getString("title");
						String[] titleArray = first.split(" ");
						for (String str : titleArray) {
							searchTerm += "+" + str + "* ";
						}
						fuzzy_terms.add(searchTerm);
					}
////					/************************************************
////					 * END fuzzy query
////					 ***********************************************/
					if (fuzzy_terms.size() == 0){
						String searchTerm = "";
						String[] titleArray = searchTermRaw.split(" ");
						for (String str : titleArray) {
							searchTerm += "+" + str + "* ";
						}
						fuzzy_terms.add(searchTerm);
					}
					String match_query = "";
					for(String fuzzy_term : fuzzy_terms)
					{
						match_query += "match(title) against(? in boolean mode)  or \r\n";
					}
					match_query = match_query.substring(0,match_query.lastIndexOf("or"));
					String sql = base_query + match_query + middle_query + match_query + order_query;
					PreparedStatement statement = dbcon.prepareStatement(sql);
////					
////					
////					// Set the parameter represented by "?" in the query to the id we get from url,
////					// num 1 indicates the first "?" in the query
					int fuzzy_size = fuzzy_terms.size();
					for(int i= 0; i< fuzzy_size; i++)
					{
						statement.setString(i+1, fuzzy_terms.get(i));
						statement.setString(i+1+fuzzy_size, fuzzy_terms.get(i));
					}
////					//statement.setString(3, searchTerm);
////					//statement.setString(4, searchTerm);
					statement.setInt((fuzzy_size*2)+1, Integer.parseInt(displayNum)); // limit
					statement.setInt((fuzzy_size*2)+2, int_offset); // offset

					ResultSet rs = statement.executeQuery();
					
					JsonArray jsonArray = new JsonArray();
					
////					// store string so we don't get repeating rows of the same movie
					String prevMovieId = "";
					
					while(rs.next()) {
						String movieId = rs.getString("id");
						String movieTitle = rs.getString("title");
						int movieYear = rs.getInt("year");
						String movieDirector = rs.getString("director");
////						String genreName = rs.getString("genreName");
////						String starName= rs.getString("starName");
						float movieRating = rs.getFloat("rating");
						
						JsonObject jsonObject = new JsonObject();
						
						if (!prevMovieId.equals(movieId)) {
							jsonObject.addProperty("movie_id", movieId);
			                jsonObject.addProperty("movie_title", movieTitle);
			                jsonObject.addProperty("movie_year", movieYear);
			                jsonObject.addProperty("movie_director", movieDirector);
			                jsonObject.addProperty("movie_rating", movieRating);
			                
			                
			                jsonArray.add(jsonObject);
						}
				
						String genreQuery = "SELECT * FROM genres, genres_in_movies\r\n" + 
		                		"WHERE genres_in_movies.movieId = ?"+ 
		                		"AND\r\n" + 
		                		"genres_in_movies.genreId = genres.id;";
		                
		                //Statement genreStatement = dbcon.createStatement();
		                PreparedStatement genreStatement = dbcon.prepareStatement(genreQuery);
		                genreStatement.setString(1, movieId);
		                ResultSet genreRS = genreStatement.executeQuery();
						
////		                // create array of genres to add to the json object
//////		                String genreQuery = "SELECT * FROM genres, genres_in_movies\r\n" + 
//////		                		"WHERE genres_in_movies.movieId = '"+ movieId + 
//////		                		"' AND\r\n" + 
//////		                		"genres_in_movies.genreId = genres.id;";
//////	                
//////		                Statement genreStatement = dbcon.createStatement();
//////		                ResultSet genreRS = genreStatement.executeQuery(genreQuery);
		                
		                JsonArray genreArr = new JsonArray();
		                
		                while(genreRS.next()) {
		                	JsonObject genreObj = new JsonObject();
		                	
		                	String genreId = genreRS.getString("id");
		                	String genreName = genreRS.getString("name");
		                	
		                	genreObj.addProperty("genre_id", genreId);
		                	genreObj.addProperty("genre_name", genreName);
		                	
		                	genreArr.add(genreObj);
		                }
		                jsonObject.add("genres", genreArr);
////		                
////		                // create array of stars to add to the json object
		                String starQuery = "SELECT * FROM stars\r\n" + 
		                		"	INNER JOIN stars_in_movies\r\n" + 
		                		"	ON stars_in_movies.starId = stars.id\r\n" + 
		                		"    and stars_in_movies.movieId = ?;";
		                
		                PreparedStatement starStatement = dbcon.prepareStatement(starQuery);
		                starStatement.setString(1, movieId);
		                ResultSet starRS = starStatement.executeQuery();
	                
		                JsonArray starArr = new JsonArray();
		                while(starRS.next()) {
		                	JsonObject starObj = new JsonObject();
		                	
		                	String starId = starRS.getString("id");
		                	String starName = starRS.getString("name");
		                	
		                	starObj.addProperty("star_id", starId);
		                	starObj.addProperty("star_name", starName);
		                	
		                	starArr.add(starObj);
		                }
		                jsonObject.add("stars", starArr);
		                
		                prevMovieId = movieId;
									
		            }
					System.out.println("Advanced Search by star that is not in movie.");
					System.out.println(jsonArray.toString());
					System.out.println(jsonArray.toString().length());
////					
////					// json array is empty "[]"
////					// add empty fields so javascript files 
////					// don't throw errors going through the 
////					// json data
					if (jsonArray.toString().length() == 2) {
						
						JsonObject jsonObject = new JsonObject();
						
////						// add empty movie
						jsonObject.addProperty("movie_id", "");
		                jsonObject.addProperty("movie_title", "");
		                jsonObject.addProperty("movie_year", "");
		                jsonObject.addProperty("movie_director", "");
		                jsonObject.addProperty("movie_rating", "");
		                jsonArray.add(jsonObject);
////		                
////		                // add empty genre
		                JsonArray genreArr = new JsonArray();
	                	JsonObject genreObj = new JsonObject();
	                	genreObj.addProperty("genre_id", "");
	                	genreObj.addProperty("genre_name", "");              	
	                	genreArr.add(genreObj);     
		                jsonObject.add("genres", genreArr);
						
		                System.out.println("before star statement.");
		                System.out.println(jsonArray.toString());
////		                
////		               // boolean starExists = false;
		                
						
					}
					
					
					sout.write(jsonArray.toString());
				
					response.setStatus(200);
		            rs.close();
		            dbcon.close();   
		            
					sout.close();
////					/***********************************************
////					 * end JDBC time
////					 **********************************************/
					long endTimeTJ = System.nanoTime();
					long elapsedTimeTJ = endTimeTJ - startTimeTJ; // elapsed time in nano seconds. Note: print the values in nano seconds 
					
					FileWriter fw = null;
					try {
						fw = new FileWriter(file, true);
						String str = String.valueOf(elapsedTimeTJ);
						str += " ";
						fw.write(str);
						
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						fw.close();
					}
	            
				} catch (Exception e) {
					// write error message JSON object to output
					JsonObject jsonObject = new JsonObject();
					Writer writer = new StringWriter();
					e.printStackTrace(new PrintWriter(writer));
					String s = writer.toString();
					jsonObject.addProperty("errorMessage", s);
					e.printStackTrace();
					sout.write(jsonObject.toString());

					// set reponse status to 500 (Internal Server Error)
					response.setStatus(500);
				}

			}
			
////			/***********************************************
////			 * end search time
////			 **********************************************/
			long endTimeTS = System.nanoTime();
			long elapsedTimeTS = endTimeTS - startTimeTS; // elapsed time in nano seconds. Note: print the values in nano seconds 
			
			FileWriter fw = null;
			try {
				fw = new FileWriter(file, true);
				String str = String.valueOf(elapsedTimeTS);
				str += "\n";
				fw.write(str);
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				fw.close();
			}
			
		}
		
}












// NO PREPARED STATEMENTS
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.io.StringWriter;
//import java.io.Writer;
//import java.sql.*;
//import java.util.ArrayList;
//import java.util.Enumeration;
//import java.util.Map;
//import java.util.Objects;
//
//import javax.annotation.Resource;
//import javax.naming.Context;
//import javax.naming.InitialContext;
//import javax.servlet.RequestDispatcher;
//import javax.servlet.ServletContext;
//import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.sql.DataSource;
//
//import com.google.gson.JsonArray;
//import com.google.gson.JsonObject;
//
//
//
//@WebServlet(name = "SearchServlet", urlPatterns = "/api/search-results")
//public class SearchServlet  extends HttpServlet {
//	private static final long serialVersionUID = 1L;
//	
////	// Time an event in a program to nanosecond precision
////	long startTimeTS = System.nanoTime();
//	
//	
//	public SearchServlet() {
//		super();
//	}
//	// Create a dataSource which registered in web.xml
//		//@Resource(name = "jdbc/moviedb")
//		//private DataSource dataSource; 
//		
////		/**
////		 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
////		 *      response)
////		 */
//		protected void doGet(HttpServletRequest request, HttpServletResponse response)
//				throws ServletException, IOException {
//			long startTimeTS = System.nanoTime();
//			
//			
////			/********************************************************
////			 * writing TS, TJ to file
////			 *******************************************************/
//			String contextPath = getServletContext().getRealPath("/");
//			String filePath = contextPath+"search-log";
//			System.out.println(filePath);
//			
//			File file = null;
//			try {
//				file = new File(filePath);
//			
//				// returns boolean
//				// true if file created, false if file already exists
//				file.createNewFile(); 
//				
//			} catch (Exception e) {
//				e.printStackTrace();
//			} 
//
//			
//			
//			response.setContentType("application/json"); // Response mime type
//
//			String userAgent = request.getHeader("User-Agent");
//			String searchTermRaw;
//			String pageNum;
//			String displayNum;
//			String sortBy;
//			String listOrder;
//			if (userAgent != null && !userAgent.contains("Android"))
//			{
//				
////			// get the parameters in the url
//				searchTermRaw = request.getParameter("search_term");
//				pageNum = request.getParameter("page-num");
//				displayNum = request.getParameter("display-num");
//				sortBy = request.getParameter("sort-by");
//				listOrder = request.getParameter("list");
//			}
//			else
//			{
//				searchTermRaw = request.getHeader("search_term");
//				pageNum = request.getHeader("page-num");
//				displayNum = request.getHeader("display-num");
//				sortBy = request.getHeader("sort-by");
//				listOrder = request.getHeader("list");
//			}
//			if (displayNum == null || displayNum.isEmpty()) {
//				displayNum = "10";
//			}
//			if (pageNum == null || pageNum.isEmpty()) {
//				pageNum = "1";
//			}
//			System.out.println("SearchTerm: " + searchTermRaw);
//			System.out.println("PageNum: " + pageNum);
//			System.out.println("DisplayNum: " + displayNum);
////			// default query, no ORDER BY, no ASC/DESC
//			String base_query = "select \r\n" + 
//					"movies.id, title, movies.`year`, \r\n" + 
//					"director, rating\r\n" + 
//					"from \r\n" + 
//					"movies\r\n" + 
//					"left join ratings \r\n" + 
//					"	on ratings.movieId = movies.id \r\n" + 
//					"where \r\n" ;
//			String middle_query = 
//					"union\r\n" +
//					"select \r\n" + 
//					"movies.id, title, movies.`year`, \r\n" + 
//					"director, rating\r\n" + 
//					"from \r\n" + 
//					"movies\r\n" + 
//					"right join ratings \r\n" + 
//					"	on ratings.movieId = movies.id \r\n" + 
//					"where \r\n" 
//					;
//			String order_query =
//					"limit 10\r\n" + 
//					"offset 0;";
////			/*
////			 * create order by statement to insert into sql query.
////			 */
//			String orderByClause = "";	
//			if (sortBy != null) {
//				if (!sortBy.isEmpty() && sortBy.equals("title")) {
//					orderByClause = "title";
//				} 
//				else if (!sortBy.isEmpty() && sortBy.equals("rating")) {
//					orderByClause = "rating";
//				}
//			}
//			
////			// query with ORDER BY but no ASC/DESC
//			if (!orderByClause.isEmpty()) {
//				if (orderByClause.equals("rating")) {
//					order_query = 
//							"order by rating\r\n" +
//							"limit 10\r\n" + 
//							"offset 0;";
//				}
//				else {
//					order_query = 
//							
//							"order by title\r\n" +
//							"limit 10\r\n" + 
//							"offset 0;";
//				}
//			}
//			
//			String listClause = "";
//			if (listOrder != null) {
//				if (!listOrder.isEmpty() && listOrder.equals("asc") && 
//						!orderByClause.isEmpty()) {
//					listClause = "asc";
//					if (orderByClause.equals("rating")) {
//						order_query = 
//								
//								"order by rating asc\r\n" +
//								"limit 10\r\n" + 
//								"offset 0;";
//					}
//					else {
//						order_query = 
//								
//								"order by title asc\r\n" +
//								"limit 10\r\n" + 
//								"offset 0;";
//					}
//				}
//				else if (!listOrder.isEmpty() && listOrder.equals("desc") && 
//						!orderByClause.isEmpty()) {
//					listClause = "desc";
//					if (orderByClause.equals("rating")) {
//						order_query = 
//								
//								"order by rating desc\r\n" +
//								"limit 10\r\n" + 
//								"offset 0;";
//					}
//					else {
//						order_query = 
//								
//								"order by title desc\r\n" +
//								"limit 10\r\n" + 
//								"offset 0;";
//					}
//				}
//			}
//						
//			System.out.printf("SearchServlet: offset (pagenum) = %s, limit (displayNum) = %s %n", pageNum, displayNum);
//			
////			// Determine whether you're going to the next or prev page.
//			int int_pg_num = Integer.parseInt(pageNum);
//		
////			
////			// none of the if statements above will execute if you're just loading the current pg
//			System.out.println("SearchServlet: int_pg_num =  " + Integer.toString(int_pg_num));
//			int int_offset = Integer.parseInt(displayNum) * (int_pg_num - 1);
//			System.out.println("SearchServlet: int_offset = " + Integer.toString(int_offset));
//			
//			if (searchTermRaw != null) {
////				//String searchTerm = "%" + searchTermRaw + "%";
////				
////				//System.out.println("SearchServlet search term: " + searchTerm);
////				
////				// Output stream to STDOUT
//				PrintWriter sout = response.getWriter();
//				try {
//					// Time an event in a program to nanosecond precision
//					long startTimeTJ = System.nanoTime();
//					
////		            // the following few lines are for connection pooling
////		            // Obtain our environment naming context
//					
//		            Context initCtx = new InitialContext();
//
//		            Context envCtx = (Context) initCtx.lookup("java:comp/env");
//		            if (envCtx == null)
//		                sout.println("envCtx is NULL");
//
////		            // Look up our data source
//		            DataSource ds = (DataSource) envCtx.lookup("jdbc/TestDB");
//		            
//		            if (ds == null)
//		                sout.println("ds is null.");
//
//		            Connection dbcon = ds.getConnection();
//		            if (dbcon == null)
//		                sout.println("dbcon is null.");
//					
//					
////					// Get a connection from dataSource
////					//Connection dbcon = dataSource.getConnection();
////					
////					/***********************************************
////					 * START fuzzy query
////					 **********************************************/
//					String fuzzyQuery = "select title from movies where " +
//							"ed(upper('" + searchTermRaw + "'), upper(title)) <= 1;";
//					Statement fuzzyStmt = dbcon.createStatement();
//					ResultSet fuzzyRS = fuzzyStmt.executeQuery(fuzzyQuery);
//					
//					String temp = "";
//					String first = "";
//					ArrayList<String> fuzzy_terms  = new ArrayList<String>();
//					while (fuzzyRS.next()) {
//						String searchTerm = "";
//						first = fuzzyRS.getString("title");
//						String[] titleArray = first.split(" ");
//						for (String str : titleArray) {
//							searchTerm += "+" + str + "* ";
//						}
//						fuzzy_terms.add(searchTerm);
//					}
////					/************************************************
////					 * END fuzzy query
////					 ***********************************************/
//					if (fuzzy_terms.size() == 0){
//						String searchTerm = "";
//						String[] titleArray = searchTermRaw.split(" ");
//						for (String str : titleArray) {
//							searchTerm += "+" + str + "* ";
//						}
//						fuzzy_terms.add(searchTerm);
//					}
//					String match_query = "";
//					for(String fuzzy_term : fuzzy_terms)
//					{
//						match_query += "match(title) against('" + fuzzy_term + "' in boolean mode)  or \r\n";
//					}
//					match_query = match_query.substring(0,match_query.lastIndexOf("or"));
//					String sql = base_query + match_query + middle_query + match_query + order_query;
//					Statement statement = dbcon.createStatement();
//					
//					
////					// Set the parameter represented by "?" in the query to the id we get from url,
////					// num 1 indicates the first "?" in the query
////					int fuzzy_size = fuzzy_terms.size();
////					for(int i= 0; i< fuzzy_size; i++)
////					{
////						statement.setString(i+1, fuzzy_terms.get(i));
////						statement.setString(i+1+fuzzy_size, fuzzy_terms.get(i));
////					}
////					//statement.setString(3, searchTerm);
////					//statement.setString(4, searchTerm);
////					//statement.setInt((fuzzy_size*2)+1, Integer.parseInt(displayNum)); // limit
////					//statement.setInt((fuzzy_size*2)+2, int_offset); // offset
////
//					ResultSet rs = statement.executeQuery(sql);
//					
//					JsonArray jsonArray = new JsonArray();
//					
////					// store string so we don't get repeating rows of the same movie
//					String prevMovieId = "";
//					
//					while(rs.next()) {
//						String movieId = rs.getString("id");
//						String movieTitle = rs.getString("title");
//						int movieYear = rs.getInt("year");
//						String movieDirector = rs.getString("director");
//////						String genreName = rs.getString("genreName");
//////						String starName= rs.getString("starName");
//						float movieRating = rs.getFloat("rating");
//						
//						JsonObject jsonObject = new JsonObject();
//						
//						if (!prevMovieId.equals(movieId)) {
//							jsonObject.addProperty("movie_id", movieId);
//			                jsonObject.addProperty("movie_title", movieTitle);
//			                jsonObject.addProperty("movie_year", movieYear);
//			                jsonObject.addProperty("movie_director", movieDirector);
//			                jsonObject.addProperty("movie_rating", movieRating);
//			                
//			                
//			                jsonArray.add(jsonObject);
//						}
//				
//						String genreQuery = "SELECT * FROM genres, genres_in_movies\r\n" + 
//		                		"WHERE genres_in_movies.movieId = '" + movieId + 
//		                		"' AND\r\n" + 
//		                		"genres_in_movies.genreId = genres.id;";
//		                
//		                Statement genreStatement = dbcon.createStatement();
////		                //PreparedStatement genreStatement = dbcon.prepareStatement(genreQuery);
////		                //genreStatement.setString(1, movieId);
//		                ResultSet genreRS = genreStatement.executeQuery(genreQuery);
//						
////		                // create array of genres to add to the json object
//////		                String genreQuery = "SELECT * FROM genres, genres_in_movies\r\n" + 
//////		                		"WHERE genres_in_movies.movieId = '"+ movieId + 
//////		                		"' AND\r\n" + 
//////		                		"genres_in_movies.genreId = genres.id;";
//////	                
//////		                Statement genreStatement = dbcon.createStatement();
//////		                ResultSet genreRS = genreStatement.executeQuery(genreQuery);
//		                
//		                JsonArray genreArr = new JsonArray();
//		                
//		                while(genreRS.next()) {
//		                	JsonObject genreObj = new JsonObject();
//		                	
//		                	String genreId = genreRS.getString("id");
//		                	String genreName = genreRS.getString("name");
//		                	
//		                	genreObj.addProperty("genre_id", genreId);
//		                	genreObj.addProperty("genre_name", genreName);
//		                	
//		                	genreArr.add(genreObj);
//		                }
//		                jsonObject.add("genres", genreArr);
//		                
////		                // create array of stars to add to the json object
//		                String starQuery = "SELECT * FROM stars\r\n" + 
//		                		"	INNER JOIN stars_in_movies\r\n" + 
//		                		"	ON stars_in_movies.starId = stars.id\r\n" + 
//		                		"    and stars_in_movies.movieId = '" + movieId + "';";
//		                
//		                Statement starStatement = dbcon.createStatement();
//
//		                ResultSet starRS = starStatement.executeQuery(starQuery);
//	                
//		                JsonArray starArr = new JsonArray();
//		                while(starRS.next()) {
//		                	JsonObject starObj = new JsonObject();
//		                	
//		                	String starId = starRS.getString("id");
//		                	String starName = starRS.getString("name");
//		                	
//		                	starObj.addProperty("star_id", starId);
//		                	starObj.addProperty("star_name", starName);
//		                	
//		                	starArr.add(starObj);
//		                }
//		                jsonObject.add("stars", starArr);
//		                
//		                prevMovieId = movieId;
//									
//		            }
//					System.out.println("Advanced Search by star that is not in movie.");
//					System.out.println(jsonArray.toString());
//					System.out.println(jsonArray.toString().length());
//					
////					// json array is empty "[]"
////					// add empty fields so javascript files 
////					// don't throw errors going through the 
////					// json data
//					if (jsonArray.toString().length() == 2) {
//						
//						JsonObject jsonObject = new JsonObject();
//						
////						// add empty movie
//						jsonObject.addProperty("movie_id", "");
//		                jsonObject.addProperty("movie_title", "");
//		                jsonObject.addProperty("movie_year", "");
//		                jsonObject.addProperty("movie_director", "");
//		                jsonObject.addProperty("movie_rating", "");
//		                jsonArray.add(jsonObject);
//		                
////		                // add empty genre
//		                JsonArray genreArr = new JsonArray();
//	                	JsonObject genreObj = new JsonObject();
//	                	genreObj.addProperty("genre_id", "");
//	                	genreObj.addProperty("genre_name", "");              	
//	                	genreArr.add(genreObj);     
//		                jsonObject.add("genres", genreArr);
//						
//		                System.out.println("before star statement.");
//		                System.out.println(jsonArray.toString());
//		                
////		               // boolean starExists = false;
//		                
//						
//					}
//					
//					
//					sout.write(jsonArray.toString());
//				
//					response.setStatus(200);
//		            rs.close();
//		            dbcon.close();   
//		            
//					sout.close();
////					/***********************************************
////					 * end JDBC time
////					 **********************************************/
//					long endTimeTJ = System.nanoTime();
//					long elapsedTimeTJ = endTimeTJ - startTimeTJ; // elapsed time in nano seconds. Note: print the values in nano seconds 
//					
//					FileWriter fw = null;
//					try {
//						fw = new FileWriter(file, true);
//						String str = String.valueOf(elapsedTimeTJ);
//						str += " ";
//						fw.write(str);
//						
//					} catch (Exception e) {
//						e.printStackTrace();
//					} finally {
//						fw.close();
//					}
//	            
//				} catch (Exception e) {
//					// write error message JSON object to output
//					JsonObject jsonObject = new JsonObject();
//					Writer writer = new StringWriter();
//					e.printStackTrace(new PrintWriter(writer));
//					String s = writer.toString();
//					jsonObject.addProperty("errorMessage", s);
//					e.printStackTrace();
//					sout.write(jsonObject.toString());
//
//					// set reponse status to 500 (Internal Server Error)
//					response.setStatus(500);
//				}
//
//			}
//			
////			/***********************************************
////			 * end search time
////			 **********************************************/
//			long endTimeTS = System.nanoTime();
//			long elapsedTimeTS = endTimeTS - startTimeTS; // elapsed time in nano seconds. Note: print the values in nano seconds 
//			
//			FileWriter fw = null;
//			try {
//				fw = new FileWriter(file, true);
//				String str = String.valueOf(elapsedTimeTS);
//				str += "\n";
//				fw.write(str);
//				
//			} catch (Exception e) {
//				e.printStackTrace();
//			} finally {
//				fw.close();
//			}
//			
//		}
//		
//}