package be.ugent.vop.backend;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.*;



@SuppressWarnings("serial")
public class CronServlet extends HttpServlet {
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        try{
            URL url = new URL("https://mystic-impulse-87918.appspot.com/_ah/api/myApi/v1/generateRewards");

            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            // optional default is GET
            con.setRequestMethod("GET");

            //add request header
            con.setRequestProperty("User-Agent", "Cron");

            int responseCode = con.getResponseCode();


        }catch(Exception e){

        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
}