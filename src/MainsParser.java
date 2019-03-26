import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MainsParser extends DefaultHandler {

	@Resource(name = "jdbc/moviedb")
	private DataSource dataSource;
	
	//List<Movie> myMovies; // List of all movies found in XML file (!!!!!is maybe redundant w/ movieSet)
	HashMap<String, String> catToName; // HashMap for (short cat name, long category name) key-value pair
	HashMap<String, Integer> genreMap; // HashMap for (genre_id, genre_name) pair
	
	HashSet<Movie> movieSet; // HashSET for all movies encountered
	
	private String tempVal; // Used in characters
	private Movie tempMovie;
	private String tempDir;
	private ArrayList<String> tempCats; // ArrayList of all categories in particular movie
	
	private Boolean hasDirName; // True if first director name found. Helps remove dupe dir names
	private Boolean hasFilmName;
	private Boolean hasFilmYear;
	
	private Boolean isDupe; // True if movie found to be dupe. stops parsing for more info about the movie and skips to next movie
	
	FileWriter fileWriter; // Writes to results file that states any occurance of a duplicate movie
	FileWriter genre_writer;
	FileWriter everything;
	
	int cur_movie_id;
	int max_id;
	
	public MainsParser()
	{
		// Default constructor
		
		//myMovies = new ArrayList<Movie>();
		catToName = new HashMap<String, String>();
		
		movieSet = new HashSet<Movie>();
		
		hasDirName = false;
		hasFilmName = false;
		hasFilmYear = false;
		
		isDupe = false;
		
		this.fillCatToName();
		this.createGenreMap();
		
		fileWriter = null;
		genre_writer = null;
		everything = null;
		
		cur_movie_id = -1;
		max_id = -1;
	}
	
	/**
	 * Creates HashMap catToName for categories lookup
	 * Key: 4-letter category name
	 * Value: Full category name
	 */
	private void fillCatToName()
	{
		// Creates HashMap to convert short cat names to full category names
		// TODO consider to move this into Movie.java to set as final static HashMap
		String[] cats = {"Ctxx", "Actn", "Advt", "AvGa", "Camp", "Cart", "CnR", "Comd", "Disa", "Docu", "Dram", "Epic", "Faml", "Hist", "Horr", "Musc", "Myst", "Noir", "Porn", "Romt", "ScFi", "Surl", "Susp", "West", "Biop", "Cnrb", "Fant", "Biopp", "Sxfi", "Biob", "Surr", "Kinky", "tvmini", "ducu", "romt comd"};
		
		String[] categories = {"Uncategorized", "Action", "Adventure", "Avant Garde", "Camp", "Cartoon", "Crime", "Comedy", "Disaster", "Documentary", "Drama", "Epic", "Family", "History", "Horror", "Musical", "Mystery", "Black", "Adult", "Romance", "Sci-Fi", "Sureal", "Thriller", "Western", "Biographical Picture", "Crime", "Fantasy", "Biographical Picture", "Sci-Fi", "Biographical Picture", "Sureal", "Adult", "TV Miniseries", "Documentary", "Romantic Comedy"};
	
		for (int i = 0; i < cats.length; i++)
		{
			catToName.put(cats[i].toLowerCase(), categories[i]);
		}
		
		System.out.println("Creating categories HashMap...");
		//System.out.println(catToName.toString());
	}
	
	/**
	 * Creates HashMape genreMap 
	 * Key: genre_name
	 * Value: genre_id
	 */
	private void createGenreMap()
	{
		// Function to create a HashMap of (genre_name, genre_id) pairs 
		// Pairs are obtained from the database
		 
		// Declares genreMap 
		genreMap = new HashMap<String, Integer>();
		
		try
		{
			// Connect to database 
			Class.forName("com.mysql.jdbc.Driver");

			Connection dbcon = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?useSSL=false",
                    "mytestuser", "mypassword");

					//dataSource.getConnection("root", "test123");
			
			// Create query to obtain all genre_id and genre_name pairs
			String genres_query = "SELECT * FROM genres;";
			PreparedStatement genres_statement = dbcon.prepareStatement(genres_query);
			ResultSet genres_rs = genres_statement.executeQuery();
			
			while (genres_rs.next())
			{
				// Insert each genre_name, genre_id pair into genreMap
				genreMap.put(genres_rs.getString("name"), genres_rs.getInt("id"));
			}
			
			genres_rs.close();
			dbcon.close();
		}
		catch (SQLException e)
		{
			System.out.println("SQL Error:");
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		//System.out.println(genreMap.toString());
	}
	
	/**
	 * Creates a new movie_id based on the "maximum" movie_id in the database
	 * Will obtain the max id in the db first, then increment that number w/o adding it into the db
	 * Ex: if Max(id) = "tt12345", then result = "tt12346"
	 * @return String movie id
	 */
	private String getMovieId()
	{		
		// NOTE: Movies are defined as Movie(title, year, dir). It's OK if it has the same name as another movie
		//System.out.println("getMovieId: Creating new movie_id");

		String max_str_id = "";
		if (max_id == -1)
		{
			// If the max_id has not been found
			try
			{
				//Connection dbcon = dataSource.getConnection();
				// Connect to database 
				Class.forName("com.mysql.jdbc.Driver");

				Connection dbcon = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?useSSL=false",
						"mytestuser","mypassword");

				
				// Connect to DB to obtain the "max" id currently in DB
				String movie_id_query = "SELECT MAX(id) AS max_id FROM movies;";
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
				max_id = Integer.valueOf(max_str_id.replaceAll("t", "")) + 1;
				
				//System.out.println("New id: " + "tt"+Integer.toString(max_id));
				return "tt"+Integer.toString(max_id);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		// If a max_id has been obtained, create a new id from the current max id
		max_id += 1;
		return "tt" + Integer.toString(max_id);
	}
	
	/**
	 * Generated a list of genre ids for the current tempMovie and appends it to tempMovie's list of genre ids
	 * First looks up the conversion from 4-letter cat to full-word category. 
	 * Then checks to see if it is in the db. If it isn't then a new genre_id is created. If it is, then the genre_id is appended.
	 */
	private void getGenreIds()
	{
		// Iterating through all genres
		//System.out.println("getGenreIds: Start iterating thru all cats");
		//System.out.println("getGenreIds: length of cats = " + Integer.toString(tempMovie.getCats().size()));
		
		ArrayList<String> new_categories = new ArrayList<String>();
		ArrayList<Integer> new_genre_ids = new ArrayList<Integer>();
		for (String cat : tempMovie.getCats())
		{
			String category = catToName.get(cat.toLowerCase());
			if (category == null)
			{
				category = "Uncategorized";
			}
			//System.out.println("HERE: cat = " + cat + ", category = " + category);
			new_categories.add(category);
			
			if (genreMap.containsKey(category))
			{
				// category exists within database; no need to insert it into database
				// Add genre_id into ArrayList
				new_genre_ids.add(genreMap.get(category));
			}
			
			else
			{
				// genre DOES NOT exist within genreMap AND database
				// create new genre_id and add it to db and genreMap
				
				int new_genre_id = -1;
				// Connecting to databases to get and insert genre_id
				try
				{
					// Connect to database 
					Class.forName("com.mysql.jdbc.Driver");

					Connection dbcon = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?useSSL=false",
							"mytestuser","mypassword");

					//Connection dbcon = dataSource.getConnection();
					
					// Create query to obtain all genre_id and genre_name pairs
					String genre_id_query = "INSERT INTO genres (`id`, name) VALUES (NULL, ?);";
					
					PreparedStatement genre_id_statement = dbcon.prepareStatement(genre_id_query, Statement.RETURN_GENERATED_KEYS);
					
					genre_id_statement.setString(1, category);
					
					genre_id_statement.executeUpdate();
					ResultSet genre_id_rs = genre_id_statement.getGeneratedKeys();
					
					while (genre_id_rs.next())
					{
						new_genre_id = genre_id_rs.getInt(1) + 1;
					}
					
					genre_id_rs.close();

					dbcon.close();
				}
				
				catch (Exception e)
				{
					e.printStackTrace();
					System.out.println("Error: " + tempMovie.getCats().toString());
					System.out.println(cat);
				}
				
				// Now need to insert into genre_map
				
				//System.out.println("getGenreIds: new genre_id created = " + Integer.toString(new_genre_id));
				
				genreMap.put(category, new_genre_id);
				new_genre_ids.add(new_genre_id);
			}

		}
		
		tempMovie.appendToCategories(new_categories);
		tempMovie.appendToGenre_ids(new_genre_ids);
		
		//System.out.println("getGenreIds: genre_ids : " + tempMovie.getGenre_ids().toString() + " added");
	}
	
	private void parseMovieDoc()
	{
		SAXParserFactory spf = SAXParserFactory.newInstance();
		
		try
		{
			SAXParser sp = spf.newSAXParser();
			
			//sp.parse("mains243.xml", this);
			
			File file = new File("mains243.xml");
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
	
	private void makeDataFiles()
	{
		// Make two different files that SQL will accept and load
		
		// File 1 will insert every Movie(id, title, year, director) into Movies
		
		try
		{
			fileWriter = new FileWriter("insert_movies.txt", true);
			genre_writer = new FileWriter("insert_genres_in_movies.txt", true);
			Iterator<Movie> it = movieSet.iterator();
		    while(it.hasNext())
		    {
		    	Movie cur_movie= it.next();
		    	fileWriter.write(cur_movie.toString("movies"));
		    	genre_writer.write(cur_movie.toString("genres_in_movies"));
		    	try
				{
					everything = new FileWriter("log.txt", true);
					everything.write(cur_movie.toString());
				}
				catch (Exception e)
				{
					e.printStackTrace();
					everything.close();
				}
		    	//System.out.println(cur_movie.toString());
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
				try {
					fileWriter.close();
					genre_writer.close();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
			}
		}
		
		
		// File 2 will insert every Genre_in_movies(genre_id, movie_id) into Genres_in_Movies
	}
	
	private void insertIntoDB()
	{
       
        try 
        {
        	int[] movieNoRows=null;
        	int[] genreNoRows=null;
        	Class.forName("com.mysql.jdbc.Driver");

    		Connection dbcon = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?useSSL=false&rewriteBatchedStatements=true",
    				"mytestuser","mypassword");

    		
    		PreparedStatement movieInsertRecord=null;
    		PreparedStatement genreInsertRecord=null;
    		String movieInsertQuery=null;
            String genreInsertQuery = null;
            
            movieInsertQuery="insert into movies (id, title, year, director) values(?,?,?,?)";
            genreInsertQuery="insert into genres_in_movies(genreId, movieId) values (?,?)";
			dbcon.setAutoCommit(false);

            movieInsertRecord = dbcon.prepareStatement(movieInsertQuery);
            genreInsertRecord = dbcon.prepareStatement(genreInsertQuery);

            System.out.println("!!!!!!!!!SIZE: " + Integer.toString(movieSet.size()));
            Iterator<Movie> it = movieSet.iterator();
            while(it.hasNext())
            {
            	Movie cur_movie = it.next();
            	movieInsertRecord.setString(1, cur_movie.getMovie_id());
            	movieInsertRecord.setString(2, cur_movie.getTitle());
            	movieInsertRecord.setInt(3, cur_movie.getYear());
            	movieInsertRecord.setString(4, cur_movie.getDirector());
            	movieInsertRecord.addBatch();
            	
            	for (int genre: cur_movie.getGenre_ids())
            	{
            		genreInsertRecord.setInt(1, genre);
            		genreInsertRecord.setString(2, cur_movie.getMovie_id());
            		genreInsertRecord.addBatch();
            	}
            }

			movieNoRows=movieInsertRecord.executeBatch();
			genreNoRows=genreInsertRecord.executeBatch();
			dbcon.commit();
			
			if(movieInsertRecord!=null) 
			{
				movieInsertRecord.close();
			}
			if(genreInsertRecord!=null) 
			{
				genreInsertRecord.close();
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
	
	
	// ====================Event handlers======================
	
	public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
        // Could try a StringBuffer if finding characters being cut off
    }
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException 
	{
		tempVal = "";

		if (qName.equalsIgnoreCase("Film"))
		{
			// Reached first instance of a film
			//System.out.println("Film encountered");
			this.tempMovie = new Movie();
			
			// Implies that dirname has been obtained
			// Therefore sets the tempDir as tempMovie's director
			this.tempMovie.setDirector(tempDir);
			//System.out.println("Director encountered: " + tempMovie.getDirector());
		}
		
		if (qName.equalsIgnoreCase("t"))
		{
			// Reached first instance of a film
								
		}
		
		// If this is the start of a categories tag
		if (qName.equalsIgnoreCase("cats"))
		{
			tempCats = new ArrayList<String>();
		}
	}
	
	public void endElement(String uri, String localName, String qName) throws SAXException 
	{
		
		// End of a dirname tag AND is the first instance of a dirName
		if (!hasDirName & qName.equalsIgnoreCase("dirname"))
		{
			// DirName has not been obtained, so set is as the tempDir
			tempDir = tempVal;
			
			// Indicates if an dirName has been obtained
			// Parser will skip additional dirnames
			hasDirName =  true;
			
		}
		
		// End of a title tag
		if (qName.equalsIgnoreCase("t"))
		{
			// Set the temp title as tempMovie's title
			String cur_val = tempVal.trim();
			if (cur_val.isEmpty())
			{
				cur_val = "Unknown Title";
			}
			//System.out.println(cur_val);
			this.tempMovie.setTitle(cur_val);
		}
		
		// End of a year tag
		if (qName.equalsIgnoreCase("year"))
		{
			// Set the temp year as tempMovie's year
			// Also convert the string int to an int
			try {
				tempMovie.setYear(Integer.parseInt(tempVal.trim()));
			} catch (NumberFormatException e) {
				tempMovie.setYear(0);
			}
		}
		
		if (qName.equalsIgnoreCase("cats"))
		{
			tempMovie.appendToCats(tempCats);
		}
		
		// End of a cat tag
		if (qName.equalsIgnoreCase("cat"))
		{
			// Add the cat value to 
			tempCats.add(tempVal.trim().toLowerCase());
		}
		
		// If this is the END of a movie/film tag
		if (qName.equalsIgnoreCase("film"))
		{
			// Get/find movie_id & set it into tempMovie
			String movie_id = getMovieId();
			tempMovie.setMovie_id(movie_id);
			
			// For all genres, get their genre_ids
			getGenreIds();

			// Check to see if movie exists in db
			String cur_movie_id = "";
			try
			{
				// Connect to database 
				Class.forName("com.mysql.jdbc.Driver");

				Connection dbcon = DriverManager.getConnection("jdbc:mysql://localhost:3306/moviedb?useSSL=false",
						"mytestuser", "mypassword");

				//Connection dbcon = dataSource.getConnection();
				
				
				// Create query to obtain all genre_id and genre_name pairs
				String dupe_id_query = "SELECT movies.id, movies.title, movies.year, movies.director FROM movies WHERE movies.title = ? AND movies.year = ? AND movies.director = ? LIMIT 1;";
				
				PreparedStatement dupe_id_statement = dbcon.prepareStatement(dupe_id_query);
				
				dupe_id_statement.setString(1, tempMovie.getTitle());
				dupe_id_statement.setInt(2, tempMovie.getYear());
				dupe_id_statement.setString(3, tempMovie.getDirector());

				ResultSet dupe_id_rs = dupe_id_statement.executeQuery();
				//ResultSet dupe_id_rs = dupe_id_statement.getGeneratedKeys();
				
				while (dupe_id_rs.next())
				{
					cur_movie_id = dupe_id_rs.getString("id");
				}
				
				dupe_id_rs.close();

				dbcon.close();
			}
			
			catch (Exception e)
			{
				e.printStackTrace();
				
			}
			
			
			if (movieSet.contains(tempMovie) || !cur_movie_id.isEmpty())
			{
				System.out.println("Duplicate movie found: Movie title: " + tempMovie.getTitle() + ", year: " + Integer.toString(tempMovie.getYear()) + ", Director: " + tempMovie.getDirector() + ", (Opt) movie_id: " + cur_movie_id);
				try 
				{
					FileWriter dupe_file = new FileWriter("dupe_results.txt", true);
					dupe_file.write(tempMovie.toString("movies"));
					dupe_file.close();
				} catch (IOException e) 
				{
						e.printStackTrace();
						
				}
			}
			
			else
			{
				// It is not in movieSet, so add it
				movieSet.add(tempMovie);
			}			
		}
		
		// End of directorfilms tag
		if (qName.equalsIgnoreCase("directorfilms"))
		{
			// Reset tempDir and hasDirName for next director
			tempDir = "";
			hasDirName = false;
		}
		
	}
	
	public static void main(String[] args) 
	{
		System.out.println("Starting MainsParser.java............");
		MainsParser sax_parser = new MainsParser();
		
		sax_parser.parseMovieDoc();
		System.out.println("Starting to make SQL data files.........");
		sax_parser.makeDataFiles();
		sax_parser.insertIntoDB();
		
		// End of program. Allows user to see through console before program terminates
		Scanner reader = new Scanner(System.in);  
		System.out.println("Finished making files. Duplicates have been written to the console and to file \"result.txt\"");
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
