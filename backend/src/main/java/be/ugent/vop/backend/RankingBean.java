package be.ugent.vop.backend;




/**
 * Created by Lars on 01/03/15.
 */
public class RankingBean {
    private GroupBean groupbean;
    private int points;

    public void setGroupBean(GroupBean groupbean) {
        this.groupbean = groupbean;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public GroupBean getGroupBean() {
        return this.groupbean;
    }

    public int getPoints() {
        return points;
    }

}
