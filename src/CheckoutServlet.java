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
 * Servlet implementation class CheckoutServlet
 */
@WebServlet(name = "/CheckoutServlet", urlPatterns = "/api/checkout")
public class CheckoutServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CheckoutServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println("You shouldn't be here: doGet @CheckoutServlet");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("application/json");
		
		// Get parameters from request
		String first_name = request.getParameter("first-name");
		String last_name = request.getParameter("last-name");
		String credit_card_num = request.getParameter("card-number");
		String exp_date = request.getParameter("exp-date");
		
		System.out.println("CheckoutServlet: first_name: " + first_name);
		System.out.println("CheckoutServlet: last_name: " + last_name);
		System.out.println("CheckoutServlet: credit_card_num: " + credit_card_num);
		System.out.println("CheckoutServlet: exp_date: " + exp_date);
		
		HttpSession session = request.getSession();
		ArrayList<Integer> sales_id_array = new ArrayList<Integer>();
		
		PrintWriter sout = response.getWriter();
		
		String rsFName = "";
		String rsLName = "";
		String rsccNum = "";
		Date rsDate = new Date();
		
		
		try
		{
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
			// Get credit card num that matches client cc num from db
			//dbcon = dataSource.getConnection();
        	// check username and password against the database
        	String query = "SELECT cus.firstName, cus.lastName, cc.id, cc.expiration FROM customers AS cus, creditcards AS cc WHERE cus.ccID = cc.id AND cc.id = ?;";
          
        	System.out.println("CheckoutServlet: preparing query");
        	
        	PreparedStatement statement = dbcon.prepareStatement(query);
        	statement.setString(1, credit_card_num);
          
        	ResultSet rs = statement.executeQuery();
          
        	while(rs.next()) 
	        {
				System.out.println("CheckoutServlet: getting matching cc info");
				// Putting values into db variables
				rsFName = rs.getString("firstName");
				rsLName = rs.getString("lastName");
				rsccNum = rs.getString("id");
				rsDate = rs.getDate("expiration");
				System.out.println("CheckoutServlet: rsFName - " + rsFName);
				System.out.println("CheckoutServlet: rsLName - " + rsLName);
				System.out.println("CheckoutServlet: rsccNum - " + rsccNum);
				System.out.println("CheckoutServlet: rsDate - " + rsDate.toString());
			}
          
        	response.setStatus(200);
          
        	rs.close();
        	statement.close();
          
        	System.out.println("CheckoutServlet: closing SQL connections");
		
        	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        	Date exp_d = new Date();
		
        	exp_d = df.parse(exp_date);
	
			System.out.println("CheckoutServlet: rsFName = first_name:" + Boolean.toString(rsFName.equals(first_name)));
			System.out.println("CheckoutServlet: rsLName = last_name:" + Boolean.toString(rsLName.equals(last_name)));
			System.out.println("CheckoutServlet: rsccNum = credit_card_num: " + Boolean.toString(rsccNum.equals(credit_card_num)));
			System.out.println("CheckoutServlet: rsDate = exp_d: " + Integer.toString(rsDate.compareTo(exp_d)));
			
			// Checking if it the client cc info matches db cc info
			if ((rsFName.equals(first_name) && rsLName.equals(last_name)) && (rsccNum.equals(credit_card_num) && (rsDate.compareTo(exp_d) == 0)))
			{
				
				System.out.println("CheckoutServlet: Credit card info matches");
			
				JsonArray jsonArray = new JsonArray();
				
				// if it does, send success message to js
				JsonObject responseJsonObject = new JsonObject();
				responseJsonObject.addProperty("status", "success");
	            responseJsonObject.addProperty("message", "success");
	            //jsonArray.add(responseJsonObject);
	            
	            // getting cart from session
	            
	            HashMap<String, Integer> cart = (HashMap<String, Integer>)session.getAttribute("previousItems");
	            if (cart != null)
	            {
	            	// cart is NOT empty
	            	System.out.println("CheckoutServlet: Items exist in the cart. Let's add them into the database.");
	            	
	            	Iterator<Map.Entry<String, Integer>> it = cart.entrySet().iterator();
	            	
	            	// iterate thru cart
	            	while (it.hasNext())
	            	{
	            		// Getting the movie_id of current cart item
	            		System.out.println("CheckoutServlet: Iterating thru cart");
	            		Map.Entry<String, Integer> cur_movie = (Map.Entry<String, Integer>)it.next();
	            		System.out.println("CheckoutServlet: Current movie is: " + cur_movie.getKey().toString());
	
	            		// Get username from User
	            		User cur_user = (User)session.getAttribute("user");
	            		System.out.println("CheckoutServlet: User to look for: " + cur_user.getUsername());
	            		
	            		System.out.println("CheckoutServlet: Preparing user query");
	            		
	            		// Getting customer ID from db
	            		String user_query = "SELECT customers.id \r\n" + "FROM customers \r\n" + "WHERE customers.email = ?;";
	            		PreparedStatement user_statement = dbcon.prepareStatement(user_query);
	            		
	            		user_statement.setString(1, cur_user.getUsername());
	            		
	            		ResultSet u_rs = user_statement.executeQuery();
	            		u_rs.next();
	            		int customer_id = u_rs.getInt("id");
	            		System.out.println("CheckoutServlet: Customer id get: " + Integer.toString(customer_id));
	            		
	            		u_rs.close();
	            		user_statement.close();
	            		
	            		// Inserting current sale of current movie into DB
	            		System.out.println("CheckoutServlet: Preparing insert statement");
	            		String insert_query = "INSERT INTO sales (customerId, movieId, saleDate, qty) values (?, ?, ?, ?);";
	            		PreparedStatement insert_statement = dbcon.prepareStatement(insert_query, Statement.RETURN_GENERATED_KEYS);
	            		
	            		insert_statement.setInt(1, customer_id);
	            		insert_statement.setString(2, cur_movie.getKey().toString());
	            		insert_statement.setDate(3, java.sql.Date.valueOf(java.time.LocalDate.now()));
	            		insert_statement.setInt(4, cur_movie.getValue());
	            		
	    				int rows_affected = insert_statement.executeUpdate();
	    				
	    				ResultSet i_rs = insert_statement.getGeneratedKeys();
	    				i_rs.next();
	    				// This hopefully provides the sales ID of the current sale
	    				int sales_id = i_rs.getInt(1);
	    				
	    				System.out.println("CheckoutServlet: # of Rows affected: " + Integer.toString(rows_affected));
	    				System.out.println("CheckoutServlet: Sales ID: " + Integer.toString(sales_id));
	            		
	    				// Adding sales info into json object & adding it to array
	    				JsonObject sales_data = new JsonObject();
	    				sales_data.addProperty("sales_id",  sales_id);
	    				sales_data.addProperty("movie_id", cur_movie.getKey().toString());
	    				sales_data.addProperty("quantity", cur_movie.getValue().toString());
	    				sales_id_array.add(sales_id);
	    				//jsonArray.add(sales_data);
	    				
	    				i_rs.close();
	    				insert_statement.close();
	            	}
	            	
	            	// You're done getting all the cart items and putting it in the sales table
	            	dbcon.close();
	    	        System.out.println("CheckoutServlet: Writing JSON: " + jsonArray.toString());
	    	        System.out.println("CheckoutServlet: Adding Sales ID to session: " + sales_id_array.toString());
	    	        sout.write(responseJsonObject.toString());
	    	        session.setAttribute("sales_id_array", sales_id_array);
	    	        // set response status to 200 (OK)
	    	        response.setStatus(200);
	            }
	            else
	            {
	            	// cart is empty
	            	System.out.println("CheckoutServlet: There are no items to checkout. Failure.");
	            	JsonObject responseObject = new JsonObject();
	                responseObject.addProperty("status", "fail");
	                responseObject.addProperty("message", "incorrect information");
	                response.getWriter().write(responseObject.toString());
	            }
			}
			else
			{
				// Credit card info is incorrect
				System.out.println("CheckoutServlet: Credit card info does NOT match.");
				
				JsonObject responseObject = new JsonObject();
	            responseObject.addProperty("status", "fail");
	            if (!rsccNum.equals(credit_card_num)) 
	            {
	                responseObject.addProperty("message", "credit card num " + credit_card_num + " doesn't exist");
	            } 
	            else 
	            {
	                responseObject.addProperty("message", "incorrect information");
	            }
	            response.getWriter().write(responseObject.toString());
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

}

