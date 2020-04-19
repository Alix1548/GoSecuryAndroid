package com.collet.gosecury;

import com.google.firebase.ml.vision.text.FirebaseVisionText;

import java.util.ArrayList;
import java.util.List;

public class Users {
    String first;
    String last;
    String number;
    ArrayList<String> dateList;

    public Users(){

    }

    public Users(String first, String last, String number, ArrayList<String> dateList) {
        this.first = first;
        this.last = last;
        this.number = number;
        this.dateList = dateList;
    }

    public String getFirst() {
        return first;
    }

    public String getLast() {
        return last;
    }

    public String getNumber() {
        return number;
    }

    public ArrayList<String> getDateList() {
        return dateList;
    }
}
