package com.nickimpact.daycare.storage.dao.sql;

import com.google.common.collect.Lists;
import com.google.gson.JsonSyntaxException;
import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.PluginInfo;
import com.nickimpact.daycare.ranch.Ranch;
import com.nickimpact.daycare.storage.dao.AbstractDao;
import com.nickimpact.daycare.storage.dao.sql.connection.AbstractConnectionFactory;
import com.nickimpact.daycare.utils.MessageUtils;
import lombok.Getter;
import org.spongepowered.api.text.Text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class SqlDao extends AbstractDao {

	private static final String ADD_RANCH = "INSERT INTO `{prefix}ranches` VALUES ('%s', '%s')";
	private static final String UPDATE_RANCH = "UPDATE `{prefix}ranches` SET RANCH='%s' WHERE UUID='%s'";
	private static final String GET_RANCH = "SELECT RANCH FROM `{prefix}ranches` WHERE UUID='%s'";
	private static final String GET_ALL_RANCHES = "SELECT * FROM `{prefix}ranches`";
	private static final String DELETE_RANCH = "DELETE FROM `{prefix}ranches` WHERE UUID='%s'";


	@Getter
	private final AbstractConnectionFactory provider;

	@Getter
	private final Function<String, String> prefix;

	public SqlDao(DaycarePlugin plugin, AbstractConnectionFactory provider, String prefix) {
		super(plugin, provider.getName());
		this.provider = provider;
		this.prefix = s -> s.replace("{prefix}", prefix);
	}

	private boolean tableExists(String table) throws SQLException {
		try(Connection connection = provider.getConnection()) {
			try (ResultSet rs = connection.getMetaData().getTables(null, null, "%", null)) {
				while(rs.next()) {
					if(rs.getString(3).equalsIgnoreCase(table)) {
						return true;
					}
				}
				return false;
			}
		}
	}

	@Override
	public void init() {
		try {
			provider.init();

			// Init tables
			if(!tableExists(prefix.apply("{prefix}listings"))) {
				String schemaFileName = "assets/schema/" + provider.getName().toLowerCase() + ".sql";
				try (InputStream is = plugin.getResourceStream(schemaFileName)) {
					if(is == null) {
						throw new Exception("Couldn't locate schema file for " + provider.getName());
					}

					try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
						try (Connection connection = provider.getConnection()) {
							try (Statement s = connection.createStatement()) {
								StringBuilder sb = new StringBuilder();
								String line;
								while ((line = reader.readLine()) != null) {
									if (line.startsWith("--") || line.startsWith("#")) continue;

									sb.append(line);

									// check for end of declaration
									if (line.endsWith(";")) {
										sb.deleteCharAt(sb.length() - 1);

										String result = prefix.apply(sb.toString().trim());
										if (!result.isEmpty())
											s.addBatch(result);

										// reset
										sb = new StringBuilder();
									}
								}
								s.executeBatch();
							}
						}
					}
				}
			}
		} catch (Exception e) {
			plugin.getConsole().ifPresent(console -> console.sendMessage(Text.of(
					PluginInfo.ERROR_PREFIX, "An error occurred whilst initializing the database..."
			)));
			e.printStackTrace();
		}
	}

	@Override
	public void shutdown() {
		try {
			provider.shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addOrUpdate(String key, Ranch ranch) {
		try (Connection connection = provider.getConnection()) {
			String stmt = prefix.apply(key);
			stmt = String.format(stmt, ranch.getOwnerUUID(), DaycarePlugin.prettyGson.toJson(ranch));
			try (PreparedStatement ps = connection.prepareStatement(stmt)) {
				ps.executeUpdate();
				ps.close();
			}
		} catch (Exception e) {
			DaycarePlugin.getInstance().getConsole().ifPresent(console -> console.sendMessage(
					Text.of(PluginInfo.ERROR_PREFIX, "Something happened during the writing process")
			));
			e.printStackTrace();
		}
	}

	@Override
	public void addRanch(Ranch ranch) {
		this.addOrUpdate(ADD_RANCH, ranch);
	}

	@Override
	public void updateRanch(Ranch ranch) {
		this.addOrUpdate(UPDATE_RANCH, ranch);
	}

	@Override
	public void deleteRanch(UUID uuid) throws Exception {
		try (Connection connection = provider.getConnection()) {
			String stmt = prefix.apply(DELETE_RANCH);
			stmt = String.format(stmt, uuid.toString());
			try (PreparedStatement ps = connection.prepareStatement(stmt)) {
				ps.executeUpdate();
				ps.close();
			}
		}
	}

	@Override
	public Ranch getRanch(UUID uuid) throws Exception {
		DaycarePlugin.getInstance().getConsole().ifPresent(console -> {
			console.sendMessage(Text.of(PluginInfo.DEBUG_PREFIX, "Attempting to fetch ranch..."));
		});
		try (Connection connection = provider.getConnection()) {
			String stmt = prefix.apply(GET_RANCH);
			stmt = String.format(stmt, uuid.toString());
			try (PreparedStatement query = connection.prepareStatement(stmt)) {
				ResultSet results = query.executeQuery();
				if(results.next()) {
					DaycarePlugin.getInstance().getConsole().ifPresent(console -> {
						console.sendMessage(Text.of(PluginInfo.DEBUG_PREFIX, "Ranch found!"));
					});

					Ranch ranch = DaycarePlugin.prettyGson.fromJson(results.getString("ranch"), Ranch.class);
					results.close();
					query.close();

					return ranch;
				} else {
					DaycarePlugin.getInstance().getConsole().ifPresent(console -> {
						console.sendMessage(Text.of(PluginInfo.DEBUG_PREFIX, "No ranch found..."));
					});
					return null;
				}
			}
		}
	}

	@Override
	public List<Ranch> getAllRanches() throws Exception {
		List<Ranch> entries = Lists.newArrayList();
		try (Connection connection = provider.getConnection()) {
			try (PreparedStatement query = connection.prepareStatement(prefix.apply(GET_ALL_RANCHES))) {
				ResultSet results = query.executeQuery();
				while(results.next()) {
					try {
						entries.add(DaycarePlugin.prettyGson.fromJson(results.getString("ranch"), Ranch.class));
					} catch (JsonSyntaxException e) {
						MessageUtils.genAndSendErrorMessage(
								"JSON Syntax Error",
								"Invalid Ranch JSON detected",
								"Ranch Owner: " + results.getString("uuid")
						);
					}
				}
				results.close();
				query.close();
			}
		}

		return entries;
	}

	@Override
	public void purge(boolean logs) throws Exception {

	}

	@Override
	public void save() throws Exception {}
}
