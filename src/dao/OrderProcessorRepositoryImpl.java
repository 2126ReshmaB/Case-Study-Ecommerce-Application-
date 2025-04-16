package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import entity.Product;
import entity.Customer;

import exception.OrderNotFoundException;
import exception.UserAlreadyExistsException;
import exception.CustomerNotFoundException;
import exception.ProductNotFoundException;

import util.DBConnUtil;

public class OrderProcessorRepositoryImpl implements OrderProcessorRepository{
	
	@Override
	public  boolean createProduct(Product product){
		Connection connection = DBConnUtil.getConnection("db.properties");
		String insertProductQuery = "INSERT INTO Products (name, description, price, stock_quantity) values(?,?,?,?)";
		try (PreparedStatement ps = connection.prepareStatement(insertProductQuery)){
			ps.setString(1,  product.getProductName());
			ps.setString(2, product.getDescription());
			ps.setDouble(3,  product.getPrice());
			ps.setInt(4, product.getStockQuantity());
			
			return ps.executeUpdate() > 0;
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	public boolean createCustomer(Customer customer) throws UserAlreadyExistsException, SQLException {

		 String checkCustomerQuery = "SELECT * FROM Customers WHERE customer_id = ?";
	    String insertCustomerQuery = "INSERT INTO customers (name, email, password) VALUES (?, ?, ?)";

	    try (Connection connection = DBConnUtil.getConnection("db.properties"); 
	    	PreparedStatement checkUserStmt = connection.prepareStatement(checkCustomerQuery)){
	        checkUserStmt.setInt(1, customer.getCustomerId());
	        ResultSet rs = checkUserStmt.executeQuery();
	        if (rs.next()) {
	            throw new UserAlreadyExistsException("Customer Already Exists");
	        }
	        else {
	        	 try (PreparedStatement ps = connection.prepareStatement(insertCustomerQuery)) {
	     	        ps.setString(1, customer.getName());
	     	        ps.setString(2, customer.getEmail());
	     	        ps.setString(3, customer.getPassword());
	     	        return ps.executeUpdate() > 0;
	     	    }
	        }
	    }
	}

	
	@Override
	public boolean deleteProduct(int productId) throws ProductNotFoundException, SQLException {
	    String checkProductQuery = "SELECT * FROM Products WHERE product_id = ?";
	    String deleteProductQuery = "DELETE FROM Products WHERE product_id = ?";

	    try (Connection connection = DBConnUtil.getConnection("db.properties");
	         PreparedStatement checkUserStmt = connection.prepareStatement(checkProductQuery)) {

	        checkUserStmt.setInt(1, productId);
	        ResultSet rs = checkUserStmt.executeQuery();

	        if (!rs.next()) {
	            throw new ProductNotFoundException("Product Not Found.");
	        }

	        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteProductQuery)) {
	            deleteStmt.setInt(1, productId);
	            return deleteStmt.executeUpdate() > 0;
	        }
	    }
	}

	
	@Override
	public boolean deleteCustomer(int customerId) throws CustomerNotFoundException, SQLException {
	    String checkCustomerQuery = "SELECT * FROM Customers WHERE customer_id = ?";
	    String deleteCustomerQuery = "DELETE FROM Customers WHERE customer_id = ?";

	    try (Connection connection = DBConnUtil.getConnection("db.properties");
	         PreparedStatement checkUserStmt = connection.prepareStatement(checkCustomerQuery)) {

	         checkUserStmt.setInt(1, customerId);
	         ResultSet rs = checkUserStmt.executeQuery();

	        if (!rs.next()) {
	            throw new CustomerNotFoundException("Customer Not Found.");
	        }
	        try (PreparedStatement ps = connection.prepareStatement(deleteCustomerQuery)) {
	            ps.setInt(1, customerId);
	            return ps.executeUpdate() > 0;
	        }
	    }
	}
	
	@Override
	public boolean addToCart(Customer customer, Product product,int quantity) throws CustomerNotFoundException, ProductNotFoundException, SQLException{
		String checkCustomerQuery = "SELECT * FROM Customers WHERE customer_id = ?";
		String checkProductQuery = "SELECT * FROM Products WHERE product_id = ?";
		String insertCartQuery = "INSERT INTO Cart (customer_id, product_id, quantity) VALUES (?, ?, ?)";
		
		try (Connection connection = DBConnUtil.getConnection("db.properties")) {

	       try (PreparedStatement checkCustomerStmt = connection.prepareStatement(checkCustomerQuery)) {
	            checkCustomerStmt.setInt(1, customer.getCustomerId());
	            ResultSet customerRs = checkCustomerStmt.executeQuery();
	            if (!customerRs.next()) {
	                throw new CustomerNotFoundException("Customer not found.");
	            }
	        }

	        try (PreparedStatement checkProductStmt = connection.prepareStatement(checkProductQuery)) {
	            checkProductStmt.setInt(1, product.getProductId());
	            ResultSet productRs = checkProductStmt.executeQuery();
	            if (!productRs.next()) {
	                throw new ProductNotFoundException("Product not found.");
	            }
	        }

	        try (PreparedStatement insertCartStmt = connection.prepareStatement(insertCartQuery)) {
	            insertCartStmt.setInt(1, customer.getCustomerId());
	            insertCartStmt.setInt(2, product.getProductId());
	            insertCartStmt.setInt(3, quantity);
	            return insertCartStmt.executeUpdate() > 0;
	        }
	    }
		
	}
	
	@Override
	public boolean removeFromCart(Customer customer, Product product) {
		Connection connection = DBConnUtil.getConnection("db.properties");
		String removeQuery = "DELETE FROM Cart WHERE customer_id = ? AND product_id = ?";
		
		try(PreparedStatement ps = connection.prepareStatement(removeQuery)){
			ps.setInt(1,  customer.getCustomerId());
			ps.setInt(2, product.getProductId());
			return ps.executeUpdate() > 0;
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		return false;
		
	}
	
	@Override
	public List<Product> getAllFromCart(Customer customer){
		Connection connection = DBConnUtil.getConnection("db.properties");
		List<Product> products = new ArrayList<>();
		String getQuery = "SELECT p.product_id, p.name,p.price,c.quantity FROM Cart c JOIN Products p ON c.product_id = p.product_id WHERE c.customer_id = ?";
		try(PreparedStatement ps = connection.prepareStatement(getQuery)){
			ps.setInt(1, customer.getCustomerId());
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				int productId = rs.getInt("product_id");
				String name = rs.getString("name");
				double price = rs.getDouble("price");
				int quantity = rs.getInt("quantity");
				products.add(new Product(productId, name, "",price,quantity));
			}
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		return products;
	}
	
	@Override
	public boolean placeOrder(Customer customer, List<Map<Product, Integer>> productsWithQuantity, String shippingAddress) {
		Connection connection = DBConnUtil.getConnection("db.properties");
		String insertOrderQuery = "INSERT INTO Orders (customer_id, order_date, total_price, shipping_address) VALUES (?, NOW(), ?, ?)";
		String insertOrderItemsQuery = "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)"; 
        double totalPrice = 0;
        
        try {
        	connection.setAutoCommit(false);
        	
        	for(Map<Product, Integer> productQuantityMap : productsWithQuantity) {
        		for(Product product : productQuantityMap.keySet()) {
        			totalPrice += product.getPrice() * productQuantityMap.get(product);
         		}
        	}
        	PreparedStatement ps = connection.prepareStatement(insertOrderQuery, Statement.RETURN_GENERATED_KEYS);
        	ps.setInt(1, customer.getCustomerId());
        	ps.setDouble(2, totalPrice);
        	ps.setString(3, shippingAddress);
        	ps.executeUpdate();
        	
        	ResultSet rs = ps.getGeneratedKeys();
        	int orderId = 0;
        	if(rs.next()) {
        		orderId = rs.getInt(1);
        	}
        	
        	for (Map<Product, Integer> productQuantityMap : productsWithQuantity) {
                for (Product product : productQuantityMap.keySet()) {
                    try (PreparedStatement orderItemsStmt = connection.prepareStatement(insertOrderItemsQuery)) {
                        orderItemsStmt.setInt(1, orderId);
                        orderItemsStmt.setInt(2, product.getProductId());
                        orderItemsStmt.setInt(3, productQuantityMap.get(product));
                        orderItemsStmt.setDouble(4, product.getPrice()); // Set price for order item
                        orderItemsStmt.executeUpdate();
                    }
                }
            }
        	connection.commit();
        	return true;
        }
        catch(SQLException e) {
        	try {
        		connection.rollback();
        	}
        	catch(SQLException ex) {
        		ex.printStackTrace();
        	}
        	e.printStackTrace();
        }
        finally {
        	try {
        		connection.setAutoCommit(true);
        	}
        	catch(SQLException ex) {
        		ex.printStackTrace();
        	}
        }
        return false;
	}
	
	@Override
	public List<Map<Product, Integer>> getOrdersByCustomer(int customerId){
		Connection connection = DBConnUtil.getConnection("db.properties");
		List<Map<Product, Integer>> orders = new ArrayList<>();
		String query = "SELECT oi.order_id, oi.product_id, oi.quantity, p.name, p.price, p.description "
                + "FROM order_items oi "
                + "JOIN products p ON oi.product_id = p.product_id "
                + "JOIN orders o ON o.order_id = oi.order_id "
                + "WHERE o.customer_id = ?";
		
		try(PreparedStatement ps = connection.prepareStatement(query)){
			ps.setInt(1,customerId);
			ResultSet rs = ps.executeQuery();
			
			while(rs.next()) {
				Product product = new Product(
						rs.getInt("product_id"),
						rs.getString("name"),
						rs.getString("description"),
						rs.getDouble("price"),
						0);
			
			int quantity = rs.getInt("quantity");
			
			Map<Product, Integer> productQuantity = new HashMap<>();
			productQuantity.put(product, quantity);
			
			orders.add(productQuantity);
			}
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		return orders;
	}

}
