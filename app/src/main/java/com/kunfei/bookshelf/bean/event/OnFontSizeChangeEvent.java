package com.kunfei.bookshelf.bean.event;

/**
 * @author Nukc.
 */

public class OnFontSizeChangeEvent {
    boolean isSelectUa;

    public OnFontSizeChangeEvent(boolean isSelectUa) {
        this.isSelectUa = isSelectUa;
    }

    public boolean isSelectUa() {
        return isSelectUa;
    }

    public void setSelectUa(boolean selectUa) {
        isSelectUa = selectUa;
    }
}
