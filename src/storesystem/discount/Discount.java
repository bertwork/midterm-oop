package storesystem.discount;

/**
 * Base class for all discount types that can be applied to an order.
 * <p>
 * A {@code Discount} has a human-readable {@link #getLabel() label}
 * (used when printing receipts/reviews) and defines how it reduces a
 * given subtotal via {@link #apply(double)}. Concrete subclasses such
 * as {@link FixedAmountDiscount} and {@link PercentageDiscount}
 * implement the actual discount calculation.
 * <p>
 * All subtotal/total amounts passed to and returned from
 * {@link #apply(double)} are in Philippine Peso (₱).
 */
public abstract class Discount {
    /** Human-readable description of this discount, e.g. "10% Promo Discount". */
    private String label;

    /**
     * Creates a discount with the given display label.
     *
     * @param label human-readable description of the discount
     */
    public Discount(String label) {
        this.label = label;
    }

    /**
     * Returns the human-readable label for this discount.
     *
     * @return the discount's label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Applies this discount to the given subtotal and returns the
     * resulting (discounted) total.
     *
     * @param subtotal the order subtotal before discount
     * @return the total after the discount has been applied
     */
    public abstract double apply(double subtotal);
}