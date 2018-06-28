/**
 * Kah Hau Yap
 *
 * @DisplayHistory.java
 * Activity launches from the previous history activity after the user selects a date to view.
 * Access the SQLite database to get nutritional information on the foods the user recorded that day.
 * The foods eaten are displayed in a ListView under the summary of Calories and nutrients consumed
 * that day. The page is a snapshot of the user's DailyPage for that date.
 */

package kah.fict;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;

public class DisplayHistory extends AppCompatActivity {

    private DataBaseHelper db;
    private TextView valueGoal;
    private TextView valueFood;
    private TextView valueRemaining;
    private TextView valueProtein;
    private TextView valueFat;
    private TextView valueCarbs;
    private TextView valueWeight;

    private ListView foodList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_history);
        ActionBar actionBar = getSupportActionBar(); //Set back button on title bar
        actionBar.setDisplayHomeAsUpEnabled(true);

        foodList = (ListView) findViewById(R.id.foodList);
        valueGoal = (TextView) findViewById(R.id.valueGoal);
        valueFood = (TextView) findViewById(R.id.valueFood);
        valueRemaining = (TextView) findViewById(R.id.valueRemaining);
        valueProtein = (TextView) findViewById(R.id.valueProtein);
        valueFat = (TextView) findViewById(R.id.valueFat);
        valueCarbs = (TextView) findViewById(R.id.valueCarbs);
        valueWeight = (TextView) findViewById(R.id.valueWeight);

        db = new DataBaseHelper(this); //Create a new database helper

        //Initialize an intent to get the date from the previous History activity
        Intent intent = getIntent();
        String date = intent.getStringExtra("date");
        setTitle("Summary for " + date); //Set title for page

        //Create a daily database entry if there isn't one already
        if (!db.dateExists(date))
            db.addEntry(date);

        //Get the calorie goal and daily values to display
        int defaultCalories = db.getCalories("0");
        int foodCalories = db.getCalories(date);
        int caloriesLeft = defaultCalories - foodCalories;

        //Set the values for calories
        valueGoal.setText(String.valueOf(defaultCalories));
        valueFood.setText(String.valueOf(foodCalories));
        valueRemaining.setText(String.valueOf(caloriesLeft));
        if (caloriesLeft < 0) //If negative calories left set to red, otherwise green
            valueRemaining.setTextColor(Color.RED);
        else
            valueRemaining.setTextColor(Color.GREEN);

        //Get the nutrition goals from the database
        int defaultProtein, defaultFat, defaultCarbs, defaultWeight;
        defaultProtein = db.getProtein("0");
        defaultFat = db.getFat("0");
        defaultCarbs = db.getCarbs("0");

        //Get the nutrition for the current date
        int dailyProtein, dailyFat, dailyCarbs, dailyWeight;
        dailyProtein = db.getProtein(date);
        dailyFat = db.getFat(date);
        dailyCarbs = db.getCarbs(date);
        dailyWeight = db.getWeight(date);

        //Set the daily nutritional values
        valueProtein.setText(dailyProtein + " / " + defaultProtein + " g");
        valueFat.setText(dailyFat + " / " + defaultFat + " g");
        valueCarbs.setText(dailyCarbs + " / " + defaultCarbs + " g");
        valueWeight.setText(dailyWeight + " lbs");

        //If negative nutrition left set to red, otherwise green
        if (dailyProtein > defaultProtein) valueProtein.setTextColor(Color.RED);
        else valueProtein.setTextColor(Color.GREEN);

        if (dailyFat > defaultFat) valueFat.setTextColor(Color.RED);
        else valueFat.setTextColor(Color.GREEN);

        if (dailyCarbs > defaultCarbs) valueCarbs.setTextColor(Color.RED);
        else valueCarbs.setTextColor(Color.GREEN);

        /* Get food entries for the day and display in a ListView */
        //Create an ArrayList to store the food entries added for the date
        final ArrayList<DataBaseHelper.Food> history = db.getFoodHistory(date);

        //Create an ArrayList with the names of the foods because we can't create an ArrayAdapter with ArrayList of object Food
        ArrayList<String> foods = new ArrayList<String>();
        for (int i=0; i<history.size(); i++){
            foods.add(history.get(i).name);
        }

        //Bind the ArrayList to an Array Adapter for display
        ArrayAdapter<String> foodAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, foods);
        foodList.setAdapter(foodAdapter);

        foodList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String selectedItem = history.get(position).name;

                //Create an alert message that displays nutrition information and asks to save the entry
                AlertDialog.Builder builder;
                //Determine which alert dialog builder based on API version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    builder = new AlertDialog.Builder(DisplayHistory.this, android.R.style.Theme_Material_Dialog_Alert);
                else
                    builder = new AlertDialog.Builder(DisplayHistory.this);

                //Build the dialog message
                builder.setTitle("Nutrition for " + selectedItem)
                        .setIcon(R.drawable.hamburger)
                        .setMessage("Calories: " + String.valueOf(history.get(position).calories)
                                + "\nProtein: " + String.valueOf(history.get(position).protein)
                                + " g\nFat: " + String.valueOf(history.get(position).fat)
                                + " g\nCarbohydrates: " + String.valueOf(history.get(position).carbs)
                                + " g" )
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Do nothing, close window
                            }
                        }).show();
            }
        });

    }
    //Go back to the previous activity on back arrow press
    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), History.class);
        startActivityForResult(myIntent, 0);
        return true;
    }
}

