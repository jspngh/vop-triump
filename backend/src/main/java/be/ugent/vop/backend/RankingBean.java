package be.ugent.vop.backend;


/**
 * Created by Lars on 01/03/15.
 */
public class RankingBean {

    private long groupId;
    private long points;

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }


    public void setPoints(long points) {
        this.points = points;
    }

    public long getGroupId() {
        return groupId;
    }


    public long getPoints() {
        return points;
    }

}
