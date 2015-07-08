package com.intuit.intuitwear.example.oauth.appcenterpoc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

//import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.intuit.intuitwear.controller.IWOAuthManager;
import com.intuit.intuitwear.controller.IWOAuthPreferenceController;
import com.intuit.intuitwear.model.IWAuthConstants;
import com.intuit.intuitwear.view.IWTextFragment;

/**
 * This is the main activity for this sample application that illustrates how to use
 * the Intuit Wear SDK in order to authorize the app to connect to the user's data
 * via Intuit's App Connect platform.  It makes use of the {@code IWOAuthManager} class
 * from the Intuit Wear SDK to display a WebView that walks the user through the App Connect
 * OAuth flow.
 *
 * This main activity must implement the {@code}IWOAuthPreferenceController methods to mark
 * whether or not the app is authenticated and must also provide the onActivityResult method
 * that will be called from the SDK once the WebView is exited.
 */
public class MainOAuthActivityFragment extends ActionBarActivity implements IWOAuthPreferenceController {

    /**
     * String representing SharedPreferences to store whether or not app has appCenterAuthenticated
     * with App Center in order to access the user's data
     */
    public static final String PREFS_NAME = "OAuthPrefs";

    /**
     * This is an example value. You will need to replace with your Google API's Project Number
     */
    static final String GCM_PROJECT_NUMBER = ""; //Todo replace the empty string w/ your GCM Project Number
    /**
     * This is an example value. You will need to replace with your PNG Sender ID
     */
    static final String INTUIT_SENDER_ID = ""; //Todo replace the empty string w/ your Intuit Sender ID


    private IWOAuthManager authManager = null;
    private boolean appCenterAuthenticated = false;
    private MainFragment mainFragment;
    private IWTextFragment textFragment;
    private Resources res = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_oauth_activity_fragment);

        res = getResources();
        FragmentManager fm = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();

        mainFragment = (MainFragment)fm.findFragmentById(R.id.main_fragment);
        textFragment = (IWTextFragment)fm.findFragmentById(R.id.text_fragment);

        if ( !textFragment.isHidden() ) {
            ft.hide(textFragment);
        }

        ft.commit();

        // Display Dialog to request Authorization if they haven't confirmed already
        if (!isAppCenterAuthenticated()) {
            doAuthentication();
        }
//        if (!isAppCenterAuthenticated()) {
//            ft = fm.beginTransaction();
//            ft.setCustomAnimations(android.R.animator.fade_in,
//                    android.R.animator.fade_out);
//            if ( textFragment.isHidden() ) {
//                ft.hide(mainFragment);
//                ft.show(textFragment);
//            }
//            String authUrl      =  res.getString(R.string.appcenter_url);
//            String authDialogMsg = res.getString(R.string.authorization_message);
//
//            authManager = new IWOAuthManager(textFragment, authUrl, authDialogMsg);
//            authManager.authenticateConnections();
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            reset();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult (int requestCode, int resultCode, Intent data) {

        System.out.println("+++++++++++>> onActivityResult called!  \nRequestCode = " + requestCode + " \nResultCode = " + resultCode);

        if (requestCode == IWAuthConstants.APP_CENTER_OAUTH_REQ_CODE ) {
            if (resultCode == RESULT_OK) {
//                Toast.makeText(this, "Authentication Successful!", Toast.LENGTH_SHORT).show();
                setAppCenterAuthenticated(true);
                String username = data.getStringExtra(IWAuthConstants.USERNAME_PROPERTY);
                Log.d("ItDuzzitMainActivity", "---- username = '" + username + "'");

                GCMIntentService.register(this, username, new String[]{"OAuthSample"});
            } else {
                setAppCenterAuthenticated(false);
            }
        }
    }

    private void reset() {
        this.appCenterAuthenticated = false;

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("AppCenterAuthenticated", this.appCenterAuthenticated);

        // Commit the edits!
        editor.commit();

        if (!isAppCenterAuthenticated()) {
            doAuthentication();
        }
    }

    private void doAuthentication() {
        FragmentManager fm = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();

        ft = fm.beginTransaction();
        ft.setCustomAnimations(android.R.animator.fade_in,
                android.R.animator.fade_out);
        if ( textFragment.isHidden() ) {
            ft.hide(mainFragment);
            ft.show(textFragment);
        }
        String authUrl      =  res.getString(R.string.appcenter_url);
        String authDialogMsg = res.getString(R.string.authorization_message);

        authManager = new IWOAuthManager(textFragment, authUrl, authDialogMsg);
        authManager.authenticateConnections();
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Implement methods from Intuit Wear SDK IWOAuthPreferenceController interface
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * This method returns a boolean indicating whether or not the user has already
     * given authorization to access to their data.  Typically, you should store
     * a value in the application's SharedPreferences to keep track of authorization
     * status.
     *
     * @return boolean representing whether or not the user has granted access to
     * their data.  Returns true if user has already granted access.  False, otherwise.
     */
    @Override
    public boolean isAppCenterAuthenticated() {

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
        appCenterAuthenticated = prefs.getBoolean("AppCenterAuthenticated", false);
        return appCenterAuthenticated;
    }

    /**
     * Setter for indicating if user has given authorization to access their data.  This
     * method must be implemented by the application's main class.
     *
     * @param appCenterAuthenticated boolean true if user grants authentication. False otherwise.
     */
    @Override
    public void setAppCenterAuthenticated(boolean appCenterAuthenticated) {
        this.appCenterAuthenticated = appCenterAuthenticated;

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("AppCenterAuthenticated", appCenterAuthenticated);

        // Commit the edits!
        editor.commit();

        FragmentManager fm = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();
        if ( !textFragment.isHidden() ) {
            ft.hide(textFragment);
        }
        ft.show(mainFragment);

        if (appCenterAuthenticated) {
            Toast.makeText(this, "Authentication Successful!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Application not authorized to access your data!", Toast.LENGTH_SHORT).show();
        }
    }
}
