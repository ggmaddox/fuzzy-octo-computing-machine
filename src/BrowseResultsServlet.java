import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
//import java.util.Enumeration;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
//import javax.servlet.http.HttpSession;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

@WebServlet(name = "BrowseResultsServlet", urlPatterns = "/api/browse-results")
public class BrowseResultsServlet  extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public BrowseResultsServlet() {
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
			
			// get the parameters in the url
			String titleTerm = request.getParameter("title");
			String genreTerm = request.getParameter("genre");
			String sortBy = request.getParameter("sort-by");
			String listOrder = request.getParameter("list");
			String page = request.getParameter("page-num");
			String limit = request.getParameter("display-num");
			
			System.out.println("title in BrowseResultsServlet: " + titleTerm);
			System.out.println("genre in BrowseResultsServlet: " + genreTerm);
			System.out.println("sort-by in BrowseResultsServlet: " + sortBy);
			System.out.println("list in BrowseResultsServlet: " + listOrder);
			System.out.println("page-num in BrowseResultsServlet: " + page);
			System.out.println("display-num in BrowseResultsServlet: " + limit);
			
			boolean byGenre = false;
			
			if (genreTerm == null) { genreTerm = ""; }
			if (titleTerm == null) { titleTerm = ""; }
			
			// default query, no sorting info
			String query = "";	
			
			if (!genreTerm.isEmpty()) {
				query = "select movies.id, movies.title, movies.director,\r\n" + 
						"movies.`year`, genres.`name`, rating \r\n" + 
						"from movies\r\n" + 
						"inner join genres_in_movies \r\n" + 
						"	on genres_in_movies.movieId = movies.id\r\n" + 
						"inner join genres \r\n" + 
						"	on genres.id = genres_in_movies.genreId\r\n" + 
						"inner join ratings\r\n" + 
						"	on ratings.movieId = movies.id\r\n" + 
						"where genres.id = ?\r\n"+
						"limit ? offset ?;";
				byGenre = true;
			}
			else {
				query = "select movies.id, movies.title, movies.director,\r\n" + 
						"movies.`year`, rating \r\n" + 
						"from movies\r\n" + 
						"inner join ratings\r\n" + 
						"	on ratings.movieId = movies.id\r\n" + 
						"where movies.title like ?\r\n" +
						"limit ? offset ?;";
				byGenre = false;
			}
			
			// get ORDER BY info, if any
			String orderByClause = "";	
			if (sortBy != null) {
				if (!sortBy.isEmpty() && sortBy.equals("title")) {
					orderByClause = "title";
				} 
				else if (!sortBy.isEmpty() && sortBy.equals("rating")) {
					orderByClause = "rating";
				}
			}
			
			// query for sorting by title/rating, no asc/desc parameters
			if (!orderByClause.isEmpty()) {
				if (orderByClause.equals("rating")) {
					if (!genreTerm.isEmpty()) {
						query = "select movies.id, movies.title, movies.director,\r\n" + 
								"movies.`year`, genres.`name`, rating \r\n" + 
								"from movies\r\n" + 
								"inner join genres_in_movies \r\n" + 
								"	on genres_in_movies.movieId = movies.id\r\n" + 
								"inner join genres \r\n" + 
								"	on genres.id = genres_in_movies.genreId\r\n" + 
								"inner join ratings\r\n" + 
								"	on ratings.movieId = movies.id\r\n" + 
								"where genres.id = ?\r\n"+
								"order by rating\r\n" +
								"limit ? offset ?;";
						byGenre = true;
					}
					else {
						query = "select movies.id, movies.title, movies.director,\r\n" + 
								"movies.`year`, rating \r\n" + 
								"from movies\r\n" + 
								"inner join ratings\r\n" + 
								"	on ratings.movieId = movies.id\r\n" + 
								"where movies.title like ?\r\n" +
								"order by rating\r\n" +
								"limit ? offset ?;";
						byGenre = false;
					}
				}
				else { // order by title
					if (!genreTerm.isEmpty()) {
						query = "select movies.id, movies.title, movies.director,\r\n" + 
								"movies.`year`, genres.`name`, rating \r\n" + 
								"from movies\r\n" + 
								"inner join genres_in_movies \r\n" + 
								"	on genres_in_movies.movieId = movies.id\r\n" + 
								"inner join genres \r\n" + 
								"	on genres.id = genres_in_movies.genreId\r\n" + 
								"inner join ratings\r\n" + 
								"	on ratings.movieId = movies.id\r\n" + 
								"where genres.id = ?\r\n"+
								"order by title\r\n" +
								"limit ? offset ?;";
						byGenre = true;
					}
					else {
						query = "select movies.id, movies.title, movies.director,\r\n" + 
								"movies.`year`, rating \r\n" + 
								"from movies\r\n" + 
								"inner join ratings\r\n" + 
								"	on ratings.movieId = movies.id\r\n" + 
								"where movies.title like ?\r\n" +
								"order by title\r\n" +
								"limit ? offset ?;";
						byGenre = false;
					}
				}
			}
			
			// query for sorting by title/rating with asc/desc parameters
			if (listOrder != null) {
				if (!listOrder.isEmpty() && listOrder.equals("asc") && 
						!orderByClause.isEmpty()) {
					
					if (orderByClause.equals("rating")) {
						if (!genreTerm.isEmpty()) {
							query = "select movies.id, movies.title, movies.director,\r\n" + 
									"movies.`year`, genres.`name`, rating \r\n" + 
									"from movies\r\n" + 
									"inner join genres_in_movies \r\n" + 
									"	on genres_in_movies.movieId = movies.id\r\n" + 
									"inner join genres \r\n" + 
									"	on genres.id = genres_in_movies.genreId\r\n" + 
									"inner join ratings\r\n" + 
									"	on ratings.movieId = movies.id\r\n" + 
									"where genres.id = ?\r\n"+ 
									"order by rating\r\n" +
									"asc\r\n" +
									"limit ? offset ?;";
							byGenre = true;
						}
						else {
							query = "select movies.id, movies.title, movies.director,\r\n" + 
									"movies.`year`, rating \r\n" + 
									"from movies\r\n" + 
									"inner join ratings\r\n" + 
									"	on ratings.movieId = movies.id\r\n" + 
									"where movies.title like ?\r\n" + 
									"order by rating\r\n" +
									"asc\r\n" +
									"limit ? offset ?;";
							byGenre = false;
						}
					}
					else if (orderByClause.equals("title")) {
						if (!genreTerm.isEmpty()) {
							query = "select movies.id, movies.title, movies.director,\r\n" + 
									"movies.`year`, genres.`name`, rating \r\n" + 
									"from movies\r\n" + 
									"inner join genres_in_movies \r\n" + 
									"	on genres_in_movies.movieId = movies.id\r\n" + 
									"inner join genres \r\n" + 
									"	on genres.id = genres_in_movies.genreId\r\n" + 
									"inner join ratings\r\n" + 
									"	on ratings.movieId = movies.id\r\n" + 
									"where genres.id = ?\r\n"+
									"order by title\r\n" +
									"asc\r\n" +
									"limit ? offset ?;";
							byGenre = true;
						}
						else {
							query = "select movies.id, movies.title, movies.director,\r\n" + 
									"movies.`year`, rating \r\n" + 
									"from movies\r\n" + 
									"inner join ratings\r\n" + 
									"	on ratings.movieId = movies.id\r\n" + 
									"where movies.title like ?\r\n" + 
									"order by title\r\n" +
									"asc\r\n" +
									"limit ? offset ?;";
							byGenre = false;
						}
					}
					
				}
				else if (!listOrder.isEmpty() && listOrder.equals("desc") && 
						!orderByClause.isEmpty()) {
					
					if (orderByClause.equals("title")) {
						if (!genreTerm.isEmpty()) {
							query = "select movies.id, movies.title, movies.director,\r\n" + 
									"movies.`year`, genres.`name`, rating \r\n" + 
									"from movies\r\n" + 
									"inner join genres_in_movies \r\n" + 
									"	on genres_in_movies.movieId = movies.id\r\n" + 
									"inner join genres \r\n" + 
									"	on genres.id = genres_in_movies.genreId\r\n" + 
									"inner join ratings\r\n" + 
									"	on ratings.movieId = movies.id\r\n" + 
									"where genres.id = ?\r\n"+
									"order by title\r\n" +
									"desc\r\n" +
									"limit ? offset ?;";
							byGenre = true;
						}
						else {
							query = "select movies.id, movies.title, movies.director,\r\n" + 
									"movies.`year`, rating \r\n" + 
									"from movies\r\n" + 
									"inner join ratings\r\n" + 
									"	on ratings.movieId = movies.id\r\n" + 
									"where movies.title like ?\r\n" + 
									"order by title\r\n" +
									"desc\r\n" +
									"limit ? offset ?;";
							byGenre = false;
						}
					}
					else if (orderByClause.equals("rating")) {
						if (!genreTerm.isEmpty()) {
							query = "select movies.id, movies.title, movies.director,\r\n" + 
									"movies.`year`, genres.`name`, rating \r\n" + 
									"from movies\r\n" + 
									"inner join genres_in_movies \r\n" + 
									"	on genres_in_movies.movieId = movies.id\r\n" + 
									"inner join genres \r\n" + 
									"	on genres.id = genres_in_movies.genreId\r\n" + 
									"inner join ratings\r\n" + 
									"	on ratings.movieId = movies.id\r\n" + 
									"where genres.id = ?\r\n"+
									"order by rating\r\n" +
									"desc\r\n" +
									"limit ? offset ?;";
							byGenre = true;
						}
						else {
							query = "select movies.id, movies.title, movies.director,\r\n" + 
									"movies.`year`, rating \r\n" + 
									"from movies\r\n" + 
									"inner join ratings\r\n" + 
									"	on ratings.movieId = movies.id\r\n" + 
									"where movies.title like ?\r\n" + 
									"order by rating\r\n" +
									"desc\r\n" +
									"limit ? offset ?;";
							byGenre = false;
						}
					}
					
				}
			}
			// TODO: cast to int and calculate offset, cast back to strings
			
			// prev and next will alter offset by 
			//String


			System.out.printf("BrowseResutlsServlet: request parameters are titleTerm = %s, genreTerm = %s, page = %s, limit = %s%n", titleTerm, genreTerm, page, limit);
			if ((page == null) || (page.compareTo("null") == 0)) {

				page = "1";
				System.out.println("AdvancedSearchServlet: set page-num: " + page);
			}


			if ((limit == null) || (limit.compareTo("null") == 0))  {

				limit = "10";
				System.out.println("AdvancedSearchServlet: set display-num: " + limit);
			}
			
			//TODO: request.getParameter("page-number")
			// and offset 
			

			int int_offset = Integer.parseInt(limit) * (Integer.parseInt(page) - 1);
			
			System.out.println("BrowseResultsServlet: int_offset = " + Integer.toString(int_offset));
			System.out.println("BrowseResultsServlet genreTerm: " + genreTerm);
			System.out.println("BrowseResultsServlet titleTerm: " + titleTerm);
		
			// Output stream to STDOUT
			PrintWriter sout = response.getWriter();

			try {
				// Get a connection from dataSource
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
				String sql = query;
		
				PreparedStatement statement = dbcon.prepareStatement(sql);
				System.out.println(statement.toString());
				
				// Set the parameter represented by "?" in the query to the id we get from url,
				// num 1 indicates the first "?" in the query
				if (byGenre) {
					statement.setString(1, genreTerm);
					statement.setInt(2, Integer.parseInt(limit));
					statement.setInt(3, int_offset);
				}
				else {
					statement.setString(1, titleTerm + "%");
					statement.setInt(2, Integer.parseInt(limit));
					statement.setInt(3, int_offset);
				}

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
	
	                // create array of genres to add to the json object
//	                String genreQuery = "SELECT * FROM genres, genres_in_movies\r\n" + 
//	                		"WHERE genres_in_movies.movieId = '"+ movieId + 
//	                		"' AND\r\n" + 
//	                		"genres_in_movies.genreId = genres.id;";
//                
//	                Statement genreStatement = dbcon.createStatement();
//	                ResultSet genreRS = genreStatement.executeQuery(genreQuery);
	                
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
				//jsonArray.add(limitOffset);
				
				sout.write(jsonArray.toString());
			
				response.setStatus(200);
	            rs.close();
	            dbcon.close();   
            
			} catch (Exception e) {
				// write error message JSON object to output
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("errorMessage", e.getMessage());
				e.printStackTrace();
				sout.write(jsonObject.toString());

				// set reponse status to 500 (Internal Server Error)
				response.setStatus(500);
				}
			sout.close();		
		}
}
