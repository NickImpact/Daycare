package com.nickimpact.daycare.storage.dao;

import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.ranch.Ranch;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractDao {

	@Getter
	protected final DaycarePlugin plugin;

	@Getter
	public final String name;

	public abstract void init();

	public abstract void shutdown();

	public abstract void addRanch(Ranch ranch);

	public abstract void updateRanch(Ranch ranch);

	public abstract void deleteRanch(UUID uuid) throws Exception;

	public abstract Ranch getRanch(UUID uuid) throws Exception;

	public abstract List<Ranch> getAllRanches() throws Exception;

	public abstract void purge(boolean logs) throws Exception;

	public abstract void save() throws Exception;
}
