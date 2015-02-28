package be.ugent.vop;

/**
 * Created by vincent on 28/02/15.
 */
public class Event {
    private String type;

    public Event(String type){
        this.type = type;
    }

    public String getType(){
        return this.type;
    }

    public String toString(){
        return "Event from type: "+type;
    }
}
