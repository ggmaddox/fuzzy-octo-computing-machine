

public class Star 
{
	private String star_name;
	private String star_id;
	
	private String movie_name;
	private String movie_id;
	
	private String director;
	
	public Star()
	{
		this.star_name = "";
		this.star_id = "";
		this.movie_name = "";
		this.movie_id = "";
		this.movie_id = "";
		this.director = "";
	}

	public Star(String star_name, String star_id, String movie_name, String movie_id, String director) {
		this.star_name = star_name;
		this.star_id = star_id;
		this.movie_name = movie_name;
		this.movie_id = movie_id;
		this.director = director;
	}

	public String getStar_name() {
		return star_name;
	}

	public void setStar_name(String star_name) {
		this.star_name = star_name;
	}

	public String getStar_id() {
		return star_id;
	}

	public void setStar_id(String star_id) {
		this.star_id = star_id;
	}

	public String getMovie_name() {
		return movie_name;
	}

	public void setMovie_name(String movie_name) {
		this.movie_name = movie_name;
	}

	public String getMovie_id() {
		return movie_id;
	}

	public void setMovie_id(String movie_id) {
		this.movie_id = movie_id;
	}

	public String getDirector() {
		return director;
	}

	public void setDirector(String director) {
		this.director = director;
	}

	@Override
	public String toString() {
		return "Star [star_name=" + star_name + ", star_id=" + star_id + ", movie_name=" + movie_name + ", movie_id="
				+ movie_id + "]";
	}

	public String toString(String table)
	{
		if (table.compareTo("stars_in_movies") == 0)
		{
			return star_id+ "," + movie_id + "\n";
		}
		return star_name+","+movie_name + "\n";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 7;
		result = prime * result + ((movie_id == null) ? 0 : movie_id.hashCode());
		result = prime * result + ((star_id == null) ? 0 : star_id.hashCode());
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
		Star other = (Star) obj;
		if (movie_id == null) {
			if (other.movie_id != null)
				return false;
		} else if (!movie_id.equals(other.movie_id))
			return false;
		if (star_id == null) {
			if (other.star_id != null)
				return false;
		} else if (!star_id.equals(other.star_id))
			return false;
		return true;
	}
}
