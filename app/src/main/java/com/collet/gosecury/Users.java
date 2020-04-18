package com.collet.gosecury;

import com.google.firebase.firestore.Exclude;

public class Users {
    String first;
    String last;
    String number;

    public Users( String first, String last, String number) {
        this.first = first;
        this.last = last;
        this.number = number;
    }

    @Exclude
    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
