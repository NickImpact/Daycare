package com.nickimpact.daycare.api.manager;

import com.google.common.collect.Lists;
import com.nickimpact.daycare.api.pens.DaycareNPC;
import net.minecraft.util.Tuple;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class NPCManager {

	private List<DaycareNPC> npcs = Lists.newArrayList();

	private List<Tuple<UUID, String>> adding = Lists.newArrayList();
	private List<UUID> removing = Lists.newArrayList();

	public List<DaycareNPC> getNPCs() {
		return this.npcs;
	}

	public void addNPC(DaycareNPC npc) {
		this.npcs.add(npc);
	}

	public Optional<DaycareNPC> isDaycareNPC(UUID uuid) {
		return npcs.stream().filter(npc -> npc.getUuid().equals(uuid)).findAny();
	}

	public void addNPCAdder(UUID uuid, String name) {
		adding.add(new Tuple<>(uuid, name));
	}

	public Optional<String> getNameIfAdding(UUID uuid) {
		return adding.stream().filter(adder -> adder.getFirst().equals(uuid)).map(Tuple::getSecond).findAny();
	}

	public void removeAdder(UUID uuid) {
		adding.removeIf(adder -> adder.getFirst().equals(uuid));
	}

	public void addRemover(UUID uuid) {
		this.removing.add(uuid);
	}

	public boolean isRemoving(UUID uuid) {
		return this.removing.contains(uuid);
	}

	public void removeRemover(UUID uuid) {
		this.removing.remove(uuid);
	}
}
