package com.nickimpact.daycare.storage.dao.file;

import com.google.common.collect.Lists;
import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.PluginInfo;
import com.nickimpact.daycare.ranch.Ranch;
import com.nickimpact.daycare.storage.dao.AbstractDao;
import lombok.Getter;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public abstract class ConfigurateDao extends AbstractDao {

	@Getter
	private final String fileExtension;

	private final Text PREFIX = Text.of(
			TextColors.YELLOW, "GTS ", TextColors.GRAY, "(", TextColors.DARK_AQUA, "File Watcher",
			TextColors.GRAY, ") \u00bb "
	);

	private File listingsDir;
	private File logsDir;

	protected ConfigurateDao(DaycarePlugin plugin, String name, String fileExtension) {
		super(plugin, name);
		this.fileExtension = fileExtension;
	}

	protected abstract ConfigurationLoader<? extends ConfigurationNode> loader(Path path);

	private ConfigurationNode readFile(StorageLocation location, String name) throws IOException {
		File file = new File(getDirectory(location), name + fileExtension);
		registerFileAction(location, file);
		return readFile(file);
	}

	private ConfigurationNode readFile(File file) throws IOException {
		if(!file.exists())
			return null;

		return loader(file.toPath()).load();
	}

	private void saveFile(StorageLocation location, String name, ConfigurationNode node) throws IOException {
		File file = new File(getDirectory(location), name + fileExtension);
		registerFileAction(location, file);
		saveFile(file, node);
	}

	private void saveFile(File file, ConfigurationNode node) throws IOException {
		if (node == null) {
			if (file.exists()) {
				file.delete();
			}
			return;
		}

		loader(file.toPath()).save(node);
	}

	private File getDirectory(StorageLocation location) {
		switch (location) {
			case LISTINGS:
				return listingsDir;
			case LOGS:
				return logsDir;
			default:
				throw new RuntimeException();
		}
	}

	private FilenameFilter getFileTypeFilter() {
		return (dir, name) -> name.endsWith(fileExtension);
	}

	private Exception reportException(String file, Exception ex) throws Exception {
		plugin.getConsole().ifPresent(console -> console.sendMessage(
				Text.of(PluginInfo.ERROR_PREFIX, "Exception thrown whilst performing i/o: " + file)
		));
		ex.printStackTrace();
		throw ex;
	}

	private void registerFileAction(StorageLocation type, File file) {
		plugin.getFileWatcher().ifPresent(fileWatcher -> fileWatcher.registerChange(type, file.getName()));
	}

	private void setupFiles() throws IOException {
		File data = new File(DaycarePlugin.getInstance().getDataDirectory(), "data");
		data.mkdirs();

		listingsDir = new File(data, "ranches");
		logsDir = new File(data, "logs");

		plugin.getFileWatcher().ifPresent(watcher -> {
			watcher.subscribe("ranches", listingsDir.toPath(), s -> {
				if(!s.endsWith(fileExtension))
					return;

				plugin.getConsole().ifPresent(console -> console.sendMessages(
						Text.of(PREFIX, "Refreshing data...")
				));
				plugin.getStorage().getAllRanches();
			});
		});
	}

	@Override
	public void init() {
		try {
			setupFiles();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void shutdown() {

	}

	@Override
	public void addRanch(Ranch ranch) {}

	@Override
	public void updateRanch(Ranch ranch) {}

	@Override
	public void deleteRanch(UUID uuid) {}

	@Override
	public Ranch getRanch(UUID uuid) {
		return null;
	}

	@Override
	public List<Ranch> getAllRanches() throws Exception {
		return Lists.newArrayList();
	}

	@Override
	public void purge(boolean logs) throws Exception {}

	@Override
	public void save() throws Exception {}
}
