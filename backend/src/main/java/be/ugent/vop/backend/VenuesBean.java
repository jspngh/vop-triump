package be.ugent.vop.backend;


import java.util.ArrayList;

/**
 * Created by vincent on 11/03/15.
 */
public class VenuesBean {
    private ArrayList<VenueBean> veneus;

    public ArrayList<VenueBean> getVenues(){
        return veneus;
    }

    public void setVenues(ArrayList<VenueBean> veneus){
        this.veneus = veneus;
    }

}
