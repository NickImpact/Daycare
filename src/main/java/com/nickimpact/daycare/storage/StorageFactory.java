package com.nickimpact.daycare.storage;

import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.PluginInfo;
import com.nickimpact.daycare.configuration.ConfigKeys;
import com.nickimpact.daycare.storage.dao.AbstractDao;
import com.nickimpact.daycare.storage.dao.sql.SqlDao;
import com.nickimpact.daycare.storage.dao.sql.connection.file.H2ConnectionFactory;
import com.nickimpact.daycare.storage.dao.sql.connection.hikari.MySqlConnectionFactory;
import com.nickimpact.daycare.utils.MessageUtils;
import org.spongepowered.api.text.Text;

import java.io.File;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
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

	public static Storage getInstance(DaycarePlugin gts, StorageType defaultMethod) {
		String method = gts.getConfig().get(ConfigKeys.STORAGE_METHOD);
		StorageType type = StorageType.parse(method);
		if (type == null) {
			type = defaultMethod;
		}

		final StorageType declared = type;

		gts.getConsole().ifPresent(console -> console.sendMessages(Text.of(PluginInfo.PREFIX, "Loading storage provider... [" + declared.getName() + "]")));
		Storage storage = makeInstance(declared, gts);
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
