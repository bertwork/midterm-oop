package storesystem.order;

/**
 * Represents a single line item within an {@link Order}: a
 * {@link Product} together with the quantity of that product being
 * purchased.
 */
public class OrderItem {
    /** The product being purchased. */
    private Product product;

    /** The quantity of the product being purchased. Must be positive. */
    private int quantity;

    /**
     * Creates a new order item.
     *
     * @param product  the product being purchased
     * @param quantity the quantity being purchased; must be positive
     * @throws IllegalArgumentException if {@code quantity} is zero or negative
     */
    public OrderItem(Product product, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive: " + quantity);
        }
        this.product = product;
        this.quantity = quantity;
    }

    /**
     * Returns the product for this line item.
     *
     * @return the product
     */
    public Product getProduct() {
        return product;
    }

    /**
     * Returns the quantity of the product for this line item.
     *
     * @return the quantity
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Calculates the subtotal for this line item (unit price times quantity).
     *
     * @return the line item subtotal, in Philippine Peso (₱)
     */
    public double getSubtotal() {
        return product.getPrice() * quantity;
    }

    /**
     * Returns a formatted, fixed-width string representation of this
     * line item suitable for printing in an order review, showing the
     * product name, quantity, and subtotal.
     *
     * @return formatted string representation of this line item
     */
    @Override
    public String toString() {
        return String.format("%-20s x%-3d %10.2f", product.getName(), quantity, getSubtotal());
    }
}