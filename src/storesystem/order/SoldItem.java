package storesystem.order;

/**
 * Represents a single sold-item record reconstructed from a saved
 * order on disk, used as input for {@link SalesReport}.
 * <p>
 * Unlike {@link OrderItem}, which references a full {@link Product},
 * a {@code SoldItem} only carries the flattened data needed for
 * reporting: the product's name, the quantity sold, and the subtotal
 * earned from that sale.
 */
public class SoldItem {
    /** Name of the product that was sold. */
    private String productName;

    /** Quantity of the product that was sold. */
    private int quantity;

    /** Revenue earned from this sold quantity (price x quantity). */
    private double subtotal;

    /**
     * Creates a new sold-item record.
     *
     * @param productName name of the product sold
     * @param quantity    quantity sold
     * @param subtotal    revenue earned from this sale
     */
    public SoldItem(String productName, int quantity, double subtotal) {
        this.productName = productName;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    /**
     * Returns the name of the product that was sold.
     *
     * @return the product name
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Returns the quantity of the product that was sold.
     *
     * @return the quantity sold
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Returns the revenue earned from this sold quantity.
     *
     * @return the subtotal (revenue) for this sale
     */
    public double getSubtotal() {
        return subtotal;
    }
}