package be.ugent.vop.foursquare;


import java.util.ArrayList;

/**
 * Created by Vincent on 23/02/15.
 */
public class FoursquareVenue {
    private String id;
    private String name;
    private String address;
    private String city;
    private String country;
    private double longitude;
    private double latitude;
    private ArrayList<Photo> photos;

    public FoursquareVenue(String id, String name, String address, String city,String country, double longitude, double latitude){
        this.id= id;
        this.name = name;
        this.address = address;
        this.city =city;
        this.country =country;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getAddress(){
        return address;
    }

    public String getCountry(){
        return country;
    }

    public String getCity(){
        return city;
    }

    public double getLongitude(){
        return longitude;
    }

    public double getLatitude(){
        return latitude;
    }

    public void setPhotos(ArrayList<Photo> photos){
        this.photos = photos;
    }

    public ArrayList<Photo> getPhotos(){
        return this.photos;
    }


    public String toString(){
        return "FoursquareVenue id:"+id+" name: "+name;
    }




}
