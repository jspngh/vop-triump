package be.ugent.vop;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.util.Pair;
import android.widget.Toast;
import android.app.ProgressDialog;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import java.io.IOException;

import be.ugent.vop.backend.myApi.MyApi;

/**
 * Created by Lars on 22/02/15.
 */

class EndpointsAsyncTask extends AsyncTask<String, Void, Object> {
    private static MyApi myApiService = null;
    private Context context;
    ProgressDialog dialog;

    public EndpointsAsyncTask(Context context) {
        this.context = context;
        dialog = new ProgressDialog(context);
    }
    @Override
    protected Object doInBackground(String... params) {
        if(myApiService == null) {  // Only do this once
            MyApi.Builder builder = new MyApi.Builder(AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(), null)
                    // options for running against local devappserver
                    // - 10.0.2.2 is localhost's IP address in Android emulator
                    // - turn off compression when running against local devappserver
                    .setRootUrl("https://triumph-app.appspot.com/_ah/api/")
                    .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                        @Override
                        public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                            abstractGoogleClientRequest.setDisableGZipContent(true);
                        }
                    });
            // end options for devappserver

            myApiService = builder.build();
        }

        String function = params[0];
        switch (function) {
            case "Hello":
                try {
                    return myApiService.sayHi(params[1]).execute().getData();
                } catch (IOException e) {
                    return e.getMessage();
                }
            case "Open":
                try {
                    return myApiService.getAuthToken(Long.parseLong(params[1]),params[2]).execute();
                } catch (IOException e) {
                    return e.getMessage();
                }
            case "Close":
                try {
                    return myApiService.closeSession(params[1]).execute();
                } catch (IOException e) {
                    return e.getMessage();
                }

            default:
                return "test";
        }
    }
    @Override
    protected void onPostExecute(Object result) {
       dialog.dismiss();
    }

}

