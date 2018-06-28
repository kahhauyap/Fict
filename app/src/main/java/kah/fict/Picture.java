/**
 * Kah Hau Yap
 *
 * @Picture.java
 * This activity allows the user to take a picture of a meal with the camera or select an image from
 * their gallery. After the image is selected the image is sent asynchronously to the IBM Watson
 * Visual Recognition API for classification. After classifying it will create an ArrayList of possible
 * options that the food may be and send it to the next activity for saving.
 */

package kah.fict;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.ibm.watson.developer_cloud.visual_recognition.v3.VisualRecognition;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifiedImage;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifiedImages;
import com.ibm.watson.developer_cloud.visual_recognition.v3.model.ClassifyOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Picture extends AppCompatActivity {

    //Authenticating API key for IBM Watson Visual Recognition API
    VisualRecognition visualRecognition = new VisualRecognition(
            "2016-05-20",
            "eb470ac4571faf155a1630ca6cd383e7da95cfe3");

    ImageButton cameraButton;
    Button galleryButton;
    ImageView pictureImage;

    static final int REQUEST_IMAGE_CAPTURE = 1; //Permission to take a picture
    static final int REQUEST_IMAGE_PICK = 2; //Permission to select picture from the photo library

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        setTitle("Visual Recognition");
        ActionBar actionBar = getSupportActionBar(); //Set back button on the title bar
        actionBar.setDisplayHomeAsUpEnabled(true);


        //Create the camera button to take a photo
        cameraButton = (ImageButton) findViewById(R.id.pictureButton);
        galleryButton = (Button) findViewById(R.id.galleryButton);
        pictureImage = (ImageView) findViewById(R.id.imageView);
        pictureImage.setVisibility(View.GONE);

        //When button pressed open up the camera
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //If we don't have permission to access the camera then request permission
                if (ContextCompat.checkSelfPermission(Picture.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    //Request the user for camera permission
                    ActivityCompat.requestPermissions(Picture.this,
                            new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
                }
                else { //If we have access to the camera proceed
                    //Start an intent that allows us to open up the camera
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    //Start the activity to take a picture and return it for display onto the screen
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        });

        //When button pressed open up the photo library
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //If we don't have permission to access the gallery then request permission
                if (ContextCompat.checkSelfPermission(Picture.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    //Request the user for camera permission
                    ActivityCompat.requestPermissions(Picture.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_IMAGE_PICK);
                }
                else { //If we have access to the gallery proceed
                    //Start an intent that allows us to open up the camera
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                    //Start the activity to take a picture and return it for display onto the screen
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, REQUEST_IMAGE_PICK);
                    }
                }
            }
        });
    }
    //Request user permission to access the camera
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Camera permission granted", Toast.LENGTH_LONG).show();
                }
            }
            if (requestCode == REQUEST_IMAGE_PICK) {
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                   Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Gallery permission granted", Toast.LENGTH_LONG).show();
                }
            }
    }
    //After the picture is taken display it onto the screen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //If a user took a picture then we can display it, otherwise if they pressed back do nothing
        if(resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                pictureImage.setVisibility(View.VISIBLE);
                Bitmap image = (Bitmap) data.getExtras().get("data");
                pictureImage.setImageBitmap(image); //Displays the image on the screen

                //Create a new ImageRecognition AsyncTask to send the image to the VisualRecognition server for processing
                ImageRecognition imageRecognition = new ImageRecognition(image,this);
                imageRecognition.execute();
            }
            //If the user selected a photo from the gallery
            else if (requestCode == REQUEST_IMAGE_PICK) {
                //Get the URI from the image we picked
                Uri pickedImage = data.getData();
                //Use ContentResolver to get the path of the image
                String[] filePath = { MediaStore.Images.Media.DATA };
                //Create a cursor to get to the image
                Cursor cursor = getContentResolver().query(pickedImage, filePath, null, null, null);
                cursor.moveToFirst();
                String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

                //Convert the image into a bitmap
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap image = BitmapFactory.decodeFile(imagePath, options);

                //Make the image visible and set the view to the selected image
                pictureImage.setVisibility(View.VISIBLE);
                pictureImage.setImageBitmap(image); //Displays the image on the screen

                //Create a new ImageRecognition AsyncTask to send the image to the VisualRecognition server for processing
                ImageRecognition imageRecognition = new ImageRecognition(image, this);
                imageRecognition.execute();
            }
        }
    }

    //AsyncTask class to allow us to access the API on a thread seperate from the main UI thread because we don't want to freeze it while fetching data
    private class ImageRecognition extends AsyncTask<Void,Void,HashMap <String,Double>> {

        private Bitmap bitmapImage;
        private Context context;
        private ArrayList<String> classificationList = new ArrayList<String>(); //ArrayList to hold the possible classifications

        //Constructor for ImageRecognition class to pass in a bitmap
        public ImageRecognition(Bitmap bitmap, Context context){
            this.bitmapImage = bitmap;
            this.context = context;
        }
        HashMap <String,Double> foodMap = new HashMap <String,Double>();

        @Override
        protected void onPreExecute() {
            //Display progress to user while classifying
            progressDialog=ProgressDialog.show(Picture.this,"Classifying","Please wait...",false);
        }

        @Override
        protected HashMap <String,Double> doInBackground(Void... voids) {
            //Compress and convert the bitmap to an InputStream to feed into the visual recognition api
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            byte[] bitmapData = bytes.toByteArray();
            InputStream imageStream = new ByteArrayInputStream(bitmapData);

            //Build options to be used for classifying the image
            ClassifyOptions classifyOptions = new ClassifyOptions.Builder()
                    .imagesFile(imageStream)
                    .imagesFilename("food.jpg")
                    .addClassifierId("food")
                    .build();
            //Initiate visual recognition API request call
            ClassifiedImages result = visualRecognition.classify(classifyOptions).execute();

            //Parse the JSON returned from the API and get the possible classifications of the food and their score
            try {
                foodMap=parseJSON(String.valueOf(result.getImages()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return foodMap;
        }

        //After the AsyncTask finishes and returns API results, save the results from the HashMap to an Arraylist and send to next activity
        @Override
        protected void onPostExecute(HashMap<String, Double> hashMap) {
            progressDialog.dismiss(); //Dismiss the progress
            super.onPostExecute(hashMap);
            Iterator iterator = foodMap.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next().toString();
                if (foodMap.get(key)>0.5) { //If the probability is over 50% then add it to an ArrayList
                    classificationList.add(key);
                }
            }
            //Create a bundle to pass the ArrayList to another activity
            Bundle bundle = new Bundle();
            bundle.putStringArrayList("ArrayList", classificationList);
            Intent intent = new Intent(context, SelectFood.class);
            intent.putExtras(bundle);
            startActivity(intent); //Start the SelectFood activity to allow the user to select the correct classification
        }
    }

    //Parse a JSON string to extract information from the API result in JSON format
    public  HashMap <String,Double> parseJSON (String JSONstring) throws JSONException {
        HashMap <String,Double> hashMap = new HashMap <String,Double>();
        //Create a JSONArray to parse the JSON information
        JSONArray classifiers = new JSONArray(JSONstring);
        JSONObject jo = classifiers.getJSONObject(0); //Get the first object of the array which is the classifiers that holds the food classification information

        // Create a JSONArray to parse through the different possible classifications of the item
        JSONArray ja = jo.getJSONArray("classifiers");
        for (int i=0; i < ja.length(); i++){
            JSONObject jo2 = ja.getJSONObject(i);
            JSONArray ja2 = jo2.getJSONArray("classes");

            //For each possible classification get the name of the food and the score
            for (int j=0; j < ja2.length(); j++){
                JSONObject jo3 = ja2.getJSONObject(j);
                String foodClass = jo3.getString("class");
                Double score = jo3.getDouble("score");
                hashMap.put(String.valueOf(foodClass),Double.valueOf(score)); //Put the name of the food and the probability into the map
            }
        }
        return hashMap;
    }

    //Go back to the previous activity on back arrow press
    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), DailyPage.class);
        startActivityForResult(myIntent, 0);
        return true;
    }
}
