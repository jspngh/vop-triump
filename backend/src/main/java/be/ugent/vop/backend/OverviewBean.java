package be.ugent.vop.backend;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by jonas on 3/19/15.
 */
public class OverviewBean {

    private ArrayList<CheckinBean> checkIns;
    private ArrayList<VenueBean> venues;
    private ArrayList<Reward> rewards;
    private ArrayList<newMemberInGroup> newMembers;

    public ArrayList<VenueBean> getVenues() {
        return venues;
    }

    public void setVenues(ArrayList<VenueBean> venues) {
        this.venues = venues;
    }

    public ArrayList<CheckinBean> getCheckIns() {
        return checkIns;
    }

    public void setCheckIns(ArrayList<CheckinBean> checkIns) {
        this.checkIns = checkIns;
    }

    public ArrayList<Reward> getRewards() {
        return rewards;
    }

    public void setRewards(ArrayList<Reward> rewards) {
        this.rewards = rewards;
    }

    public ArrayList<newMemberInGroup> getNewMembers() {
        return newMembers;
    }

    public void setNewMembers(ArrayList<newMemberInGroup> newMembers) {
        this.newMembers = newMembers;
    }
}

class newMemberInGroup{
    private UserBean member;
    private GroupBean group;
    private Date date;
    public newMemberInGroup(UserBean newMember, GroupBean group, Date date){
        member = newMember;
        this.group = group;
        this.date = date;
    }

    public UserBean getMember() {
        return member;
    }

    public void setMember(UserBean member) {
        this.member = member;
    }

    public GroupBean getGroup() {
        return group;
    }

    public void setGroup(GroupBean group) {
        this.group = group;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}

class Reward{
    private EventBean event;
    private Date date;
    public Reward(EventBean event, Date date){
        this.event = event;
        this.date = date;
    }

    public EventBean getEvent() {
        return event;
    }

    public void setEvent(EventBean event) {
        this.event = event;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}