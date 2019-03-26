import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@WebServlet(name = "AdvancedSearchServlet", urlPatterns = "/api/advanced-search")
public class AdvancedSearchServlet  extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public AdvancedSearchServlet() {
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
			
			response.setContentType("application/json"); // Response mime type

			System.out.print("AdvancedSearchServlet: In GET -------------------------------------");
			// get the parameters in the url
			String titleRaw = request.getParameter("movie-title");
			String title = "%" + titleRaw + "%";
			
			String yearRaw = request.getParameter("movie-year");
			String year = "%" + yearRaw + "%";
			
			String directorRaw = request.getParameter("movie-director");
			String director = "%" + directorRaw + "%";
				
			String starRaw = request.getParameter("star-name");
			String star = "%" + starRaw + "%";
			
			String page = request.getParameter("page-num");
			String limit = request.getParameter("display-num");

			String sortBy = request.getParameter("sort-by");
			String listOrder = request.getParameter("list");
			
			/*
			 * create sql query based on parameters gotten from request
			 */
			String query = "";
			query = "SELECT movies.id, title, movies.`year`, director,\r\n" + 
					"rating, stars.`name` from \r\n" + 
					"movies, ratings, stars, stars_in_movies  \r\n" + 
					"where \r\n" + 
					"movies.`year` LIKE ? and \r\n" + 
					"title like ? and  \r\n" + 
					"director like ? and\r\n" + 
					"stars.`name` like ? \r\n" + 
					"and ratings.movieId = movies.id and \r\n" + 
					"stars_in_movies.starId = stars.id and \r\n" + 
					"stars_in_movies.movieId = movies.id \r\n" + 
					"group by movies.id \r\n" +
					"limit ? offset ?;";
			
			/*
			 * create order by statement to insert into sql query.
			 */
			String orderByClause = "";	
			if (sortBy != null) {
				if (!sortBy.isEmpty() && sortBy.equals("title")) {
					orderByClause = "title";
					
				} 
				else if (!sortBy.isEmpty() && sortBy.equals("rating")) {
					orderByClause = "rating";
				}
			}
			
			if (listOrder != null) {
				if (!listOrder.isEmpty() && listOrder.equals("asc") && 
						!orderByClause.isEmpty()) {
					
					if (orderByClause.equals("rating")) {
						query = "SELECT movies.id, title, movies.`year`, director,\r\n" + 
								"rating, stars.`name` from \r\n" + 
								"movies, ratings, stars, stars_in_movies  \r\n" + 
								"where \r\n" + 
								"movies.`year` LIKE ? and \r\n" + 
								"title like ? and  \r\n" + 
								"director like ? and\r\n" + 
								"stars.`name` like ? \r\n" + 
								"and ratings.movieId = movies.id and \r\n" + 
								"stars_in_movies.starId = stars.id and \r\n" + 
								"stars_in_movies.movieId = movies.id \r\n" + 
								"group by movies.id \r\n" +
								"order by rating asc limit ? offset ?;";
					}
					else {
						query = "SELECT movies.id, title, movies.`year`, director,\r\n" + 
								"rating, stars.`name` from \r\n" + 
								"movies, ratings, stars, stars_in_movies  \r\n" + 
								"where \r\n" + 
								"movies.`year` LIKE ? and \r\n" + 
								"title like ? and  \r\n" + 
								"director like ? and\r\n" + 
								"stars.`name` like ? \r\n" + 
								"and ratings.movieId = movies.id and \r\n" + 
								"stars_in_movies.starId = stars.id and \r\n" + 
								"stars_in_movies.movieId = movies.id \r\n" + 
								"group by movies.id \r\n" +
								"order by title asc limit ? offset ?;";
					}
				}
				else if (!listOrder.isEmpty() && listOrder.equals("desc") && 
						!orderByClause.isEmpty()) {
					
					if (orderByClause.equals("rating")) {
						query = "SELECT movies.id, title, movies.`year`, director,\r\n" + 
								"rating, stars.`name` from \r\n" + 
								"movies, ratings, stars, stars_in_movies  \r\n" + 
								"where \r\n" + 
								"movies.`year` LIKE ? and \r\n" + 
								"title like ? and  \r\n" + 
								"director like ? and\r\n" + 
								"stars.`name` like ? \r\n" + 
								"and ratings.movieId = movies.id and \r\n" + 
								"stars_in_movies.starId = stars.id and \r\n" + 
								"stars_in_movies.movieId = movies.id \r\n" + 
								"group by movies.id \r\n" +
								"order by rating desc limit ? offset ?;";
					}
					else {
						query = "SELECT movies.id, title, movies.`year`, director,\r\n" + 
								"rating, stars.`name` from \r\n" + 
								"movies, ratings, stars, stars_in_movies  \r\n" + 
								"where \r\n" + 
								"movies.`year` LIKE ? and \r\n" + 
								"title like ? and  \r\n" + 
								"director like ? and\r\n" + 
								"stars.`name` like ? \r\n" + 
								"and ratings.movieId = movies.id and \r\n" + 
								"stars_in_movies.starId = stars.id and \r\n" + 
								"stars_in_movies.movieId = movies.id \r\n" + 
								"group by movies.id \r\n" +
								"order by title desc limit ? offset ?;";
					}
				}
			}
			
			if ((page == null) || (page.compareTo("null") == 0)) {

				page = "1";
//				request.setAttribute("page-num", new String("1"));
//				offset = (String)request.getAttribute("page-num");
				System.out.println("AdvancedSearchServlet: set page-num: " + page);
			}

			if ((limit == null) || (limit.compareTo("null") == 0))  {

				limit = "10";
//				request.setAttribute("display-num", new String("10"));
//				limit = (String)request.getAttribute("display-num");
				System.out.println("AdvancedSearchServlet: set display-num: " + limit);
			}
			
			System.out.printf("AdvancedSearchServlet: request parameters are title = %s, year = %s, star = %s, page = %s, limit = %s%n", titleRaw, yearRaw, starRaw, page, limit);
			
			// get the int value of the page number/offset
			int int_pg_num = Integer.parseInt(page);
//			if (action != null)
//			{
//				if (action.compareTo("Prev") == 0)
//				{
//					int_pg_num -= 1;
//				}
//				if (action.compareTo("Next") == 0)
//				{
//					int_pg_num += 1;
//				}
//			}
			
			// none of the if statements above will execute if you're just loading the current pg
			System.out.println("AdvancedSearchServlet: int_pg_num is " + Integer.toString(int_pg_num));
			
			// calculate the value of the offset
			int int_offset = Integer.parseInt(limit) * (int_pg_num - 1);
			
			//int int_offset = Integer.parseInt(limit) * (Integer.parseInt(offset) - 1);
			System.out.println("AdvancedSearchServlet: int_offset = " + Integer.toString(int_offset));
			System.out.println("title: " + title);
			System.out.println("year: " + year);
			System.out.println("director: " + director);
			System.out.println("star: " + star);
			
			
			if (titleRaw != null && yearRaw != null && directorRaw != null
					&& starRaw != null) {
				// Output stream to STDOUT
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
					// Get a connection from dataSource
					//Connection dbcon = dataSource.getConnection();
					String sql = query;	

					PreparedStatement statement = dbcon.prepareStatement(sql);
					System.out.println("PreparedStatement");
					
					// Set the parameter represented by "?" in the query to the id we get from url,
					// num 1 indicates the first "?" in the query
					statement.setString(1, year);
					statement.setString(2, title);
					statement.setString(3, director);
					statement.setString(4, star);
					statement.setInt(5, Integer.parseInt(limit));
					statement.setInt(6, int_offset);
					
					//System.out.println(statement);
					System.out.println(statement.toString());
					ResultSet rs = statement.executeQuery();
					
					JsonArray jsonArray = new JsonArray();
					
					// store string so we don't get repeating rows of the same movie
					String prevMovieId = "";
					
					while(rs.next()) {
						String movieId = rs.getString("id");
						String movieTitle = rs.getString("title");
						int movieYear = rs.getInt("year");
						String movieDirector = rs.getString("director");
						float movieRating = rs.getFloat("rating");
						
						JsonObject jsonObject = new JsonObject();
						
						if (!prevMovieId.equals(movieId) || prevMovieId.equals("")) {
							jsonObject.addProperty("movie_id", movieId);
			                jsonObject.addProperty("movie_title", movieTitle);
			                jsonObject.addProperty("movie_year", movieYear);
			                jsonObject.addProperty("movie_director", movieDirector);
			                jsonObject.addProperty("movie_rating", movieRating);
			                
			                jsonArray.add(jsonObject);
						}
						
		                // create array of genres to add to the json object
		                String genreQuery = "SELECT * FROM genres, genres_in_movies\r\n" + 
		                		"WHERE genres_in_movies.movieId = ?"+ 
		                		"AND\r\n" + 
		                		"genres_in_movies.genreId = genres.id;";
		                
		                //Statement genreStatement = dbcon.createStatement();
		                PreparedStatement genreStatement = dbcon.prepareStatement(genreQuery);
		                genreStatement.setString(1, movieId);
		                ResultSet genreRS = genreStatement.executeQuery();
		                
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
		                
		                // create array of stars to add to the json object
		                String starQuery = "SELECT * FROM stars, stars_in_movies\r\n" + 
		                		"WHERE stars_in_movies.movieId = ?" +
		                		"AND\r\n" + 
		                		"stars_in_movies.starId = stars.id\r\n" +
		                		"AND stars.`name` LIKE ?;";
		                
		                //Statement starStatement = dbcon.createStatement();
		                PreparedStatement starStatement = dbcon.prepareStatement(starQuery);
		                starStatement.setString(1, movieId);
		                starStatement.setString(2, star);
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
					
					// json array is empty "[]"
					if (jsonArray.toString().length() == 2) {
						
						JsonObject jsonObject = new JsonObject();
						
						// add empty movie
						jsonObject.addProperty("movie_id", "");
		                jsonObject.addProperty("movie_title", "");
		                jsonObject.addProperty("movie_year", "");
		                jsonObject.addProperty("movie_director", "");
		                jsonObject.addProperty("movie_rating", "");
		                jsonArray.add(jsonObject);
		                
		                // add empty genre
		                JsonArray genreArr = new JsonArray();
	                	JsonObject genreObj = new JsonObject();
	                	genreObj.addProperty("genre_id", "");
	                	genreObj.addProperty("genre_name", "");              	
	                	genreArr.add(genreObj);     
		                jsonObject.add("genres", genreArr);
						
		                System.out.println("before star statement.");
		                System.out.println(jsonArray.toString());
						if (!star.isEmpty()) {
							String starSql = "select * from stars where name like ?;";
							PreparedStatement starStatement = dbcon.prepareStatement(starSql);
				            starStatement.setString(1, star);
				            ResultSet starRS = starStatement.executeQuery();
				            
				            JsonArray starArr = new JsonArray();
				            String prevStarId = "";
			                while(starRS.next()) {
			                	JsonObject starObj = new JsonObject();
			                	
			                	String starId = starRS.getString("id");
			                	String starName = starRS.getString("name");
			                	
			                	starObj.addProperty("star_id", starId);
			                	starObj.addProperty("star_name", starName);
			                	
			                	if (!prevStarId.equals(starId) || !prevStarId.equals("")) {
			                		starArr.add(starObj);
			                	}
			                	
			                	
			                	prevStarId = starId;
			                }
			                jsonObject.add("stars", starArr);	                
			                jsonArray.add(jsonObject);
						}
					}
					System.out.println(jsonArray.toString());
					sout.write(jsonArray.toString());
				
					response.setStatus(200);
		            rs.close();
		            dbcon.close();   
		            
				} catch (Exception e) {
					// write error message JSON object to output
					JsonObject jsonObject = new JsonObject();
					jsonObject.addProperty("errorMessage", e.getMessage());
					sout.write(jsonObject.toString());

					// set reponse status to 500 (Internal Server Error)
					response.setStatus(500);
				}
			sout.close();
			}
		System.out.println("AdvancedSearchServlet: End of GET -----------------------------------");
		}
		
		
//	    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
//	       /* HttpSession session = request.getSession();
//	        String sessionId = session.getId();
//	        Long lastAccessTime = session.getLastAccessedTime();
//
//	        JsonObject responseJsonObject = new JsonObject();
//	        responseJsonObject.addProperty("sessionID", sessionId);
//	        responseJsonObject.addProperty("lastAccessTime", new Date(lastAccessTime).toString());
//
//	        // write all the data into the jsonObject
//	        response.getWriter().write(responseJsonObject.toString());*/
//	    	
//	    	String item = request.getParameter("product_id");
//	        System.out.println("Item is:" + item);
//	        System.out.println("Is this runnning!!!!!!!!");
//	        HttpSession session = request.getSession();
//
//	        // get the previous items in a ArrayList
//	        ArrayList<String> previousItems = (ArrayList<String>) session.getAttribute("previousItems");
//	        if (previousItems == null) {
//	            previousItems = new ArrayList<>();
//	            previousItems.add(item);
//	            session.setAttribute("previousItems", previousItems);
//	        } else {
//	            // prevent corrupted states through sharing under multi-threads
//	            // will only be executed by one thread at a time
//	            synchronized (previousItems) {
//	                previousItems.add(item);
//	            }
//	        }
//
//	        //response.getWriter().write(String.join(",", previousItems));
//	    }
	    
}

