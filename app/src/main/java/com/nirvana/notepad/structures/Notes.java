package com.nirvana.notepad.structures;

public class Notes {

    private int serialNo;
    private String title;
    private String content;
    private String dateTime;
    private boolean selected;

    public Notes(int serialNo, String title, String content, String dateTime) {
        this.serialNo = serialNo;
        this.title = title;
        this.content = content;
        this.dateTime = dateTime;
    }

    public int getSerialNo() {
        return serialNo;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getDateTime() {
        return dateTime;
    }

    public boolean getSelected() {
        return selected;
    }

    public void setSelected(boolean selectedState) {
        this.selected = selectedState;
    }
}
