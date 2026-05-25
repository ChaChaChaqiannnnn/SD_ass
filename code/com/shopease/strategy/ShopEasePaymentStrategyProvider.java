package com.shopease.strategy;

/**
 * ShopEasePaymentStrategyProvider — Strategy Provider
 * =================================================
 * Centralises payment strategy creation so that no UI class needs to know
 * which concrete strategy class maps to which display name.
 *
 * Adding a new payment method only requires:
 *   1. A new ShopEasePaymentStrategy implementation.
 *   2. One new case here.
 *
 * Design Pattern: Strategy (Provider)
 * Used by: CartDialog.doCheckout()
 */
public final class ShopEasePaymentStrategyProvider {

    /** Display names shown in the payment combo-box. */
    public static final String CREDIT_CARD = "Credit Card";
    public static final String DUIT_NOW    = "DuitNow QR";
    public static final String MAE         = "MAE";
    public static final String TNG         = "Touch 'n Go";

    /** Ordered list — matches the JComboBox index in CartDialog. */
    public static final String[] ALL_METHODS = {
        CREDIT_CARD, DUIT_NOW, MAE, TNG
    };

    private ShopEasePaymentStrategyProvider() {}

    /**
     * Creates and returns the payment strategy for the given display name.
     * Falls back to CreditCard if the name is unrecognised.
     *
     * @param displayName one of the ALL_METHODS constants
     * @return a concrete ShopEasePaymentStrategy ready for use
     */
    public static ShopEasePaymentStrategy create(String displayName) {
        if (displayName == null) {
            return new ShopEaseCreditCardStrategy();
        }
        return switch (displayName) {
            case DUIT_NOW -> new ShopEaseDuitNowStrategy();
            case MAE      -> new ShopEaseMAEStrategy();
            case TNG      -> new ShopEaseTNGStrategy();
            default       -> new ShopEaseCreditCardStrategy();
        };
    }
}
