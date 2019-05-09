package com.kunfei.bookshelf.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class WebHistoryBean {

    @Id(autoincrement = true)
    private long id;
    private String time;
    private String title;
    private String webUrl;
    private String icon;

    public WebHistoryBean(String time, String title, String webUrl, String icon) {
        this.time = time;
        this.title = title;
        this.webUrl = webUrl;
        this.icon = icon;
    }

    @Generated(hash = 749582714)
    public WebHistoryBean(long id, String time, String title, String webUrl,
                          String icon) {
        this.id = id;
        this.time = time;
        this.title = title;
        this.webUrl = webUrl;
        this.icon = icon;
    }

    @Generated(hash = 407221786)
    public WebHistoryBean() {
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
