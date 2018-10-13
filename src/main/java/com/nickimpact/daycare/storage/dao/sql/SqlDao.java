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

package com.nickimpact.daycare.storage.dao.sql;

import com.google.common.collect.Lists;
import com.google.gson.JsonSyntaxException;
import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.DaycareInfo;
import com.nickimpact.daycare.ranch.DaycareNPC;
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

public class SqlDao extends AbstractDao {

	private static final String ADD_RANCH = "INSERT INTO `{prefix}ranches` VALUES ('%s', '%s')";
	private static final String UPDATE_RANCH = "UPDATE `{prefix}ranches` SET RANCH='%s' WHERE UUID='%s'";
	private static final String GET_RANCH = "SELECT RANCH FROM `{prefix}ranches` WHERE UUID='%s'";
	private static final String GET_ALL_RANCHES = "SELECT * FROM `{prefix}ranches`";
	private static final String DELETE_RANCH = "DELETE FROM `{prefix}ranches` WHERE UUID='%s'";
	private static final String ADD_NPC = "INSERT INTO `{prefix}npcs` VALUES ('%s', '%s')";
	private static final String DELETE_NPC = "DELETE FROM `{prefix}npcs` WHERE UUID='%s'";
	private static final String GET_NPCS = "SELECT * FROM `{prefix}npcs`";

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
	public void init() throws Exception {
		provider.init();

		// Init tables
		if(!tableExists(prefix.apply("{prefix}ranches"))) {
			String schemaFileName = "schema/" + provider.getName().toLowerCase() + ".sql";
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
			if(key.equals(UPDATE_RANCH)) {
				stmt = String.format(stmt, DaycarePlugin.prettyGson.toJson(ranch), ranch.getOwnerUUID());
			} else {
				stmt = String.format(stmt, ranch.getOwnerUUID(), DaycarePlugin.prettyGson.toJson(ranch));
			}
			try (PreparedStatement ps = connection.prepareStatement(stmt)) {
				ps.executeUpdate();
			}
		} catch (Exception e) {
			DaycarePlugin.getInstance().getLogger().error(Text.of("Something happened during the writing process"));
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
	public void updateAll(List<Ranch> ranches) throws Exception {
		try (Connection connection = provider.getConnection()) {
			for(Ranch ranch : ranches) {
				String stmt = prefix.apply(UPDATE_RANCH);
				stmt = String.format(stmt, DaycarePlugin.prettyGson.toJson(ranch), ranch.getOwnerUUID());

				try (PreparedStatement ps = connection.prepareStatement(stmt)) {
					ps.executeUpdate();
				}
			}
		}

	}

	@Override
	public void deleteRanch(UUID uuid) throws Exception {
		try (Connection connection = provider.getConnection()) {
			String stmt = prefix.apply(DELETE_RANCH);
			stmt = String.format(stmt, uuid.toString());
			try (PreparedStatement ps = connection.prepareStatement(stmt)) {
				ps.executeUpdate();
			}
		}
	}

	@Override
	public Ranch getRanch(UUID uuid) throws Exception {
		try (Connection connection = provider.getConnection()) {
			String stmt = prefix.apply(GET_RANCH);
			stmt = String.format(stmt, uuid.toString());
			try (PreparedStatement query = connection.prepareStatement(stmt)) {
				ResultSet results = query.executeQuery();
				if(results.next()) {
					Ranch ranch = DaycarePlugin.prettyGson.fromJson(results.getString("ranch"), Ranch.class);
					results.close();
					query.close();

					return ranch;
				} else {
					return null;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
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
			}
		}

		return entries;
	}

	@Override
	public void addNPC(DaycareNPC npc) throws Exception {
		try (Connection connection = provider.getConnection()) {
			String stmt = prefix.apply(ADD_NPC);
			stmt = String.format(stmt, npc.getUuid(), npc.getName());
			try (PreparedStatement ps = connection.prepareStatement(stmt)) {
				ps.executeUpdate();
			}
		} catch (Exception e) {
			DaycarePlugin.getInstance().getLogger().error(Text.of("Something happened during the writing process"));
			e.printStackTrace();
		}
	}

	@Override
	public void deleteNPC(DaycareNPC npc) throws Exception {
		try (Connection connection = provider.getConnection()) {
			String stmt = prefix.apply(DELETE_NPC);
			stmt = String.format(stmt, npc.getUuid());
			try (PreparedStatement ps = connection.prepareStatement(stmt)) {
				ps.executeUpdate();
			}
		}
	}

	@Override
	public List<DaycareNPC> getNPCS() throws Exception {
		List<DaycareNPC> npcs = Lists.newArrayList();
		try (Connection connection = provider.getConnection()) {
			try (PreparedStatement query = connection.prepareStatement(prefix.apply(GET_NPCS))) {
				ResultSet results = query.executeQuery();
				while(results.next()) {
					UUID uuid = UUID.fromString(results.getString("uuid"));
					String name = results.getString("name");
					npcs.add(new DaycareNPC(uuid, name));
				}
				results.close();
			}
		}

		return npcs;
	}

	@Override
	public void purge(boolean logs) throws Exception {

	}

	@Override
	public void save() throws Exception {}
}
