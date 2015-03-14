package be.ugent.vop.backend;

import com.google.appengine.repackaged.org.joda.time.LocalDateTime;

import java.util.Date;
import java.util.List;

/**
 * Created by siebe on 19/02/15.
 */
public class GroupBean {

    public static int AMOUNT_SMALL = 10;
    public static int AMOUNT_MEDIUM = 50;
    public static int AMOUNT_BIG = 100;

    public static String TYPE_SMALL = "small";
    public static String TYPE_MEDIUM = "medium";
    public static String TYPE_BIG = "large";

    private long groupId;
    private String name;
    private String adminId;
    private Date created;
    private String description;
    private List<UserBean> members;
    private String type;


    public String getDescription(){
        return this.description;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void setType(String type){
        this.type = type;
    }

    public String getType(){
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public List<UserBean> getMembers() {
        return members;
    }

    public void setMembers(List<UserBean> members) {
        this.members = members;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }


}
