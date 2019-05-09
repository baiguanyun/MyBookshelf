package com.kunfei.bookshelf.bean;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class SearchplBean {
    /**
     * id : 1
     * name : 搜狗
     * pinyin : sogou
     * url : https://wap.sogou.com/web/sl?bid=sogou-appi-19b1b73d63d4c9ea&keyword=
     */
    @Id(autoincrement = true)
    private long id;
    private String name;
    private String pinyin;
    private String url;

    @Generated(hash = 1765229068)
    public SearchplBean(long id, String name, String pinyin, String url) {
        this.id = id;
        this.name = name;
        this.pinyin = pinyin;
        this.url = url;
    }

    @Generated(hash = 950424966)
    public SearchplBean() {
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

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}