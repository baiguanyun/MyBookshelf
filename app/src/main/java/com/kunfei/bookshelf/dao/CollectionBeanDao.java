package com.kunfei.bookshelf.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.kunfei.bookshelf.bean.CollectionBean;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * DAO for table "COLLECTION_BEAN".
 */
public class CollectionBeanDao extends AbstractDao<CollectionBean, Long> {

    public static final String TABLENAME = "COLLECTION_BEAN";

    public CollectionBeanDao(DaoConfig config) {
        super(config);
    }


    public CollectionBeanDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /**
     * Creates the underlying database table.
     */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists ? "IF NOT EXISTS " : "";
        db.execSQL("CREATE TABLE " + constraint + "\"COLLECTION_BEAN\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL ," + // 0: id
                "\"COLLECTION_ID\" INTEGER NOT NULL ," + // 1: collectionId
                "\"USER_ID\" INTEGER NOT NULL ," + // 2: user_id
                "\"TITLE\" TEXT UNIQUE ," + // 3: title
                "\"URL\" TEXT UNIQUE ," + // 4: url
                "\"CREATE_TIME\" TEXT," + // 5: create_time
                "\"IS_UPLOAD\" TEXT);"); // 6: isUpload
    }

    /**
     * Drops the underlying database table.
     */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"COLLECTION_BEAN\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, CollectionBean entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getId());
        stmt.bindLong(2, entity.getCollectionId());
        stmt.bindLong(3, entity.getUser_id());

        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(4, title);
        }

        String url = entity.getUrl();
        if (url != null) {
            stmt.bindString(5, url);
        }

        String create_time = entity.getCreate_time();
        if (create_time != null) {
            stmt.bindString(6, create_time);
        }

        String isUpload = entity.getIsUpload();
        if (isUpload != null) {
            stmt.bindString(7, isUpload);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, CollectionBean entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getId());
        stmt.bindLong(2, entity.getCollectionId());
        stmt.bindLong(3, entity.getUser_id());

        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(4, title);
        }

        String url = entity.getUrl();
        if (url != null) {
            stmt.bindString(5, url);
        }

        String create_time = entity.getCreate_time();
        if (create_time != null) {
            stmt.bindString(6, create_time);
        }

        String isUpload = entity.getIsUpload();
        if (isUpload != null) {
            stmt.bindString(7, isUpload);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.getLong(offset + 0);
    }

    @Override
    public CollectionBean readEntity(Cursor cursor, int offset) {
        CollectionBean entity = new CollectionBean( //
                cursor.getLong(offset + 0), // id
                cursor.getInt(offset + 1), // collectionId
                cursor.getInt(offset + 2), // user_id
                cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // title
                cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // url
                cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // create_time
                cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6) // isUpload
        );
        return entity;
    }

    @Override
    public void readEntity(Cursor cursor, CollectionBean entity, int offset) {
        entity.setId(cursor.getLong(offset + 0));
        entity.setCollectionId(cursor.getInt(offset + 1));
        entity.setUser_id(cursor.getInt(offset + 2));
        entity.setTitle(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setUrl(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setCreate_time(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setIsUpload(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
    }

    @Override
    protected final Long updateKeyAfterInsert(CollectionBean entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }

    @Override
    public Long getKey(CollectionBean entity) {
        if (entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(CollectionBean entity) {
        throw new UnsupportedOperationException("Unsupported for entities with a non-null key");
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }

    /**
     * Properties of entity CollectionBean.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, long.class, "id", true, "_id");
        public final static Property CollectionId = new Property(1, int.class, "collectionId", false, "COLLECTION_ID");
        public final static Property User_id = new Property(2, int.class, "user_id", false, "USER_ID");
        public final static Property Title = new Property(3, String.class, "title", false, "TITLE");
        public final static Property Url = new Property(4, String.class, "url", false, "URL");
        public final static Property Create_time = new Property(5, String.class, "create_time", false, "CREATE_TIME");
        public final static Property IsUpload = new Property(6, String.class, "isUpload", false, "IS_UPLOAD");
    }

}
