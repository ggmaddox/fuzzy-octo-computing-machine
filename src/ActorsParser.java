import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ActorsParser extends DefaultHandler
{
	// Attributes
	HashSet<Actor> actorSet;
	
	private String tempVal; // Used in characters
	private Actor tempActor;
	private String tempName;
	private int tempYear;
	
	private int max_id;
	
	FileWriter dupe_file;
	FileWriter fileWriter;
	
	public ActorsParser()
	{
		actorSet = new HashSet<Actor>();
		
		dupe_file = null;
		fileWriter = null;
		
		max_id = -1;
	}
	
	public String getStarId()
	{
		if (max_id == -1)
		{
			try
			{
				//Connection dbcon = dataSource.getConnection();
				String max_str_id = "";
				
				Class.forName("com.mysql.jdbc.Driver");

				Connection dbcon = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?useSSL=false",
						"mytestuser","mypassword");

				
				// Connect to DB to obtain the "max" id currently in DB
				String movie_id_query = "SELECT MAX(id) AS max_id FROM stars;";
				PreparedStatement movie_id_statement = dbcon.prepareStatement(movie_id_query);
							
				ResultSet movie_id_rs = movie_id_statement.executeQuery();
				
				while (movie_id_rs.next())
				{
					max_str_id = movie_id_rs.getString("max_id");
					//System.out.println("getMovieId: max_str_id is: " + max_str_id);
				}
				
				movie_id_rs.close();
				dbcon.close();
				
				// Removes the "tt" part of the movie_id and sets this as the max id
				// Since we want to return a new max id, increases this by 1 and returns it
				max_id = Integer.valueOf(max_str_id.replaceAll("nm", "")) + 1;
				
				//System.out.println("New id: " + "tt"+Integer.toString(max_id));
				return "nm"+Integer.toString(max_id);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		max_id +=1;
		return "nm"+Integer.toString(max_id);
	}
	
	private void parseMovieDoc()
	{
		SAXParserFactory spf = SAXParserFactory.newInstance();
		
		try
		{
			SAXParser sp = spf.newSAXParser();
			
			//sp.parse("mains243.xml", this);
			
			File file = new File("actors63.xml");
  	      	InputStream inputStream= new FileInputStream(file);
  	      	Reader reader = new InputStreamReader(inputStream,"ISO-8859-1");
  	      
  	      	InputSource is = new InputSource(reader);
  	      	is.setEncoding("ISO-8859-1");
  	      
  	      	sp.parse(is, this);
  	      
		}
		 catch (SAXException se) {
	        se.printStackTrace();
	    } catch (ParserConfigurationException pce) {
	        pce.printStackTrace();
	    } catch (IOException ie) {
	        ie.printStackTrace();
	    }
	}
	
	public void makeDataFiles()
	{
		try
		{
			fileWriter = new FileWriter("insert_stars.txt", true);
			//genre_writer = new FileWriter("insert_genres_in_movies.txt", true);
			Iterator<Actor> it = actorSet.iterator();
		    while(it.hasNext())
		    {
		    	Actor cur_movie= it.next();
		    	fileWriter.write(cur_movie.toString("movies"));
		    }
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (fileWriter != null)
			{
				try 
				{
					fileWriter.close();	
				} 
				catch (IOException e) 
				{
					
					e.printStackTrace();
				}
			}
		}
	} 	
	
	private void insertIntoDB()
	{
        try 
        {
        	int[] actorNoRows=null;
        	
        	Class.forName("com.mysql.jdbc.Driver");

        	Connection dbcon = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?useSSL=false&rewriteBatchedStatements=true",
    				"mytestuser","mypassword");

    		
    		PreparedStatement actorInsertRecord=null;
    		
    		String actorInsertQuery=null;
            
            
            actorInsertQuery="insert into stars (id, name, birthYear) values(?,?,?)";
            
			dbcon.setAutoCommit(false);

            actorInsertRecord = dbcon.prepareStatement(actorInsertQuery);
            

            System.out.println("!!!!!!!!!SIZE: " + Integer.toString(actorSet.size()));
            Iterator<Actor> it = actorSet.iterator();
            while(it.hasNext())
            {
            	Actor curActor = it.next();
            	actorInsertRecord.setString(1, curActor.getStar_id());
            	actorInsertRecord.setString(2, curActor.getName());
            	actorInsertRecord.setInt(3, curActor.getBirth_year());
            	
            	actorInsertRecord.addBatch();
      
            }

			actorNoRows=actorInsertRecord.executeBatch();
			dbcon.commit();
			
			if(actorInsertRecord!=null) 
			{
				actorInsertRecord.close();
			}

            if(dbcon!=null) 
            {
            	dbcon.close();
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        catch (Exception e)
        {
        	e.printStackTrace();
        	
        }
	}
	
	
	// ================== Event Handlers ================== 
	public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
        // Could try a StringBuffer if finding characters being cut off
    }
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException 
	{
		tempVal = "";
		if (qName.equalsIgnoreCase("actor"))
		{
			tempActor = new Actor();
		}
	}
	
	public void endElement(String uri, String localName, String qName) throws SAXException 
	{
		
		if (qName.equalsIgnoreCase("stagename"))
		{
			String cur_val = tempVal.trim();
			if (cur_val.isEmpty())
			{
				cur_val = "Unknown Title";
			}
			//System.out.println(cur_val);
			this.tempActor.setName(cur_val);
		}
		
		if (qName.equalsIgnoreCase("dob"))
		{
			// Set the temp year as tempMovie's year
			// Also convert the string int to an int
			try {
				tempActor.setBirth_year(Integer.parseInt(tempVal.trim()));
			} catch (NumberFormatException e) {
				tempActor.setBirth_year(0);
			}
		}
		if (qName.equalsIgnoreCase("actor"))
		{
			// Get stars_id
			String star_id = getStarId();
			tempActor.setStar_id(star_id);
			
			String cur_star_id = "";
			try
			{
				// Connect to database 
				Class.forName("com.mysql.jdbc.Driver");

				Connection dbcon = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?useSSL=false",
						"mytestuser","mypassword");

				//Connection dbcon = dataSource.getConnection();
				
				
				// Create query to obtain all genre_id and genre_name pairs
				String dupe_id_query = "SELECT stars.id, stars.name, stars.birthYear FROM stars WHERE stars.name = ? AND stars.birthYear = ? LIMIT 1;";
				
				PreparedStatement dupe_id_statement = dbcon.prepareStatement(dupe_id_query);
				
				dupe_id_statement.setString(1, tempActor.getName());
				dupe_id_statement.setInt(2, tempActor.getBirth_year());

				ResultSet dupe_id_rs = dupe_id_statement.executeQuery();
				//ResultSet dupe_id_rs = dupe_id_statement.getGeneratedKeys();
				
				while (dupe_id_rs.next())
				{
					cur_star_id = dupe_id_rs.getString("id");
				}
				
				dupe_id_rs.close();

				dbcon.close();
			}
			
			catch (Exception e)
			{
				e.printStackTrace();
				
			}
			
			
			if (actorSet.contains(tempActor) || !cur_star_id.isEmpty())
			{
				System.out.println("Duplicate Actor found: " + tempActor.toString());
//				try 
//				{
//					dupe_file = new FileWriter("dupe_actor_results.txt", true);
//					dupe_file.write(tempActor.toString() + ", star_id found: " + cur_star_id);
//					dupe_file.close();
//				} 
//				catch (IOException e) 
//				{
//						e.printStackTrace();
//						try {
//							dupe_file.close();
//						} catch (IOException e1) {
//							
//							e1.printStackTrace();
//						}
//				}
				
			}
			
			else
			{
				// It is not in movieSet, so add it
				actorSet.add(tempActor);
			}
		}
	}
	
	public static void main(String[] args) 
	{
		System.out.println("Starting ActorsParser...");
		ActorsParser sax_parser = new ActorsParser();
		
		sax_parser.parseMovieDoc();
		System.out.println("Starting to make Data Files...");
		sax_parser.makeDataFiles();
		
		sax_parser.insertIntoDB();
		
		Scanner reader = new Scanner(System.in);  
		System.out.println("Finished making files. Duplicates have been written to the console and to file \"dupe_actor_results.txt\"");
		String n = "";
		do
			{
			// Prompts for "q" to quit
			System.out.println("Type q to quit");
			n = reader.nextLine(); // Scans the next token of the input as an int.
			}
		while (n.compareTo("q") != 0);
		// Closes prompt
		reader.close();
	}
	
}
