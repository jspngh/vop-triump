package be.ugent.vop;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;

import be.ugent.vop.backend.myApi.model.AuthTokenResponse;
import be.ugent.vop.foursquare.TokenStore;
import be.ugent.vop.loaders.AuthTokenLoader;

public class LoginActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<AuthTokenResponse> {

    private static final String CLIENT_ID = "PNTT2Y4XCYL35PHKIRLNV0YSO50XHALPX1SBDMAY3BIZWRBA";
    private static final int REQUEST_CODE_FSQ_CONNECT = 200;
    private static final int REQUEST_CODE_FSQ_TOKEN_EXCHANGE = 201;
    private static final String CLIENT_SECRET = "P5KO0OFRI2Z13GQH0YLS5BS5SYLIYMW21Q1YBSWHP4XJJ5Q5";

    public static final String LOGIN_ACTION = "loginaction";
    public static final int LOGIN_FS = 1;
    public static final int LOGIN_BACKEND = 2;
    public static final int LOGOUT = 3;
    private Button btnLogin;
    private Button btnLogout;
    private TextView logInMessage;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        logInMessage = (TextView) findViewById(R.id.logInMessage);

        prefs = getSharedPreferences(getString(R.string.sharedprefs), Context.MODE_PRIVATE);

        int loginAction = getIntent().getExtras().getInt(LOGIN_ACTION);
        switch(loginAction){
            case LOGIN_FS:
                startFoursquareLogin();
                break;
            case LOGIN_BACKEND:
                startBackendLogin();
                break;
            case LOGOUT:
                logout();
                break;
            default:
        }
    }

    private void getUserId (){
        SharedPreferences prefs = getSharedPreferences(getString(R.string.sharedprefs), Context.MODE_PRIVATE);
        String token = prefs.getString(getString(R.string.foursquaretoken), "N.A.");
        String base = "https://api.foursquare.com/v2/users/self?oauth_token=";
        String version = "&v=20150221";
        String address = base.concat(token).concat(version);
        String userInfo = "";
        GetJSONTask getUserInfo = new GetJSONTask();
        getUserInfo.execute(address);

        try{
            userInfo = getUserInfo.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        if(!userInfo.equals("")){
            try {
                JSONObject jsonObject = new JSONObject(userInfo);
                String userId = jsonObject.getJSONObject("response").getJSONObject("user").getString("id");
                SharedPreferences.Editor editor = prefs.edit();
                editor.putLong(getString(R.string.userid), Long.valueOf(userId).longValue());
                editor.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            Log.e(LoginActivity.class.getName(),"Response invalid");
        }
    }

    //region Foursquare API
    /**********************************
     Start Foursquare API
     **********************************/

    private void startFoursquareLogin(){

        SharedPreferences.Editor editor = prefs.edit();

        // Close sessions that might still be open
        if(prefs.contains(getString(R.string.backendtoken)))
            closeBackendSession();

        // Make sure to remove all tokens in SharedPreferences
        editor.remove(getString(R.string.backendtoken));
        editor.remove(getString(R.string.foursquaretoken));
        editor.commit();

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
    }

    private void startBackendLogin(){
        // Close sessions that might still be open
        if(prefs.contains(getString(R.string.backendtoken)))
            closeBackendSession();

        performBackendTokenExchange();
    }

    private void performBackendTokenExchange(){
        String fsToken = prefs.getString(getString(R.string.foursquaretoken), "N.A.");
        long userId = prefs.getLong(getString(R.string.userid), 0);

        Bundle args = new Bundle(2);
        args.putLong("userId", userId);
        args.putString("fsToken", fsToken);

        try {
            getSupportLoaderManager().initLoader(0, args, this);
            //String token = ((AuthTokenResponse)new EndpointsAsyncTask(this).execute(open).get()).getAuthToken();
            //SharedPreferences.Editor editor = prefs.edit();
            //editor.putString(getString(R.string.backendtoken), token);
            //editor.commit();

            //Toast.makeText(getApplicationContext(), "Login on backend successfull!", Toast.LENGTH_LONG).show();
            //finish(); //Dismiss activity
        } catch (Exception e) {
            //
        }
    }

    private void closeBackendSession(){
        String token = prefs.getString(getString(R.string.backendtoken), "N.A.");
        String[] close = {"Close", token};
        new EndpointsAsyncTask(this).execute(close);
    }

    private void logout() {
        btnLogin.setVisibility(View.GONE);
        logInMessage.setVisibility(View.GONE);
        btnLogout.setVisibility(View.VISIBLE);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeBackendSession();

                // Start the native logout flow.
                SharedPreferences.Editor editor = prefs.edit();
                editor.remove(getString(R.string.userid));
                editor.remove(getString(R.string.foursquaretoken));
                editor.commit();
                startFoursquareLogin();
            }
        });
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
            Log.d(LoginActivity.class.getName(), "Access token: " + accessToken);

            // Persist the token for later use. In this example, we save
            // it to shared prefs.
            TokenStore.get().setToken(accessToken);
            SharedPreferences prefs = getSharedPreferences(getString(R.string.sharedprefs), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putString(getString(R.string.foursquaretoken), accessToken);
            editor.commit();

            getUserId();

            performBackendTokenExchange();

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


    @Override
    public void onLoadFinished(Loader<AuthTokenResponse> loader, AuthTokenResponse token) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(getString(R.string.backendtoken), token.toString());
        editor.commit();

        Toast.makeText(getApplicationContext(), "Login on backend successfull!", Toast.LENGTH_LONG).show();
        finish(); //Dismiss activity
    }

    @Override
    public Loader<AuthTokenResponse> onCreateLoader (int id, Bundle args){
        AuthTokenLoader mSessionLoader = new AuthTokenLoader(this, args.getLong("userId"),args.getString("fsToken"));
        return mSessionLoader;
    }

    @Override
    public void onLoaderReset(Loader<AuthTokenResponse> loader) {

    }
}

class GetJSONTask extends AsyncTask<String, Void , String> {

    protected String doInBackground(String... address) {
        StringBuilder builder = new StringBuilder();
        HttpClient client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(address[0]);
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



