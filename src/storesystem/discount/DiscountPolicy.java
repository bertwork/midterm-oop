package storesystem.discount;

/**
 * Encapsulates the store's rules for deciding which discount, if any,
 * applies to a given order subtotal.
 * <p>
 * Currently implements a single promotional rule: orders that reach
 * {@link #PROMO_THRESHOLD} or more receive a
 * {@value #PROMO_PERCENTAGE}% {@link PercentageDiscount}.
 * <p>
 * All monetary amounts here are in Philippine Peso (₱) — e.g.
 * {@link #PROMO_THRESHOLD} is ₱500.00.
 */
public class DiscountPolicy {
    /**
     * Minimum subtotal, in Philippine Peso (₱), required (inclusive) to qualify for
     * the promo discount.
     */
    private static final double PROMO_THRESHOLD = 500.0;

    /** Percentage taken off the subtotal when the promo discount applies. */
    private static final double PROMO_PERCENTAGE = 10.0;

    /**
     * Determines which discount, if any, applies to the given subtotal.
     *
     * @param subtotal the order subtotal to evaluate
     * @return a {@link PercentageDiscount} if the subtotal meets the
     *         promo threshold, or {@code null} if no discount applies
     */
    public Discount determineDiscount(double subtotal) {
        if (subtotal >= PROMO_THRESHOLD) {
            return new PercentageDiscount(
                    "10% Promo Discount (Spend " + PROMO_THRESHOLD + "+)",
                    PROMO_PERCENTAGE);
        }
        return null;
    }
}