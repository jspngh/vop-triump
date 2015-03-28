package be.ugent.vop.backend;

import java.util.Date;
import java.util.List;

/**
 * Created by Lars on 21/03/15.
 */
public class EventBean {
    private VenueBean venue;
    private UserBean user;
    private Date start;
    private Date end;
    private String description;
    private String reward;
    private int minParticipants;
    private int maxParticipants;
    private List<GroupBean> groups;
    private boolean verified;

    public boolean isVerified(){
        return verified;
    }

    public void setVerified(boolean flag){
        verified = flag;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public int getMinParticipants() {
        return minParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public void setMinParticipants(int minParticipants) {
        this.minParticipants = minParticipants;
    }

    public void setGroups(List<GroupBean> groups){this.groups = groups;}

    public List<GroupBean> getGroups() { return groups;}

    public VenueBean getVenue() {
        return venue;
    }

    public void setVenue(VenueBean venue) {
        this.venue = venue;
    }

    public UserBean getOrganizer() {
        return user;
    }

    public void setOrganizer(UserBean organizer) {
        this.user = organizer;
    }


    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReward() {
        return reward;
    }

    public void setReward(String reward) {
        this.reward = reward;
    }
}
