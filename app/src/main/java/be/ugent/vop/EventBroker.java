package be.ugent.vop;

import java.util.ArrayList;

/**
 * Created by vincent on 28/02/15.
 */
public class EventBroker {

    private static EventBroker instance = new EventBroker();
    private ArrayList<EventListener> listeners;

    private EventBroker(){
        listeners = new ArrayList<EventListener>();
    }

    public static EventBroker get(){
        return instance;
    }

    public void addListener(EventListener listener){
        listeners.add(listener);
    }

    public void addEvent(Event e){
        process(e);
    }

    private void process(Event e){
        for(EventListener l:listeners){
            l.handleEvent(e);
        }
    }

}
