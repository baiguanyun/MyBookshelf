package com.kunfei.bookshelf.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class BookmarksBean {
    /**
     * id : 1
     * name : 微博
     * icon_url : https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1526475741587&di=e7b88e9ca143b1acf483e66f7ea5c52a&imgtype=0&src=http://img.zcool.cn/community/0104e9571c743432f875a399db949b.jpg@1280w_1l_2o_100sh.png
     * url : https://weibo.com/
     * create_time : 2018-05-16T16:55:52
     * status : false
     */
    @Id(autoincrement = true)
    private long id;
    private String name;
    private String icon_url;
    private String url;
    private String create_time;

    @Generated(hash = 329684732)
    public BookmarksBean(long id, String name, String icon_url, String url, String create_time) {
        this.id = id;
        this.name = name;
        this.icon_url = icon_url;
        this.url = url;
        this.create_time = create_time;
    }

    @Generated(hash = 936166190)
    public BookmarksBean() {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon_url() {
        return icon_url;
    }

    public void setIcon_url(String icon_url) {
        this.icon_url = icon_url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

}