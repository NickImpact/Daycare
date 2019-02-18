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

package com.nickimpact.daycare.configuration;

import com.google.common.collect.ImmutableMap;
import com.nickimpact.daycare.storage.StorageCredentials;
import com.nickimpact.impactor.api.configuration.ConfigKey;
import com.nickimpact.impactor.api.configuration.IConfigKeys;
import com.nickimpact.impactor.api.configuration.keys.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigKeys implements IConfigKeys {

	public static final ConfigKey<String> LANG = StringKey.of("plugin-language", "en_US");

	public static final ConfigKey<Integer> NUM_PENS = IntegerKey.of("pens.base-num-pens", 14);
	public static final ConfigKey<Boolean> FIRST_PEN_UNLOCKED = BooleanKey.of("pens.first-pen-unlocked", true);
	public static final ConfigKey<Double> BASE_PEN_PRICE = DoubleKey.of("pens.pricing.base-price", 5000.0);
	public static final ConfigKey<Double> INCREMENT_PEN_PRICE = DoubleKey.of("pens.pricing.increment", 500.0);
	public static final ConfigKey<String> PEN_PRICE_EQUATION = StringKey.of("pens.pricing.price-equation", "b + i * p");

	public static final ConfigKey<String> STORAGE_METHOD = StringKey.of("storage.storage-method", "h2");
	public static final ConfigKey<String> SQL_TABLE_PREFIX = StringKey.of("storage.data.sql-table-prefix", "daycare_");
	public static final ConfigKey<StorageCredentials> DATABASE_VALUES = EnduringKey.wrap(AbstractKey.of(c -> new StorageCredentials(
			c.getString("storage.data.connection-info.address", null),
			c.getString("storage.data.connection-info.database", null),
			c.getString("storage.data.connection-info.username", null),
			c.getString("storage.data.connection-info.password", null)
	)));
	public static final ConfigKey<Long> LVL_WAIT_TIME = LongKey.of("leveling.wait-time", 60);
	public static final ConfigKey<Integer> LVL_TASK_TIME = IntegerKey.of("leveling.task-run", 60);
	public static final ConfigKey<Double> PRICE_PER_LVL = DoubleKey.of("leveling.price-per-level", 100.0);

	public static final ConfigKey<Boolean> LEVELING_ENABLED = BooleanKey.of("leveling.enabled", true);
	public static final ConfigKey<Boolean> BREEDING_ENABLED = BooleanKey.of("breeding.enabled", true);

	// Breeding style options
	public static final ConfigKey<String> BREED_STYLE = StringKey.of("breeding.styles.method", "timed-chance");

	public static final ConfigKey<Long> BREED_TIMED_MIN_WAIT = LongKey.of("breeding.styles.timed-chance.min-wait", 15);
	public static final ConfigKey<Long> BREED_TIMED_MAX_WAIT = LongKey.of("breeding.styles.timed-chance.max-wait", 60);
	public static final ConfigKey<Double> BREED_TIMED_EGG_CHANCE = DoubleKey.of("breeding.styles.timed-chance.egg-chance", 25.0);
	public static final ConfigKey<Integer> BREED_TIMED_CHECK_PERIOD = IntegerKey.of("breeding.styles.timed-chance.check-period", 300);
	public static final ConfigKey<Long> BREED_TIMED_UPDATE_INTERVAL = LongKey.of("breeding.styles.timed-chance.update-interval", 1);
	public static final ConfigKey<Long> BREED_PNATIVE_DELAY = LongKey.of("breeding.styles.pixelmon-native.stage-delay", 5);
	public static final ConfigKey<Long> BREED_PNATIVE_INTERVAL = LongKey.of("breeding.styles.pixelmon-native.stage-interval", 5);
	public static final ConfigKey<Long> GENERAL_UPDATE_PERIOD = LongKey.of("general.update-period", 5);
	public static final ConfigKey<String> RANCH_UNLOCK_MODULE = StringKey.of("pens.unlock-module", "economic");

	private static Map<String, ConfigKey<?>> KEYS = null;

	@Override
	public synchronized Map<String, ConfigKey<?>> getAllKeys() {
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
