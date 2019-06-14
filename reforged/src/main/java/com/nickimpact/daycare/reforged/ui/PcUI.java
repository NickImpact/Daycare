package com.nickimpact.daycare.reforged.ui;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nickimpact.daycare.SpongeDaycarePlugin;
import com.nickimpact.daycare.configuration.MsgConfigKeys;
import com.nickimpact.daycare.implementation.SpongeRanch;
import com.nickimpact.daycare.reforged.implementation.ReforgedPen;
import com.nickimpact.daycare.ui.common.CommonUIComponents;
import com.nickimpact.daycare.utils.SpongeItemTypeUtil;
import com.nickimpact.impactor.sponge.ui.SpongeIcon;
import com.nickimpact.impactor.sponge.ui.SpongeLayout;
import com.nickimpact.impactor.sponge.ui.SpongePage;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PCBox;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import lombok.AllArgsConstructor;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class PcUI {

	private SpongePage<PcPosition> page;
	private Player viewer;

	private SpongeRanch ranch;
	private ReforgedPen pen;
	private int slot;

	private PCStorage pc;

	public PcUI(Player viewer, SpongeRanch ranch, ReforgedPen pen, int slot) {
		this.viewer = viewer;
		this.ranch = ranch;
		this.pen = pen;
		this.slot = slot;

		this.pc = Pixelmon.storageManager.getPCForPlayer(viewer.getUniqueId());
		this.page = SpongePage.builder()
				.viewer(viewer)
				.view(this.display())
				.title(SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsg(viewer, MsgConfigKeys.PC_TITLE, null, null))
				.contentZone(InventoryDimension.of(6, 5))
				.previousPage(SpongeItemTypeUtil.getOrDefaultItemTypeFromID("pixelmon:trade_holder_left"), 51)
				.currentPage(SpongeItemTypeUtil.getOrDefaultItemTypeFromID("pixelmon:trade_monitor"), 52)
				.nextPage(SpongeItemTypeUtil.getOrDefaultItemTypeFromID("pixelmon:trade_holder_right"), 53)
				.build();
		this.page.applier(wrapper -> {
			if(wrapper.pokemon != null) {
				ItemStack rep = CommonUIComponents.pokemonDisplay(wrapper.pokemon);

				Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
				tokens.put("calced_lvl", src -> Optional.of(Text.of(wrapper.pokemon.getLevel())));

				Map<String, Object> variables = Maps.newHashMap();
				variables.put("poke", wrapper.pokemon);

				rep.offer(Keys.DISPLAY_NAME, SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsg(this.viewer, MsgConfigKeys.POKEMON_TITLE_PEN, tokens, variables));
				rep.offer(Keys.ITEM_LORE, SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsgs(this.viewer, MsgConfigKeys.POKEMON_LORE_SELECT, tokens, variables));
				SpongeIcon icon = new SpongeIcon(rep);
				icon.addListener(clickable -> {
					this.page.close();
					this.pc.set(wrapper.box, wrapper.pos, null);
					this.pen.addAtSlot(wrapper.pokemon, this.slot);
					SpongeDaycarePlugin.getSpongeInstance().getService().getStorage().updateRanch(this.ranch);
					this.viewer.sendMessage(SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsg(this.viewer, MsgConfigKeys.ADD_POKEMON, tokens, variables));

					new ReforgedPenUI(this.viewer, this.ranch, this.pen).open();
				});

				return icon;
			} else {
				return new SpongeIcon(ItemStack.builder().itemType(ItemTypes.AIR).build());
			}
		});
		this.page.define(this.getBoxContents());
	}

	public void open() {
		this.page.open();
	}

	private SpongeLayout display() {
		SpongeLayout.SpongeLayoutBuilder builder = SpongeLayout.builder();
		builder.slots(SpongeIcon.BORDER, 6, 7, 8, 15, 17, 24, 26, 33, 35, 42, 43, 44, 45, 46, 47, 48, 49, 50);

		ItemStack party = ItemStack.builder()
				.itemType(SpongeItemTypeUtil.getOrDefaultItemTypeFromID("pixelmon:gs_ball"))
				.add(Keys.DISPLAY_NAME, SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsg(this.viewer, MsgConfigKeys.PC_PARTY, null, null))
				.build();
		SpongeIcon icon = new SpongeIcon(party);
		icon.addListener(clickable -> {
			new PartyUI(this.viewer, this.ranch, this.pen, this.slot).open();
		});
		builder.slot(icon, 25);

		return builder.build();
	}

	private List<PcPosition> getBoxContents() {
		List<PcPosition> pokemon = Lists.newArrayList();
		int b = 0;
		for(PCBox box : this.pc.getBoxes()) {
			for(int i = 0; i < 30; i++) {
				pokemon.add(new PcPosition(box.get(i), b, i));
			}
			b++;
		}

		return pokemon;
	}

	@AllArgsConstructor
	private static class PcPosition {
		private Pokemon pokemon;
		private int box;
		private int pos;
	}
}
