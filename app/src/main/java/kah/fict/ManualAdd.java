/**
 * Kah Hau Yap
 *
 * @ManualAdd.java
 * There is an option to manually add a food rather than taking a picture. If the user just wants to
 * add a meal normally, they can input the nutritional values on this page and it will be saved to
 * the user history.
 */

package kah.fict;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ManualAdd extends AppCompatActivity {

    private DataBaseHelper db;
    private EditText editFood;
    private EditText editCalories;
    private EditText editProtein;
    private EditText editFat;
    private EditText editCarbs;
    private Button saveButton;

    private String foodName;
    private int calories=0, protein=0, fat=0, carbohydrates=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_add);
        ActionBar actionBar = getSupportActionBar(); //Set back button on title bar
        actionBar.setDisplayHomeAsUpEnabled(true);
        setTitle("Manually Add Food");

        //Set up the editTexts and button
        editFood = (EditText) findViewById(R.id.editFood);
        editCalories = (EditText) findViewById(R.id.editCalories);
        editProtein = (EditText) findViewById(R.id.editProtein);
        editFat = (EditText) findViewById(R.id.editFat);
        editCarbs = (EditText) findViewById(R.id.editCarbs);
        saveButton = (Button) findViewById(R.id.saveButton);

        db = new DataBaseHelper(this); //Create a DataBaseHelper

        //When the save button is pressed, get information from the EditTexts and create a food entry to save
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String date = getDate();


                //Get information from the edit texts
                foodName = editFood.getText().toString();
                calories = Integer.parseInt(editCalories.getText().toString());
                protein = Integer.parseInt(editProtein.getText().toString());
                fat = Integer.parseInt(editFat.getText().toString());
                carbohydrates = Integer.parseInt(editCarbs.getText().toString());

                AlertDialog.Builder builder;
                //Determine which alert dialog builder based on API version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    builder = new AlertDialog.Builder(ManualAdd.this, android.R.style.Theme_Material_Dialog_Alert);
                else
                    builder = new AlertDialog.Builder(ManualAdd.this);

                //Build the dialog message
                builder.setTitle("Save " + foodName + "?")
                        //       .setMessage(calories + " Calories: 0 \nNutrition: 0")
                        .setMessage("Calories: " + calories + "\nProtein: " + protein + " g\nFat: " + fat + " g\nCarbohydrates: " + carbohydrates + " g")
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Save the entry into the user's daily intake
                                String date = getDate();
                                if (db.dateExists(date)) { //If there is an entry for that date in the database then update and add food
                                    updateFood(date, calories, protein, fat, carbohydrates); //Update the nutrition for the day with the food added
                                    db.addFoodHistory(date, foodName, calories, protein, fat, carbohydrates); //Add the food to the food history table that stores foods eaten
                                    startActivity(new Intent(ManualAdd.this, DailyPage.class));
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Do nothing, go back
                            }
                        }).show();
            }
        });
    }

    //Updates the database adding the nutrition from the food to the daily total
    public void updateFood (String date, int calories, int protein, int fat, int carbohydrates) {
        int Calories, Protein, Fat, Carbohydrates, Weight;
        Calories = calories;
        Protein= protein;
        Fat = fat;
        Carbohydrates = carbohydrates;
        Weight = db.getWeight("0"); //Get the default weight from database

        //Add the food's nutritional values to the daily total
        Calories += db.getCalories(date);
        Protein += db.getProtein(date);
        Fat += db.getFat(date);
        Carbohydrates += db.getCarbs(date);

        db.updateNutrition(date, Calories, Protein, Fat, Carbohydrates, Weight); //Update the nutrition for the day with food added
    }
    
    //Function returns the current date
    public String getDate() {
        Date date = Calendar.getInstance().getTime(); //Get the current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy"); //Set up a format to convert the date
        String formattedDate = dateFormat.format(date); //Convert date into the format
        return formattedDate;
    }

    //Go back to the previous activity on back arrow press
    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), DailyPage.class);
        startActivityForResult(myIntent, 0);
        return true;
    }
    
    
    
}
