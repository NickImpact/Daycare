package com.nickimpact.daycare.sponge.listeners;

import com.google.common.collect.Lists;
import com.nickimpact.daycare.sponge.SpongeDaycarePlugin;
import com.nickimpact.daycare.api.pens.DaycareNPC;
import com.nickimpact.daycare.sponge.configuration.MsgConfigKeys;
import com.nickimpact.daycare.sponge.implementation.SpongeRanch;
import com.nickimpact.daycare.sponge.ui.RanchUI;
import com.nickimpact.daycare.sponge.utils.SpongeItemTypeUtil;
import com.nickimpact.daycare.sponge.utils.TextParser;
import com.pixelmonmod.pixelmon.api.dialogue.Choice;
import com.pixelmonmod.pixelmon.api.dialogue.Dialogue;
import com.pixelmonmod.pixelmon.comm.packetHandlers.dialogue.DialogueNextAction;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class NPCInteractionListener {

	@Listener
	public void addDaycareNPC(InteractEntityEvent.Secondary event, @First Player player) {
		Sponge.getRegistry().getType(EntityType.class, "pixelmon:npc_chatting").ifPresent(et -> {
			if(event.getTargetEntity().getType().equals(et)) {
				if(SpongeDaycarePlugin.getSpongeInstance().getService().getNPCManager().getNameIfAdding(player.getUniqueId()).isPresent()) {
					event.setCancelled(true);
					String name = SpongeDaycarePlugin.getSpongeInstance().getService().getNPCManager().getNameIfAdding(player.getUniqueId()).get();
					DaycareNPC npc = new DaycareNPC(event.getTargetEntity().getUniqueId(), name);
					SpongeDaycarePlugin.getSpongeInstance().getService().getNPCManager().addNPC(npc);
					SpongeDaycarePlugin.getSpongeInstance().getService().getStorage().addNPC(npc);
					player.sendMessages(TextParser.parse(TextParser.read(MsgConfigKeys.NPC_REGISTERED)));
					SpongeDaycarePlugin.getSpongeInstance().getService().getNPCManager().removeAdder(player.getUniqueId());
				} else if(SpongeDaycarePlugin.getSpongeInstance().getService().getNPCManager().isRemoving(player.getUniqueId())) {
					event.setCancelled(true);
				} else {
					SpongeDaycarePlugin.getSpongeInstance().getService().getNPCManager().isDaycareNPC(event.getTargetEntity().getUniqueId()).ifPresent(npc -> {
						if(player.getItemInHand(HandTypes.MAIN_HAND).isPresent() && player.getItemInHand(HandTypes.MAIN_HAND).get().getType().equals(SpongeItemTypeUtil.getOrDefaultItemTypeFromID("pixelmon:npc_editor"))) {
							return;
						}
						event.setCancelled(true);
						Dialogue.setPlayerDialogueData((EntityPlayerMP) player, forgeDialogue(player, npc), true);
					});
				}
			}
		});
	}

	private ArrayList<Dialogue> forgeDialogue(Player player, DaycareNPC npc) {
		ArrayList<Dialogue> prompt = Lists.newArrayList();
		List<Supplier<Object>> sources = Lists.newArrayList();
		sources.add(() -> player);

		for (Text text : TextParser.parse(TextParser.read(MsgConfigKeys.NPC_INTERACT_DIALOGUE), sources)) {
			prompt.add(Dialogue.builder()
					.setName(npc.getName())
					.setText(text.toPlain())
					.build()
			);
		}

		SpongeRanch ranch = (SpongeRanch) SpongeDaycarePlugin.getSpongeInstance().getService().getRanchManager().getLoadedRanches().stream().filter(r -> r.getOwnerUUID().equals(player.getUniqueId())).findAny().get();
		if (ranch.getPens().stream().anyMatch(p -> p.getEgg().isPresent())) {
			for (Text text : TextParser.parse(TextParser.read(MsgConfigKeys.NPC_INTERACT_DIALOGUE_EGGS))) {
				prompt.add(Dialogue.builder()
						.setName(npc.getName())
						.setText(text.toPlain())
						.build()
				);
			}
		}

		prompt.add(
				Dialogue.builder()
						.setName(npc.getName())
						.setText(TextParser.parse(TextParser.read(MsgConfigKeys.NPC_INTERACT_DIALOGUE_ACTION)).toPlain())
						.addChoice(
								Choice.builder()
										.setText(TextParser.parse(TextParser.read(MsgConfigKeys.NPC_INTERACT_DIALOGUE_ACTION_YES)).toPlain())
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
										.setText(TextParser.parse(TextParser.read(MsgConfigKeys.NPC_INTERACT_DIALOGUE_ACTION_NO)).toPlain())
										.setHandle(e -> {
											List<Dialogue> responses = Lists.newArrayList();
											for(Text text : TextParser.parse(TextParser.read(MsgConfigKeys.NPC_INTERACT_DIALOGUE_NO))) {
												responses.add(Dialogue.builder().setName(npc.getName()).setText(text.toPlain()).build());
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
