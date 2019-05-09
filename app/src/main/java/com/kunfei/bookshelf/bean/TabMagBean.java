package com.kunfei.bookshelf.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class TabMagBean {
    @Id(autoincrement = true)
    private long id;
    private String title;
    private String url;

    public TabMagBean(String title, String url) {
        this.title = title;
        this.url = url;
    }

    @Generated(hash = 1337838424)
    public TabMagBean(long id, String title, String url) {
        this.id = id;
        this.title = title;
        this.url = url;
    }

    @Generated(hash = 1524275142)
    public TabMagBean() {
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
