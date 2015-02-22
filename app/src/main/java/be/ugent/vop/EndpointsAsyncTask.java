package be.ugent.vop;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.util.Pair;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import java.io.IOException;

import be.ugent.vop.backend.myApi.MyApi;

/**
 * Created by Lars on 22/02/15.
 */

   public final class EndpointsAsyncTask {
    public final MyApi myApiService;

     //Constructor

       public EndpointsAsyncTask(){
           MyApi.Builder builder = new MyApi.Builder(AndroidHttp.newCompatibleTransport(),
                   new AndroidJsonFactory(), null)
                   .setRootUrl("https://triumph-app.appspot.com/_ah/api/")
                   .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                       @Override
                       public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                           abstractGoogleClientRequest.setDisableGZipContent(true);
                       }
                   });
           myApiService = builder.build();
       }

   }

