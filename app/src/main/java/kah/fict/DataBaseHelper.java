/**
 * Kah Hau Yap
 *
 * @DataBaseHelper.java
 *
 * SQLITE Data Base Helper class used to create a database with two tables: One to store daily
 * nutritional information to keep track of progress, and the other to store a log of foods that
 * the user can view in the history page. A basic Food class is also defined to bundle information
 * on a food.
 */

package kah.fict;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DataBaseHelper extends SQLiteOpenHelper {
    //Database
    private static final int DATABASE_VERSION=1;
    private static final String DATABASE_NAME="DailyNutrition.db";

    //Table entries for daily nutrition
    private static final String TABLE_NUTRITION="DailyNutrition";
    private static final String KEY_DATE="Date";
    private static final String KEY_CALORIES="Calories";
    private static final String KEY_PROTEIN="Protein";
    private static final String KEY_FAT="Fat";
    private static final String KEY_CARBS="Carbohydrates";
    private static final String KEY_WEIGHT="Weight";

    private static final String TABLE_FOOD="FoodHistory";
    private static final String KEY_FOOD="Food";

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //Create commands for SQLite to create tables
        String CREATE_NUTRITION_TABLE="CREATE TABLE " + TABLE_NUTRITION + "("
                + KEY_DATE + " STRING PRIMARY KEY NOT NULL UNIQUE,"
                + KEY_CALORIES + " INT DEFAULT 0,"
                + KEY_PROTEIN + " INT DEFAULT 0,"
                + KEY_FAT + " INT DEFAULT 0,"
                + KEY_CARBS + " INT DEFAULT 0,"
                + KEY_WEIGHT + " INT DEFAULT 0"
                + ")";

        String CREATE_FOOD_TABLE="CREATE TABLE " + TABLE_FOOD + "("
                + KEY_DATE + " STRING NOT NULL,"
                + KEY_FOOD + " STRING,"
                + KEY_CALORIES + " INT DEFAULT 0,"
                + KEY_PROTEIN + " INT DEFAULT 0,"
                + KEY_FAT + " INT DEFAULT 0,"
                + KEY_CARBS + " INT DEFAULT 0"
                + ")";

        sqLiteDatabase.execSQL(CREATE_NUTRITION_TABLE); //Execute SQL command to create a table with the proper fields
        sqLiteDatabase.execSQL(CREATE_FOOD_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NUTRITION); //If the table already exists then delete it
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_FOOD);
        onCreate(sqLiteDatabase); //Call onCreate to create the table
    }

    /*
     *  Check if a food entry is in the Food Table, returns true if there is, or false if there isn't
     */
    public boolean foodExists (String food) {
        SQLiteDatabase db = this.getReadableDatabase(); //Get database for reading
        //Create cursor to query the table and finding user that matches the ID
        Cursor cursor = db.query(TABLE_FOOD, new String[]{KEY_FOOD},
                KEY_FOOD + " =?",
                new String[]{food}, null, null, null, null);

        if (cursor.getCount()>0) //If the cursor count is >= 1 then return that there is a valid entry in the table, otherwise the item does not exist
            return true;
        else
            return false;
    }

    /*
     *  Get the food's nutritional value from an existing entry in the Food Table and return it
     */
    public Food getFood (String foodName) {
        Food food = new Food();
        SQLiteDatabase db= this.getReadableDatabase(); //Get database for reading

        //Create cursor to query the table and finding user that matches the ID
        Cursor cursor = db.query(TABLE_FOOD, new String[]{KEY_FOOD, KEY_CALORIES, KEY_PROTEIN, KEY_FAT, KEY_CARBS},
                KEY_FOOD + " =?",
                new String[]{foodName}, null, null, null, null);
        if (cursor!=null) {//If the cursor found the entry move to the first instance
            cursor.moveToFirst();
            //Create a new Food object with the values of the existing food in the database
            food = new Food(cursor.getString(0), cursor.getInt(1), cursor.getInt(2), cursor.getInt(3), cursor.getInt(4));
        }
        return food;
    }

    /*
     *  Insert an entry into the table food history table
     */
    public void addFoodHistory (String date, String food, double calories, double protein, double fat, double carbohydrates) {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues values= new ContentValues();
        values.put(KEY_DATE, date);
        values.put(KEY_FOOD, food);
        values.put(KEY_CALORIES, calories);
        values.put(KEY_PROTEIN, protein);
        values.put(KEY_FAT, fat);
        values.put(KEY_CARBS, carbohydrates);

        db.insert(TABLE_FOOD, null, values); //Update and insert into table
        db.close();
    }

    /*
     *  Update the weight for the user
     */
    public void updateWeight(String date, int weight) {
        SQLiteDatabase db = this.getWritableDatabase(); //Get the database to make changes
        ContentValues values= new ContentValues(); //Prepare content values to update
        values.put(KEY_WEIGHT, weight);

        String date_number= String.valueOf(date); //Convert the date to a string to update the table
        db.update(TABLE_NUTRITION, values, KEY_DATE + " =?", new String[]{"0"}); //Update the default weight
        db.update(TABLE_NUTRITION, values, KEY_DATE + " =?", new String[]{date_number}); //Update the weight for the date

        db.close();
    }

    /*
     *  Retrieve all the dates that have been recorded in the database for entries
     */
    public ArrayList <String> getDateHistory (){
        SQLiteDatabase db=this.getWritableDatabase();
        ArrayList <String> dates = new ArrayList<String>();
        Cursor  cursor = db.rawQuery("select Date from " + TABLE_NUTRITION,null); //Get all the entries from the table
        if (cursor!=null) //If the cursor found the entry move to the first instance
            cursor.moveToFirst();

        for (int i=0; i<cursor.getCount(); i++) { //Get the dates from the Nutrition table which has daily updated entries
            String date = cursor.getString(cursor.getColumnIndex(KEY_DATE));
            dates.add(date);
            cursor.moveToNext();
        }
        return dates;
    }

    /*
     *  Retrieve the foods for a given date
     */
    public ArrayList <Food> getFoodHistory (String date) {
        ArrayList <Food> foods = new ArrayList<Food>();

        SQLiteDatabase db= this.getReadableDatabase(); //Get database for reading
        //Create cursor to query the table and finding user that matches the ID
        Cursor cursor = db.query(TABLE_FOOD, new String[]{KEY_FOOD, KEY_CALORIES, KEY_PROTEIN, KEY_FAT, KEY_CARBS},
                KEY_DATE + " =?",
                new String[]{String.valueOf(date)}, null, null, null, null);
        if (cursor!=null) //If the cursor found the entry move to the first instance
            cursor.moveToFirst();

        for (int i=0; i<cursor.getCount(); i++){ //Get the foods for the date
            String foodName;
            int calories=0,  protein=0, fat=0, carbs=0;

            //Move the cursor and get the relevant information
            foodName = cursor.getString(0);
            calories = cursor.getInt(1);
            protein = cursor.getInt(2);
            fat = cursor.getInt(3);
            carbs = cursor.getInt(4);

            //Create a Food object to store food information, and add to the ArrayList
            Food food = new Food(foodName,calories,protein,fat,carbs);
            foods.add(food);

            cursor.moveToNext(); //Move to next entry in the database with the same date
        }
        return foods;
    }

    /*
     *  Insert an entry into the nutrition table
     */
    public void addEntry(String date) {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues values= new ContentValues();
        values.put(KEY_DATE, date);
        values.put(KEY_CALORIES, 0);
        values.put(KEY_PROTEIN, 0);
        values.put(KEY_FAT, 0);
        values.put(KEY_CARBS, 0);
        values.put(KEY_WEIGHT, 0);

        db.insert(TABLE_NUTRITION, null, values); //Update and insert into table
        db.close();
    }

    /*
     *  Update the change in nutrition for a date
     */
    public int updateNutrition(String date, int calories, int protein, int fat, int carbs, int weight) {
        SQLiteDatabase db= this.getWritableDatabase(); //Get the database to make changes
        ContentValues values= new ContentValues(); //Prepare content values to update
        values.put(KEY_CALORIES, calories);
        values.put(KEY_PROTEIN, protein);
        values.put(KEY_FAT, fat);
        values.put(KEY_CARBS, carbs);
        values.put(KEY_WEIGHT, weight);

        String date_number= String.valueOf(date); //Convert the date to a string to update the table
        return db.update(TABLE_NUTRITION, values, KEY_DATE + " =?", new String[]{date_number}); //Update constraints where IDs match
    }

    /*
     *  Delete the entry for the date
     */
    public void deleteEntry(String date) {
        SQLiteDatabase db = this.getWritableDatabase(); // Get database for writing to delete
        db.delete(TABLE_NUTRITION, KEY_DATE + " =?", new String[]{String.valueOf(date)}); //Delete the entry in the table where IDs match
        db.close();
    }

    /*
    *  Check if the user entry exists in the database
    */
    public boolean dateExists (String date) {
        SQLiteDatabase db= this.getReadableDatabase(); //Get database for reading
        //Create cursor to query the table and finding user that matches the ID
        Cursor cursor = db.query(TABLE_NUTRITION, new String[]{KEY_CALORIES, KEY_PROTEIN, KEY_FAT, KEY_CARBS, KEY_WEIGHT},
                KEY_DATE + " =?",
                new String[]{String.valueOf(date)}, null, null, null, null);
        if (cursor!=null) {
            //If the cursor doesn't point to anything the entry doesn't exist
            if (cursor.getCount() <= 0) {
                cursor.close();
                db.close();
                return false;
            }
        }
        cursor.close();
        db.close();
        return true;
    }

    /*
     *  Get calorie intake of user
     */
    public int getCalories(String date) {
        SQLiteDatabase db= this.getReadableDatabase(); //Get database for reading
        //Create cursor to query the table and finding user that matches the ID
        Cursor cursor = db.query(TABLE_NUTRITION, new String[]{KEY_CALORIES, KEY_PROTEIN, KEY_FAT, KEY_CARBS, KEY_WEIGHT},
                KEY_DATE + " =?",
                new String[]{String.valueOf(date)}, null, null, null, null);
        if (cursor!=null) //If the cursor found the entry move to the first instance
            cursor.moveToFirst();
        return cursor.getInt(0); //Get the budget in position 1 of the table
    }

    /*
     *  Get protein intake of the user
     */
    public int getProtein(String date) {
        SQLiteDatabase db= this.getReadableDatabase(); //Get database for reading
        //Create cursor to query the table and finding user that matches the ID
        Cursor cursor = db.query(TABLE_NUTRITION, new String[]{KEY_CALORIES, KEY_PROTEIN, KEY_FAT, KEY_CARBS, KEY_WEIGHT},
                KEY_DATE + " =?",
                new String[]{String.valueOf(date)}, null, null, null, null);
        if (cursor!=null) //If the cursor found the entry move to the first instance
            cursor.moveToFirst();
        return cursor.getInt(1); //Get the budget in position 1 of the table
    }

    /*
     *  Get fat intake of the user
     */
    public int getFat(String date) {
        SQLiteDatabase db= this.getReadableDatabase(); //Get database for reading
        //Create cursor to query the table and finding user that matches the ID
        Cursor cursor = db.query(TABLE_NUTRITION, new String[]{KEY_CALORIES, KEY_PROTEIN, KEY_FAT, KEY_CARBS, KEY_WEIGHT},
                KEY_DATE + " =?",
                new String[]{String.valueOf(date)}, null, null, null, null);
        if (cursor!=null) //If the cursor found the entry move to the first instance
            cursor.moveToFirst();
        return cursor.getInt(2); //Get the budget in position 1 of the table
    }

    /*
     *  Get fat intake of the user
     */
    public int getCarbs(String date) {
        SQLiteDatabase db= this.getReadableDatabase(); //Get database for reading
        //Create cursor to query the table and finding user that matches the ID
        Cursor cursor = db.query(TABLE_NUTRITION, new String[]{KEY_CALORIES, KEY_PROTEIN, KEY_FAT, KEY_CARBS, KEY_WEIGHT},
                KEY_DATE + " =?",
                new String[]{String.valueOf(date)}, null, null, null, null);
        if (cursor!=null) //If the cursor found the entry move to the first instance
            cursor.moveToFirst();
        return cursor.getInt(3); //Get the budget in position 1 of the table
    }

    /*
     *  Get weight of the user
     */
    public int getWeight(String date) {
        SQLiteDatabase db= this.getReadableDatabase(); //Get database for reading
        //Create cursor to query the table and finding user that matches the ID
        Cursor cursor = db.query(TABLE_NUTRITION, new String[]{KEY_CALORIES, KEY_PROTEIN, KEY_FAT, KEY_CARBS, KEY_WEIGHT},
                KEY_DATE + " =?",
                new String[]{String.valueOf(date)}, null, null, null, null);
        if (cursor!=null) //If the cursor found the entry move to the first instance
            cursor.moveToFirst();
        return cursor.getInt(4); //Get the budget in position 1 of the table
    }

    /*
     *  Food class to hold information about a food
     */    
    public class Food {

        public Food () {
            this.name="";
            this.calories=0;
            this.protein=0;
            this.fat=0;
            this.carbs=0;
        }
        public Food ( String name, int calories, int protein, int fat, int carbs ) {
            this.name = name;
            this.calories = calories;
            this.protein = protein;
            this.fat = fat;
            this.carbs = carbs;
        }

        public String name;
        public int calories;
        public int protein;
        public int fat;
        public int carbs;
    }
}
