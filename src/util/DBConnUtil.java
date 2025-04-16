package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public class DBConnUtil {
	
	private DBConnUtil() {
	}
	
	public static Connection getConnection(String file_name) {
		Connection connection = null;
		try {
			Properties props = DBPropertyUtil.getPropertyString(file_name);
			
			if (props == null) {
                throw new RuntimeException("Properties file could not be loaded.");
            }

            String driver = props.getProperty("driver_class_name");
            String url = props.getProperty("db.url");
            String username = props.getProperty("db.username");
            String password = props.getProperty("db.password");

            Class.forName(driver);
            connection = DriverManager.getConnection(url, username, password);
            System.out.println("Successfull!!!");
		}
		catch(ClassNotFoundException | SQLException e) {
			System.out.println("Nope!!!");
			e.printStackTrace();
		}
		return connection;
	}

}
