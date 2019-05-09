package com.kunfei.bookshelf.bean.event;


public class MainSearchEvent {

    private String url;

    public MainSearchEvent(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
