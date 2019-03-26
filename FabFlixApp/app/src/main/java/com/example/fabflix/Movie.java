package com.example.fabflix;

import java.util.ArrayList;

public class Movie {

    private String movie_id;
    private String movie_title;
    private int movie_year;
    private String movie_director;
    private ArrayList<String> stars; // List of star names
    private ArrayList<String> star_ids;
    private ArrayList<String> genres; // List of genre names
    private ArrayList<Integer> genre_ids;

    public Movie()
    {
        movie_id = "";
        movie_title = "";
        movie_year = 0;
        movie_director = "";
        stars = new ArrayList<String>();
        star_ids = new ArrayList<String>();
        genres = new ArrayList<String>();
        genre_ids = new ArrayList<Integer>();
    }

    public Movie(String movie_id, String movie_title, int movie_year, String movie_director)
    {
        this.movie_id = movie_id;
        this.movie_title = movie_title;
        this.movie_year = movie_year;
        this.movie_director = movie_director;
    }

    public String getMovie_id() {
        return movie_id;
    }

    public void setMovie_id(String movie_id) {
        this.movie_id = movie_id;
    }

    public String getMovie_title() {
        return movie_title;
    }

    public void setMovie_title(String movie_title) {
        this.movie_title = movie_title;
    }

    public int getMovie_year() {
        return movie_year;
    }

    public void setMovie_year(int movie_year) {
        this.movie_year = movie_year;
    }

    public String getMovie_director() {
        return movie_director;
    }

    public void setMovie_director(String movie_director) {
        this.movie_director = movie_director;
    }

    public ArrayList<String> getStars() {
        return stars;
    }

    public ArrayList<String> getStar_ids() {
        return star_ids;
    }

    public ArrayList<String> getGenres() {
        return genres;
    }

    public ArrayList<Integer> getGenre_ids() {
        return genre_ids;
    }

    public void addToStars(String star)
    {
        stars.add(star);
    }

    public void addToStarIds(String star_id)
    {
        star_ids.add(star_id);
    }

    public void addToGenres(String genre)
    {
        genres.add(genre);
    }

    public void addToGenreIds(int genre_id)
    {
        genre_ids.add(genre_id);
    }

    public String genresToString()
    {
        String result = "";
        if (genres.size() == 1)
        {
            return genres.get(0);
        }

        for (int i = 0; i < genres.size()-1; i++)
        {
            result += genres.get(i) + ", ";
        }
        result += genres.get(genres.size()-1);

        return result;
    }

    public String starsToString()
    {
        String result = "";
        if (stars.size() == 1)
        {
            return stars.get(0);
        }

        for (int i = 0; i < stars.size()-1; i++)
        {
            result += stars.get(i) + ", ";
        }
        result += stars.get(stars.size()-1);

        return result;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "movie_id='" + movie_id + '\'' +
                ", movie_title='" + movie_title + '\'' +
                ", movie_year=" + movie_year +
                ", movie_director='" + movie_director + '\'' +
                '}';
    }
}
