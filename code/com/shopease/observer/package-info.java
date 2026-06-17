/**
 * GoF Observer Pattern — {@code com.shopease.observer}
 * <p>
 * UML roles:
 * <ul>
 *   <li>{@link com.shopease.observer.Subject} — attach / detach / notify + getState()</li>
 *   <li>{@link com.shopease.observer.Observer} — update()</li>
 *   <li>{@link com.shopease.observer.ShopEaseInventorySubject} — ConcreteSubject (subjectState)</li>
 *   <li>{@link com.shopease.observer.ShopEaseAbstractObserver} + concrete *Observer — ConcreteObserver</li>
 * </ul>
 */
package com.shopease.observer;
