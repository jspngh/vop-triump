package be.ugent.vop.backend;

import java.util.List;

/**
 * Created by siebe on 20/02/15.
 */
public class AllGroupsBean {
    private int numGroups;
    private List<GroupBean> groups;

    public int getNumGroups() {
        return numGroups;
    }

    public void setNumGroups(int numGroups) {
        this.numGroups = numGroups;
    }

    public List<GroupBean> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupBean> groups) {
        this.groups = groups;
    }
}
