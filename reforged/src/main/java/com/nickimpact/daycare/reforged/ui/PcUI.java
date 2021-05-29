package com.nickimpact.daycare.reforged.ui;

import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import com.nickimpact.daycare.api.events.DaycareEvent;
import com.nickimpact.daycare.sponge.SpongeDaycarePlugin;
import com.nickimpact.daycare.sponge.configuration.MsgConfigKeys;
import com.nickimpact.daycare.sponge.implementation.SpongeRanch;
import com.nickimpact.daycare.reforged.implementation.ReforgedPen;
import com.nickimpact.daycare.sponge.ui.common.CommonUIComponents;
import com.nickimpact.daycare.sponge.utils.SpongeItemTypeUtil;
import com.nickimpact.daycare.sponge.utils.TextParser;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PCBox;
import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import lombok.AllArgsConstructor;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.sponge.ui.SpongeIcon;
import net.impactdev.impactor.sponge.ui.SpongeLayout;
import net.impactdev.impactor.sponge.ui.SpongePage;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;

import java.util.List;
import java.util.function.Supplier;

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
				.title(TextParser.parse(TextParser.read(MsgConfigKeys.PC_TITLE)))
				.contentZone(InventoryDimension.of(6, 5))
				.previousPage(SpongeItemTypeUtil.getOrDefaultItemTypeFromID("pixelmon:trade_holder_left"), 51)
				.currentPage(SpongeItemTypeUtil.getOrDefaultItemTypeFromID("pixelmon:trade_monitor"), 52)
				.nextPage(SpongeItemTypeUtil.getOrDefaultItemTypeFromID("pixelmon:trade_holder_right"), 53)
				.build();
		this.page.applier(wrapper -> {
			if(wrapper.pokemon != null) {
				ItemStack rep = CommonUIComponents.pokemonDisplay(wrapper.pokemon);
				List<Supplier<Object>> sources = Lists.newArrayList();
				sources.add(() -> wrapper.pokemon);

				rep.offer(Keys.DISPLAY_NAME, TextParser.parse(TextParser.read(MsgConfigKeys.POKEMON_TITLE_SELECT), sources));
				rep.offer(Keys.ITEM_LORE, TextParser.parse(TextParser.read(MsgConfigKeys.POKEMON_LORE_SELECT), sources));
				SpongeIcon icon = new SpongeIcon(rep);
				icon.addListener(clickable -> {
					if(!wrapper.pokemon.isEgg()) {
						this.page.close();
						this.pc.set(wrapper.box, wrapper.pos, null);

						Impactor.getInstance().getEventBus().post(
								DaycareEvent.AddPokemon.class,
								new TypeToken<Pokemon>(){},
								this.viewer.getUniqueId(),
								this.pen,
								wrapper.pokemon
						);
						this.pen.addAtSlot(wrapper.pokemon, this.slot);
						SpongeDaycarePlugin.getSpongeInstance().getService().getStorage().updateRanch(this.ranch);
						this.viewer.sendMessage(TextParser.parse(TextParser.read(MsgConfigKeys.ADD_POKEMON), sources));

						new ReforgedPenUI(this.viewer, this.ranch, this.pen).open();
					}
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
				.add(Keys.DISPLAY_NAME, TextParser.parse(TextParser.read(MsgConfigKeys.PC_PARTY)))
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
