package com.akoraingdkb.notesapp;

// The model class for the recycler view adapter
class Note {
    private String title;
    private String firstLetter;

    Note() {
    }

    String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    String getFirstLetter() {
        return firstLetter;
    }

    void setFirstLetter(String firstLetter) {
        this.firstLetter = firstLetter;
    }
}
