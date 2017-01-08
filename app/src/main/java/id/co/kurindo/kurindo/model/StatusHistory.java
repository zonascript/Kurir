package id.co.kurindo.kurindo.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by dwim on 1/3/2017.
 */

public class StatusHistory implements Parcelable {
    private String status;
    private String remarks;
    private User pic;
    private String location;
    private String created_date;
    private User created_by;


    protected StatusHistory(Parcel in) {
        status = in.readString();
        remarks = in.readString();
        location = in.readString();
        created_date = in.readString();
        pic = in.readParcelable(User.class.getClassLoader());
        created_by = in.readParcelable(User.class.getClassLoader());
    }

    public static final Creator<StatusHistory> CREATOR = new Creator<StatusHistory>() {
        @Override
        public StatusHistory createFromParcel(Parcel in) {
            return new StatusHistory(in);
        }

        @Override
        public StatusHistory[] newArray(int size) {
            return new StatusHistory[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(status);
        dest.writeString(remarks);
        dest.writeString(location);
        dest.writeString(created_date);
        dest.writeParcelable(pic, flags);
        dest.writeParcelable(created_by, flags);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public User getPic() {
        return pic;
    }

    public void setPic(User pic) {
        this.pic = pic;
    }

    public User getCreated_by() {
        return created_by;
    }

    public void setCreated_by(User created_by) {
        this.created_by = created_by;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCreated_date() {
        return created_date;
    }

    public void setCreated_date(String created_date) {
        this.created_date = created_date;
    }

}