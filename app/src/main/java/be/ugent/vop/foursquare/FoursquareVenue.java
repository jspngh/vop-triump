package be.ugent.vop.foursquare;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Vincent on 23/02/15.
 */
public class FoursquareVenue implements Parcelable {
    private String id;
    private String name;
    private String address;
    private String city;
    private String country;
    private double longitude;
    private double latitude;
    private ArrayList<Photo> photos;
    private boolean verified = false;

    public FoursquareVenue(String id, String name, String address,
                           String city, String country, double longitude,
                           double latitude, boolean verified){
        this.id= id;
        this.name = name;
        this.address = address;
        this.city =city;
        this.country =country;
        this.longitude = longitude;
        this.latitude = latitude;
        this.verified = verified;
        photos = new ArrayList<>();
    }

    public void setVerified(boolean flag){
        this.verified = flag;
    }

    public boolean isVerified(){
        return this.verified;
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


    protected FoursquareVenue(Parcel in) {
        id = in.readString();
        name = in.readString();
        address = in.readString();
        city = in.readString();
        country = in.readString();
        longitude = in.readDouble();
        latitude = in.readDouble();
        if (in.readByte() == 0x01) {
            photos = new ArrayList<Photo>();
            in.readList(photos, Photo.class.getClassLoader());
        } else {
            photos = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(address);
        dest.writeString(city);
        dest.writeString(country);
        dest.writeDouble(longitude);
        dest.writeDouble(latitude);
        if (photos == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(photos);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<FoursquareVenue> CREATOR = new Parcelable.Creator<FoursquareVenue>() {
        @Override
        public FoursquareVenue createFromParcel(Parcel in) {
            return new FoursquareVenue(in);
        }

        @Override
        public FoursquareVenue[] newArray(int size) {
            return new FoursquareVenue[size];
        }
    };
}