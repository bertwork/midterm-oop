package storesystem.order;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Aggregates a list of {@link SoldItem} records into summary
 * statistics: total quantity sold per product, total revenue per
 * product, and overall total revenue.
 * <p>
 * Call {@link #generateReport(List)} first to compute the aggregates;
 * the getter methods then expose the results.
 */
public class SalesReport {
    /** Total quantity sold, keyed by product name. */
    private Map<String, Integer> quantitySold;

    /** Total revenue earned, keyed by product name. */
    private Map<String, Double> revenueByProduct;

    /** Total revenue across all products. */
    private double totalRevenue;

    /**
     * Computes the sales aggregates (quantity sold, revenue by product,
     * and total revenue) from the given list of sold items. Overwrites
     * any previously computed report data.
     *
     * @param soldItems the sold items to aggregate, typically loaded from the
     *                  orders file
     */
    public void generateReport(List<SoldItem> soldItems) {
        quantitySold = new HashMap<>();
        revenueByProduct = new HashMap<>();
        totalRevenue = 0.0;

        for (SoldItem item : soldItems) {
            String name = item.getProductName();

            if (quantitySold.containsKey(name)) {
                int oldQty = quantitySold.get(name);
                quantitySold.put(name, oldQty + item.getQuantity());
            } else {
                quantitySold.put(name, item.getQuantity());
            }

            if (revenueByProduct.containsKey(name)) {
                double oldRevenue = revenueByProduct.get(name);
                revenueByProduct.put(name, oldRevenue + item.getSubtotal());
            } else {
                revenueByProduct.put(name, item.getSubtotal());
            }

            totalRevenue += item.getSubtotal();
        }
    }

    /**
     * Returns the total quantity sold for each product.
     * Must be called after {@link #generateReport(List)}.
     *
     * @return a map of product name to total quantity sold
     */
    public Map<String, Integer> getQuantitySold() {
        return quantitySold;
    }

    /**
     * Returns the total revenue earned for each product.
     * Must be called after {@link #generateReport(List)}.
     *
     * @return a map of product name to total revenue
     */
    public Map<String, Double> getRevenueByProduct() {
        return revenueByProduct;
    }

    /**
     * Returns the total revenue earned across all products.
     * Must be called after {@link #generateReport(List)}.
     *
     * @return the total revenue
     */
    public double getTotalRevenue() {
        return totalRevenue;
    }
}