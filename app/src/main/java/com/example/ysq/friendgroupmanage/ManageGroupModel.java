package com.example.ysq.friendgroupmanage;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ysq on 16/10/8.
 */

public class ManageGroupModel implements Parcelable {

    public int serverId;

    public String groupName;

    public  int memberNum;

    public  int groupOrder;

    public  int defaultFlag;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.serverId);
        dest.writeString(this.groupName);
        dest.writeInt(this.memberNum);
        dest.writeInt(this.groupOrder);
        dest.writeInt(this.defaultFlag);
    }

    public ManageGroupModel() {
    }

    protected ManageGroupModel(Parcel in) {
        this.serverId = in.readInt();
        this.groupName = in.readString();
        this.memberNum = in.readInt();
        this.groupOrder = in.readInt();
        this.defaultFlag = in.readInt();
    }

    public static final Parcelable.Creator<ManageGroupModel> CREATOR = new Parcelable.Creator<ManageGroupModel>() {
        @Override
        public ManageGroupModel createFromParcel(Parcel source) {
            return new ManageGroupModel(source);
        }

        @Override
        public ManageGroupModel[] newArray(int size) {
            return new ManageGroupModel[size];
        }
    };
}
