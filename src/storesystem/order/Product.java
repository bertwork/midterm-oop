package storesystem.order;

/**
 * Represents a single product in the store's catalog, with a name and
 * a unit price.
 * <p>
 * <b>Currency:</b> all monetary values in this application (product
 * prices, subtotals, discounts, totals) are assumed to be in
 * Philippine Peso (₱ / PHP). The console output and data files do not
 * print a currency symbol or code alongside the numbers — this is
 * simply the intended unit for every {@code double} amount used
 * throughout the codebase.
 */
public class Product {
    /** The product's display name. */
    private String name;

    /** The product's unit price, in Philippine Peso (₱). Must be non-negative. */
    private double price;

    /**
     * Creates a new product.
     *
     * @param name  the product's display name
     * @param price the product's unit price; must not be negative
     * @throws IllegalArgumentException if {@code price} is negative
     */
    public Product(String name, double price) {
        if (price < 0) {
            throw new IllegalArgumentException("Product price cannot be negative: " + price);
        }

        this.name = name;
        this.price = price;
    }

    /**
     * Returns the product's display name.
     *
     * @return the product name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the product's unit price.
     *
     * @return the unit price
     */
    public double getPrice() {
        return price;
    }

    /**
     * Returns a simple "name - price" string representation of this product.
     *
     * @return string representation of the product
     */
    @Override
    public String toString() {
        return name + " - " + price;
    }
}