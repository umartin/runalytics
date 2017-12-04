package com.github.umartin.runalytics;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class PostgresStorage {

	public interface Template {
		void accept(Connection con) throws Exception;
	}

	String url = System.getProperty("db.url", "jdbc:postgresql://localhost:5432/runalytics");
	String user = System.getProperty("db.user", "postgres");
	String password = System.getProperty("db.password", "");

	private Connection getConnection() throws SQLException, ClassNotFoundException {
		Class.forName("org.postgresql.Driver");
		Properties props = new Properties();
		props.setProperty("user", user);
		props.setProperty("password", password);
		return DriverManager.getConnection(url, props);
	}

	private void execute(Template consumer) {
		try (Connection con = getConnection()) {
			consumer.accept(con);
		} catch (Exception exc) {
			throw new RuntimeException(exc);
		}
	}

	public void storeFile(String filename, String jsonContent) {
		System.out.println(String.format("Storing %s, %s", filename, jsonContent));
		execute(con -> {
			try (PreparedStatement stmt = con.prepareStatement("insert into activity (filename, json) values (?, cast(? as jsonb))")) {
				stmt.setString(1, filename);
				stmt.setString(2, jsonContent);
				stmt.executeUpdate();
			}
		});
	}
}
