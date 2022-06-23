package com.example.mobileproject.model;

public enum ChapterStatus {
    EMPTY(1),
    DOWNLOADING(2),
    ERROR(3),
    DOWNLOADED(4);

    private int numVal;

    ChapterStatus(int numVal) {
        this.numVal = numVal;
    }

    public int getNumVal() {
        return numVal;
    }

    public ChapterStatus setValue( int value){
        switch(value){
            case 1: return ChapterStatus.EMPTY;
            case 2: return ChapterStatus.DOWNLOADING;
            case 4: return ChapterStatus.DOWNLOADED;
            case 3:

            default:
                return ChapterStatus.ERROR;
        }
    }

}
