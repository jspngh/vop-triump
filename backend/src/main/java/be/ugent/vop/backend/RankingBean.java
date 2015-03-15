package be.ugent.vop.backend;




/**
 * Created by Lars on 01/03/15.
 */
public class RankingBean {
    private GroupBean groupbean;
    private long points;

    public void setGroupBean(GroupBean groupbean) {
        this.groupbean = groupbean;
    }

    public void setPoints(long points) {
        this.points = points;
    }

    public GroupBean getGroupBean() {
        return this.groupbean;
    }

    public long getPoints() {
        return points;
    }

}
