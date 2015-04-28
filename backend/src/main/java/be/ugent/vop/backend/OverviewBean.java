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
    private String memberName;
    private String memberIconUrl;
    private long groupId;
    private String groupName;
    private Date date;
    public newMemberInGroup(UserBean newMember, GroupBean group, Date date){
        memberName = newMember.getFirstName() + " " + newMember.getLastName();
        memberIconUrl = newMember.getProfilePictureUrl();
        groupId = group.getGroupId();
        groupName = group.getName();
        this.date = date;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberIconUrl() {
        return memberIconUrl;
    }

    public void setMemberIconUrl(String memberIconUrl) {
        this.memberIconUrl = memberIconUrl;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}

class OverviewReward{
    private String eventDescription;
    private String eventReward;
    private String venueId;
    private String venueName;
    private Date date;
    public OverviewReward(EventBean event, Date date){
        eventDescription = event.getDescription();
        this.venueId = event.getVenueId();
        eventReward = event.getReward();
        this.date = date;
    }

    public String getEventReward() {
        return eventReward;
    }

    public void setEventReward(String eventReward) {
        this.eventReward = eventReward;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public String getVenueId() {
        return venueId;
    }

    public void setVenueId(String venueId) {
        this.venueId = venueId;
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}

class OverviewCheckin{
    private String memberName;
    private String memberIconUrl;
    private long groupId;
    private String groupName;
    private String venueId;
    private String venueName;
    private Date date;

    public OverviewCheckin(CheckinBean checkinBean, UserBean user, GroupBean group){
        memberName = user.getFirstName() + " " + user.getLastName();
        memberIconUrl = user.getProfilePictureUrl();
        groupId = group.getGroupId();
        groupName = group.getName();
        venueId = checkinBean.getVenueId();
        this.date = checkinBean.getDate();
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberIconUrl() {
        return memberIconUrl;
    }

    public void setMemberIconUrl(String memberIconUrl) {
        this.memberIconUrl = memberIconUrl;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getVenueId() {
        return venueId;
    }

    public void setVenueId(String venueId) {
        this.venueId = venueId;
    }

    public String getVenueName() {
        return venueName;
    }

    public void setVenueName(String venueName) {
        this.venueName = venueName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}