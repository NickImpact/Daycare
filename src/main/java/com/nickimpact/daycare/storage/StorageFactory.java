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

package com.nickimpact.daycare.storage;

import com.google.common.collect.Maps;
import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.DaycareInfo;
import com.nickimpact.daycare.configuration.ConfigKeys;
import com.nickimpact.daycare.configuration.MsgConfigKeys;
import com.nickimpact.daycare.storage.dao.AbstractDao;
import com.nickimpact.daycare.storage.dao.sql.SqlDao;
import com.nickimpact.daycare.storage.dao.sql.connection.file.H2ConnectionFactory;
import com.nickimpact.daycare.storage.dao.sql.connection.hikari.MySqlConnectionFactory;
import com.nickimpact.daycare.utils.MessageUtils;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class StorageFactory {

	public static StorageType getStorageType(DaycarePlugin gts, StorageType defaultType) {
		String type = gts.getConfig().get(ConfigKeys.STORAGE_METHOD);
		StorageType st = StorageType.parse(type);

		if(st == null) {
			MessageUtils.genAndSendErrorMessage(
					"Config Error",
					"Invalid Storage Type found",
					"Type Specified: " + type,
					"Defaulting to: " + defaultType.getName()
			);
			st = defaultType;
		}

		return st;
	}

	public static Storage getInstance(DaycarePlugin plugin, StorageType defaultMethod) throws Exception {
		String method = plugin.getConfig().get(ConfigKeys.STORAGE_METHOD);
		StorageType type = StorageType.parse(method);
		if (type == null) {
			type = defaultMethod;
		}

		final StorageType declared = type;

		Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
		tokens.put("storage_type", src -> Optional.of(Text.of(declared.getName())));
		plugin.getLogger().info(MessageUtils.fetchAndParseMsg(null, MsgConfigKeys.STARTUP_STORAGE_PROVIDER, tokens, null));
		Storage storage = makeInstance(declared, plugin);
		storage.init();
		return storage;
	}

	private static Storage makeInstance(StorageType type, DaycarePlugin plugin) {
		return AbstractStorage.create(plugin, makeDao(type, plugin));
	}

	private static AbstractDao makeDao(StorageType type, DaycarePlugin plugin) {
		switch(type) {
			case MYSQL:
				return new SqlDao(
						plugin,
						new MySqlConnectionFactory(plugin.getConfig().get(ConfigKeys.DATABASE_VALUES)),
						plugin.getConfig().get(ConfigKeys.SQL_TABLE_PREFIX)
				);
			case H2:
				return new SqlDao(
						plugin,
						new H2ConnectionFactory(new File(plugin.getDataDirectory(), "daycare-h2")),
						plugin.getConfig().get(ConfigKeys.SQL_TABLE_PREFIX)
				);
			default:
				//return new JsonDao(plugin);
				return new SqlDao(
						plugin,
						new H2ConnectionFactory(new File(plugin.getDataDirectory(), "daycare-h2")),
						plugin.getConfig().get(ConfigKeys.SQL_TABLE_PREFIX)
				);
		}
	}
}
