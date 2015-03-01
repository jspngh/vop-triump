package be.ugent.vop.foursquare;

/**
 * Created by jonas on 1-3-2015.
 */
public class FoursquareCheckinObject {
    private String id;
    private String type;
    private String createdAt;
    private String timeZoneOffset;

    public FoursquareCheckinObject(){
        id = "N.A.";
        type = "N.A.";
        createdAt = "N.A.";
        timeZoneOffset = "N.A.";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }


    public String getTimeZoneOffset() {
        return timeZoneOffset;
    }

    public void setTimeZoneOffset(String timeZoneOffset) {
        this.timeZoneOffset = timeZoneOffset;
    }
}
