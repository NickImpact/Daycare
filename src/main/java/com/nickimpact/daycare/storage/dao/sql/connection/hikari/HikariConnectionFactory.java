/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.nickimpact.daycare.storage.dao.sql.connection.hikari;

import com.nickimpact.daycare.storage.StorageCredentials;
import com.nickimpact.daycare.storage.dao.sql.connection.AbstractConnectionFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public abstract class HikariConnectionFactory extends AbstractConnectionFactory {

	protected final StorageCredentials configuration;

	private HikariDataSource hikari;

	public HikariConnectionFactory(String name, StorageCredentials configuration) {
		super(name);
		this.configuration = configuration;
	}

	protected String getDriverClass() {
		return null;
	}

	protected void appendProperties(HikariConfig config, StorageCredentials credentials) {}

	protected void appendConfigurationInfo(HikariConfig config) {
		String address = configuration.getAddress();
		String[] addressSplit = address.split(":");
		address = addressSplit[0];
		String port = addressSplit.length > 1 ? addressSplit[1] : "3306";

		config.setDataSourceClassName(getDriverClass());
		config.addDataSourceProperty("serverName", address);
		config.addDataSourceProperty("port", port);
		config.addDataSourceProperty("databasename", configuration.getDatabase());
		config.setUsername(configuration.getUsername());
		config.setPassword(configuration.getPassword());
	}

	@Override
	public void init() {
		HikariConfig config = new HikariConfig();
		config.setPoolName("daycare");

		appendConfigurationInfo(config);
		appendProperties(config, configuration);

		config.setMaximumPoolSize(10);
		config.setMinimumIdle(10);
		config.setMaxLifetime(1800000);
		config.setConnectionTimeout(5000);

		config.setLeakDetectionThreshold(TimeUnit.SECONDS.toMillis(10));

		config.setConnectionTestQuery("/* GTS ping */ SELECT 1");

		config.setInitializationFailFast(false);

		hikari = new HikariDataSource(config);
	}

	@Override
	public void shutdown() throws Exception {
		if(hikari != null && !hikari.isClosed())
			hikari.close();
	}

	@Override
	public Connection getConnection() throws SQLException {
		Connection connection = hikari.getConnection();
		if(connection == null)
			throw new SQLException("Unable to get a connection from the pool");

		return connection;
	}
}
