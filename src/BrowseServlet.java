// Include an interable title list from 0-9, A-Z
// And another for a list of genres w/ genre IDs

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Servlet implementation class BrowseServlet
 */
@WebServlet(name = "BrowseServlet", urlPatterns = "/api/browse")
//@WebServlet("/BrowseServlet")
public class BrowseServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	// Create a dataSource which registered in web.xml
    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public BrowseServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("application/json");
		
		PrintWriter nout = response.getWriter();

		try
		{
			Connection dbcon = dataSource.getConnection();
			//Statement statement = dbcon.createStatement();
			Statement genres_statement = dbcon.createStatement();
			
			String genres_query = "SELECT id, name FROM genres ORDER BY name";
			
			ResultSet gs = genres_statement.executeQuery(genres_query);
		
			JsonObject jsonObject = new JsonObject();
			JsonArray letters_array = new JsonArray();
			
			String[] characters = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
			for (String c: characters)
			{
				letters_array.add(c);
			}
			
			jsonObject.add("title_characters", letters_array);

			JsonArray genres_array = new JsonArray();
			
			while (gs.next())
			{
				int genre_id = gs.getInt("id");
				String genre_name = gs.getString("name");
				
				JsonObject genres_jsonObject = new JsonObject();
				genres_jsonObject.addProperty("genre_id", genre_id);
				genres_jsonObject.addProperty("genre_name", genre_name);
				genres_array.add(genres_jsonObject);
			}
			
			jsonObject.add("genres", genres_array);
			
			nout.write(jsonObject.toString());

            response.setStatus(200);
            gs.close();
            dbcon.close();
		}
		
		catch (Exception e)
		{
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("errorMessage", e.getMessage());
			nout.write(jsonObject.toString());
			
			response.setStatus(500);
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}


}

