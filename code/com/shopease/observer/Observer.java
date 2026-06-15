package com.shopease.observer;

/**
 * GoF Observer — Observer role.
 * Concrete observers implement {@link #update()} and pull {@link SubjectState}
 * from the attached {@link Subject} via {@link Subject#getState()}.
 */
public interface Observer {
    void update();
}
