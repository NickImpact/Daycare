package com.nickimpact.daycare.spigot.listeners;

import com.google.common.collect.Lists;
import com.nickimpact.daycare.spigot.SpigotDaycarePlugin;
import com.nickimpact.daycare.api.pens.DaycareNPC;
import com.nickimpact.daycare.spigot.implementation.SpigotRanch;
import com.nickimpact.daycare.spigot.ui.RanchUI;
import com.nickimpact.daycare.spigot.utils.MessageUtils;
import com.pixelmonmod.pixelmon.api.dialogue.Choice;
import com.pixelmonmod.pixelmon.api.dialogue.Dialogue;
import com.pixelmonmod.pixelmon.comm.packetHandlers.dialogue.DialogueNextAction;
import com.pixelmonmod.pixelmon.entities.npcs.NPCChatting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.ArrayList;
import java.util.List;

public class NPCInteractionListener implements Listener {

	@EventHandler
	public void addDaycareNPC(PlayerInteractEntityEvent event) {
		Player player = event.getPlayer();
		Entity entity = (Entity) (Object) ((CraftEntity) event.getRightClicked()).getHandle();
		if(entity instanceof NPCChatting) {
			if (SpigotDaycarePlugin.getInstance().getService().getNPCManager().getNameIfAdding(player.getUniqueId()).isPresent()) {
				if(!SpigotDaycarePlugin.getInstance().getService().getNPCManager().isDaycareNPC(entity.getUniqueID()).isPresent()) {
					event.setCancelled(true);
					String name = SpigotDaycarePlugin.getInstance().getService().getNPCManager().getNameIfAdding(player.getUniqueId()).get();
					DaycareNPC npc = new DaycareNPC(entity.getUniqueID(), name);
					SpigotDaycarePlugin.getInstance().getService().getNPCManager().addNPC(npc);
					SpigotDaycarePlugin.getInstance().getService().getStorage().addNPC(npc);
					player.sendMessage(MessageUtils.parse("NPC registered!", true, false));
					SpigotDaycarePlugin.getInstance().getService().getNPCManager().removeAdder(player.getUniqueId());
				}
			} else if (SpigotDaycarePlugin.getInstance().getService().getNPCManager().isRemoving(player.getUniqueId())) {
				event.setCancelled(true);

			} else {
				SpigotDaycarePlugin.getInstance().getService().getNPCManager().isDaycareNPC(entity.getUniqueID()).ifPresent(npc -> {
					if (player.getItemOnCursor() != null && player.getItemOnCursor().getType().equals(Material.matchMaterial("PIXELMON_NPC_EDITOR"))) {
						return;
					}
					event.setCancelled(true);
					Dialogue.setPlayerDialogueData((EntityPlayerMP) (Object) ((CraftPlayer) player).getHandle(), forgeDialogue(player, npc), true);
				});
			}
		}
	}

	private ArrayList<Dialogue> forgeDialogue(Player player, DaycareNPC npc) {
		ArrayList<Dialogue> prompt = Lists.newArrayList();
		for (String text : Lists.newArrayList(
				ChatColor.WHITE + "Welcome to the Pokemon Daycare " + ChatColor.YELLOW + player.getName(),
				ChatColor.WHITE + "Here you may leave your pokemon to be leveled up, or even breed some fresh new babies!"
		)) {
			prompt.add(Dialogue.builder()
					.setName(npc.getName())
					.setText(text)
					.build()
			);
		}

		SpigotRanch ranch = (SpigotRanch) SpigotDaycarePlugin.getInstance().getService().getRanchManager().getLoadedRanches().stream().filter(r -> r.getOwnerUUID().equals(player.getUniqueId())).findAny().get();
		if (ranch.getPens().stream().anyMatch(p -> p.getEgg().isPresent())) {
			prompt.add(Dialogue.builder()
					.setName(npc.getName())
					.setText("Hmm... It seems that you have an egg waiting for you!")
					.build()
			);
		}

		prompt.add(
				Dialogue.builder()
						.setName(npc.getName())
						.setText("How would you like to proceed?")
						.addChoice(
								Choice.builder()
										.setText(ChatColor.GREEN + "Open the Daycare")
										.setHandle(e -> {
											try {
												e.setAction(DialogueNextAction.DialogueGuiAction.CLOSE);
												new RanchUI(player).open();
											} catch (Exception e1) {
												e1.printStackTrace();
											}
										})
										.build()
						)
						.addChoice(
								Choice.builder()
										.setText(ChatColor.RED + "I'll come back later")
										.setHandle(e -> {
											List<Dialogue> responses = Lists.newArrayList();
											responses.add(Dialogue.builder().setName(npc.getName()).setText("Alrighty, catch you later!").build());
											e.reply(responses.toArray(new Dialogue[0]));
										})
										.build()
						)
						.build()
		);
		return prompt;
	}

}
