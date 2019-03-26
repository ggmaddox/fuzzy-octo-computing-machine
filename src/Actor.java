/**
 * Actor class that is used primarily in ActorsParser.java
 * Similar to Star class, yet contains less than Star
 */

public class Actor 
{
	private String name;
	private int birth_year;
	private String star_id;
	
	public Actor()
	{
		this.name = "";
		this.birth_year = 0;
		this.star_id = "";
	}
	
	public Actor(String name, int year)
	{
		this.name = name;
		this.birth_year = year;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getBirth_year() {
		return birth_year;
	}

	public void setBirth_year(int birth_year) {
		this.birth_year = birth_year;
	}

	public String getStar_id() {
		return star_id;
	}

	public void setStar_id(String star_id) {
		this.star_id = star_id;
	}

	@Override
	public String toString() {
		return "Star [name=" + name + ", birth_year=" + birth_year + ", star_id=" + star_id + "]";
	}

	public String toString(String table)
	{
		// String made for being written into a file
		if (table.compareTo("stars")==0)
		{
			return name + "," + Integer.toString(birth_year) + "\n";
		}
		return name + ", year=" + Integer.toString(birth_year) + "\n";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 7;
		result = prime * result + Integer.hashCode(birth_year);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Actor other = (Actor) obj;
		if (birth_year != other.birth_year)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}

