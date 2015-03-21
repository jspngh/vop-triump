package be.ugent.vop.backend;

import java.util.ArrayList;

/**
 * Created by jonas on 3/19/15.
 */
public class OverviewBean {
    private GroupBean group;
    private ArrayList<VenueBean> venues;
    //private EventBean update;

    public ArrayList<VenueBean> getVenues() {
        return venues;
    }

    public void setVenues(ArrayList<VenueBean> venues) {
        this.venues = venues;
    }

    public GroupBean getGroup() {
        return group;
    }

    public void setGroup(GroupBean group) {
        this.group = group;
    }

/*
    public EventBean getUpdate() {
        return update;
    }

    public void setUpdate(EventBean update) {
        this.update = update;
    }
*/
}
