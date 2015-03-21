package be.ugent.vop.backend;



import java.util.Date;

/**
 * Created by Lars on 19/02/15.
 */
public class VenueBean {
    private String adminId;

    private boolean verified;
    private Date firstCheckin;
    private String VenueId;

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public Date getFirstCheckin() {
        return firstCheckin;
    }

    public void setFirstCheckin(Date firstCheckin) {
        this.firstCheckin = firstCheckin;
    }

    public String getVenueId() {
        return VenueId;
    }

    public void setVenueId(String VenueId) {
        this.VenueId = VenueId;
    }

}
