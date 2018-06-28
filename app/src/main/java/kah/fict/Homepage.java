/**
 * Kah Hau Yap
 *
 * @Homepage.java
 * Start screen activity with two buttons: One brings user to the camera to classify an image,
 * the other button brings user to a personalized Macro Nutrients calculator to determine nutritional
 * values needed to meet the user's weight goals. If it is an existing user, the profile button will
 * send them to a daily homepage to display status and progress.
 */

package kah.fict;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;


public class Homepage extends AppCompatActivity {

    private Boolean firstTime = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        //Hide the title bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        updateGooglePlayServices(); //Update GooglePlayServices

        //Create the profile button
        Button profileButton = (Button) findViewById(R.id.profileButton);
        //When button pressed start a new page to the profile screen
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if (isFirstTime()) //If it is the first install for the app then send to the macro calculator
                startActivity(new Intent(Homepage.this, MacrosCalculator.class));
            else //If existing user then send to the daily homepage instead
                startActivity(new Intent(Homepage.this, DailyPage.class));
            }
        });

        //Create the take a picture button
        Button pictureButton = (Button) findViewById(R.id.pictureButton);
        //When button pressed start a new page to the picture screen
        pictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start the profile activity
                startActivity(new Intent(Homepage.this, Picture.class));
            }
        });
    }

    //Check if it is the first time the user has opened the app
    private boolean isFirstTime() {
        if (firstTime == null) {
            SharedPreferences mPreferences = this.getSharedPreferences("first_time", Context.MODE_PRIVATE);
            firstTime = mPreferences.getBoolean("firstTime", true);
            if (firstTime) {
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putBoolean("firstTime", false);
                editor.commit();
            }
        }
        return firstTime;
    }

    //APIs of 4.4 and lower have a bug : Javax.net.ssl.SSLHandshakeException: javax.net.ssl.SSLProtocolException: SSL handshake aborted: Failure in SSL library, usually a protocol error
    //To fix we use GoogleplayServices to force the SSLengine to use "TLSv1.2" so it can run the API if it is needed
    public void updateGooglePlayServices() {
        try {
            ProviderInstaller.installIfNeeded(getApplicationContext());
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLSv1.2");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            sslContext.init(null, null, null);
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        SSLEngine engine = sslContext.createSSLEngine();
    }
}