

public class Genre {

	private String name; // equivalent name in database
	private String cat; // category as defined in http://infolab.stanford.edu/pub/movies/doc.html#CATS
	
	public Genre()
	{
		
	}
	
	public Genre(String cat)
	{
		this.cat = cat;
	}
	
	public Genre(String name, String cat)
	{
		this.name = name;
		this.cat = cat;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCat() {
		return cat;
	}

	public void setCat(String cat) {
		this.cat = cat;
	}

	@Override
	public String toString() {
		return "Genre [name=" + name + ", cat=" + cat + "]";
	}
}
