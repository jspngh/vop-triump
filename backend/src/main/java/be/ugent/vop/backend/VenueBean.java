package be.ugent.vop.backend;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Lars on 19/02/15.
 */
public class VenueBean {
    private String venueId;
    private HashMap<Long,Long> ranking;

    public String getVenueId() {
        return venueId;
    }

    public void setVenueId(String venueId) {
        this.venueId= venueId;
    }

    public HashMap<Long,Long> getRanking() {
        return ranking;
    }

    public void setRanking(HashMap<Long,Long> ranking) {
        this.ranking= ranking;
    }

}
