package be.ugent.vop;

/**
 * Created by vincent on 03/03/15.
 */

/**
 *
 * Mogelijks tijdelijke klasse 'Group', had deze nodig om de ranking te kunnen opstellen in venueFragment.
 *
 */
public class Group {
    private String name;
    private int points;

    public Group(String name, int points){
        this.name = name;
        this.points = points;
    }

    public String getName(){
        return name;
    }

    public int getPoints(){
        return points;
    }
}
