

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

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

import java.lang.*;

/**
 * Servlet implementation class ShoppingCartServlet
 */
@WebServlet(name = "/ShoppingCartServlet", urlPatterns ="/api/shopping-cart")
public class ShoppingCartServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource; 
    /**
     * @see HttpServlet#HttpServlet()
     */
//    public ShoppingCartServlet() {
//        super();
//        // TODO Auto-generated constructor stub
//    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException 
    {
    	response.setContentType("application/json");

        HttpSession session = request.getSession();
        
        String movie_id = request.getParameter("movie_id");
        String quantity = request.getParameter("q");
        String action = request.getParameter("action");
        
        System.out.println("CartServlet: in Shopping Cart Servlet");
        // Movie_id ONLY exists when action == "add", "update", or "delete"
        // If it's missing, then action is also missing, AND you just want the JSON of the shopping cart
        System.out.println("Movie_id: " + movie_id + "!!!");
        
        // Quantity only exists when action == update
        System.out.println("Quantity: " + quantity + "!!!");
        
        // Action should only exists when a movie_id exists
        // No specified action means that the client just wants the JSON data of the cart
        System.out.println("Action: " + action + "!!!");
        

        PrintWriter sout = response.getWriter();
        JsonArray jsonArray = new JsonArray();
        
        //Map<String,Integer> synchronizedHashMap = Collections.synchronizedMap(new HashMap<String, Integer>());
        //ArrayList<String> previousItems = (ArrayLists<String>) session.getAttribute("previousItems");
        //Map<String, Integer> previousItems = Collections.synchronizedMap((Map<String, Integer>) session.getAttribute("previousItems"));
        
        System.out.println("CartServlet: getting cart");
        HashMap<String, Integer> cart = (HashMap<String, Integer>)session.getAttribute("previousItems");
        
        // If the cart does not exist in the session 
        try
        {
	        if (cart == null) 
	        {
	        	System.out.println("CartServlet: Cart is empty");
	        	// Create a new cart
	            cart = new HashMap<String, Integer>();
	            // If you're adding a movie & making the cart for the first time
	            if (movie_id != null)
	            {
	            	System.out.println("CartServlet: Adding item " + movie_id +" to empty Cart");
	            	// Add the movie to the HashMap
	            	// !! Add synchronization?
	            	cart.put(movie_id, 1);
	            }
	            //previousItems.add(item);
	            // Even if you didn't add a movie, you'lll add it to the session
	            System.out.println("CartServlet: Empty cart added to attributes"); 
	            session.setAttribute("previousItems", cart);
	        } 
	        
	        // If a cart exists
	        else 
	        {
	        	System.out.println("CartServlet: Cart exists in session");
	//            synchronized (previousItems) {
	        	
	        	// if you're adding an item to an existing cart\
	        	if  ((movie_id == null) && (action == null))
	        	{
	        		System.out.println("CartServlet: Action and movie_id are null. Just print out existing cart info");
	        	}
	        	else if ((movie_id != null) && (action == null))
	        	{
	        		System.out.println("CartServlet: Adding item" + movie_id+ " to existing cart AND there is no action specified");
	        		if (cart.containsKey(movie_id))
	        		{
	        			System.out.println("CartServlet: Movie exists, so increment.");
	        			cart.put(movie_id, cart.get(movie_id) + 1);
	        		}
	        		else
	        		{
	        			System.out.println("CartServlet: movie DOES NOT exists, so leave as 1");
	        			cart.put(movie_id, 1);
	        		}
	        	}
	        	else if (action.equals("add"))
	        	{
	        		System.out.println("CartServlet: Adding item" + movie_id+ " to existing cart");
	                cart.put(movie_id, 1);
	        	}
	        	// if you are updating an item to an existing cart
	        	else if (action.equals("Update"))
	        	{
	        		System.out.println("CartServlet: Updating item" + movie_id+ " in existing cart");
	        		
	        		// If user wants to update it to 0, remove movie
	        		if (Integer.valueOf(quantity) == 0)
	        		{
	        			System.out.println("CartServlet: Quantity = " + quantity + ", removing movie "  + movie_id);
	        			cart.remove(movie_id);
	        		}
	        		else
	        		{
	        			// Update movie quantity
	        			System.out.println("CartServlet: Quantity = " + quantity + ", updating movie "+ movie_id);
	        			cart.put(movie_id, Integer.valueOf(quantity));
	        		}
	        	}
	        	// Remove the movie
	        	else if (action.equals("Remove"))
	        	{
	        		System.out.println("CartServlet: Removing movie " + movie_id);
	        		cart.remove(movie_id);
	        	}
	        	else
	        	{
	        		System.out.println("CartServlet: Action is null/nonexistant");
	        	}
	        }
	        
	        // Any sort of manipulation of the cart has been done, so make the json
	        System.out.println("CartServlet: Iterating thru HashMap");
	        
	        // Make an iterator to iterate thru cart
	        // Helps with any weird manipulation that occurs while iterating
	        Iterator<Map.Entry<String, Integer>> it = cart.entrySet().iterator();
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
	        while (it.hasNext()) 
	        {
	        	
	        	System.out.println("CartServlet: Preparing query...");
	        	
	        	// Get this current movie
	        	Map.Entry<String, Integer> cur_movie = (Map.Entry<String, Integer>)it.next();
	        	
	        	System.out.println("CartServlet: Current movie is: " + cur_movie.getKey().toString());
	        	// Just get the movie title
	        	String query = "SELECT m.title \r\n" +
						"FROM movies as m \r\n" + 
						"WHERE m.id = '" + cur_movie.getKey().toString() + "' \r\n" +
						"GROUP BY m.title;";
				
				PreparedStatement statement = dbcon.prepareStatement(query);
				ResultSet rs = statement.executeQuery();
				
				rs.next();
				JsonObject jsonObject = new JsonObject();
				
				String m_title = rs.getString("title");
				
				jsonObject.addProperty("movie_id", cur_movie.getKey().toString());
				jsonObject.addProperty("movie_title", m_title);
				jsonObject.addProperty("quantity", cur_movie.getValue().toString());
				
				jsonArray.add(jsonObject);
				
				rs.close();
		        statement.close();
	        }
	        dbcon.close();
	        System.out.println("CartServlet: Writing JSON: " + jsonArray.toString());
	        sout.write(jsonArray.toString());
//          // set response status to 200 (OK)
	        response.setStatus(200);
        }
        catch (Exception e)
        {
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
        sout.close();
    }
    
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
		System.out.print("ShoppingCartServlet: POST");
	}

}

