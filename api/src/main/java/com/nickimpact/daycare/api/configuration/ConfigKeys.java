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

package com.nickimpact.daycare.api.configuration;

import com.google.common.collect.ImmutableMap;
import com.nickimpact.impactor.api.configuration.ConfigKey;
import com.nickimpact.impactor.api.configuration.ConfigKeyHolder;
import com.nickimpact.impactor.api.configuration.keys.BaseConfigKey;
import com.nickimpact.impactor.api.storage.StorageCredentials;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.nickimpact.impactor.api.configuration.ConfigKeyTypes.*;

public class ConfigKeys implements ConfigKeyHolder {

	public static final ConfigKey<String> LANG = stringKey("plugin-language", "en_US");

	public static final ConfigKey<Integer> NUM_PENS = intKey("pens.base-num-pens", 14);
	public static final ConfigKey<Boolean> FIRST_PEN_UNLOCKED = booleanKey("pens.first-pen-unlocked", true);
	public static final ConfigKey<Double> BASE_PEN_PRICE = doubleKey("pens.pricing.base-price", 5000.0);
	public static final ConfigKey<Double> INCREMENT_PEN_PRICE = doubleKey("pens.pricing.increment", 500.0);
	public static final ConfigKey<String> PEN_PRICE_EQUATION = stringKey("pens.pricing.price-equation", "b + i * p");

	public static final ConfigKey<String> STORAGE_METHOD = stringKey("storage.storage-method", "h2");
	public static final ConfigKey<String> SQL_TABLE_PREFIX = stringKey("storage.data.sql-table-prefix", "daycare_");
	public static final ConfigKey<StorageCredentials> DATABASE_VALUES = enduringKey(customKey(c -> {
		String address = c.getString("storage.data.address", "localhost");
		String database = c.getString("storage.data.database", "daycare");
		String user = c.getString("storage.data.username", "user");
		String password = c.getString("storage.data.password", "pass");
		int maxPoolSize = c.getInteger("storage.data.pool-settings.maximum-pool-size", c.getInteger("storage.data.pool-size", 10));
		int minIdle = c.getInteger("storage.data.pool-settings.minimum-idle", maxPoolSize);
		int maxLifetime = c.getInteger("storage.data.pool-settings.maximum-lifetime", 1800000);
		int connectionTimeout = c.getInteger("storage.data.pool-settings.connection-timeout", 5000);
		Map<String, String> props = ImmutableMap.copyOf(c.getStringMap("storage.data.pool-settings.properties", ImmutableMap.of()));

		return new StorageCredentials(address, database, user, password, maxPoolSize, minIdle, maxLifetime, connectionTimeout, props);
	}));
	public static final ConfigKey<Long> LVL_WAIT_TIME = longKey("leveling.wait-time", 60);
	public static final ConfigKey<Integer> LVL_TASK_TIME = intKey("leveling.task-run", 60);
	public static final ConfigKey<Double> PRICE_PER_LVL = doubleKey("leveling.price-per-level", 100.0);

	public static final ConfigKey<Long> MIN_EGG_WAIT = longKey("breeding.min-wait-seconds", 900);
	public static final ConfigKey<Long> MAX_EGG_WAIT = longKey("breeding.max-wait-seconds", 3600);
	public static final ConfigKey<Integer> EGG_CHANCE = intKey("breeding.chance-to-create-egg", 10);

	public static final ConfigKey<Boolean> LEVELING_ENABLED = booleanKey("leveling.enabled", true);
	public static final ConfigKey<Boolean> BREEDING_ENABLED = booleanKey("breeding.enabled", true);

	public static final ConfigKey<String> RANCH_UNLOCK_MODULE = stringKey("pens.unlock-module", "economic");

	public static final ConfigKey<Boolean> TEXTUREFLAG_CAPITALIZE = booleanKey("variables.texture.capitalize", true);
	public static final ConfigKey<Boolean> TEXTUREFLAG_TRIM_TRAILING_NUMS = booleanKey("variables.texture.trim-trailing-numbers", true);

	private static final Map<String, ConfigKey<?>> KEYS;
	private static final int SIZE;

	static {
		Map<String, ConfigKey<?>> keys = new LinkedHashMap<>();
		Field[] values = ConfigKeys.class.getFields();
		int i = 0;

		for (Field f : values) {
			// ignore non-static fields
			if (!Modifier.isStatic(f.getModifiers())) {
				continue;
			}

			// ignore fields that aren't configkeys
			if (!ConfigKey.class.equals(f.getType())) {
				continue;
			}

			try {
				// get the key instance
				BaseConfigKey<?> key = (BaseConfigKey<?>) f.get(null);
				// set the ordinal value of the key.
				key.ordinal = i++;
				// add the key to the return map
				keys.put(f.getName(), key);
			} catch (Exception e) {
				throw new RuntimeException("Exception processing field: " + f, e);
			}
		}

		KEYS = ImmutableMap.copyOf(keys);
		SIZE = i;
	}

	@Override
	public Map<String, ConfigKey<?>> getKeys() {
		return KEYS;
	}

	@Override
	public int getSize() {
		return SIZE;
	}
}
