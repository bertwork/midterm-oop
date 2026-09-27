package storesystem.order;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import storesystem.discount.Discount;

/**
 * Handles all file I/O for the store system: loading the product
 * catalog, saving completed orders, and re-loading past orders as
 * {@link SoldItem}s for reporting.
 * <p>
 * Data is stored in flat, pipe-delimited text files (true PSV — every
 * field on every line uses the same single {@code |} delimiter, with
 * no nested sub-delimiters):
 * <ul>
 * <li>Products file: one product per line,
 * {@code name|price}.</li>
 * <li>Orders file: one line <b>per sold item</b> (not per order), in the form
 * {@code orderId|productName|quantity|itemSubtotal|orderSubtotal|discountLabel|discountAmount|orderTotal}.
 * All items belonging to the same order share the same
 * {@code orderId}, {@code orderSubtotal}, {@code discountLabel},
 * {@code discountAmount}, and {@code orderTotal} values, so an
 * order with 3 items produces 3 lines. This keeps every line
 * flat and consistently delimited, at the cost of repeating the
 * order-level values across each of an order's item lines.</li>
 * </ul>
 * Product names must not themselves contain the {@code |} character,
 * since it is used as the field delimiter.
 */
public class OrderFileHandler {
    /**
     * Loads the product catalog from a pipe-delimited file.
     * <p>
     * Each non-empty line is expected to be in the form
     * {@code name|price}. Lines that are empty, malformed (fewer than
     * two fields), or have an unparsable price are silently skipped so
     * that a single bad line does not prevent the rest of the catalog
     * from loading. If the file does not exist, an empty list is
     * returned.
     *
     * @param filePath path to the products file
     * @return the list of successfully parsed products (possibly empty)
     */
    public ProductLoadResult loadProducts(String filePath) {
        List<Product> products = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            return new ProductLoadResult(products, warnings);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0; // use to track bad file input

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (line.isEmpty())
                    continue;

                String[] parts = line.split("\\|");

                if (parts.length < 2) {
                    warnings.add("Line " + lineNumber + ": missing price (\"" + line + "\") — skipped");
                    continue;
                }

                try {
                    String name = parts[0].trim();
                    double price = Double.parseDouble(parts[1].trim());
                    products.add(new Product(name, price));
                } catch (IllegalArgumentException e) {
                    warnings.add("Line " + lineNumber + ": invalid price (\"" + line + "\") — skipped");
                }
            }
        } catch (IOException e) {
            System.out.println("Could not read products file: " + e.getMessage());
        }

        return new ProductLoadResult(products, warnings);
    }

    /**
     * Appends a completed order to the orders file, writing one flat,
     * pipe-delimited line per sold item.
     * <p>
     * Each line has the form
     * {@code orderId|productName|quantity|itemSubtotal|orderSubtotal|discountLabel|discountAmount|orderTotal}.
     * All lines produced by a single call to this method share the same
     * {@code orderId} (a timestamp generated at save time) so they can
     * later be grouped back into one order if needed, and the same
     * order-level subtotal/discount/total values. The discount label is
     * {@code "none"} and the discount amount is {@code 0.0} when no
     * discount was applied.
     *
     * @param order    the order to save
     * @param filePath path to the orders file; lines are appended if the file
     *                 already exists
     */
    public void saveOrder(Order order, String filePath) {
        List<OrderItem> items = order.getItems();

        long orderId = System.currentTimeMillis();

        String discountLabel = "none";
        double discountAmount = 0.0;

        Discount discount = order.getDiscount();
        double subtotal = order.getSubtotal();
        double total = order.getTotal();

        if (discount != null) {
            discountLabel = discount.getLabel();
            discountAmount = subtotal - total;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            for (OrderItem item : items) {
                String line = orderId + "|" +
                        item.getProduct().getName() + "|" +
                        item.getQuantity() + "|" +
                        item.getSubtotal() + "|" +
                        subtotal + "|" +
                        discountLabel + "|" +
                        discountAmount + "|" +
                        total;

                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Could not save order: " + e.getMessage());
        }
    }

    /**
     * Loads every sold item from the orders file for use by
     * {@link SalesReport}.
     * <p>
     * Each line is expected to have the flat, pipe-delimited form
     * {@code orderId|productName|quantity|itemSubtotal|orderSubtotal|discountLabel|discountAmount|orderTotal}.
     * Only the product name, quantity, and item subtotal are needed for
     * reporting, so the remaining order-level fields are read but not
     * kept on the resulting {@link SoldItem}. Lines that are empty,
     * malformed (fewer than 4 fields), or contain unparsable numbers
     * are skipped rather than aborting the whole load. If the file does
     * not exist, an empty list is returned.
     *
     * @param filePath path to the orders file
     * @return a flat list of all sold items across all saved orders
     */
    public List<SoldItem> loadAllSoldItems(String filePath) {
        List<SoldItem> soldItems = new ArrayList<>();
        File file = new File(filePath);

        if (!file.exists()) {
            return soldItems;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;

                String[] fields = line.split("\\|");

                // Need at least orderId, productName, quantity, itemSubtotal
                if (fields.length < 4)
                    continue; // skip malformed lines

                try {
                    String name = fields[1];
                    int qty = Integer.parseInt(fields[2]);
                    double itemSubtotal = Double.parseDouble(fields[3]);
                    soldItems.add(new SoldItem(name, qty, itemSubtotal));
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        } catch (IOException e) {
            System.out.println("Could not read orders file: " + e.getMessage());
        }

        return soldItems;
    }
}