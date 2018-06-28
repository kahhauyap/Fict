/**
 * Kah Hau Yap
 *
 * @History.java
 * An activity page that displays in a ListView format previous entries listed by dates of the user
 * to help track progress and look back on meals for that day to see if goals are being met.
 * Upon selected a date, it will lead to another activity which displays more detailed information.
 */

package kah.fict;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;

public class History extends AppCompatActivity {

    private DataBaseHelper db;
    private ListView historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        setTitle("History");
        ActionBar actionBar = getSupportActionBar(); //Set back button on the title bar
        actionBar.setDisplayHomeAsUpEnabled(true);

        historyList = (ListView) findViewById(R.id.historyList);

        db = new DataBaseHelper(this); //Create a new database helper to access the history items

        //Create an ArrayList to store the dates entries were created
        ArrayList<String> dates = new ArrayList<String>();
        dates = db.getDateHistory(); //Retrieve all the dates that have been recorded in the database for entries
        dates.remove(0); //Delete first index because the first date recorded is "0" which is not a valid entry, but used to store Goal values

        //Bind the ArrayList to an Array Adapter for display
        ArrayAdapter<String> historyAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dates);
        historyList.setAdapter(historyAdapter);


        //Upon user click start another activity and pass in the date in order to display more detailed information
        historyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String selectedItem = (String) parent.getItemAtPosition(position); //Get the item at the position selected
                //Create a new intent to pass date to next activity to fetch information
                Intent intent = new Intent(History.this, DisplayHistory.class);
                intent.putExtra("date", selectedItem);
                startActivity(intent); //Start activity and pass in the date
            }
        });
    }

    //Go back to the previous activity on back arrow press
    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), DailyPage.class);
        startActivityForResult(myIntent, 0);
        return true;
    }
}
