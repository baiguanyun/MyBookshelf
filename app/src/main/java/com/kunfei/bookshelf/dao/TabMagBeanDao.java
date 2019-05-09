package com.kunfei.bookshelf.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.kunfei.bookshelf.bean.TabMagBean;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * DAO for table "TAB_MAG_BEAN".
 */
public class TabMagBeanDao extends AbstractDao<TabMagBean, Long> {

    public static final String TABLENAME = "TAB_MAG_BEAN";

    public TabMagBeanDao(DaoConfig config) {
        super(config);
    }


    public TabMagBeanDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /**
     * Creates the underlying database table.
     */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists ? "IF NOT EXISTS " : "";
        db.execSQL("CREATE TABLE " + constraint + "\"TAB_MAG_BEAN\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL ," + // 0: id
                "\"TITLE\" TEXT," + // 1: title
                "\"URL\" TEXT);"); // 2: url
    }

    /**
     * Drops the underlying database table.
     */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"TAB_MAG_BEAN\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, TabMagBean entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getId());

        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(2, title);
        }

        String url = entity.getUrl();
        if (url != null) {
            stmt.bindString(3, url);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, TabMagBean entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getId());

        String title = entity.getTitle();
        if (title != null) {
            stmt.bindString(2, title);
        }

        String url = entity.getUrl();
        if (url != null) {
            stmt.bindString(3, url);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.getLong(offset + 0);
    }

    @Override
    public TabMagBean readEntity(Cursor cursor, int offset) {
        TabMagBean entity = new TabMagBean( //
                cursor.getLong(offset + 0), // id
                cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // title
                cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2) // url
        );
        return entity;
    }

    @Override
    public void readEntity(Cursor cursor, TabMagBean entity, int offset) {
        entity.setId(cursor.getLong(offset + 0));
        entity.setTitle(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setUrl(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
    }

    @Override
    protected final Long updateKeyAfterInsert(TabMagBean entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }

    @Override
    public Long getKey(TabMagBean entity) {
        if (entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(TabMagBean entity) {
        throw new UnsupportedOperationException("Unsupported for entities with a non-null key");
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }

    /**
     * Properties of entity TabMagBean.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, long.class, "id", true, "_id");
        public final static Property Title = new Property(1, String.class, "title", false, "TITLE");
        public final static Property Url = new Property(2, String.class, "url", false, "URL");
    }

}
