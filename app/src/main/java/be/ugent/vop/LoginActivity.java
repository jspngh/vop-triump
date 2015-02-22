package be.ugent.vop;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.foursquare.android.nativeoauth.FoursquareCancelException;
import com.foursquare.android.nativeoauth.FoursquareDenyException;
import com.foursquare.android.nativeoauth.FoursquareInvalidRequestException;
import com.foursquare.android.nativeoauth.FoursquareOAuth;
import com.foursquare.android.nativeoauth.FoursquareOAuthException;
import com.foursquare.android.nativeoauth.FoursquareUnsupportedVersionException;
import com.foursquare.android.nativeoauth.model.AccessTokenResponse;
import com.foursquare.android.nativeoauth.model.AuthCodeResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import be.ugent.vop.foursquare.TokenStore;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends ActionBarActivity {

    private static final String CLIENT_ID = "PZTHHDGA3DTEDWTKRFCRXF5KOXXQN5RCIAM3GYAWKFTMXPLE";
    private static final int REQUEST_CODE_FSQ_CONNECT = 200;
    private static final int REQUEST_CODE_FSQ_TOKEN_EXCHANGE = 201;
    private static final String CLIENT_SECRET = "UQSJN0HCIR0LSBT2PEK3CR331JQJUYSINHZ12MHE0A2CWNNQ";

    private Button btnUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!  */
        // NEED TO IMPLEMENT HTTP REQUEST AS ASYNC IN DIFFERENT THREAD !!!!
        StrictMode.ThreadPolicy policy = new StrictMode.
                ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        /* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!  */

        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.title_activity_login);
        Log.d("","LoginActivity created: launching startFoursquareLogin");
        startFoursquareLogin();
    }

    //region Foursquare API
    /**********************************
     Start Foursquare API
     **********************************/

    private void startFoursquareLogin(){

        final SharedPreferences prefs = getSharedPreferences(getString(R.string.sharedprefs), Context.MODE_PRIVATE);
        String token = prefs.getString(getString(R.string.foursquaretoken), "N.A.");



        Button btnLogin = (Button) findViewById(R.id.btnLogin);
        Button btnLogout = (Button) findViewById(R.id.btnLogout);
        TextView logInMessage = (TextView) findViewById(R.id.logInMessage);

        if(token.equalsIgnoreCase("N.A.")){
            // Start the native auth flow.
            Log.d("","Not yet logged in, start native auth flow");
            btnLogin.setVisibility(View.VISIBLE);
            logInMessage.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.GONE);
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Start the native auth flow.
                    Intent intent = FoursquareOAuth.getConnectIntent(LoginActivity.this, CLIENT_ID);

                    // If the device does not have the Foursquare app installed, we'd
                    // get an intent back that would open the Play Store for download.
                    // Otherwise we start the auth flow.
                    if (FoursquareOAuth.isPlayStoreIntent(intent)) {
                        toastMessage(LoginActivity.this, getString(R.string.app_not_installed_message));
                        startActivity(intent);
                    } else {
                        startActivityForResult(intent, REQUEST_CODE_FSQ_CONNECT);
                    }
                }
            });
        }else{
            Log.d("","Already logged in, start native logout flow");
            btnLogin.setVisibility(View.GONE);
            logInMessage.setVisibility(View.GONE);
            btnLogout.setVisibility(View.VISIBLE);
            btnLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Start the native logout flow.
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.remove(getString(R.string.userid));
                    editor.remove(getString(R.string.foursquaretoken));
                    editor.commit();
                    startFoursquareLogin();
                }
            });

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_FSQ_CONNECT:
                onCompleteConnect(resultCode, data);
                break;

            case REQUEST_CODE_FSQ_TOKEN_EXCHANGE:
                onCompleteTokenExchange(resultCode, data);
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void getUserId(){
        String UserId="N.A.";
        SharedPreferences prefs = getSharedPreferences(getString(R.string.sharedprefs), Context.MODE_PRIVATE);
        String token = prefs.getString(getString(R.string.foursquaretoken), "N.A.");
        String base = "https://api.foursquare.com/v2/users/self?oauth_token=";
        String address = base.concat(token);
        String version = "&v=20150221";
        address = address.concat(version);
        String readJSON = getJSON(address);
        try{
            JSONObject jsonObject = new JSONObject(readJSON);
            JSONObject response = jsonObject.getJSONObject("response");
            JSONObject user = response.getJSONObject("user");

            UserId=user.getString("id");
            Log.d("","UserId "+ UserId);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(getString(R.string.userid), UserId);
            editor.commit();
        } catch(Exception e){e.printStackTrace();}
    }

    private void onCompleteConnect(int resultCode, Intent data) {
        AuthCodeResponse codeResponse = FoursquareOAuth.getAuthCodeFromResult(resultCode, data);
        Exception exception = codeResponse.getException();

        if (exception == null) {
            // Success.
            String code = codeResponse.getCode();
            performTokenExchange(code);

        } else {
            if (exception instanceof FoursquareCancelException) {
                // Cancel.
                toastMessage(this, "Canceled");

            } else if (exception instanceof FoursquareDenyException) {
                // Deny.
                toastMessage(this, "Denied");

            } else if (exception instanceof FoursquareOAuthException) {
                // OAuth error.
                String errorMessage = exception.getMessage();
                String errorCode = ((FoursquareOAuthException) exception).getErrorCode();
                toastMessage(this, errorMessage + " [" + errorCode + "]");

            } else if (exception instanceof FoursquareUnsupportedVersionException) {
                // Unsupported Fourquare app version on the device.
                toastError(this, exception);

            } else if (exception instanceof FoursquareInvalidRequestException) {
                // Invalid request.
                toastError(this, exception);

            } else {
                // Error.
                toastError(this, exception);
            }
        }
    }

    private void onCompleteTokenExchange(int resultCode, Intent data) {
        AccessTokenResponse tokenResponse = FoursquareOAuth.getTokenFromResult(resultCode, data);
        Exception exception = tokenResponse.getException();

        if (exception == null) {
            String accessToken = tokenResponse.getAccessToken();
            // Success.
            Log.d("", "Access token: " + accessToken);

            // Persist the token for later use. In this example, we save
            // it to shared prefs.
            TokenStore.get().setToken(accessToken);
            SharedPreferences prefs = getSharedPreferences(getString(R.string.sharedprefs), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putString(getString(R.string.foursquaretoken), accessToken);
            editor.commit();
            getUserId();
            startFoursquareLogin();

        } else {
            if (exception instanceof FoursquareOAuthException) {
                // OAuth error.
                String errorMessage = (exception).getMessage();
                String errorCode = ((FoursquareOAuthException) exception).getErrorCode();
                toastMessage(this, errorMessage + " [" + errorCode + "]");

            } else {
                // Other exception type.
                toastError(this, exception);
            }
        }
    }

    /**
     * Exchange a code for an OAuth Token. Note that we do not recommend you
     * do this in your app, rather do the exchange on your server. Added here
     * for demo purposes.
     *
     * @param code
     *          The auth code returned from the native auth flow.
     */
    private void performTokenExchange(String code) {
        Intent intent = FoursquareOAuth.getTokenExchangeIntent(this, CLIENT_ID, CLIENT_SECRET, code);
        startActivityForResult(intent, REQUEST_CODE_FSQ_TOKEN_EXCHANGE);
    }

    public static void toastMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void toastError(Context context, Throwable t) {
        Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
    }

    /**********************************
     End Foursquare API
     **********************************/
//endregion

    public String getJSON(String address){
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(address);
        try{
            HttpResponse response = client.execute(httpGet);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if(statusCode == 200){
                HttpEntity entity = response.getEntity();
                InputStream content = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                String line;
                while((line = reader.readLine()) != null){
                    builder.append(line);
                }
            } else {
                Log.e(LoginActivity.class.toString(), "Failed JSON object");
                Log.e(LoginActivity.class.toString(), "" + statusCode);
            }
        }catch(ClientProtocolException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        return builder.toString();
    }
}



