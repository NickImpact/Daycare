package com.nickimpact.daycare.listeners;

import com.google.common.collect.Lists;
import com.nickimpact.daycare.DaycareInfo;
import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.commands.admin.AddNPCCmd;
import com.nickimpact.daycare.configuration.MsgConfigKeys;
import com.nickimpact.daycare.ranch.DaycareNPC;
import com.nickimpact.daycare.ranch.Ranch;
import com.nickimpact.daycare.ui.RanchUI;
import com.nickimpact.daycare.utils.MessageUtils;
import com.pixelmonmod.pixelmon.api.dialogue.Choice;
import com.pixelmonmod.pixelmon.api.dialogue.Dialogue;
import com.pixelmonmod.pixelmon.comm.packetHandlers.dialogue.DialogueNextAction;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class NPCListener {

	@Listener
	public void onNPCClick(InteractEntityEvent.Secondary.MainHand e, @First Player player) {
		EntityType et = Sponge.getRegistry().getType(EntityType.class, "pixelmon:chattingnpc").get();

		if(e.getTargetEntity().getType().equals(et)) {
			if(AddNPCCmd.getAdding().containsKey(player.getUniqueId())) {
				e.setCancelled(true);
				DaycareNPC dNPC = new DaycareNPC(e.getTargetEntity().getUniqueId(), AddNPCCmd.getAdding().get(player.getUniqueId()));
				DaycarePlugin.getInstance().addNPC(dNPC);
				player.sendMessages(MessageUtils.fetchMsgs(player, MsgConfigKeys.NPC_REGISTERED));
				AddNPCCmd.getAdding().remove(player.getUniqueId());
			} else {
				Optional<DaycareNPC> dNpc = DaycarePlugin.getInstance().getNpcs().stream().filter(npc -> npc.getUuid().equals(e.getTargetEntity().getUniqueId())).findAny();
				dNpc.ifPresent(npc -> {
					e.setCancelled(true);
					Dialogue.setPlayerDialogueData((EntityPlayerMP) player, forgeDialogue(player, npc.getName()), true);
				});
			}
		}
	}

	private ArrayList<Dialogue> forgeDialogue(Player player, String name) {
		ArrayList<Dialogue> prompt = Lists.newArrayList();
		for(Text text : MessageUtils.fetchMsgs(player, MsgConfigKeys.NPC_INTERACT_DIALOGUE)) {
			prompt.add(Dialogue.builder()
					.setName(name)
					.setText(text.toPlain())
					.build()
			);
		}

		Ranch ranch = DaycarePlugin.getInstance().getRanches().stream().filter(r -> r.getOwnerUUID().equals(player.getUniqueId())).findAny().get();
		if(ranch.getPens().stream().anyMatch(p -> p.getEgg().isPresent())) {
			for(Text text : MessageUtils.fetchMsgs(player, MsgConfigKeys.NPC_INTERACT_DIALOGUE_EGGS)) {
				prompt.add(Dialogue.builder()
						.setName(name)
						.setText(text.toPlain())
						.build()
				);
			}
		}

		prompt.add(
				Dialogue.builder()
						.setName(name)
						.setText(MessageUtils.fetchMsg(player, MsgConfigKeys.NPC_INTERACT_DIALOGUE_ACTION).toPlain())
						.addChoice(
								Choice.builder()
										.setText(MessageUtils.fetchMsg(player, MsgConfigKeys.NPC_INTERACT_DIALOGUE_ACTION_YES).toPlain())
										.setHandle(e -> {
											try {
												e.setAction(DialogueNextAction.DialogueGuiAction.CLOSE);
												new RanchUI(player).open();
											} catch (Exception e1) {}
										})
										.build()
						)
						.addChoice(
								Choice.builder()
										.setText(MessageUtils.fetchMsg(player, MsgConfigKeys.NPC_INTERACT_DIALOGUE_ACTION_NO).toPlain())
										.setHandle(e -> {
											List<Dialogue> responses = Lists.newArrayList();
											for(Text text : MessageUtils.fetchMsgs(player, MsgConfigKeys.NPC_INTERACT_DIALOGUE_NO)) {
												responses.add(Dialogue.builder().setName(name).setText(text.toPlain()).build());
											}

											e.reply(responses.toArray(new Dialogue[0]));
										})
										.build()
						)
						.build()
		);
		return prompt;
	}
}
