package com.kunfei.bookshelf.bean.event;

public class LoadWebUrlEvent {

    private String url;

    public LoadWebUrlEvent(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
