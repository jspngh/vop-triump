package be.ugent.vop.backend;



import com.google.appengine.repackaged.org.joda.time.LocalDateTime;

import java.util.Date;
import java.util.List;

/**
 * Created by Lars on 19/02/15.
 */
public class VenueBean {
    private long venueId;
    private String adminId;
    //picture
    private String city;
    private String street;
    private String houseNr;
    private double longitude;
    private double latitude;
    private int type;
    private String description;
    private Date created;
    private List<RankingBean> ranking;
    private double currentDistance;

    public void setCurrentDistance(double currentDistance){
           this.currentDistance = currentDistance;
    }

    public double getCurrentDistance(){
        return this.currentDistance;
    }

    public void setAdminId(String adminId) {
        this.adminId= adminId;
    }

    public void setCity(String city) {
        this.city= city;
    }

    public void setStreet(String street) {
        this.street= street;
    }

    public void setHouseNr(String houseNr) {
        this.houseNr= houseNr;
    }

    public void setDescription(String description) {
        this.description= description;
    }

    public void setCreated(Date createdDate){ this.created = createdDate; }

    public void setType(int type){this.type = type;}

    public void setLongitude(double longitude){ this.longitude = longitude;}

    public void setLatitude(double latitude){ this.latitude = latitude;}

    public String getCity(){
        return city;
    }

    public String getStreet(){
        return street;
    }

    public String getHouseNr(){
        return houseNr;
    }

    public String getDescription(){
        return description;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getAdminId(){
        return adminId;
    }

    public Date getCreated(){
        return created;
    }

    public void setVenueId(long venueId) {
        this.venueId= venueId;
    }

    public long getVenueId() { return venueId; }

    public List<RankingBean> getRanking() {
        return ranking;
    }
    public void setRanking(List<RankingBean> ranking) {
        this.ranking= ranking;
    }
}
