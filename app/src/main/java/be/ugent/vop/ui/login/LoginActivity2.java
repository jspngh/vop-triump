package be.ugent.vop.ui.login;

import android.app.Activity;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

import java.util.Arrays;

import be.ugent.vop.R;
import be.ugent.vop.backend.BackendAPI;
import be.ugent.vop.backend.loaders.AuthTokenLoaderFB;
import be.ugent.vop.backend.loaders.EndSessionLoader;
import be.ugent.vop.backend.myApi.model.AuthTokenResponseFB;
import be.ugent.vop.backend.myApi.model.CloseSessionResponse;
import be.ugent.vop.ui.main.MainActivity;

public class LoginActivity2 extends Activity{
    private SharedPreferences prefs;
    private Context context = this;

    private static final int AUTH_TOKEN_LOADER = 1;
    private static final int END_SESSION_LOADER = 2;
    private ProgressDialog connectionDialog;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login2);

        prefs = getSharedPreferences(getString(R.string.sharedprefs), Context.MODE_PRIVATE);

        connectionDialog = new ProgressDialog(context);
        connectionDialog.setMessage("Talking to the Triump servers...");

        LoginButton authButton = (LoginButton) findViewById(R.id.authButton);
        authButton.setReadPermissions(Arrays.asList("public_profile"));
        authButton.setSessionStatusCallback(new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                Log.d("LoginActivity2", state.toString());

                if(state.isOpened())
                    startBackendLogin();
                else if(state.isClosed())
                    closeBackendSession();
            }
        });
    }

    private void startBackendLogin(){
        // Show progress dialog
        connectionDialog.show();

        // Close sessions that might still be open
        if(prefs.contains(getString(R.string.backendtoken)))
            closeBackendSession();

        // Get user info from FB servers
        Request.newMeRequest(Session.getActiveSession(),
                new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        performBackendTokenExchange(user.getId(), Session.getActiveSession().getAccessToken());
                    }
                }).executeAsync();
    }

    private void performBackendTokenExchange(String userId, String accessToken){
        Bundle args = new Bundle(2);
        args.putString("userId", userId);
        args.putString("accessToken", accessToken);
        Log.d("accessToken", accessToken);
        Log.d("userId ", userId);

        try {
            getLoaderManager().initLoader(AUTH_TOKEN_LOADER, args, mAuthTokenLoaderListener);
        } catch (Exception e) {
            //
        }
    }

    private void closeBackendSession(){
        getLoaderManager().initLoader(END_SESSION_LOADER, null, mEndSessionLoaderListener);
    }

    private void onLoggedIn() {
        Intent main = new Intent(this, MainActivity.class);
        startActivity(main);
        finish();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode,
                resultCode, data);
    }

    private LoaderManager.LoaderCallbacks<AuthTokenResponseFB> mAuthTokenLoaderListener
            = new LoaderManager.LoaderCallbacks<AuthTokenResponseFB>() {
        @Override
        public void onLoadFinished(Loader<AuthTokenResponseFB> loader, AuthTokenResponseFB token) {
            SharedPreferences.Editor editor = prefs.edit();
            Log.d("FUCK LOGIN", token.getAuthToken());
            editor.putString(getString(R.string.backendtoken), token.getAuthToken());
            editor.apply();

            BackendAPI.get(context).setToken(token.getAuthToken());
            connectionDialog.dismiss();
            onLoggedIn();
        }

        @Override
        public Loader<AuthTokenResponseFB> onCreateLoader (int id, Bundle args){
            return new AuthTokenLoaderFB(context, args.getString("userId"),args.getString("accessToken"));
        }

        @Override
        public void onLoaderReset(Loader<AuthTokenResponseFB> loader) {

        }
    };



    private LoaderManager.LoaderCallbacks<CloseSessionResponse> mEndSessionLoaderListener
            = new LoaderManager.LoaderCallbacks<CloseSessionResponse>() {
        @Override
        public void onLoadFinished(Loader<CloseSessionResponse> loader, CloseSessionResponse response) {
            Log.d("Backendsession closed: ", response.getMessage());
            prefs.edit().remove(getString(R.string.backendtoken)).apply();
        }

        @Override
        public Loader<CloseSessionResponse> onCreateLoader (int id, Bundle args){
            return new EndSessionLoader(context);
        }

        @Override
        public void onLoaderReset(Loader<CloseSessionResponse> loader) {
        }
    };
}
