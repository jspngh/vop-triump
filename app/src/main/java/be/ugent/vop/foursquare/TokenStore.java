package be.ugent.vop.foursquare;

/**
 * Created by siebe on 12/02/15.
 */
public class TokenStore {
    private static TokenStore sInstance;
    private String token;

    public static TokenStore get() {
        if (sInstance == null) {
            sInstance = new TokenStore();
        }

        return sInstance;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;

    }
}
