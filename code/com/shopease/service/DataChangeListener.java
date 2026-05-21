package com.shopease.service;

/** UI components register to refresh when inventory, cart, or orders change. */
public interface DataChangeListener {
    void onDataChanged();
}
