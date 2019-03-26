package com.example.fabflix;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class IndexActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Creation of Index Activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
    }

    public void onSearchButtonPress(final View view)
    {
        // Get the query inputted into text_search_bar (search bar)
        String query = ((EditText) findViewById(R.id.text_search_bar)).getText().toString();

        // Set the next Intent
        Intent goToIntent = new Intent(this, SearchResultsActivity.class);

        // Send query to next Intent
        goToIntent.putExtra("query", query);

        // Switch to next Intent
        startActivity(goToIntent);
    }
}
