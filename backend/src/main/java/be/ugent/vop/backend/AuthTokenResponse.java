package be.ugent.vop.backend;

/**
 * Created by siebe on 17/02/15.
 */
public class AuthTokenResponse {
    private String authToken;
    private long userId;

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
