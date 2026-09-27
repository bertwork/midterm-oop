package storesystem.discount;

/**
 * A {@link Discount} that subtracts a fixed amount from the subtotal,
 * e.g. "$50 off".
 * <p>
 * The result is never allowed to go below zero, even if the fixed
 * amount exceeds the subtotal.
 */
public class FixedAmountDiscount extends Discount {
    /** The flat amount to subtract from the subtotal, in Philippine Peso (₱). */
    private double amount;

    /**
     * Creates a fixed-amount discount.
     *
     * @param label  human-readable description of the discount
     * @param amount the flat amount to subtract from the subtotal, in Philippine
     *               Peso (₱)
     */
    public FixedAmountDiscount(String label, double amount) {
        super(label);
        this.amount = amount;
    }

    /**
     * Subtracts the fixed {@link #amount} from the subtotal, clamped so
     * the result is never negative.
     *
     * @param subtotal the order subtotal before discount
     * @return {@code subtotal - amount}, or {@code 0} if that would be negative
     */
    @Override
    public double apply(double subtotal) {
        double result = subtotal - amount;
        return Math.max(result, 0);
    }
}