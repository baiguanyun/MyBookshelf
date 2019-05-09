package com.kunfei.bookshelf.bean;

public class HomeMenuBean {

    private String name;
    private int icon;

    public HomeMenuBean() {
    }

    public HomeMenuBean(String name, int icon) {
        this.name = name;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
