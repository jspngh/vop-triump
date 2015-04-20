package be.ugent.vop.backend;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by jonas on 3/19/15.
 */
public class OverviewBean {

    private ArrayList<OverviewCheckin> checkIns;
    private ArrayList<VenueBean> venues;
    private ArrayList<OverviewReward> rewards;
    private ArrayList<newMemberInGroup> newMembers;

    public ArrayList<VenueBean> getVenues() {
        return venues;
    }

    public void setVenues(ArrayList<VenueBean> venues) {
        this.venues = venues;
    }

    public ArrayList<OverviewCheckin> getCheckIns() {
        return checkIns;
    }

    public void setCheckIns(ArrayList<OverviewCheckin> checkIns) {
        this.checkIns = checkIns;
    }

    public ArrayList<OverviewReward> getRewards() {
        return rewards;
    }

    public void setRewards(ArrayList<OverviewReward> rewards) {
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

class OverviewReward{
    private EventBean event;
    private Date date;
    public OverviewReward(EventBean event, Date date){
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

class OverviewCheckin{
    private CheckinBean checkin;
    private UserBean checkinUser;
    private GroupBean checkinGroup;
    private String venueName;

    public OverviewCheckin(CheckinBean checkinBean, UserBean user, GroupBean group){
        this.checkin = checkinBean;
        this.checkinUser = user;
        this.checkinGroup = group;
    }

    public CheckinBean getCheckin() {
        return checkin;
    }

    public void setCheckin(CheckinBean checkin) {
        this.checkin = checkin;
    }

    public UserBean getCheckinUser() {
        return checkinUser;
    }

    public void setCheckinUser(UserBean checkinUser) {
        this.checkinUser = checkinUser;
    }

    public GroupBean getCheckinGroup() {
        return checkinGroup;
    }

    public void setCheckinGroup(GroupBean checkinGroup) {
        this.checkinGroup = checkinGroup;
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }
}