
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.sql.Connection;

// server endpoint URL
@WebServlet("/index") // was index
public class SearchSuggestions extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	/*
	 * populate the Super hero hash map.
	 * Key is hero ID. Value is hero name.
	 */
	//public static HashMap<Integer, String> superHeroMap = new HashMap<>();
    
    public SearchSuggestions() {
        super();
    }

    /*
     * 
     * Match the query against superheroes and return a JSON response.
     * 
     * For example, if the query is "super":
     * The JSON response look like this:
     * [
     * 	{ "value": "Superman", "data": { "heroID": 101 } },
     * 	{ "value": "Supergirl", "data": { "heroID": 113 } }
     * ]
     * 
     * The format is like this because it can be directly used by the 
     *   JSON auto complete library this example is using. So that you don't have to convert the format.
     *   
     * The response contains a list of suggestions.
     * In each suggestion object, the "value" is the item string shown in the dropdown list,
     *   the "data" object can contain any additional information.
     * 
     * 
     */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
//			Class.forName("com.mysql.jdbc.Driver").newInstance();
//			
//			Connection dbcon =
//				       DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?useSSL=false",
//								"mytestuser","mypassword");
			
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

			// setup the response json arrray
			JsonArray jsonArray = new JsonArray();
			
			// get the query string from parameter
			String query = request.getParameter("query");
			System.out.println(query);
			 
			// return the empty json array if query is null or empty
			if (query == null || query.trim().isEmpty()) {
				response.getWriter().write(jsonArray.toString());
				return;
			}	
			
			// format query(movie title) to be in full text mode
			String ftTitle = "";
			String[] titleArray = query.split(" ");
			for (String str : titleArray) {
				ftTitle += "+" + str + "* ";
			}
	
			// search on superheroes and add the results to JSON Array
			// this example only does a substring match
			// TODO: in project 4, you should do full text search with MySQL to find the matches on movies and stars
			
//			String sql = "select \r\n" + 
//					"movies.id, title, movies.`year`, \r\n" + 
//					"director, rating\r\n" + 
//					"from   \r\n" + 
//					"movies\r\n" + 
//					"inner join ratings\r\n" + 
//					"	on movies.id = ratings.movieId\r\n" + 
//					"where \r\n" + 
//					"MATCH (title) AGAINST (? IN BOOLEAN MODE);  ";
			
			String sql = "select movies.id, title, movies.`year`, \r\n" + 
					"director, rating\r\n" + 
					"from movies\r\n" + 
					"left join ratings on ratings.movieId = movies.id\r\n" + 
					"where match(title) against(? in boolean mode)\r\n" + 
					"union\r\n" + 
					"select movies.id, title, movies.`year`, \r\n" + 
					"director, rating\r\n" + 
					"from movies\r\n" + 
					"right join ratings on ratings.movieId = movies.id\r\n" + 
					"where match(title) against(? in boolean mode);";
			
			PreparedStatement stmnt = dbcon.prepareStatement(sql);
			stmnt.setString(1, ftTitle);
			stmnt.setString(2, ftTitle);
			
			String movieId = "";
			String movieTitle = "";
			int movieYear = 0;
			String movieDirector = "";
			float movieRating = 0;
			
			// store string so we don't get repeating rows of the same movie
			String prevMovieId = "";
						
			ResultSet rs = stmnt.executeQuery();
			while (rs.next()) {
				
				JsonObject jsonObjectValue = new JsonObject();
				JsonObject jsonObjectData = new JsonObject();
				movieId = rs.getString("id");
				movieTitle = rs.getString("title");
				movieYear = rs.getInt("year");
				movieDirector = rs.getString("director");
				movieRating = rs.getFloat("rating"); 
				
				if (!prevMovieId.equals(movieId)) {
					jsonObjectValue.addProperty("value", movieTitle);
					
					jsonObjectData.addProperty("movie_id", movieId);
					jsonObjectData.addProperty("movie_title", movieTitle);
					jsonObjectData.addProperty("movie_year", movieYear);
					jsonObjectData.addProperty("movie_director", movieDirector);
					jsonObjectData.addProperty("movie_rating", movieRating);
	                
	                
	                //jsonArray.add(jsonObject);
				}
				
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
                jsonObjectData.add("genres", genreArr);
                
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
                jsonObjectData.add("stars", starArr);
                
                jsonObjectValue.add("data", jsonObjectData);
                jsonArray.add(jsonObjectValue);
                
                prevMovieId = movieId;
			}

			response.getWriter().write(jsonArray.toString());
			return;
		} catch (Exception e) {
			System.out.println(e);
			response.sendError(500, e.getMessage());
		}
	}


}
