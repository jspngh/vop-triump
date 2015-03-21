package be.ugent.vop.backend;

import java.util.Date;

/**
 * Created by vincent on 12/03/15.
 */
public class CheckinBean {
        private Date date;
        private int points;
        private String userId;
        private String venueId;
        private long groupId;

        public void setDate(Date date){
            this.date = date;
        }

        public Date getDate(){
            return date;
        }

        public void setPoints(int points){
            this.points = points;
        }

        public int getPoints(){
            return this.points;
        }

        public void setVenueId(String venueId){
            this.venueId = venueId;
        }

        public void setGroupId(long groupId){
            this.groupId = groupId;
        }

        public void setUserId(String userId){
            this.userId = userId;
        }

        public String getUserId(){
            return this.userId;
        }

        public String getVenueId(){
            return this.venueId;
        }

        public long getGroupId(){
            return this.groupId;
        }
}
