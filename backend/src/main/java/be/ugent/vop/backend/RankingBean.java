package be.ugent.vop.backend;


/**
 * Created by Lars on 01/03/15.
 */
public class RankingBean {

    private GroupBean group;
    private VenueBean venue;
    private long points;

    public void setGroup(GroupBean group) {
        this.group = group;
    }

    public void setVenue(VenueBean venue) {
        this.venue = venue;
    }

    public void setPoints(long points) {
        this.points = points;
    }

    public GroupBean getGroup() {
        return group;
    }

    public VenueBean getVenue() {
        return venue;
    }

    public long getPoints() {
        return points;
    }

}
