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

package com.nickimpact.daycare.common.storage.implementation.file;

import com.google.common.base.Throwables;
import com.nickimpact.daycare.api.IDaycarePlugin;
import com.nickimpact.daycare.api.pens.DaycareNPC;
import com.nickimpact.daycare.api.pens.Ranch;
import com.nickimpact.daycare.common.storage.implementation.StorageImplementation;
import com.nickimpact.daycare.common.storage.implementation.file.loaders.ConfigurateLoader;
import ninja.leaping.configurate.ConfigurationNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ConfigurateStorage implements StorageImplementation {

    private final IDaycarePlugin plugin;
    private final String implementationName;

    // The loader responsible for I/O
    private final ConfigurateLoader loader;

    private String extension;

    private Path dataDir;
    private String dataDirName;

    private Path userDir;

    private FileWatcher.WatchedLocation userWatcher = null;

    public ConfigurateStorage(IDaycarePlugin plugin, String implementationName, ConfigurateLoader loader, String extension, String dataDirName) {
        this.plugin = plugin;
        this.implementationName = implementationName;
        this.loader = loader;
        this.extension = extension;
        this.dataDirName = dataDirName;
    }

    @Override
    public IDaycarePlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public String getName() {
        return this.implementationName;
    }

    @Override
    public void init() throws Exception {
        this.dataDir = this.plugin.getConfigDir().resolve(this.dataDirName);
        this.createDirectoriesIfNotExists(this.dataDir);

        this.userDir = this.dataDir.resolve("data");
        this.createDirectoriesIfNotExists(this.userDir);

        FileWatcher watcher = new FileWatcher(this.plugin, dataDir);
        this.userWatcher = watcher.getWatcher(this.userDir);
        this.userWatcher.addListener(path -> {
//            String s = path.getFileName().toString();
//
//            if (!s.endsWith(this.extension)) {
//                return;
//            }
//
//            String user = s.substring(0, s.length() - this.extension.length());
//            UUID uuid;
//            try {
//                uuid = UUID.fromString(user);
//            } catch (Exception e) {
//                return;
//            }
        });
    }

    @Override
    public void shutdown() throws Exception {}

    @Override
    public boolean addRanch(Ranch ranch) {
        return false;
    }

    @Override
    public boolean updateRanch(Ranch ranch) {
        return false;
    }

    @Override
    public boolean deleteRanch(Ranch ranch) {
        return false;
    }

    @Override
    public Optional<Ranch> getRanch(UUID player) {
        return null;
    }

    @Override
    public boolean addNPC(DaycareNPC npc) {
        return false;
    }

    @Override
    public boolean deleteNPC(DaycareNPC npc) {
        return false;
    }

    @Override
    public List<DaycareNPC> getNPCs() {
        return null;
    }


    private ConfigurationNode readFile(String name) throws IOException {
        Path file = this.userDir.resolve(name + this.extension);
        if(this.userWatcher != null) {
            this.userWatcher.recordChange(file.getFileName().toString());
        }

        if(!Files.exists(file)) {
            return null;
        }

        return this.loader.loader(file).load();
    }

    private void saveFile(String name, ConfigurationNode node) throws IOException {
        Path file = this.userDir.resolve(name + this.extension);
        if(this.userWatcher != null) {
            this.userWatcher.recordChange(file.getFileName().toString());
        }

        if(node == null) {
            Files.deleteIfExists(file);
            return;
        }

        this.loader.loader(file).save(node);
    }

    private void createDirectoriesIfNotExists(Path path) throws IOException {
        if (Files.exists(path) && (Files.isDirectory(path) || Files.isSymbolicLink(path))) {
            return;
        }

        Files.createDirectories(path);
    }

    // used to report i/o exceptions which took place in a specific file
    private RuntimeException reportException(String file, Exception ex) throws RuntimeException {
        this.plugin.getPluginLogger().warn("Exception thrown whilst performing i/o: " + file);
        ex.printStackTrace();
        throw Throwables.propagate(ex);
    }
}
