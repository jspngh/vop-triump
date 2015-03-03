package be.ugent.vop.foursquare;

/**
 * Created by siebe on 03/03/15.
 */
public class Photo {
    private String prefix;
    private String suffix;
    private int width;
    private int height;

    public Photo(String prefix, String suffix, int width, int height){
        this.prefix = prefix;
        this.suffix = suffix;
        this.width = width;
        this.height = height;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
