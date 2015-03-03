package be.ugent.vop.backend;


/**
 * Created by Lars on 01/03/15.
 */
public class RankingBean {

    private long groupId;
    private VenueBean venue;
    private long points;

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public void setVenue(VenueBean venue) {
        this.venue = venue;
    }

    public void setPoints(long points) {
        this.points = points;
    }

    public long getGroupId() {
        return groupId;
    }

    public VenueBean getVenue() {
        return venue;
    }

    public long getPoints() {
        return points;
    }

}
