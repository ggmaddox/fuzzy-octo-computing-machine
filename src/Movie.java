import java.util.ArrayList;

public class Movie {

	// Used to insert movie into database
	private String movie_id;
	
	// A movies consists of a title, year, and director
	// Any other info is helper info
	private String title;
	private int year;
	private String director;
	
	// ====ArrayLists for genres====
	
	// Array of full-word categories
	private ArrayList<String> categories; 
	// Array of 4-letter categories (as defined in Stanford xml)
	private ArrayList<String> cats;
	// Array of genre_ids
	private ArrayList<Integer> genre_ids;
	
	public Movie()
	{
		// Default constructor
		
		this.movie_id = "";
		
		this.title = "\\N";
		this.year = 0;
		this.director = "\\N";
		
		this.categories = new ArrayList<String>();
		this.cats = new ArrayList<String>();
		this.genre_ids = new ArrayList<Integer>();
	}
	
	public Movie(String title, int year, String director)
	{
		// Constructor for when all title, year, and director is known
		this.title = title;
		this.year = year;
		this.director = director;
	}
	
	// ===== Getters and Setters =====
	
	// Year
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	// Year
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	
	// Director
	public String getDirector() {
		return director;
	}
	public void setDirector(String director) {
		this.director = director;
	}

	// Movie_id
	public String getMovie_id() {
		return movie_id;
	}
	public void setMovie_id(String movie_id) {
		this.movie_id = movie_id;
	}

	// Categories
	public ArrayList<String> getCategories() {
		return categories;
	}
	public void appendToCategories(ArrayList<String> new_categories)
	{
		// Adds all items in new_categories into categories
		this.categories.addAll(new_categories);
	}
	
	// Cats
	public ArrayList<String> getCats() {
		return cats;
	}
	public void appendToCats(ArrayList<String> new_cats)
	{
		// Setter for cats
		// Adds all items in new_cats to cats
		this.cats.addAll(new_cats);
	}
	
	// Genre ids
	public ArrayList<Integer> getGenre_ids() {
		return genre_ids;
	}

	public void appendToGenre_ids(ArrayList<Integer> new_genre_ids) 
	{
		// Adds all items in new_genre_ids into genre_ids
		this.genre_ids.addAll(new_genre_ids);
	}

	// ===== toString for database insertion =====
	public String toString(String table) 
	{
		String result = "";
		if (table.equalsIgnoreCase("movies"))
		{
			// Creates LOAD DATA-friendly string for inserting into database
			result += movie_id + "," + title + "," + Integer.toString(year) + "," + director + "\n";
		}
		
		if (table.equalsIgnoreCase("genres_in_movies"))
		{
			for (int genre_id: this.genre_ids)
			{
				result += Integer.toString(genre_id) + "," + this.movie_id + "\n";
			}
		}
		return result;
		
	}

	@Override
	public String toString() {
		return "Movie [movie_id=" + movie_id + ", title=" + title + ", year=" + year + ", director=" + director
				+ ", categories=" + categories + ", cats=" + cats + ", genre_ids=" + genre_ids + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 7;
		result = prime * result + ((director == null) ? 0 : director.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + Integer.valueOf(year).hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Movie other = (Movie) obj;
		if (director == null) {
			if (other.director != null)
				return false;
		} else if (!director.equals(other.director))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (year != other.year)
			return false;
		return true;
	}

	
	
	
}
