package com.kunfei.bookshelf.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class CollectionBean {

    /**
     * id : 2
     * user_id : 82571
     * title : 天猫TMALL
     * url : https://www.tmall.com/
     * create_time : 2018-05-30 15:36:02
     */
    @Id(autoincrement = true)
    private long id;
    private int collectionId;

    private int user_id;
    @Unique
    private String title;
    @Unique
    private String url;
    private String create_time;
    private String isUpload;

    public CollectionBean(int collectionId, String title, String url, String create_time, String isUpload) {
        this.collectionId = collectionId;
        this.title = title;
        this.url = url;
        this.create_time = create_time;
        this.isUpload = isUpload;
    }

    public CollectionBean(String title, String url, String create_time, String isUpload) {
        this.title = title;
        this.url = url;
        this.create_time = create_time;
        this.isUpload = isUpload;
    }

    @Generated(hash = 172288685)
    public CollectionBean(long id, int collectionId, int user_id, String title, String url, String create_time,
                          String isUpload) {
        this.id = id;
        this.collectionId = collectionId;
        this.user_id = user_id;
        this.title = title;
        this.url = url;
        this.create_time = create_time;
        this.isUpload = isUpload;
    }

    @Generated(hash = 1423617684)
    public CollectionBean() {
    }

    public int getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(int collectionId) {
        this.collectionId = collectionId;
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

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
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

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getIsUpload() {
        return isUpload;
    }

    public void setIsUpload(String isUpload) {
        this.isUpload = isUpload;
    }
}
