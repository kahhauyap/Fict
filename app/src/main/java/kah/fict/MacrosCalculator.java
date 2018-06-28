/**
 * Kah Hau Yap
 *
 * @MacrosCalculator.java
 * This activity takes in information from the user and plugs them into an IIFYM (If It Fits Your Macros)
 * equation that calculates how much the user should be eating based on their goals and body type.
 * The results are then saved into the database to be compared against.
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class MacrosCalculator extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private DataBaseHelper db;
    private EditText editAge;
    private EditText editWeight;
    private EditText editHeight;
    private EditText editHeightInches;
    private Button calculateButton;
    private Spinner activitySpinner;
    private Spinner genderSpinner;
    private Spinner goalSpinner;

    TextView result;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_macros_calculator);
        setTitle("Macros Calculator");
        ActionBar actionBar = getSupportActionBar(); //Set back button on the title bar
        actionBar.setDisplayHomeAsUpEnabled(true);

        //Set up the editTexts and button
        editAge = (EditText) findViewById(R.id.editAge);
        editWeight = (EditText) findViewById(R.id.editWeight);
        editHeight = (EditText) findViewById(R.id.editHeight);
        editHeightInches = (EditText) findViewById(R.id.editHeightInch);
        calculateButton = (Button) findViewById(R.id.calculateButton);
        activitySpinner = (Spinner) findViewById(R.id.activeSpinner);
        genderSpinner = (Spinner) findViewById(R.id.genderSpinner);
        goalSpinner = (Spinner) findViewById(R.id.goalSpinner);
        result = (TextView) findViewById(R.id.resultText); //Test result

        db = new DataBaseHelper(this); //Create a DataBaseHelper

        //Listener for the spinner
        activitySpinner.setOnItemSelectedListener( MacrosCalculator.this);
        genderSpinner.setOnItemSelectedListener( MacrosCalculator.this);

        // Spinner Drop down elements added to array list
        ArrayList<String> activityCategories = new ArrayList<String>();
        activityCategories.add("( Activity Level )");
        activityCategories.add("Sedentary");
        activityCategories.add("Lightly Active");
        activityCategories.add("Moderately Active");

        // Spinner Drop down elements added to array list
        ArrayList<String> genderCategories = new ArrayList<String>();
        genderCategories.add("( Gender )");
        genderCategories.add("Male");
        genderCategories.add("Female");

        // Spinner Drop down elements added to array list
        ArrayList<String> goalCategories = new ArrayList<String>();
        goalCategories.add("( Select Goal )");
        goalCategories.add("Lose Weight");
        goalCategories.add("Maintain Weight");
        goalCategories.add("Gain Weight");

        // Creating adapter for spinner
        ArrayAdapter<String> activityAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, activityCategories);
        activitySpinner.setAdapter(activityAdapter);

        // Creating adapter for gender spinner
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, genderCategories);
        genderSpinner.setAdapter(genderAdapter);

        //Creating adapter for goal spinner
        ArrayAdapter<String> goalAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, goalCategories);
        goalSpinner.setAdapter(goalAdapter);

        //When button pressed calculate the caloric information
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int age=0;
                double weight=0, height=0, feet=0, inches=0;
                //Get information from the editTexts : Age, Gender, Height, Activity, Goals
                if(editAge.getText().toString().length()>0)
                    age=Integer.parseInt(editAge.getText().toString());
                if(editWeight.getText().toString().length()>0)
                    weight = Integer.parseInt(editWeight.getText().toString());
                if(editHeight.getText().length()>0)
                    feet=Integer.parseInt(editHeight.getText().toString());
                if(editHeightInches.getText().length()>0)
                    inches=Double.parseDouble(editHeight.getText().toString());

                //Convert the feet and inches for height into cm for the formula
                height += feet * 30.48; //1:30.48, convert feet to cm
                height += inches * 2.54; //1:2.54, convert inches to cm

                //Get the position of the activity spinner: how active is the user (0,1,2)
                int activityPosition=activitySpinner.getSelectedItemPosition();
                //Get the position of the gender spinner: how active is the user (male, female)
                int genderPosition=genderSpinner.getSelectedItemPosition();
                //Get the position of the goal spinner: lose, maintain, gain weight
                int goalPosition=goalSpinner.getSelectedItemPosition();

                //Calculate the TDEE: Total Daily Energy Expenditure aka Calories needed
                // TDEE = RDEE * Activity level
                double TDEE=0, activeLevel=0, calories=0;

                //Get the activity level of the user for the formula
                if (activityPosition==1) activeLevel = 1.2; //Sedentary
                else if (activityPosition==2) activeLevel = 1.375; //Lightly Active
                else if (activityPosition==3) activeLevel = 1.725; //Moderately Active

                //Get the weight goal of the user
                if (activityPosition==1) activeLevel = 1.2; //Sedentary
                else if (activityPosition==2) activeLevel = 1.375; //Lightly Active
                else if (activityPosition==3) activeLevel = 1.725; //Moderately Active

                //Calculate the TDEE: Total Daily Energy Expenditure
                if (genderPosition==1) {//If user is a man
                    //Calculate the RDEE: Resting Daily Energy Expenditure for men
                    double maleRDEE = (10 * weight * 0.45359237) + (6.25 * height) - (5 * age) + 5; // RDEE = (10 * weight(kg)) + (6.25 * height(cm)) - (5 * age(years)) + 5
                    TDEE=maleRDEE*activeLevel;
                }
                if (genderPosition==2){//If user is a woman
                    //Calculate the RDEE: Resting Daily Energy Expenditure for women
                    double femaleRDEE =  (10 * weight * 0.45359237) + (6.25 * height) - (5 * age) -161;
                    TDEE=femaleRDEE*activeLevel;
                }

                //Calculate the calories the user needs depending on their weight goals
                if (goalPosition==1) calories = TDEE - (TDEE*.20); //Lose weight, eat 20% less calories
                else if (goalPosition==2) calories = TDEE; //Maintain, eat same calories as TDEE
                else if (goalPosition==3) calories = TDEE + (TDEE*.20); //Maintain, eat 20% more calories

                //Calculate the macronutrients
                double protein = weight * 0.825; //Protein intake should be around 1-.8.25 per lb of body weight
                double fat = (calories* 0.25)/9 ; //Fat intake is 25% of TDEE divided by 9 because there are 9 grams of fat per calorie
                double carbohydrates = (calories - (protein + fat))/4; //Carbohydrates intake is the leftovers after protein and fat from total, divided by 4 since 1g of carbs is 4 calories

                final int Calories = (int) calories; //Convert double into an int to cut off decimals
                final int Protein = (int) Math.round(protein);
                final int Fat = (int) Math.round(fat);
                final int Carbohydrates = (int) Math.round(carbohydrates);
                final int Weight = (int) weight; //Convert double to int to store in the database

                /* Create an Alert Dialog to display daily intake information to user */
                AlertDialog.Builder builder;
                //Determine which alert dialog builder based on API version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    builder = new AlertDialog.Builder(view.getContext(), android.R.style.Theme_Material_Dialog_Alert);
                else
                    builder = new AlertDialog.Builder(view.getContext());

                //Build the dialog message
                builder.setTitle("Daily Nutrition Goals")
                        .setMessage("Calories: " + Calories +
                        "\nProtein: " + Protein + " g\n" + "Fat: " + Fat + " g\n" + "Carbohydrates: " + Carbohydrates + " g")
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Create and save a new default entry in the database with the calories and nutrition values that the user should meet
                                if (!db.dateExists("0")) { //Set the date to "0" which we will use to store the daily values for the user and access to set the values each day
                                    db.addEntry("0"); //Create a new entry
                                    db.updateNutrition("0", Calories, Protein, Fat, Carbohydrates, Weight); //Update the values for that entry to set default goals
                                }
                                else { //If it is an existing user and they just want to update their goals
                                    db.updateNutrition("0", Calories, Protein, Fat, Carbohydrates, Weight); //Update the values for that entry to set default goals
                                }
                                startActivity(new Intent(MacrosCalculator.this, DailyPage.class));
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Do nothing, go back
                            }
                        }).show();
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
        //Do nothing
    }
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        //Do nothing
    }

    //Go back to the previous activity on back arrow press
    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), DailyPage.class);
        startActivityForResult(myIntent, 0);
        return true;
    }
}
