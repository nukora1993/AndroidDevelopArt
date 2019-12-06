package com.example.chapter2.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    public int userId;
    public String userName;
    public boolean isMale;

    public Book book;
    public User(){

    }

    public User(int userId,String userName,boolean isMale){
        this.userId=userId;
        this.userName=userName;
        this.isMale=isMale;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(userId);
        dest.writeString(userName);
        dest.writeInt(isMale?1:0);
        dest.writeParcelable(book,0);
    }

    public static final Parcelable.Creator<User> CREATOR=new Parcelable.Creator<User>(){
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        public User[] newArray(int size){
            return new User[size];
        }
    };

    private User(Parcel source){
        userId=source.readInt();
        userName=source.readString();
        isMale=source.readInt()==1;
        book=source.readParcelable(Thread.currentThread().getContextClassLoader());
    }


}
