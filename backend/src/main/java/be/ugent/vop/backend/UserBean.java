package be.ugent.vop.backend;

/**
 * Created by siebe on 19/02/15.
 */
public class UserBean {
    private long fsUserId;
    private String firstName;
    private String lastName;
    private String email;

    public long getFsUserId() {
        return fsUserId;
    }

    public void setFsUserId(long fsUserId) {
        this.fsUserId = fsUserId;
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
