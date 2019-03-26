import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;


// Declaring a WebServlet called StarsServlet, which maps to url "/api/stars"
/// TODO change star name to movie
@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movie-list")
public class MoviesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.xml
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
    		throws ServletException, IOException {


    	response.setContentType("application/json"); // Response mime type
    	   	
//    	String genre = request.getParameter("title");
//    	System.out.println("MoviesServlet title from browse: " + genre);
//    	
//    	if (genre != null) {
//    		
//    		String searchTerm = "%" + genre + "%";
//			
//			System.out.println("SearchServlet search term: " + searchTerm);
//			
//			// Output stream to STDOUT
//			PrintWriter out = response.getWriter();
//
//    	}
//    }
//}
// original MoviesServlet
    	
    	
        response.setContentType("application/json"); // Response mime type

//        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try {
//            // Get a connection from dataSource
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

//            // Declare our statement
            
            

            String query = "SELECT * from stars";
            String movies_query = "SELECT m.title, m.id, m.year, m.director, r.rating \r\n" + 
            		"FROM movies AS m, \r\n" + 
            		"ratings AS r \r\n" + 
            		"WHERE m.id = r.movieID \r\n" + 
            		"ORDER BY r.rating DESC LIMIT 10 OFFSET 0;";
//
//            // Perform the query
            PreparedStatement statement = dbcon.prepareStatement(query);

            PreparedStatement movies_statement = dbcon.prepareStatement(movies_query);
            
            ResultSet rs = statement.executeQuery();
            ResultSet movies_rs = movies_statement.executeQuery();


            JsonArray jsonArray = new JsonArray();

//            // Iterate through each row of rs

            while (movies_rs.next()) {
            	
                String movie_id = movies_rs.getString("id");
                String movie_title = movies_rs.getString("title");
				int movie_year = movies_rs.getInt("year");
				String movie_director = movies_rs.getString("director");
				float movie_rating = movies_rs.getFloat("rating");
//                // Create a JsonObject based on the data we retrieve from rs
//                // TODO add the appropriate attributes to json
//                // TODO setup genres & stars for loop
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                
                
//                // Create list of genres (w/ genre name and id)
                Statement genres_statement = dbcon.createStatement();
                String genres_query = "SELECT g.name, g.id FROM genres AS g, genres_in_movies AS gm WHERE '" + movie_id + "' = gm.movieId AND gm.genreId = g.id";
                ResultSet genres_rs = genres_statement.executeQuery(genres_query);
                
                JsonArray genres_array = new JsonArray();
                while(genres_rs.next())
                {
                	JsonObject genre_object = new JsonObject();
                	String genre_id = genres_rs.getString("id");
                	String genre_name = genres_rs.getString("name");
                	genre_object.addProperty("genre_id", genre_id);
                	genre_object.addProperty("genre_name", genre_name);
                	genres_array.add(genre_object);
                }
                jsonObject.add("genres", genres_array);
                
//                // Create list of stars (w/ star name and id)
                Statement stars_statement = dbcon.createStatement();
                String stars_query = "SELECT s.name, s.id FROM stars AS s, stars_in_movies AS sm WHERE '" + movie_id + "' = sm.movieId AND sm.starId = s.id";
                ResultSet stars_rs = stars_statement.executeQuery(stars_query);
                
                JsonArray stars_array = new JsonArray();
                while(stars_rs.next())
                {
                	JsonObject star_object = new JsonObject();
                	String star_id = stars_rs.getString("id");
                	String star_name = stars_rs.getString("name");
                	star_object.addProperty("star_id", star_id);
                	star_object.addProperty("star_name", star_name);
                	stars_array.add(star_object);
                }
                jsonObject.add("stars", stars_array);
                
                jsonObject.addProperty("movie_rating", movie_rating);
                
                jsonArray.add(jsonObject);
            }
            
            // write JSON string to output
            out.write(jsonArray.toString());
//            // set response status to 200 (OK)
            response.setStatus(200);


            
            
            rs.close();

//            // TODO if you do create new statements, close them

            statement.close();
            dbcon.close();
        } catch (Exception e) {
        	
			// write error message JSON object to output
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			out.write(jsonObject.toString());

			// set reponse status to 500 (Internal Server Error)
			response.setStatus(500);

        }
        out.close();

    }
}
