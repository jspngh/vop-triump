package be.ugent.vop.backend;


import java.util.List;

/**
 * Created by Lars on 19/02/15.
 */
public class VenueBean {
    private String venueId;
    private List<RankingBean> ranking;

    public String getVenueId() {
        return venueId;
    }

    public void setVenueId(String venueId) {
        this.venueId= venueId;
    }

    public List<RankingBean> getRanking() {
        return ranking;
    }

    public void setRanking(List<RankingBean> ranking) {
        this.ranking= ranking;
    }

}
