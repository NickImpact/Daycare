package com.nickimpact.daycare.common.storage.implementation.file;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import com.nickimpact.daycare.api.IDaycarePlugin;
import com.nickimpact.daycare.api.pens.DaycareNPC;
import com.nickimpact.daycare.api.pens.Pen;
import com.nickimpact.daycare.api.pens.Ranch;
import com.nickimpact.daycare.api.util.PluginInstance;
import com.nickimpact.daycare.common.storage.implementation.StorageImplementation;
import org.checkerframework.checker.units.qual.A;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class FlatfileStorage implements StorageImplementation {

    private final IDaycarePlugin plugin;
    private final String implementationName;

    private String extension;

    private Path dataDir;
    private String dataDirName;

    private Path userDir;
    private Path npcDir;

    private FileWatcher.WatchedLocation userWatcher = null;

    public FlatfileStorage(IDaycarePlugin plugin, String implementationName) {
        this.plugin = plugin;
        this.implementationName = implementationName;
        this.extension = ".json";
        this.dataDirName = "file-data";
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

        this.userDir = this.dataDir.resolve("players");
        this.createDirectoriesIfNotExists(this.userDir);

        this.npcDir = this.dataDir.resolve("npcs");
        this.createDirectoriesIfNotExists(this.npcDir);

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
    public boolean addRanch(Ranch ranch) throws Exception {
        Path file = this.userDir.resolve(ranch.getOwnerUUID().toString().substring(0, 2)).resolve(ranch.getOwnerUUID().toString());
        if(this.userWatcher != null) {
            this.userWatcher.recordChange(file.getFileName().toString());
        }

        Files.deleteIfExists(file);
        if(!Files.exists(file)) {
            Files.createDirectories(file);
        }

        File pens = new File(file.toFile(), "pens");
        pens.mkdirs();
        AtomicInteger penID = new AtomicInteger(1);
        ranch.getPens().forEach(pen -> {
            if(((Pen) pen).isUnlocked()) {
                File target = new File(pens, String.format("Pen-%d.json", penID.get()));
                try {
                    if(!target.exists()) {
                        target.createNewFile();
                    }

                    FileWriter fw = new FileWriter(target);
                    fw.write(PluginInstance.getPlugin().getGson().toJson(ranch.getPen(penID.get() - 1), Pen.class));
                    fw.flush();
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            penID.incrementAndGet();
        });

        return true;
    }

    @Override
    public boolean updateRanch(Ranch ranch) throws Exception {
        return this.addRanch(ranch);
    }

    @Override
    public boolean deleteRanch(Ranch ranch) throws Exception  {
        Path file = this.userDir.resolve(ranch.getOwnerUUID().toString().substring(0, 2)).resolve(ranch.getOwnerUUID().toString() + this.extension);
        if(this.userWatcher != null) {
            this.userWatcher.recordChange(file.getFileName().toString());
        }

        Files.deleteIfExists(file);
        return true;
    }

    @Override
    public Optional<Ranch> getRanch(UUID player) throws Exception  {
        Path file = this.userDir.resolve(player.toString().substring(0, 2)).resolve(player.toString() + this.extension);
        if(this.userWatcher != null) {
            this.userWatcher.recordChange(file.getFileName().toString());
        }

        if(!Files.exists(file)) {
            return Optional.empty();
        }

        return Optional.of(PluginInstance.getPlugin().getGson().fromJson(new FileReader(file.toFile()), Ranch.class));
    }

    @Override
    public boolean addNPC(DaycareNPC npc) throws Exception  {
        return false;
    }

    @Override
    public boolean deleteNPC(DaycareNPC npc) throws Exception  {
        return false;
    }

    @Override
    public List<DaycareNPC> getNPCs() throws Exception  {
        return Lists.newArrayList();
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
