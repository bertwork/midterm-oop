package storesystem.order;

import java.util.List;

/**
 * Result of loading the product catalog from disk: the successfully
 * parsed products, plus a human-readable warning for every line that
 * was skipped (missing price, unparsable price, or malformed line).
 */
public class ProductLoadResult {
    private final List<Product> products;
    private final List<String> warnings;

    public ProductLoadResult(List<Product> products, List<String> warnings) {
        this.products = products;
        this.warnings = warnings;
    }

    public List<Product> getProducts() {
        return products;
    }

    public List<String> getWarnings() {
        return warnings;
    }
}