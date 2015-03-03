package be.ugent.vop.ui.group;

/**
 * Created by vincent on 03/03/15.
 */

/**
 *
 * Mogelijks tijdelijke klasse 'Group', had deze nodig om de ranking te kunnen opstellen in venueFragment.
 *
 */
public class Group {
    private long id;
    private String name;
    private long points;

    public Group(long id, String name, long points){
        this.name = name;
        this.points = points;
        this.id = id;
    }

    public String getName(){
        return name;
    }

    public long getPoints(){
        return points;
    }

    public long getId() { return id; }
}
