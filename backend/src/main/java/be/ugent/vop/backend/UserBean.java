package be.ugent.vop.backend;

import com.google.appengine.repackaged.org.joda.time.LocalDateTime;

import java.util.Date;

/**
 * Created by siebe on 19/02/15.
 */
public class UserBean {
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private Date joined;

    public void setJoined(Date joined){
        this.joined= joined;
    }

    public Date getJoined(){
        return this.joined;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
