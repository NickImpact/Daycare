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

package com.nickimpact.daycare.common.storage;

import com.nickimpact.daycare.api.IDaycarePlugin;
import com.nickimpact.daycare.api.configuration.ConfigKeys;
import com.nickimpact.daycare.common.storage.implementation.StorageImplementation;
import com.nickimpact.daycare.common.storage.implementation.file.ConfigurateStorage;
import com.nickimpact.daycare.common.storage.implementation.file.loaders.HoconLoader;
import com.nickimpact.daycare.common.storage.implementation.file.loaders.JsonLoader;
import com.nickimpact.daycare.common.storage.implementation.file.loaders.YamlLoader;
import com.nickimpact.daycare.common.storage.implementation.sql.SqlImplementation;
import com.nickimpact.impactor.api.storage.StorageType;
import com.nickimpact.impactor.api.storage.sql.file.H2ConnectionFactory;
import com.nickimpact.impactor.api.storage.sql.hikari.MariaDBConnectionFactory;
import com.nickimpact.impactor.api.storage.sql.hikari.MySQLConnectionFactory;

import java.io.File;

public class StorageFactory {

    private final IDaycarePlugin plugin;

    public StorageFactory(IDaycarePlugin plugin) {
        this.plugin = plugin;
    }

    public DaycareStorage getInstance(StorageType defaultMethod) {
        DaycareStorage storage;
        String method = plugin.getConfiguration().get(ConfigKeys.STORAGE_METHOD);
        StorageType type = StorageType.parse(method);
        if(type == null) {
            type = defaultMethod;
        }

        this.plugin.getPluginLogger().info("Loading storage provider... [" + type.getName() + "]");
        storage = makeInstance(type);
        storage.init();
        return storage;
    }

    private DaycareStorage makeInstance(StorageType type) {
        return new DaycareStorage(this.plugin, createNewImplementation(type));
    }

    private StorageImplementation createNewImplementation(StorageType type) {
        switch(type) {
            case MARIADB:
                return new SqlImplementation(
                        this.plugin,
                        new MariaDBConnectionFactory(this.plugin.getConfiguration().get(ConfigKeys.DATABASE_VALUES)),
                        this.plugin.getConfiguration().get(ConfigKeys.SQL_TABLE_PREFIX)
                );
            case MYSQL:
                return new SqlImplementation(
                        this.plugin,
                        new MySQLConnectionFactory(this.plugin.getConfiguration().get(ConfigKeys.DATABASE_VALUES)),
                        this.plugin.getConfiguration().get(ConfigKeys.SQL_TABLE_PREFIX)
                );
            case H2:
                return new SqlImplementation(
                        this.plugin,
                        new H2ConnectionFactory(this.plugin, new File("daycare").toPath().resolve("daycare-h2")),
                        this.plugin.getConfiguration().get(ConfigKeys.SQL_TABLE_PREFIX)
                );
            case YAML:
            default:
                return new ConfigurateStorage(
                        this.plugin,
                        "YAML",
                        new YamlLoader(),
                        ".yml",
                        "yaml-storage"
                );
            case JSON:
                return new ConfigurateStorage(
                        this.plugin,
                        "JSON",
                        new JsonLoader(),
                        ".json",
                        "json-storage"
                );
            case HOCON:
                return new ConfigurateStorage(
                        this.plugin,
                        "HOCON",
                        new HoconLoader(),
                        ".hocon",
                        "hocon-storage"
                );
        }
    }
}
