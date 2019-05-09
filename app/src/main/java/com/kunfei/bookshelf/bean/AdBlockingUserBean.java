package com.kunfei.bookshelf.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class AdBlockingUserBean {
    @Unique
    private int id;
    private String hostName;
    private String host;

    public AdBlockingUserBean(String hostName, String host) {
        this.hostName = hostName;
        this.host = host;
    }

    public AdBlockingUserBean(String hostName) {
        this.hostName = hostName;
    }

    @Generated(hash = 808569166)
    public AdBlockingUserBean(int id, String hostName, String host) {
        this.id = id;
        this.hostName = hostName;
        this.host = host;
    }

    @Generated(hash = 990767519)
    public AdBlockingUserBean() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
