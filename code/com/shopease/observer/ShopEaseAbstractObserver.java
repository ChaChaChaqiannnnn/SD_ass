package com.shopease.observer;

/**
 * GoF Observer — base for concrete observers.
 * Holds a reference to the subject; {@link #update()} reads {@link Subject#getState()}.
 */
public abstract class ShopEaseAbstractObserver implements Observer {
    protected Subject subject;

    void bindSubject(Subject subject) {
        this.subject = subject;
    }

    @Override
    public void update() {
        if (subject != null) {
            onStateChanged(subject.getState());
        }
    }

    protected abstract void onStateChanged(SubjectState state);
}
