package be.ugent.vop.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.widget.LoginButton;

import java.util.Arrays;

import be.ugent.vop.R;

/**
 * Created by siebe on 11/03/15.
 */
public class LoginActivity2 extends Activity{
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login2);

        LoginButton authButton = (LoginButton) findViewById(R.id.authButton);
        authButton.setReadPermissions(Arrays.asList("public_profile"));
        authButton.setSessionStatusCallback(new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                Log.d("LoginActivity2", state.toString());

                if(state.isOpened()){
                    new Request(session,
                            "/me",
                            null,
                            HttpMethod.GET,
                            new Request.Callback(){

                                @Override
                                public void onCompleted(Response response) {
                                    Log.d("LoginActivity2", response.getRawResponse());
                                }
                            }).executeAsync();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode,
                resultCode, data);
    }
}
