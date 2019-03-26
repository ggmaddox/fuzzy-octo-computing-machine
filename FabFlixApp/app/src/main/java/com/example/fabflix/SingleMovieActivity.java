package com.example.fabflix;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

public class SingleMovieActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_movie);

        Bundle bundle = getIntent().getExtras();
        String movie_title= bundle.getString("movie_title");
        String movie_year= bundle.getString("movie_year");
        String movie_director= bundle.getString("movie_director");
        ArrayList<String> movie_cast = bundle.getStringArrayList("movie_cast");
        ArrayList<String> movie_genres = bundle.getStringArrayList("movie_genres");


        TextView titleView = (TextView) findViewById(R.id.text_single_title);
        TextView yearView = (TextView) findViewById(R.id.text_single_year);
        TextView directorView = (TextView) findViewById(R.id.text_single_director);
        TextView starsView = (TextView) findViewById(R.id.text_single_cast);
        TextView genresView = (TextView) findViewById(R.id.text_single_genres);

        Log.d("SearchResultsAct.Tomcat", "Finished w/ getting text views. Now setting textS");
        titleView.setText(getString(R.string.default_movie_title) + movie_title);
        yearView.setText(getString(R.string.default_movie_year) + movie_year);
        directorView.setText(getString(R.string.default_director) + movie_director);

        String cast_string = "";
        if (movie_cast.size() == 1)
        {
            cast_string = movie_cast.get(0);
        }
        else
        {
            for (int i = 0; i < movie_cast.size()-1; i++)
            {
                cast_string+= movie_cast.get(i) + ", ";
            }
            cast_string+= movie_cast.get(movie_cast.size()-1);
        }

        String genres_string = "";
        if (movie_genres.size() == 1)
        {
            genres_string = movie_genres.get(0);
        }
        else
        {
            for (int j = 0; j < movie_genres.size()-1; j++)
            {
                genres_string+= movie_genres.get(j) + ", ";
            }
            genres_string+= movie_genres.get(movie_genres.size()-1);
        }

        starsView.setText(getString(R.string.default_cast) + cast_string);
        genresView.setText(getString(R.string.default_genres) + genres_string);

    }
}
