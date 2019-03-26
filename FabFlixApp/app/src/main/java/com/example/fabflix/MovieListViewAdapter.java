package com.example.fabflix;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MovieListViewAdapter extends ArrayAdapter<Movie>
{
    private ArrayList<Movie> movies;

    public MovieListViewAdapter(ArrayList<Movie> movies, Context context) {
        super(context, R.layout.layout_listview_row, movies);
        this.movies = movies;

        Log.d("MovieListViewAdapter", movies.toString());
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Log.d("MovieListAdapter", "Starting getView");
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.layout_listview_row, parent, false);

        Movie cur_movie = movies.get(position);
        Log.d("MovieListAdapter", "Got position " + Integer.toString(position));
        TextView titleView = (TextView) view.findViewById(R.id.text_movie_title);
        TextView yearView = (TextView) view.findViewById(R.id.text_movie_year);
        TextView directorView = (TextView) view.findViewById(R.id.text_movie_director);
        TextView starsView = (TextView) view.findViewById(R.id.text_movie_stars);
        TextView genresView = (TextView) view.findViewById(R.id.text_genres);

        Log.d("SearchResultsAct.Tomcat", "Finished w/ getting text views. Now setting textS");
        titleView.setText(cur_movie.getMovie_title());
        yearView.setText(Integer.toString(cur_movie.getMovie_year()));
        directorView.setText(cur_movie.getMovie_director());
        starsView.setText(cur_movie.starsToString());
        genresView.setText(cur_movie.genresToString());
        Log.d("SearchResultsAct.Tomcat", "End of setting text");
        return view;
    }
}

