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

package com.nickimpact.daycare.common.storage.implementation.sql;

import com.google.common.collect.Lists;
import com.nickimpact.daycare.api.IDaycarePlugin;
import com.nickimpact.daycare.api.breeding.BreedStage;
import com.nickimpact.daycare.common.deprecated.OldRanch;
import com.nickimpact.daycare.common.storage.implementation.StorageImplementation;
import com.nickimpact.daycare.api.pens.DaycareNPC;
import com.nickimpact.daycare.api.pens.DaycarePokemonWrapper;
import com.nickimpact.daycare.api.pens.Pen;
import com.nickimpact.daycare.api.pens.Ranch;
import com.nickimpact.daycare.api.pens.Settings;
import com.nickimpact.daycare.api.pens.Statistics;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.storage.sql.ConnectionFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@SuppressWarnings("all")
public class SqlImplementation implements StorageImplementation {

	// Ranches
	private static final String ADD_RANCH = "INSERT INTO `{prefix}ranch` VALUES(?, ?, ?)";
	private static final String ADD_PEN = "INSERT INTO `{prefix}pens` VALUES(?, ?, ?)";
	private static final String ADD_PEN_DATA = "INSERT INTO `{prefix}pen` (pen, unlocked, settings) VALUES(?, ?, ?)";

	private static final String UPDATE_RANCH_STATISTICS = "UPDATE `{prefix}ranch` SET stats = ? WHERE owner = ? AND ranch = ?";
	private static final String UPDATE_PEN_SLOT = "UPDATE `{prefix}pen` SET {{pen_slot}} = ? WHERE pen = ?";
	private static final String UPDATE_PEN_EGG = "UPDATE `{prefix}pen` SET egg = ? WHERE pen = ?";
	private static final String UPDATE_PEN_STAGE = "UPDATE `{prefix}pen` SET stage = ? WHERE pen = ?";
	private static final String UPDATE_PEN_SETTINGS = "UPDATE `{prefix}pen` SET settings = ? WHERE pen = ?";
	private static final String UPDATE_PEN_UNLOCK_STATUS = "UPDATE `{prefix}pen` SET unlocked = ?, dateUnlock = ? WHERE pen = ?";

	private static final String DELETE_RANCH = "DELETE FROM `{prefix}ranch` WHERE owner = ?";
	private static final String DELETE_PEN_BASE = "DELETE FROM `{prefix}pens` WHERE ranch = ?";
	private static final String DELETE_PEN_DATA = "DELETE FROM `{prefix}pen` WHERE pen = ?";

	// NPCs
	private static final String ADD_NPC = "INSERT INTO `{prefix}npcs` VALUES(?, ?)";
	private static final String DELETE_NPC = "DELETE FROM  `{prefix}npcs` WHERE uuid = ?";
	private static final String GET_NPCS = "SELECT * FROM `{prefix}npcs`";

	private static final String GET_RANCH =
			"SELECT ranch, stats " +
			"FROM `{prefix}ranch`" +
			"WHERE owner = ?";
	private static final String GET_RANCH_DATA =
			"SELECT p.pen, p.id, d.slot1, d.slot2, d.egg, d.unlocked, d.dateUnlock, d.settings, d.stage " +
			"FROM `{prefix}pens` p " +
			"INNER JOIN `{prefix}pen` d " +
			"WHERE p.ranch = ? AND p.pen = d.pen";

	@Deprecated
	private static final String FETCH_OLD = "SELECT * FROM {prefix}listings_v2";

	private final IDaycarePlugin plugin;

	private final ConnectionFactory connectionFactory;
	private final Function<String, String> processor;

	public SqlImplementation(IDaycarePlugin plugin, ConnectionFactory connectionFactory, String tablePrefix) {
		this.plugin = plugin;
		this.connectionFactory = connectionFactory;
		this.processor = connectionFactory.getStatementProcessor().compose(s -> s.replaceAll("\\{prefix}", tablePrefix));
	}

	@Override
	public IDaycarePlugin getPlugin() {
		return this.plugin;
	}

	@Override
	public String getName() {
		return this.connectionFactory.getImplementationName();
	}

	public ConnectionFactory getConnectionFactory() {
		return this.connectionFactory;
	}

	public Function<String, String> getStatementProcessor() {
		return this.processor;
	}

	@Override
	public void init() throws Exception {
		this.connectionFactory.init();

		if (!tableExists(this.processor.apply("{prefix}pens"))) {
			String schemaFileName = "com/nickimpact/daycare/schema/" + this.connectionFactory.getImplementationName().toLowerCase() + ".sql";
			try (InputStream is = plugin.getResourceStream(schemaFileName)) {
				if (is == null) {
					throw new Exception("Couldn't locate schema file for " + this.connectionFactory.getImplementationName());
				}

				try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
					try (Connection connection = this.connectionFactory.getConnection()) {
						try (Statement s = connection.createStatement()) {
							StringBuilder sb = new StringBuilder();
							String line;
							while ((line = reader.readLine()) != null) {
								if (line.startsWith("--") || line.startsWith("#")) continue;

								sb.append(line);

								// check for end of declaration
								if (line.endsWith(";")) {
									sb.deleteCharAt(sb.length() - 1);

									String result = this.processor.apply(sb.toString().trim());
									if (!result.isEmpty()) {
										int start = result.indexOf('`');
										if (!tableExists(result.substring(start + 1, result.indexOf('`', start + 1)))) {
											s.addBatch(result);
										}
									}

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
	public void shutdown() throws Exception {
		this.connectionFactory.shutdown();
	}

	@Override
	public Map<String, String> getMeta() {
		return this.connectionFactory.getMeta();
	}

	@Override
	public boolean addRanch(Ranch ranch) throws Exception {
		try {
			Connection connection = this.connectionFactory.getConnection();
			PreparedStatement ps = connection.prepareStatement(this.processor.apply(ADD_RANCH));
			ps.setString(1, ranch.getOwnerUUID().toString());
			ps.setString(2, ranch.getIdentifier().toString());
			Clob stats = connection.createClob();
			stats.setString(1, plugin.getGson().toJson(new Statistics()));
			ps.setClob(3, stats);
			ps.executeUpdate();

			for (Pen pen : (List<Pen>) ranch.getPens()) {
				PreparedStatement ps2 = connection.prepareStatement(this.processor.apply(ADD_PEN));
				ps2.setString(1, ranch.getIdentifier().toString());
				ps2.setString(2, pen.getIdentifier().toString());
				ps2.setInt(3, pen.getID());
				ps2.executeUpdate();

				PreparedStatement ps3 = connection.prepareStatement(this.processor.apply(ADD_PEN_DATA));
				ps3.setString(1, pen.getIdentifier().toString());
				ps3.setBoolean(2, pen.isUnlocked());

				Clob settings = connection.createClob();
				settings.setString(1, plugin.getGson().toJson(pen.getSettings()));
				ps3.setClob(3, settings);
				ps3.executeUpdate();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	@Override
	public boolean updateRanch(Ranch ranch) throws Exception {
		try {
			Connection connection = this.connectionFactory.getConnection();
			PreparedStatement r = connection.prepareStatement(this.processor.apply(UPDATE_RANCH_STATISTICS));
			Clob stats = connection.createClob();
			stats.setString(1, plugin.getGson().toJson(ranch.getStats()));
			r.setClob(1, stats);
			r.setString(2, ranch.getOwnerUUID().toString());
			r.setString(3, ranch.getIdentifier().toString());
			r.executeUpdate();

			for (Pen pen : (List<Pen>) ranch.getPens()) {
				if (pen.getAtPosition(1).isPresent()) {
					this.updatePenSlot(connection, pen.getIdentifier(), PenSlot.FIRST, (DaycarePokemonWrapper) pen.getAtPosition(1).get());
				} else {
					this.updatePenSlot(connection, pen.getIdentifier(), PenSlot.FIRST, null);
				}

				if (pen.getAtPosition(2).isPresent()) {
					this.updatePenSlot(connection, pen.getIdentifier(), PenSlot.SECOND, (DaycarePokemonWrapper) pen.getAtPosition(2).get());
				} else {
					this.updatePenSlot(connection, pen.getIdentifier(), PenSlot.SECOND, null);
				}

				PreparedStatement egg = connection.prepareStatement(processor.apply(UPDATE_PEN_EGG));
				if (pen.getEgg().isPresent()) {
					Clob e = connection.createClob();
					e.setString(1, plugin.getGson().toJson(pen.getEgg().get(), DaycarePokemonWrapper.class));
					egg.setClob(1, e);
				} else {
					egg.setNull(1, Types.CLOB);
				}
				egg.setString(2, pen.getIdentifier().toString());
				egg.executeUpdate();

				PreparedStatement settings = connection.prepareStatement(processor.apply(UPDATE_PEN_SETTINGS));
				Clob s = connection.createClob();
				s.setString(1, plugin.getGson().toJson(pen.getSettings()));
				settings.setClob(1, s);
				settings.setString(2, pen.getIdentifier().toString());
				settings.executeUpdate();
				pen.getSettings().clean();

				PreparedStatement stage = connection.prepareStatement(processor.apply(UPDATE_PEN_STAGE));
				if (pen.getStage() == null) {
					stage.setNull(1, Types.VARCHAR);
				} else {
					stage.setString(1, pen.getStage().name());
				}
				stage.setString(2, pen.getIdentifier().toString());
				stage.executeUpdate();

				if (pen.isUnlocked()) {
					PreparedStatement unlocked = connection.prepareStatement(processor.apply(UPDATE_PEN_UNLOCK_STATUS));
					unlocked.setBoolean(1, true);
					if (pen.getDateUnlocked() == null) {
						pen.unlock();
					}
					unlocked.setTimestamp(2, Timestamp.valueOf(pen.getDateUnlocked()));
					unlocked.setString(3, pen.getIdentifier().toString());
					unlocked.executeUpdate();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	private void updatePenSlot(Connection connection, UUID pen, PenSlot slot, DaycarePokemonWrapper wrapper) throws Exception {
		String query = processor.apply(UPDATE_PEN_SLOT.replace("{{pen_slot}}", slot.getKey()));
		PreparedStatement ps = connection.prepareStatement(query);
		if(wrapper == null) {
			ps.setNull(1, Types.CLOB);
		} else {
			Clob pokemon = connection.createClob();
			pokemon.setString(1, plugin.getGson().toJson(wrapper, DaycarePokemonWrapper.class));
			ps.setClob(1, pokemon);
		}
		ps.setString(2, pen.toString());
		ps.executeUpdate();
	}

	@Override
	public boolean deleteRanch(Ranch ranch) throws Exception {
		Connection connection = connectionFactory.getConnection();
		PreparedStatement r = connection.prepareStatement(processor.apply(DELETE_RANCH));
		r.setString(1, ranch.getOwnerUUID().toString());
		r.executeUpdate();

		PreparedStatement p = connection.prepareStatement(processor.apply(DELETE_PEN_BASE));
		p.setString(1, ranch.getIdentifier().toString());
		p.executeUpdate();

		for(Pen pen : (List<Pen>) ranch.getPens()) {
			PreparedStatement pens = connection.prepareStatement(processor.apply(DELETE_PEN_DATA));
			pens.setString(1, pen.getIdentifier().toString());
			pens.executeUpdate();
		}

		return true;
	}

	@Override
	public Optional<Ranch> getRanch(UUID player) throws Exception {
		Ranch ranch = null;
		if(tableExists(processor.apply("{prefix}ranches"))) {
			plugin.getPluginLogger().warn("Detected legacy database, attempting to update...");

			Connection connection = connectionFactory.getConnection();
			PreparedStatement ps = connection.prepareStatement(processor.apply("SELECT * FROM `{prefix}ranches`"));
			ResultSet results = ps.executeQuery();

			int success = 0;
			int failed = 0;
			while(results.next()) {
				try {
					OldRanch r = plugin.getGson().fromJson(results.getString("ranch"), OldRanch.class);
					plugin.getPluginLogger().warn("Attempting to convert ranch for owner with ID: " + r.getOwnerUUID().toString());
					this.convert(r);
					++success;
				} catch (Exception e) {
					++failed;
					e.printStackTrace();
				}
			}

			if(failed == 0) {
				plugin.getPluginLogger().warn(String.format("Converted %d ranches successfully, purging old database...", success));
				PreparedStatement drop = connection.prepareStatement(processor.apply("DROP TABLE `{prefix}ranches`"));
				drop.executeUpdate();
			} else {
				plugin.getPluginLogger().warn("Failed to convert " + failed + " ranches, preserving any data that failed to be dropped!");
			}
		}

		Connection connection = connectionFactory.getConnection();
		PreparedStatement ps = connection.prepareStatement(processor.apply(GET_RANCH));
		ps.setString(1, player.toString());
		ResultSet results = ps.executeQuery();

		if (results.next()) {
			Ranch.RanchBuilder builder = Impactor.getInstance().getRegistry().createBuilder(Ranch.RanchBuilder.class);

			String rID = results.getString("ranch");
			builder.identifier(UUID.fromString(rID));
			builder.owner(player);
			builder.stats(plugin.getGson().fromJson(results.getString("stats"), Statistics.class));

			PreparedStatement ps2 = connection.prepareStatement(processor.apply(GET_RANCH_DATA));
			ps2.setString(1, rID);
			ResultSet rs2 = ps2.executeQuery();

			List<Pen> pens = Lists.newArrayList();
			while (rs2.next()) {
				Pen.PenBuilder pb = Impactor.getInstance().getRegistry().createBuilder(Pen.PenBuilder.class);
				pb.identifier(UUID.fromString(rs2.getString(1)));
				pb.id(rs2.getInt(2));

				pb.slot1(plugin.getGson().fromJson(rs2.getString(3), DaycarePokemonWrapper.class));
				pb.slot2(plugin.getGson().fromJson(rs2.getString(4), DaycarePokemonWrapper.class));
				pb.egg(plugin.getGson().fromJson(rs2.getString(5), DaycarePokemonWrapper.class));

				pb.unlocked(rs2.getBoolean(6));
				Timestamp timestamp = rs2.getTimestamp(7);
				if(timestamp != null) {
					pb.dateUnlocked(rs2.getTimestamp(7).toLocalDateTime());
				}
				pb.settings(plugin.getGson().fromJson(rs2.getString(8), Settings.class));

				String stage = rs2.getString(9);
				if(stage != null) {
					pb.stage(BreedStage.valueOf(rs2.getString(9)));
				}
				pens.add(pb.build());
			}

			ranch = builder.pens(pens).build();
		}

		return Optional.ofNullable(ranch);
	}

	@Override
	public boolean addNPC(DaycareNPC npc) throws Exception {
		Connection connection = this.connectionFactory.getConnection();
		PreparedStatement ps = connection.prepareStatement(this.processor.apply(ADD_NPC));
		ps.setString(1, npc.getUuid().toString());
		ps.setString(2, npc.getName());
		ps.executeUpdate();

		return true;
	}

	@Override
	public boolean deleteNPC(DaycareNPC npc) throws Exception {
		Connection connection = this.connectionFactory.getConnection();
		PreparedStatement ps = connection.prepareStatement(this.processor.apply(DELETE_NPC));
		ps.setString(1, npc.getUuid().toString());
		ps.executeUpdate();

		return true;
	}

	@Override
	public List<DaycareNPC> getNPCs() throws Exception {
		List<DaycareNPC> npcs = Lists.newArrayList();
		Connection connection = this.connectionFactory.getConnection();
		PreparedStatement ps = connection.prepareStatement(this.processor.apply(GET_NPCS));
		ResultSet results = ps.executeQuery();
		while(results.next()) {
			npcs.add(new DaycareNPC(UUID.fromString(results.getString("uuid")), results.getString("name")));
		}

		return npcs;
	}

	private boolean tableExists(String table) throws SQLException {
		try (Connection connection = this.connectionFactory.getConnection()) {
			try (ResultSet rs = connection.getMetaData().getTables(null, null, "%", null)) {
				while (rs.next()) {
					if (rs.getString(3).equalsIgnoreCase(table)) {
						return true;
					}
				}
				return false;
			}
		}
	}

	private void convert(OldRanch ranch) throws Exception {
		List<Pen> pens = Lists.newArrayList();
		AtomicInteger i = new AtomicInteger(1);
		ranch.getPens().forEach(op -> {
			pens.add(Pen.builder()
					.id(i.getAndIncrement())
					.identifier(UUID.randomUUID())
					.settings(ranch.getSettings())
					.dateUnlocked(op.getDateUnlocked() != null ? op.getDateUnlocked().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null)
					.unlocked(op.isUnlocked())
					.slot1(op.getSlot1() != null ? DaycarePokemonWrapper
							.builder()
							.json(op.getSlot1().getJson())
							.gainedLvls(op.getSlot1().getGainedLvls())
							.lastLvl(op.getSlot1().getLastLvl().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
							.build() : null
					)
					.slot2(op.getSlot2() != null ? DaycarePokemonWrapper
							.builder()
							.json(op.getSlot2().getJson())
							.gainedLvls(op.getSlot2().getGainedLvls())
							.lastLvl(op.getSlot2().getLastLvl().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
							.build() : null
					)
					.egg(op.getEgg() != null ? DaycarePokemonWrapper
							.builder()
							.json(op.getEgg().getJson())
							.build() : null
					)
					.build()
			);
		});

		Ranch r = Ranch.builder()
				.identifier(UUID.randomUUID())
				.owner(ranch.getOwnerUUID())
				.pens(pens)
				.stats(ranch.getStats())
				.build();
		this.addRanch(r);
	}
}
