package be.ugent.vop.backend;

import java.util.List;

/**
 * Created by Lars on 01/03/15.
 */
public class EventRewardBean {
    private List<EventBean> events;
    private List<EventBean> rewards;

    public List<EventBean> getEvents() {
        return events;
    }

    public void setEvents(List<EventBean> events) {
        this.events = events;
    }

    public List<EventBean> getRewards() {
        return rewards;
    }

    public void setRewards(List<EventBean> rewards) {
        this.rewards = rewards;
    }
}
