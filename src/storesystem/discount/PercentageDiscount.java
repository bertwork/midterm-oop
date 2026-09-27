package storesystem.discount;

/**
 * A {@link Discount} that reduces the subtotal by a percentage,
 * e.g. "10% off".
 */
public class PercentageDiscount extends Discount {
    /** The percentage (0-100) to take off the subtotal. */
    private double percentage;

    /**
     * Creates a percentage-based discount.
     *
     * @param label      human-readable description of the discount
     * @param percentage the percentage of the subtotal to discount (e.g. {@code 10}
     *                   for 10%)
     */
    public PercentageDiscount(String label, double percentage) {
        super(label);
        this.percentage = percentage;
    }

    /**
     * Reduces the subtotal by {@link #percentage} percent.
     *
     * @param subtotal the order subtotal before discount
     * @return the subtotal minus {@code percentage}% of itself
     */
    @Override
    public double apply(double subtotal) {
        return subtotal - (subtotal * (percentage / 100.0));
    }
}