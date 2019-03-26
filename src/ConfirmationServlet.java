import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import javax.sql.DataSource;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.sql.*;
import java.text.SimpleDateFormat;
/**
 * Servlet implementation class ConfirmationServlet
 */
@WebServlet(name = "/ConfirmationServlet", urlPatterns = "/api/confirmation")
public class ConfirmationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ConfirmationServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession();
		PrintWriter sout = response.getWriter();
		response.setContentType("application/json");
		
		// Get the sales array from session
		System.out.println("ConfirmationServlet: getting sales array");
		ArrayList<Integer> sales_id_array = (ArrayList<Integer>)session.getAttribute("sales_id_array");
		
		System.out.println("ConfirmationServlet: Array: " + sales_id_array.toString());
		
		try
		{
			if (sales_id_array == null)
			{
				System.out.println("ConfirmationServlet: Sales array is null. How?");
			}
			else
			{
				// Starting to iterate thru sales array
				System.out.println("ConfirmationServlet: Sales array contains items");
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
				JsonArray jsonArray = new JsonArray();
				
				System.out.println("ConfirmationServlet: Iterating thru sales array");
				for (int sales_id : sales_id_array)
				{
					System.out.println("ConfirmationServlet: On " + Integer.toString(sales_id));
					String query = "SELECT * \r\n" +
				"FROM sales \r\n" +
							"WHERE sales.id = ?;";
					
					PreparedStatement sales_statement = dbcon.prepareStatement(query);
					
					sales_statement.setInt(1, sales_id);
					
					ResultSet s_rs = sales_statement.executeQuery();
					
					// Getting the result
					s_rs.next();
					
					int customer_id = s_rs.getInt("customerId");
					String movie_id = s_rs.getString("movieId");
					int count = s_rs.getInt("qty");
					
					System.out.println("ConfirmationServlet: Got sales info: ");
					System.out.println("ConfirmationServlet: cust_id: " + Integer.toString(customer_id));
					System.out.println("ConfirmationServlet: movie_id: " + movie_id);
					System.out.println("ConfirmationServlet: count: " + Integer.toString(count));
					JsonObject jsonObject = new JsonObject();
					s_rs.close();
					sales_statement.close();
					
					String movie_query = "SELECT movies.title, movies.id \r\n" +
							"FROM movies \r\n" +
							"WHERE movies.id = ?;";
								
					PreparedStatement movie_statement = dbcon.prepareStatement(movie_query);
					movie_statement.setString(1, movie_id);
					ResultSet m_rs = movie_statement.executeQuery();
					
					m_rs.next();
					String movie_title = m_rs.getString("title");
					
					jsonObject.addProperty("sales_id", sales_id);
					jsonObject.addProperty("movie_id", movie_id);
					jsonObject.addProperty("movie_title", movie_title);
					jsonObject.addProperty("count", count);
					
					jsonArray.add(jsonObject);
					
					
				}
				
				dbcon.close();
		        System.out.println("CartServlet: Done. Writing JSON: " + jsonArray.toString());
		        sout.write(jsonArray.toString());
	//          // set response status to 200 (OK)
		        session.removeAttribute("sales_id_array");
		        session.removeAttribute("previousItems");
		        if (null != session.getAttribute("sales_id_array"))
		        {
		        	System.out.println("ConfirmationServlet: Sales array somehow exists after being removed.......");
		        	System.out.println("ConfirmationServlet: array: " + session.getAttribute("sales_id_array").toString());
		        }
		        response.setStatus(200);
			}
			
			
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
		System.out.println("ConfirmationServlet: You're in POST. Should redirect to GET");
		doGet(request, response);
	}

}

