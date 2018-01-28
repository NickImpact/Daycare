package com.nickimpact.daycare.configuration;

import com.google.common.collect.ImmutableMap;
import com.nickimpact.daycare.api.configuration.ConfigKey;
import com.nickimpact.daycare.api.configuration.keys.*;
import com.nickimpact.daycare.storage.StorageCredentials;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * (Some note will appear here)
 *
 * @author NickImpact (Nick DeGruccio)
 */
public class ConfigKeys {

	public static final ConfigKey<Integer> NUM_PENS = IntegerKey.of("gfs", 14);
	public static final ConfigKey<Boolean> FIRST_PEN_UNLOCKED = BooleanKey.of("sgsda", true);
	public static final ConfigKey<Double> BASE_PEN_PRICE = DoubleKey.of("dgadsgas", 5000.0);
	public static final ConfigKey<Double> INCREMENT_PEN_PRICE = DoubleKey.of("sfdhasdg", 500.0);


	public static final ConfigKey<String> STORAGE_METHOD = StringKey.of("storage-method", "h2");
	public static final ConfigKey<String> SQL_TABLE_PREFIX = StringKey.of("data.sql_table_prefix", "daycare_");
	public static final ConfigKey<StorageCredentials> DATABASE_VALUES = EnduringKey.wrap(AbstractKey.of(c -> new StorageCredentials(
			c.getString("data.address", null),
			c.getString("data.database", null),
			c.getString("data.username", null),
			c.getString("data.password", null)
	)));
	public static final ConfigKey<Long> MAX_BREEDING_WAIT_TIME = LongKey.of("aga", 50);
	public static final ConfigKey<Double> EGG_CHANCE = DoubleKey.of("dsgqe", 15.0);
	public static final ConfigKey<Long> LVL_WAIT_TIME = LongKey.of("asdga", 60);
	public static final ConfigKey<Integer> LVL_TASK_TIME = IntegerKey.of("asdhgah", 60);
	public static final ConfigKey<Integer> BREEDING_TASK_TIME = IntegerKey.of("ahfns", 300);

	private static Map<String, ConfigKey<?>> KEYS = null;

	public static synchronized Map<String, ConfigKey<?>> getAllKeys() {
		if(KEYS == null) {
			Map<String, ConfigKey<?>> keys = new LinkedHashMap<>();

			try {
				Field[] values = ConfigKeys.class.getFields();
				for(Field f : values) {
					if(!Modifier.isStatic(f.getModifiers()))
						continue;

					Object val = f.get(null);
					if(val instanceof ConfigKey<?>)
						keys.put(f.getName(), (ConfigKey<?>) val);
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

			KEYS = ImmutableMap.copyOf(keys);
		}

		return KEYS;
	}
}
