package dao;

import java.util.List;
import java.util.Map;

import entity.Product;
import entity.Customer;
import exception.*;

import java.sql.SQLException;

public interface OrderProcessorRepository {
	
   boolean createProduct(Product product);
   boolean createCustomer(Customer customer) throws UserAlreadyExistsException, SQLException;
   boolean deleteProduct(int productId) throws ProductNotFoundException, SQLException;
   boolean deleteCustomer(int customerId) throws CustomerNotFoundException, SQLException;
   boolean addToCart(Customer customer, Product product, int quantity) throws CustomerNotFoundException, ProductNotFoundException, SQLException;
   boolean removeFromCart(Customer customer, Product product);
   List<Product> getAllFromCart(Customer customer);
   List<Map<Product, Integer>> getOrdersByCustomer(int customerId);
   boolean placeOrder(Customer customer, List<Map<Product, Integer>> productsWithQuantity, String shippingAddress);
}
