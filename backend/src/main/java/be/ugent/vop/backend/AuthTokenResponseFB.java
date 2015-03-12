package be.ugent.vop.backend;

/**
 * Created by siebe on 11/03/15.
 */
public class AuthTokenResponseFB {
    private String authToken;
    private String userId;

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
