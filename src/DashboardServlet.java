import com.google.gson.JsonObject;

import javax.annotation.Resource;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;

/**
 * This IndexServlet is declared in the web annotation below, 
 * which is mapped to the URL pattern /api/index.
 */
@WebServlet(name = "DashboardServlet", urlPatterns = "/api/_dashboard")
public class DashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

	// Create a dataSource which registered in web.xml
	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource; 
    
    /**
     * handles POST requests to store session information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
    		throws IOException {
    	
    	response.setContentType("application/json");
        HttpSession session = request.getSession();
        String sessionId = session.getId();
        Long lastAccessTime = session.getLastAccessedTime();
        
        Enumeration <String> en = session.getAttributeNames();
        while(en.hasMoreElements()) {
        	String name = en.nextElement();
            System.out.println(session.getAttribute(name));
        }

        JsonObject responseJsonObject = new JsonObject();
        responseJsonObject.addProperty("sessionID", sessionId);
        responseJsonObject.addProperty("lastAccessTime", new Date(lastAccessTime).toString());

        // write all the data into the jsonObject
        response.getWriter().write(responseJsonObject.toString());
    }
}
    /**
     * handles GET requests to add and show the item list information
     */
//    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
//    		throws IOException {
//    	
//        String item = request.getParameter("item");
//        System.out.println(item);
//        HttpSession session = request.getSession();
//
//        // get the previous items in a ArrayList
//        ArrayList<String> previousItems = (ArrayList<String>) session.getAttribute("previousItems");
//        if (previousItems == null) {
//            previousItems = new ArrayList<>();
//            previousItems.add(item);
//            session.setAttribute("previousItems", previousItems);
//        } else {
//            // prevent corrupted states through sharing under multi-threads
//            // will only be executed by one thread at a time
//            synchronized (previousItems) {
//                previousItems.add(item);
//            }
//        }
//
//        response.getWriter().write(String.join(",", previousItems));
//    }
//}
