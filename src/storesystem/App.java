package storesystem;

import utils.InputReader;

import java.util.List;
import java.util.Map;

import storesystem.discount.Discount;
import storesystem.discount.DiscountPolicy;

import storesystem.order.Order;
import storesystem.order.OrderFileHandler;
import storesystem.order.OrderItem;
import storesystem.order.Product;
import storesystem.order.SalesReport;
import storesystem.order.SoldItem;

import utils.ConsoleUI;

/**
 * Entry point and main controller for the Store Ordering System.
 * <p>
 * This class drives the console-based menu loop, letting the user
 * start new orders, view a sales summary, or exit the application.
 * It coordinates the {@link OrderFileHandler} (for reading/writing data),
 * the {@link DiscountPolicy} (for computing discounts), and the
 * {@link ConsoleUI} / {@link InputReader} helpers (for console output
 * and input).
 */
public class App {
	/** Relative path to the pipe-separated file containing the product catalog. */
	private static final String PRODUCTS_FILE = "data/products.psv";

	/**
	 * Relative path to the pipe-separated file where completed orders are saved.
	 */
	private static final String ORDERS_FILE = "data/orders.psv";

	/** Handles reading products/orders from and writing orders to disk. */
	private static OrderFileHandler fileHandler = new OrderFileHandler();

	/** Determines which discount, if any, applies to a given order subtotal. */
	private static DiscountPolicy discountPolicy = new DiscountPolicy();

	/**
	 * Starts and runs the main application loop.
	 * <p>
	 * Loads the product catalog, then repeatedly displays the main menu
	 * and dispatches to the appropriate action (new order, sales summary,
	 * or exit) based on the user's input, until the user chooses to exit.
	 */
	public static void run() {
		List<Product> catalog = fileHandler.loadProducts(PRODUCTS_FILE);

		if (catalog.isEmpty()) {
			System.out.println("Warning: no products loaded. Check " + PRODUCTS_FILE);
		}

		boolean isRunning = true;
		while (isRunning) {
			printMenu();
			int choice = InputReader.readInt("Enter choice: ");

			switch (choice) {
				case 1:
					startNewOrder(catalog);
					InputReader.pauseScreen();
					break;
				case 2:
					viewSalesSummary();
					InputReader.pauseScreen();
					break;
				case 3:
					isRunning = false;
					System.out.println("\nApp close!");
					break;
				default:
					System.out.println("Invalid choice. Try again.\n");
			}

		}
	}

	/**
	 * Walks the user through building and confirming a new order.
	 * <p>
	 * Displays the catalog, repeatedly prompts for a product number and
	 * quantity to add items to the order, applies any discount that the
	 * subtotal qualifies for, shows a review of the order, and saves it
	 * to the orders file if the user confirms.
	 *
	 * @param catalog the list of available products the user can order from
	 */
	private static void startNewOrder(List<Product> catalog) {
		// nothing to sell, so there's no point starting an order
		if (catalog.isEmpty()) {
			System.out.println("No products available. Cannot start an order.\n");
			return;
		}

		printCatalog(catalog);

		// creating new order
		Order order = new Order();
		boolean addingItems = true;

		// loop: keep asking for products until the user finishes (enters 0)
		// or says "no" to adding another item
		while (addingItems) {
			int productNum = InputReader.readInt("Enter product number (or 0 to finish): ");
			if (productNum == 0)
				break; // done adding items

			// validate the product number is within range before using it as an index
			if (productNum < 1 || productNum > catalog.size()) {
				System.out.println("Invalid product number. Try again.\n");
				continue;
			}

			int quantity = InputReader.readInt("Enter quantity: ");
			if (quantity <= 0) {
				System.out.println("Quantity must be a positive number. Try again.\n");
				continue;
			}

			// productNum is 1-based for the user, so subtract 1 for the 0-based list index
			Product chosen = catalog.get(productNum - 1);
			OrderItem item = new OrderItem(chosen, quantity);
			order.addItem(item);

			System.out.printf("Added: %s x%d = %.2f%n%n", chosen.getName(), quantity, item.getSubtotal());

			// ask whether to keep adding items; anything other than "y" ends the loop
			String again = InputReader.readLine("Add another item? (y/n): ");

			if (!again.equalsIgnoreCase("y")) {
				addingItems = false;
			}
		}

		// if the user never added a valid item, there's nothing to check out
		if (order.getItems().isEmpty()) {
			System.out.println("No items added. Order cancelled.\n");
			return;
		}

		double subtotal = order.getSubtotal();

		// check the discount policy and attach whatever discount (if any) applies
		Discount discount = discountPolicy.determineDiscount(subtotal);
		order.setDiscount(discount);

		printReview(order);

		// let the user back out before anything is written to disk
		String confirm = InputReader.readLine("Confirm order? (y/n): ");
		if (confirm.equalsIgnoreCase("y")) {
			fileHandler.saveOrder(order, ORDERS_FILE);
			System.out.println("Order saved. Thank you!\n");
		} else {
			System.out.println("Order cancelled.\n");
		}
	}

	/**
	 * Loads all previously saved orders, aggregates them into a sales
	 * report, and prints a formatted summary (quantity sold and revenue
	 * per product, plus total revenue) to the console.
	 */
	private static void viewSalesSummary() {
		// load past orders and total them up by product
		List<SoldItem> soldItems = fileHandler.loadAllSoldItems(ORDERS_FILE);
		SalesReport report = new SalesReport();

		report.generateReport(soldItems);

		int symbolCount = 12;

		// print a "SALES SUMMARY" banner between two rows of '=' characters
		System.out.print("\n" + ConsoleUI.headerLine(symbolCount, '='));
		System.out.print("  SALES SUMMARY  ");
		System.out.print(ConsoleUI.headerLine(symbolCount, '=') + '\n');

		// column headers for the table below
		System.out.printf("%-20s %10s %10s%n", "Product", "Qty Sold", "Revenue");


		// print one row per product with its qty sold and revenue
		Map<String, Integer> qtyMap = report.getQuantitySold();
		Map<String, Double> revenueMap = report.getRevenueByProduct();
		
		for (String name : qtyMap.keySet()) {
			System.out.printf("%-20s %10d %10.2f%n", name, qtyMap.get(name), revenueMap.get(name));
		}

		// print the grand total revenue across all products
		System.out.println(ConsoleUI.headerLine(40, '-'));
		System.out.printf("%-20s %20.2f%n", "TOTAL REVENUE:", report.getTotalRevenue());
		System.out.println(ConsoleUI.headerLine(40, '=') + "\n");
	}

	/**
	 * Prints a numbered listing of all available products with their prices.
	 *
	 * @param catalog the list of products to display
	 */
	private static void printCatalog(List<Product> catalog) {
		int symbolCount = 10;

		System.out.print(ConsoleUI.headerLine(symbolCount, '='));
		System.out.print("  AVAILABLE PRODUCTS  ");
		System.out.print(ConsoleUI.headerLine(symbolCount, '=') + '\n');

		System.out.printf("%-4s %-20s %10s%n", "No.", "Product", "Price");
		for (int i = 0; i < catalog.size(); i++) {
			Product p = catalog.get(i);
			System.out.printf("%-4d %-20s %10.2f%n", i + 1, p.getName(), p.getPrice());
		}
		System.out.println(ConsoleUI.headerLine(42, '='));
	}

	/**
	 * Prints a formatted review of an order, including each line item,
	 * the subtotal, any applied discount, and the final total.
	 *
	 * @param order the order to display
	 */
	private static void printReview(Order order) {
		int symbolCount = 10;

		System.out.print("\n" + ConsoleUI.headerLine(symbolCount, '='));
		System.out.print("  ORDER SUMMARY  ");
		System.out.print(ConsoleUI.headerLine(symbolCount, '=') + '\n');

		for (OrderItem item : order.getItems()) {
			System.out.println(item);
		}

		System.out.println(ConsoleUI.headerLine(40, '-'));

		System.out.printf("%-25s %8.2f%n", "Subtotal:", order.getSubtotal());
		if (order.getDiscount() != null) {
			double discountAmount = order.getSubtotal() - order.getTotal();
			System.out.printf("%-25s %8.2f%n", order.getDiscount().getLabel() + ":", -discountAmount);
		}

		System.out.println(ConsoleUI.headerLine(40, '-'));
		System.out.printf("%-25s %8.2f%n", "TOTAL:", order.getTotal());
		System.out.println(ConsoleUI.headerLine(40, '='));
	}

	/**
	 * Prints the main menu options to the console.
	 */
	private static void printMenu() {
		ConsoleUI.printHeader("STORE ORDERING SYSTEM");
		System.out.println("1. Start New Order");
		System.out.println("2. View Sales Summary");
		System.out.println("3. Exit\n");
	}
}