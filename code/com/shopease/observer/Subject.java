package com.shopease.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * GoF Observer — Subject role.
 * <ul>
 *   <li>{@link #attach(Observer)} / {@link #detach(Observer)} — register listeners</li>
 *   <li>{@link #notifyObservers()} — foreach o in observers: o.update()</li>
 *   <li>{@link #getState()} — state concrete observers pull in update()</li>
 * </ul>
 */
public abstract class Subject {
    private final List<Observer> observers = new ArrayList<>();

    public void attach(Observer observer) {
        if (observer instanceof ShopEaseAbstractObserver abstractObserver) {
            abstractObserver.bindSubject(this);
        }
        observers.add(observer);
    }

    public void detach(Observer observer) {
        observers.remove(observer);
    }

    protected void notifyObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }

    /** GoF ConcreteSubject — {@code return subjectState}. */
    public abstract SubjectState getState();
}
