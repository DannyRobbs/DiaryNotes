package com.example.dairynotes;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;

public class notesModel implements Parcelable {
    private String title;
    private ArrayList<customType> noteItemList = new ArrayList<customType>();

    public notesModel(String title) {
        this.title = title;

    }


    protected notesModel(Parcel in) {
        title = in.readString();
    }

    public static final Creator<notesModel> CREATOR = new Creator<notesModel>() {
        @Override
        public notesModel createFromParcel(Parcel in) {
            return new notesModel(in);
        }

        @Override
        public notesModel[] newArray(int size) {
            return new notesModel[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public void addItemToNote(Object o,String t,String n){
        noteItemList.add(new customType(o,t,n));
    }
    public void removeItemFromNote(Object o,String t,String n){
       Log.e("contains item? : ",String.valueOf(noteItemList.contains(new customType(o,t,n)))) ;
    }
    public ArrayList<customType> getNoteItemList(){
        return noteItemList;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
    }
}
