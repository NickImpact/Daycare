package com.nickimpact.daycare.generations.listeners;

import com.google.common.collect.Lists;
import com.nickimpact.daycare.api.pens.DaycareNPC;
import com.nickimpact.daycare.sponge.SpongeDaycarePlugin;
import com.nickimpact.daycare.sponge.configuration.MsgConfigKeys;
import com.nickimpact.daycare.sponge.implementation.SpongeRanch;
import com.nickimpact.daycare.sponge.ui.RanchUI;
import com.nickimpact.daycare.sponge.utils.SpongeItemTypeUtil;
import com.pixelmongenerations.api.dialogue.Choice;
import com.pixelmongenerations.api.dialogue.Dialogue;
import com.pixelmongenerations.core.network.packetHandlers.dialogue.DialogueNextAction;
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


public class GenerationsNPCInteractListener {

    @Listener
    public void addDaycareNPC(InteractEntityEvent.Secondary event, @First Player player) {
        EntityType et = Sponge.getRegistry().getType(EntityType.class, "pixelmon:chattingnpc").get();

        if(event.getTargetEntity().getType().equals(et)) {
            if(SpongeDaycarePlugin.getSpongeInstance().getService().getNPCManager().getNameIfAdding(player.getUniqueId()).isPresent()) {
                event.setCancelled(true);
                String name = SpongeDaycarePlugin.getSpongeInstance().getService().getNPCManager().getNameIfAdding(player.getUniqueId()).get();
                DaycareNPC npc = new DaycareNPC(event.getTargetEntity().getUniqueId(), name);
                SpongeDaycarePlugin.getSpongeInstance().getService().getNPCManager().addNPC(npc);
                SpongeDaycarePlugin.getSpongeInstance().getService().getStorage().addNPC(npc);
                player.sendMessages(SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsgs(player, MsgConfigKeys.NPC_REGISTERED, null, null));
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
    }

    private ArrayList<Dialogue> forgeDialogue(Player player, DaycareNPC npc) {
        ArrayList<Dialogue> prompt = Lists.newArrayList();
        TextParsingUtils parser = SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils();
        for (Text text : parser.fetchAndParseMsgs(player, MsgConfigKeys.NPC_INTERACT_DIALOGUE, null, null)) {
            prompt.add(Dialogue.builder()
                    .setName(npc.getName())
                    .setText(text.toPlain())
                    .build()
            );
        }

        SpongeRanch ranch = (SpongeRanch) SpongeDaycarePlugin.getSpongeInstance().getService().getRanchManager().getLoadedRanches().stream().filter(r -> r.getOwnerUUID().equals(player.getUniqueId())).findAny().get();
        if (ranch.getPens().stream().anyMatch(p -> p.getEgg().isPresent())) {
            for (Text text : parser.fetchAndParseMsgs(player, MsgConfigKeys.NPC_INTERACT_DIALOGUE_EGGS, null, null)) {
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
                        .setText(parser.fetchAndParseMsg(player, MsgConfigKeys.NPC_INTERACT_DIALOGUE_ACTION, null, null).toPlain())
                        .addChoice(
                                Choice.builder()
                                        .setText(parser.fetchAndParseMsg(player, MsgConfigKeys.NPC_INTERACT_DIALOGUE_ACTION_YES, null, null).toPlain())
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
                                        .setText(parser.fetchAndParseMsg(player, MsgConfigKeys.NPC_INTERACT_DIALOGUE_ACTION_NO, null, null).toPlain())
                                        .setHandle(e -> {
                                            List<Dialogue> responses = Lists.newArrayList();
                                            for(Text text : parser.fetchAndParseMsgs(player, MsgConfigKeys.NPC_INTERACT_DIALOGUE_NO, null, null)) {
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