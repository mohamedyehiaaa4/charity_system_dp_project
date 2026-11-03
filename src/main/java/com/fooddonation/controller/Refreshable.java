package com.fooddonation.controller;

/** A tab that reloads its data from the model each time it is opened. */
public interface Refreshable {
    void refresh();

    /** Small count shown next to the tab in the sidebar, e.g. unread messages. Empty means none. */
    default String badge() {
        return "";
    }
}
