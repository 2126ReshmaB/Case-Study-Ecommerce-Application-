package main;

import java.util.*;

import entity.Product;
import entity.Customer;
import dao.OrderProcessorRepositoryImpl;

public class Main {
	public static void main(String args[]) throws Exception {
		Scanner sc = new Scanner(System.in);
		OrderProcessorRepositoryImpl op = new OrderProcessorRepositoryImpl();

		while (true) {
			System.out.println("------------- Welcome to Ecommerce Application -------------");
			System.out.println("==============================================================");
			System.out.println(
					" 1. Register Customer \n 2. Create Product \n 3. Delete Product \n 4. Add to Cart \n 5. View Cart \n 6. Remove from Cart \n 7. Place Order \n 8. View Custoemr Orders \n 9. Exit");
			System.out.println("==================");
			System.out.print("Enter you choice : ");
			int ch = sc.nextInt();

			switch (ch) {
			case 1:
				System.out.print("Enter customer name: ");
				String customerName = sc.next();
				sc.nextLine();
				System.out.print("Enter customer email: ");
				String email = sc.next();
				sc.nextLine();
				System.out.print("Enter customer password: ");
				String password = sc.next();
				sc.nextLine();

				Customer customer = new Customer(0, customerName, email, password); // 0 for customer_id because it will
																					// be auto-incremented
				boolean r1 = op.createCustomer(customer);
				if (r1) {
					System.out.println("Customer registered successfully!");
				} else {
					System.out.println("Failed to register customer.");
				}
				break;
			case 2:
				System.out.print("Enter Product Name : ");
				String productName = sc.next();
				sc.nextLine();
				System.out.print("Enter Product Price : ");
				double price = sc.nextDouble();
				sc.nextLine();
				System.out.print("Enter Product Description : ");
				String description = sc.next();
				sc.nextLine();
				System.out.print("Enter Product Stock Quantity : ");
				int stockQuantity = sc.nextInt();

				Product product = new Product(0, productName, description, price, stockQuantity);
				boolean r2 = op.createProduct(product);
				if (r2) {
					System.out.println("Product created successfully!");
				} else {
					System.out.println("Failed to create product.");
				}
				break;

			case 3:
				System.out.print("Enter product ID to delete: ");
				int productId = sc.nextInt();
				boolean result = op.deleteProduct(productId);
				if (result) {
					System.out.println("Product deleted successfully.");
				} else {
					System.out.println("Failed to delete product.");
				}
				break;

			case 4:
				System.out.print("Enter customer ID: ");
				int customerId = sc.nextInt();
				sc.nextLine();
				System.out.print("Enter product ID: ");
				productId = sc.nextInt();
				System.out.print("Enter quantity: ");
				int quantity = sc.nextInt();
				sc.nextLine();

				customer = new Customer(customerId, "", "", "");
				product = new Product(productId, "", "", 0.0, 0);
				result = op.addToCart(customer, product, quantity);
				if (result) {
					System.out.println("Product added to cart successfully!");
				} else {
					System.out.println("Failed to add product to cart.");
				}
				break;
			case 5:
				System.out.print("Enter customer ID: ");
				customerId = sc.nextInt();
				sc.nextLine(); // Consume newline

				customer = new Customer(customerId, "", "", ""); // Dummy customer object with only ID
				List<Product> products = op.getAllFromCart(customer);
				if (products.isEmpty()) {
					System.out.println("Cart is empty.");
				} else {
					System.out.println("Products in cart:");
					for (Product p : products) {
						System.out.println("Product ID: " + p.getProductId() + ", Name: " + p.getProductName()
								+ ", Price: " + p.getPrice() + ", Quantity: " + p.getStockQuantity());
					}
				}
				break;
			case 6:
				System.out.print("Enter Customer Id : ");
				customerId = sc.nextInt();
				sc.nextLine();
				System.out.print("Enter Product Id : ");
				productId = sc.nextInt();
				sc.nextLine();
				customer = new Customer(customerId, "", "", "");
				product = new Product(productId, "", "", 0.0, 0);
				if(op.removeFromCart(customer, product)) {
					System.out.println("Product removed from cart sucessfully.");
				}
				
			case 7:
				System.out.print("Enter customer ID: ");
				customerId = sc.nextInt();
				sc.nextLine(); // Consume newline
				System.out.print("Enter shipping address: ");
				String shippingAddress = sc.nextLine();

				customer = new Customer(customerId, "", "", ""); // Dummy customer object with only ID

				// Dummy product with quantity to simulate placing an order
				List<Map<Product, Integer>> productsWithQuantity = new ArrayList<>();
				System.out.print("Enter the number of products in the order: ");
				int numProducts = sc.nextInt();
				for (int i = 0; i < numProducts; i++) {
					System.out.print("Enter product ID: ");
					productId = sc.nextInt();
					System.out.print("Enter quantity: ");
					quantity = sc.nextInt();

					product = new Product(productId, "", "", 0.0, 0); // Dummy product with only ID
					Map<Product, Integer> productQuantityMap = new HashMap<>();
					productQuantityMap.put(product, quantity);
					productsWithQuantity.add(productQuantityMap);
				}

				result = op.placeOrder(customer, productsWithQuantity, shippingAddress);
				if (result) {
					System.out.println("Order placed successfully!");
				} else {
					System.out.println("Failed to place order.");
				}
				break;
			case 8:
				System.out.print("Enter customer ID: ");
				customerId = sc.nextInt();
				sc.nextLine(); // Consume newline

				List<Map<Product, Integer>> orders = op.getOrdersByCustomer(customerId);
				if (orders.isEmpty()) {
					System.out.println("No orders found.");
				} else {
					System.out.println("Orders for customer ID " + customerId + ":");
					for (Map<Product, Integer> order : orders) {
						for (Product p : order.keySet()) {
							System.out.println("Product ID: " + p.getProductId() + ", Name: " + p.getProductName()
									+ ", Quantity: " + order.get(p));
						}
					}
				}
				break;
			case 9:
                System.out.println("Exiting the application...");
                sc.close();
                return;
            default:
                System.out.println("Invalid option. Please try again.");

			}
		}
	}
}
