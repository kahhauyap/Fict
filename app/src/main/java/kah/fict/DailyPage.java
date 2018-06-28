/**
 * Kah Hau Yap
 *
 * @DailyPage.java
 * The objective of this activity is to create a clean UI and practical page that displays important information,
 * and acts as a hub to all the other features.
 *
 * Activity launches from the previous history activity after the user selects a date to view.
 * Accesses the SQLite database to get nutritional information on the foods the user recorded that day.
 * The foods eaten are displayed in a ListView under the daily summary of Calories and nutrients consumed
 * that day. When the food item is selected, an alert dialog will display the nutritional values of
 * the food as well. On the bottom of the screen are three buttons: One to check your history of
 * entries, another one that brings you to the activity to save foods, and lastly one to allow you to
 * adjust goals.
 */

package kah.fict;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class DailyPage extends AppCompatActivity {

    private TextView valueGoal;
    private TextView valueFood;
    private TextView valueRemaining;
    private TextView valueProtein;
    private TextView valueFat;
    private TextView valueCarbs;
    private TextView valueWeight;

    private ListView foodList;

    private ImageButton weightButton;
    private String date;

    DataBaseHelper db;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.history:
                    startActivity(new Intent(DailyPage.this, History.class));
                    return true;
                case R.id.add_food:
                    addFoodDialogue(); //Display an alert dialog to give the user the option to take a picture, or manually input food
                    return true;
                case R.id.nutrition_calculator:
                    startActivity(new Intent(DailyPage.this, MacrosCalculator.class));
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_page);
        setTitle(getDate()); //Set page title
        ActionBar actionBar = getSupportActionBar(); //Set back button on the title bar
        actionBar.setDisplayHomeAsUpEnabled(true);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        foodList = (ListView) findViewById(R.id.foodList);

        valueGoal = (TextView) findViewById(R.id.valueGoal);
        valueFood = (TextView) findViewById(R.id.valueFood);
        valueRemaining = (TextView) findViewById(R.id.valueRemaining);
        valueProtein = (TextView) findViewById(R.id.valueProtein);
        valueFat = (TextView) findViewById(R.id.valueFat);
        valueCarbs = (TextView) findViewById(R.id.valueCarbs);
        valueWeight = (TextView) findViewById(R.id.valueWeight);
        weightButton = (ImageButton) findViewById(R.id.weightButton);

        db = new DataBaseHelper(this); //Create a new database helper

        date = getDate();
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
        for (int i = 0; i < history.size(); i++) {
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
                    builder = new AlertDialog.Builder(DailyPage.this, android.R.style.Theme_Material_Dialog_Alert);
                else
                    builder = new AlertDialog.Builder(DailyPage.this);

                //Build the dialog message
                builder.setTitle("Nutrition for " + selectedItem)
                        .setIcon(R.drawable.hamburger)
                        .setMessage("Calories: " + String.valueOf(history.get(position).calories)
                                + "\nProtein: " + String.valueOf(history.get(position).protein)
                                + " g\nFat: " + String.valueOf(history.get(position).fat)
                                + " g\nCarbohydrates: " + String.valueOf(history.get(position).carbs)
                                + " g")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Do nothing, close window
                            }
                        }).show();
            }
        });

        weightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(DailyPage.this);
                builder.setTitle("Weight (lbs)");
                final EditText input = new EditText(DailyPage.this);
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                builder.setView(input);
                builder.setIcon(R.drawable.scale);
                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int weight=0;
                        String weightInput = input.getText().toString();
                        if (weightInput.length()>0){
                            weight = Integer.valueOf(weightInput);
                            db.updateWeight(date, weight); //Update the nutrition table's default weight, and weight for the date
                            valueWeight.setText(weight + " lbs"); //Update the weight on the page
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });
    }

    //Returns the current date
    public String getDate() {
        Date date = Calendar.getInstance().getTime(); //Get the current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy"); //Set up a format to convert the date
        String formattedDate = dateFormat.format(date); //Convert date into the format
        return formattedDate;
    }

    //Create an Alert Dialog that gives the user the option to manually add a food, or take a picture to classify
    public void addFoodDialogue() {
        //Create an alert message that displays nutrition information and asks to save the entry
        AlertDialog.Builder builder;
        //Determine which alert dialog builder based on API version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder = new AlertDialog.Builder(DailyPage.this, android.R.style.Theme_Material_Dialog_Alert);
        else
            builder = new AlertDialog.Builder(DailyPage.this);

        //Build the dialog message
        builder.setTitle("Add Food")
                .setIcon(R.drawable.hamburger)
                .setMessage("Take a photo to classify, or manually input food.")
                .setNegativeButton("Photo", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(DailyPage.this, Picture.class)); //Open the activity to take a picture
                    }
                })
                .setPositiveButton("Manual", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(DailyPage.this, ManualAdd.class)); //Open the activity to manually input nutritional information fro food
                    }
                }).show();
    }

    //Go back to the previous activity on back arrow press
    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), Homepage.class);
        startActivityForResult(myIntent, 0);
        return true;
    }
}
