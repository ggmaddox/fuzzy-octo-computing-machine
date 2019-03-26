package com.example.fabflix;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//import org.json.JSONException;
////import org.json.JSONObject;

public class SearchResultsActivity extends AppCompatActivity {

    ArrayList<Movie> movies;
    String query;
    int page_num = 1;
    int display_num = 10;


    MovieListViewAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);
        movies = new ArrayList<Movie>();

        page_num = 1;
        display_num = 10;

        // Get query
        Bundle bundle = getIntent().getExtras();
        query = bundle.getString("query");

        try
        {
            page_num = bundle.getInt("page_num");
        }
        catch (NullPointerException n_e)
        {
            page_num = 1;
        }

        if (page_num == 0)
        {
            page_num = 1;
        }



        Button prevButton = (Button) findViewById(R.id.button_prev);
        if (page_num <= 1)
        {
            prevButton.setVisibility(View.INVISIBLE);
            Log.d("SearchResultsAct.prevButton", "prevButton is disabled, page num is <= 1");
        }



        // Send it to tomcat server & SearchServlet
        // Will fill in movies;
        Log.d("SearchResultsActivity.onCreate.pageNum", Integer.toString(page_num));
        Log.d("SearchResultsActivity.onCreate.displayNum", Integer.toString(display_num));
        connectToTomcat(query);
        Log.d("SearchResultsAct.onCreate", "Finished connecting to tomcat");

        adapter = new MovieListViewAdapter(movies, this);
        Log.d("SearchResultsAct.onCreate", "Finished making an apdapter");
        ListView listView = (ListView)findViewById(R.id.list);
        listView.setAdapter(adapter);
        Log.d("SearchResultsAct.onCreate", "Adapter set up");
        adapter.notifyDataSetChanged();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie cur_movie = movies.get(position);
                String message = String.format("Clicked on position: %d, name: %s, %d", position, cur_movie.getMovie_title(), cur_movie.getMovie_year());
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                Log.d("SearchResultsAct.onClick", "Movie made: "+ cur_movie.toString());

                Intent goToIntent = new Intent(getApplicationContext(), SingleMovieActivity.class);

                // Send query to next Intent
                goToIntent.putExtra("movie_title", cur_movie.getMovie_title());
                goToIntent.putExtra("movie_year", Integer.toString(cur_movie.getMovie_year()));
                goToIntent.putExtra("movie_director", cur_movie.getMovie_director());
                goToIntent.putExtra("movie_cast", cur_movie.getStars());
                goToIntent.putExtra("movie_genres", cur_movie.getGenres());

                // Switch to next Intent
                startActivity(goToIntent);
            }
        });



    }


    public void connectToTomcat(final String query) {
        Log.d("SearchResultsAct.Tomcat", "Starting tomcat funct");
        // Happens on click to email_login_button

        // no user is logged in, so we must connect to the server

        // Use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        // local: 10.0.2.2
        final StringRequest searchRequest = new StringRequest(Request.Method.GET, "https://192.168.1.16:8443/fabflix/api/search-results",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.d("SearchResultsResponse", response);
                        try
                        {

                            JSONArray json_response = new JSONArray(response);
                            Log.d("JSON response:", json_response.toString());
                            if (json_response.toString().isEmpty())
                            {
                                // If there's no response
                                ((TextView) findViewById(R.id.error_message)).setText("No results found");
                            }
                            else
                            {
                                // Iterate thru Json to populate
                                movies.clear();
                                int len  = json_response.length();
                                for (int i = 0; i < len; i++)
                                {
                                    Movie cur_movie = new Movie();
                                    JSONObject cur_json_object= json_response.getJSONObject(i);

                                    String movie_id = cur_json_object.getString("movie_id");
                                    String movie_title = cur_json_object.getString("movie_title");
                                    int movie_year = cur_json_object.getInt("movie_year");
                                    String movie_director = cur_json_object.getString("movie_director");

                                    cur_movie.setMovie_id(movie_id);
                                    cur_movie.setMovie_title(movie_title);
                                    cur_movie.setMovie_year(movie_year);
                                    cur_movie.setMovie_director(movie_director);
                                    Log.d("SearchResultsAct.onResponse", "Movie: " + cur_movie.toString());

                                    JSONArray genres = cur_json_object.getJSONArray("genres");
                                    for (int j = 0; j < genres.length(); j++)
                                    {
                                        // TODO you need to get the json object for the genre that you're on
                                        // THEN you get the genre id and name
                                        JSONObject cur_genre = genres.getJSONObject(j);
                                        String genre_id = cur_genre.getString("genre_id");
                                        String genre_name = cur_genre.getString("genre_name");

                                        cur_movie.addToGenreIds(Integer.valueOf(genre_id));
                                        cur_movie.addToGenres(genre_name);
                                    }

                                    JSONArray stars = cur_json_object.getJSONArray("stars");
                                    for (int k = 0; k < stars.length(); k++)
                                    {
                                        JSONObject cur_star = stars.getJSONObject(k);
                                        String star_id = cur_star.getString("star_id");
                                        String star_name = cur_star.getString("star_name");

                                        cur_movie.addToStarIds(star_id);
                                        cur_movie.addToStars(star_name);
                                    }
                                    movies.add(cur_movie);

                                    adapter.notifyDataSetChanged();


                                }
                            }
                        }
                        catch (Exception j_e)
                        {
                            j_e.printStackTrace();
                        }
                        Log.d("SearchResultsAct", "Done w/ parsing JSON data");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("search.error", error.toString());
                        //((TextView) findViewById(R.id.error_message)).setText(error.toString());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                // Post request form data
                Log.d("SearchResultsAct.Tomcat.getHeaders", "making Headers");
                final Map<String, String> params = new HashMap<String, String>();
                // This should send the email and password to the login servlet
                params.put("search_term", query);
                params.put("page-num", Integer.toString(page_num));
                params.put("display-num", Integer.toString(display_num));
                //params.put("password", mPasswordView.getText().toString());
                Log.d("getParams.page_num", Integer.toString(page_num));
                Log.d("getParams.display_num", Integer.toString(display_num));
                Log.d("getParams!!!!!!!!!!!!!!!!!!", params.toString());
                return params;
            }
        };

        // !important: queue.add is where the login request is actually sent
        Log.d("SearchResultsActivity.connectToTomcat.pageNum", Integer.toString(page_num));
        Log.d("SearchResultsActivity.connectToTomcat", Integer.toString(display_num));
        queue.add(searchRequest);

    }


    public void getNextPage(View view)
    {
        Intent goToIntent = new Intent(this, SearchResultsActivity.class);

        goToIntent.putExtra("query", query);
        goToIntent.putExtra("page_num", page_num + 1);
        goToIntent.putExtra("display_num", display_num);

        startActivity(goToIntent);
    }

    public void getPrevPage(View view)
    {
        Intent goToIntent = new Intent(this, SearchResultsActivity.class);

        goToIntent.putExtra("query", query);
        goToIntent.putExtra("page_num", page_num - 1);
        goToIntent.putExtra("display_num", display_num);

        startActivity(goToIntent);
    }
}
