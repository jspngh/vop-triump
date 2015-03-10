package be.ugent.vop.ui.login;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.app.LoaderManager;
import android.content.Loader;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

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

import be.ugent.vop.R;
import be.ugent.vop.backend.BackendAPI;
import be.ugent.vop.backend.myApi.model.AuthTokenResponse;
import be.ugent.vop.backend.myApi.model.CloseSessionResponse;
import be.ugent.vop.foursquare.TokenStore;
import be.ugent.vop.backend.loaders.AuthTokenLoader;
import be.ugent.vop.backend.loaders.EndSessionLoader;

public class LoginFragment extends Fragment {
    private static final String CLIENT_ID = "PZTHHDGA3DTEDWTKRFCRXF5KOXXQN5RCIAM3GYAWKFTMXPLE";
    private static final int REQUEST_CODE_FSQ_CONNECT = 200;
    private static final int REQUEST_CODE_FSQ_TOKEN_EXCHANGE = 201;
    private static final String CLIENT_SECRET = "UQSJN0HCIR0LSBT2PEK3CR331JQJUYSINHZ12MHE0A2CWNNQ";

    public static final String LOGIN_ACTION = "loginaction";
    public static final int LOGIN_FS = 1;
    public static final int LOGIN_BACKEND = 2;
    public static final int LOGOUT = 3;
    public static final int LOGOUT_NOW = 4;
    private Button btnLogin;
    private Button btnLogout;
    private TextView logInMessage;
    private SharedPreferences prefs;

    private Context context = null;
    private static final int AUTH_TOKEN_LOADER = 1;
    private static final int END_SESSION_LOADER = 2;

    private OnFragmentInteractionListener mListener;

    private ProgressDialog connectionDialog;

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("LOGIN ACTIVITY", "starting");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        btnLogin = (Button) rootView.findViewById(R.id.btnLogin);
        btnLogout = (Button) rootView.findViewById(R.id.btnLogout);
        logInMessage = (TextView) rootView.findViewById(R.id.logInMessage);
        return rootView;
    }

    public void onLoggedIn() {
        if (mListener != null) {
            mListener.onLoginFragmentInteraction();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStart(){
        super.onStart();
        context = getActivity();
        prefs = getActivity().getSharedPreferences(getString(R.string.sharedprefs), Context.MODE_PRIVATE);
        connectionDialog = new ProgressDialog(context);
        connectionDialog.setMessage("Talking to the Triump servers...");

        int loginAction = this.getArguments().getInt(LOGIN_ACTION);
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
            case LOGOUT_NOW:
                logOutNow();
                break;
            default:
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * See http://developer.android.com/training/basics/fragments/communicating.html for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onLoginFragmentInteraction();
    }

    private void getUserId (){
        SharedPreferences prefs = getActivity().getSharedPreferences(getString(R.string.sharedprefs), Context.MODE_PRIVATE);
        String token = prefs.getString(getString(R.string.foursquaretoken), "N.A.");
        String base = "https://api.foursquare.com/v2/users/self?oauth_token=";
        String version = "&v=20150221";
        final String address = base.concat(token).concat(version);
        final Handler h = new Handler(){
            @Override
            public void handleMessage(Message msg){
                if(msg.what == 1)
                    performBackendTokenExchange();
            }
        };
        Runnable runnable = new Runnable(){
            @Override
            public void run(){
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
                String userInfo = builder.toString();
                try {
                    JSONObject jsonObject = new JSONObject(userInfo);
                    String userId = jsonObject.getJSONObject("response").getJSONObject("user").getString("id");
                    SharedPreferences prefs = getActivity().getSharedPreferences(getString(R.string.sharedprefs), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong(getString(R.string.userid), Long.valueOf(userId).longValue());
                    editor.commit();
                    h.sendEmptyMessage(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(runnable).start();
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
            Intent intent = FoursquareOAuth.getConnectIntent(context, CLIENT_ID);

            // If the device does not have the Foursquare app installed, we'd
            // get an intent back that would open the Play Store for download.
            // Otherwise we start the auth flow.
            if (FoursquareOAuth.isPlayStoreIntent(intent)) {
                Log.e("onClick :", getString(R.string.app_not_installed_message));
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
        Log.d("fsToken and userId", fsToken + userId);

        try {
            getLoaderManager().initLoader(AUTH_TOKEN_LOADER, args, mAuthTokenLoaderListener);
        } catch (Exception e) {
            //
        }
    }

    private void closeBackendSession(){
        getLoaderManager().initLoader(END_SESSION_LOADER, null, mEndSessionLoaderListener);
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

    public void logOutNow(){
        closeBackendSession();

        // Start the native logout flow.
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(getString(R.string.userid));
        editor.remove(getString(R.string.foursquaretoken));
        editor.commit();
        startFoursquareLogin();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                Log.e("onCompleteConnect:", "Canceled");

            } else if (exception instanceof FoursquareDenyException) {
                // Deny.
                Log.e("onCompleteConnect:", "Denied");

            } else if (exception instanceof FoursquareOAuthException) {
                // OAuth error.
                String errorMessage = exception.getMessage();
                String errorCode = ((FoursquareOAuthException) exception).getErrorCode();
                Log.e("onCompleteConnect:", errorMessage + " [" + errorCode + "]");

            } else if (exception instanceof FoursquareUnsupportedVersionException) {
                // Unsupported Fourquare app version on the device.
                Log.e("onCompleteConnect:", "Foursquare version unsupported");

            } else if (exception instanceof FoursquareInvalidRequestException) {
                // Invalid request.
                Log.e("onCompleteConnect:", "Invalid Request");
            } else {
                // Error.
                Log.e("onCompleteConnect:", "Error");
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
            SharedPreferences prefs = getActivity().getSharedPreferences(getString(R.string.sharedprefs), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            editor.putString(getString(R.string.foursquaretoken), accessToken);
            editor.commit();

            connectionDialog.show();

            getUserId();

        } else {
            if (exception instanceof FoursquareOAuthException) {
                // OAuth error.
                String errorMessage = (exception).getMessage();
                String errorCode = ((FoursquareOAuthException) exception).getErrorCode();
                Log.e("CompleteTokenExchange:", errorMessage + " [" + errorCode + "]");
            } else {
                // Other exception type.
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
        Intent intent = FoursquareOAuth.getTokenExchangeIntent(context, CLIENT_ID, CLIENT_SECRET, code);
        startActivityForResult(intent, REQUEST_CODE_FSQ_TOKEN_EXCHANGE);
    }

    /**********************************
     End Foursquare API
     **********************************/
//endregion

    private LoaderManager.LoaderCallbacks<AuthTokenResponse> mAuthTokenLoaderListener
            = new LoaderManager.LoaderCallbacks<AuthTokenResponse>() {
        @Override
        public void onLoadFinished(Loader<AuthTokenResponse> loader, AuthTokenResponse token) {
            SharedPreferences.Editor editor = prefs.edit();
            Log.d("FUCK LOGIN", token.getAuthToken());
            editor.putString(getString(R.string.backendtoken), token.getAuthToken());
            editor.commit();

            BackendAPI.get(context).setToken(token.getAuthToken());
            connectionDialog.dismiss();
            onLoggedIn();
        }

        @Override
        public Loader<AuthTokenResponse> onCreateLoader (int id, Bundle args){
            AuthTokenLoader mLoader = new AuthTokenLoader(context, args.getLong("userId"),args.getString("fsToken"));
            return mLoader;
        }

        @Override
        public void onLoaderReset(Loader<AuthTokenResponse> loader) {

        }
    };

    private LoaderManager.LoaderCallbacks<CloseSessionResponse> mEndSessionLoaderListener
            = new LoaderManager.LoaderCallbacks<CloseSessionResponse>() {
        @Override
        public void onLoadFinished(Loader<CloseSessionResponse> loader, CloseSessionResponse response) {
            Log.d("Backendsession closed: ", response.getMessage());
        }

        @Override
        public Loader<CloseSessionResponse> onCreateLoader (int id, Bundle args){
            EndSessionLoader mLoader = new EndSessionLoader(context);
            return mLoader;
        }

        @Override
        public void onLoaderReset(Loader<CloseSessionResponse> loader) {
        }
    };
}
