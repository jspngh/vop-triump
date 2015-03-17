package be.ugent.vop.backend;

/**
 * Created by siebe on 17/02/15.
 */
public class AuthTokenResponse {
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
