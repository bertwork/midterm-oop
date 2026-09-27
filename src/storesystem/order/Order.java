package storesystem.order;

import java.util.ArrayList;
import java.util.List;

import storesystem.discount.Discount;

/**
 * Represents a customer's order in progress: a collection of
 * {@link OrderItem}s plus an optional {@link Discount} applied to the
 * order's subtotal.
 */
public class Order {
    /** The line items that make up this order. */
    private List<OrderItem> items;

    /** The discount applied to this order, or {@code null} if none applies. */
    private Discount discount;

    /**
     * Creates a new, empty order with no items and no discount.
     */
    public Order() {
        this.items = new ArrayList<>();
        this.discount = null;
    }

    /**
     * Adds a line item to this order.
     *
     * @param item the item to add
     */
    public void addItem(OrderItem item) {
        items.add(item);
    }

    /**
     * Returns the list of line items in this order.
     *
     * @return the order's items
     */
    public List<OrderItem> getItems() {
        return items;
    }

    /**
     * Sets the discount to apply to this order.
     *
     * @param discount the discount to apply, or {@code null} for no discount
     */
    public void setDiscount(Discount discount) {
        this.discount = discount;
    }

    /**
     * Returns the discount applied to this order, if any.
     *
     * @return the applied discount, or {@code null} if none
     */
    public Discount getDiscount() {
        return discount;
    }

    /**
     * Calculates the order's subtotal by summing the subtotals of all
     * line items, before any discount is applied.
     *
     * @return the order subtotal, in Philippine Peso (₱)
     */
    public double getSubtotal() {
        double subtotal = 0;
        for (OrderItem item : items) {
            subtotal += item.getSubtotal();
        }
        return subtotal;
    }

    /**
     * Calculates the final total for this order: the subtotal with the
     * applied discount (if any) taken into account.
     *
     * @return the discounted total, or the subtotal itself if no discount is set,
     *         in Philippine Peso (₱)
     */
    public double getTotal() {
        double subtotal = getSubtotal();
        if (discount != null) {
            return discount.apply(subtotal);
        }
        return subtotal;
    }
}