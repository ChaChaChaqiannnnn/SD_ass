package com.shopease.strategy;

/**
 * Strategy Pattern — picks which payment class to use based on what the customer selected.
 * CartDialog calls create() at checkout; no big switch statement needed in the UI.
 */
public final class ShopEasePaymentStrategySelector {

    public static final String CREDIT_CARD = "Credit Card";
    public static final String DUIT_NOW    = "DuitNow QR";
    public static final String MAE         = "MAE";
    public static final String TNG         = "Touch 'n Go";

    public static final String[] ALL_METHODS = {
        CREDIT_CARD, DUIT_NOW, MAE, TNG
    };

    private ShopEasePaymentStrategySelector() {}

    /** Returns the right Strategy object for "Credit Card", "DuitNow QR", "MAE", or "Touch 'n Go". */
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
