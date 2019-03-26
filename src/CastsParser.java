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

public class CastsParser extends DefaultHandler
{
	// Attributes
	HashSet<Star> starSet;
	
	private String tempVal; // Used in characters
	private Star tempStar;
	private String tempMovieName;
	private String tempMovieId;
	private String tempDirector;
	
	private Boolean haveMovieName;
	private Boolean haveMovieId;
	private Boolean noMovieFound;
	private Boolean noStarFound;
	
	FileWriter dupe_casts_file;
	FileWriter insert_file;
	
	public CastsParser()
	{
		starSet = new HashSet<Star>();
		
		dupe_casts_file = null;
		insert_file = null;
		
		tempMovieName = "";
		tempMovieId = "";
		tempDirector = "";
		
		haveMovieName = false;
		haveMovieId = false;
		noMovieFound = false;
		noStarFound = false;
	}
	
	public void getStarId()
	{
		// Unlike other get___Id() methods, 
		// This is looking up whether the star_id exists 
		String curStarId = "";
		try
		{
			// Get ID of existing star via tempStar's data
			
			//Connection dbcon = dataSource.getConnection();
			
			Class.forName("com.mysql.jdbc.Driver");

			Connection dbcon = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?useSSL=false",
					"mytestuser","mypassword");


			// Connect to DB to obtain the "max" id currently in DB
			String star_id_query = "SELECT * FROM stars WHERE stars.name = ? LIMIT 1;";
			PreparedStatement star_id_statement = dbcon.prepareStatement(star_id_query);

			star_id_statement.setString(1, tempStar.getStar_name());
			ResultSet star_id_rs = star_id_statement.executeQuery();

			while (star_id_rs.next())
			{
				// If it exists, will return is a non-empty string
				curStarId = star_id_rs.getString("id");
			}

			star_id_rs.close();
			dbcon.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (curStarId.isEmpty())
		{
			// if no Star was found, set bool flag
			noStarFound = true;
		}
		else
		{
			// if Star was found, set tempStar's Star ID to the id found
			tempStar.setStar_id(curStarId);
		}
	}
	
	public void getMovieId()
	{
		// Look for a possibly existing movie id
		String curMovieId = "";
		try
		{
			//Connection dbcon = dataSource.getConnection();
			
			Class.forName("com.mysql.jdbc.Driver");

			Connection dbcon = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?useSSL=false",
					"mytestuser","mypassword");


			// Gets the first matching movie w/ equal title and director
			String movie_id_query = "SELECT * FROM movies WHERE movies.title = ? AND movies.director  = ? LIMIT 1;";
			PreparedStatement movie_id_statement = dbcon.prepareStatement(movie_id_query);

			movie_id_statement.setString(1, tempStar.getMovie_name());
			movie_id_statement.setString(2, tempDirector);
			
			ResultSet movie_id_rs = movie_id_statement.executeQuery();

			while (movie_id_rs.next())
			{
				// If no movie exists, then curMovieId will be empty
				curMovieId = movie_id_rs.getString("id");
			}

			movie_id_rs.close();
			dbcon.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		if (curMovieId.isEmpty())
		{
			// If no movie exists, set bool flag
			noMovieFound = true;
		}
		else
		{
			tempMovieId = curMovieId;
			haveMovieId = true;
		}
	}
	
	private void parseMovieDoc()
	{
		SAXParserFactory spf = SAXParserFactory.newInstance();
		
		try
		{
			SAXParser sp = spf.newSAXParser();
			
			//sp.parse("mains243.xml", this);
			
			File file = new File("casts124.xml");
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
			insert_file = new FileWriter("insert_stars_in_movies.txt", true);
			//genre_writer = new FileWriter("insert_genres_in_movies.txt", true);
			Iterator<Star> it = starSet.iterator();
		    while(it.hasNext())
		    {
		    	Star cur_star= it.next();
		    	insert_file.write(cur_star.toString("stars_in_movies"));
		    }
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (insert_file != null)
			{
				try 
				{
					insert_file.close();	
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
        	int[] castNoRows=null;
        	
        	Class.forName("com.mysql.jdbc.Driver");

    		Connection dbcon = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?useSSL=false&rewriteBatchedStatements=true",
    				"mytestuser","mypassword");

    		
    		PreparedStatement castInsertRecord=null;
    		
    		String castInsertQuery=null;
            
            
            castInsertQuery="insert into stars_in_movies (starId, movieId) values(?,?)";
            
			dbcon.setAutoCommit(false);

            castInsertRecord = dbcon.prepareStatement(castInsertQuery);
            

            System.out.println("!!!!!!!!!SIZE: " + Integer.toString(starSet.size()));
            Iterator<Star> it = starSet.iterator();
            while(it.hasNext())
            {
            	Star curStar = it.next();
            	castInsertRecord.setString(1, curStar.getStar_id());
            	castInsertRecord.setString(2, curStar.getMovie_id());
            	
            	
            	castInsertRecord.addBatch();
      
            }

			castNoRows=castInsertRecord.executeBatch();
			dbcon.commit();
			
			if(castInsertRecord!=null) 
			{
				castInsertRecord.close();
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
		if (qName.equalsIgnoreCase("filmc"))
		{
			haveMovieName = false;
			haveMovieId = false;
			noMovieFound = false;
			tempMovieName = "";
			tempMovieId = "";
		}
		
		if (qName.equalsIgnoreCase("m"))
		{
			tempStar = new Star();
			tempStar.setDirector(tempDirector);
			noStarFound = false;
			if (haveMovieName)
			{
				tempStar.setMovie_name(tempMovieName);
			}
			if (haveMovieId)
			{
				tempStar.setMovie_id(tempMovieId);
			}
		}
	}
	
	public void endElement(String uri, String localName, String qName) throws SAXException 
	{
		
		if (qName.equalsIgnoreCase("is"))
		{
			// Get director name
			tempDirector = tempVal.trim();
		}
		
		if (qName.equalsIgnoreCase("t") && !haveMovieName)
		{
			// If we encounter the title and HAVE NOT obtained title
			// Should encounter once per filmc
			String cur_val = tempVal.trim();
			if (cur_val.isEmpty())
			{
				cur_val = "Unknown Title";
			}
			//System.out.println(cur_val);
			this.tempStar.setMovie_name(cur_val);
			
			
			haveMovieName = true;
			tempMovieName = cur_val;
		}
		
		if (qName.equalsIgnoreCase("a"))
		{
			// Set the current actor name
			tempStar.setStar_name(tempVal.trim());
		}
		
		if (qName.equalsIgnoreCase("m"))
		{
			// At the end of a current star
			
			getStarId(); // If successful, will set star_id; If not, will raise noStarFound flag
			
			
			if (!haveMovieId && !noMovieFound)
			{
				// If a movie id does not exist, get it
				getMovieId(); // If successful, will set tempMovieId and raise haveMovieId flag; If not, will raise noMovieFound flag
				
			}

				// If a movie_id exists, set tempStar's movie id
			tempStar.setMovie_id(tempMovieId);

			
			// If either the Star Id or the Movie Id has NOT been found, 
			if (noStarFound || noMovieFound)
			{
				// Don't append it
				return;
			}
			
			// At this point, movie and star exist
			// So, check if (star_id, movie_id) pair exists in database
			String cur_sim_id = "";
			try
			{
				// Connect to database 
				Class.forName("com.mysql.jdbc.Driver");

				Connection dbcon = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?useSSL=false",
						"mytestuser","mypassword");

				//Connection dbcon = dataSource.getConnection();
				
				
				// Create query to check for existing star_id and movie_id pari
				String dupe_sim_query = "SELECT * FROM stars_in_movies WHERE stars_in_movies.starId = ? AND stars_in_movies.movieId = ? LIMIT 1;";
				
				PreparedStatement dupe_sim_statement = dbcon.prepareStatement(dupe_sim_query);
				
				dupe_sim_statement.setString(1, tempStar.getStar_id());
				dupe_sim_statement.setString(2, tempStar.getMovie_id());

				ResultSet dupe_sim_rs = dupe_sim_statement.executeQuery();
				//ResultSet dupe_id_rs = dupe_id_statement.getGeneratedKeys();
				
				while (dupe_sim_rs.next())
				{
					cur_sim_id = dupe_sim_rs.getString("starId");
				}
				
				dupe_sim_rs.close();

				dbcon.close();
			}
			
			catch (Exception e)
			{
				e.printStackTrace();
				
			}
			
			
			if (starSet.contains(tempStar) || !cur_sim_id.isEmpty())
			{
				System.out.println("Duplicate Star found: " + tempStar.toString());
//				try 
//				{
//					dupe_casts_file = new FileWriter("dupe_star_results.txt", true);
//					dupe_casts_file.write(tempStar.toString());
//					dupe_casts_file.close();
//				} 
//				catch (IOException e) 
//				{
//					e.printStackTrace();
//					try {
//						dupe_casts_file.close();
//					} catch (IOException e1) {
//						
//						e1.printStackTrace();
//					}
//				}	
			}
			else
			{
				// It is not in movieSet, so add it
				starSet.add(tempStar);
			}
		}
		
		if (qName.equalsIgnoreCase("dirfilms"))
		{
			// When you're at the end of this current director's films
			// Reset tempDirector
			tempDirector = "";
		}
	}
	


	public static void main(String[] args) 
	{
		System.out.println("Starting CastsParser...");
		CastsParser sax_parser = new CastsParser();
		
		sax_parser.parseMovieDoc();
		System.out.println("Starting to make Data Files...");
		sax_parser.makeDataFiles();
		sax_parser.insertIntoDB();
		
		Scanner reader = new Scanner(System.in);  
		System.out.println("Finished making files. Duplicates have been written to the console and to file \"dupe_star_results.txt\"");
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

