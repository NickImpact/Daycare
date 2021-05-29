package com.nickimpact.daycare.reforged.ui;

import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import com.nickimpact.daycare.api.events.DaycareEvent;
import com.nickimpact.daycare.reforged.implementation.ReforgedDaycarePokemonWrapper;
import com.nickimpact.daycare.sponge.SpongeDaycarePlugin;
import com.nickimpact.daycare.sponge.configuration.MsgConfigKeys;
import com.nickimpact.daycare.sponge.implementation.SpongeRanch;
import com.nickimpact.daycare.reforged.implementation.ReforgedPen;
import com.nickimpact.daycare.sponge.ui.common.CommonUIComponents;
import com.nickimpact.daycare.sponge.utils.SpongeItemTypeUtil;
import com.nickimpact.daycare.sponge.utils.TextParser;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.sponge.ui.SpongeIcon;
import net.impactdev.impactor.sponge.ui.SpongeLayout;
import net.impactdev.impactor.sponge.ui.SpongeUI;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;

import java.util.List;
import java.util.function.Supplier;

public class PartyUI {

	private SpongeUI display;
	private Player viewer;

	private SpongeRanch ranch;
	private ReforgedPen pen;
	private int slot;

	private PlayerPartyStorage party;

	public PartyUI(Player viewer, SpongeRanch ranch, ReforgedPen pen, int slot) {
		this.viewer = viewer;
		this.ranch = ranch;
		this.pen = pen;
		this.slot = slot;

		this.party = Pixelmon.storageManager.getParty(viewer.getUniqueId());

		this.display = this.createDisplay();
	}

	public void open() {
		this.display.open(this.viewer);
	}

	private SpongeUI createDisplay() {
		return SpongeUI.builder()
				.title(TextParser.parse(TextParser.read(MsgConfigKeys.PARTY_TITLE)))
				.dimension(InventoryDimension.of(9, 3))
				.build().define(this.design());
	}

	private SpongeLayout design() {
		SpongeLayout.SpongeLayoutBuilder slb = SpongeLayout.builder();
		slb.rows(SpongeIcon.BORDER, 0, 2);
		slb.slots(SpongeIcon.BORDER, 9, 16);

		for(int i = 10; i < 16; i++) {
			Pokemon pokemon = this.party.get(i - 10);
			if(pokemon == null) continue;

			List<Supplier<Object>> sources = Lists.newArrayList();
			sources.add(pokemon::getLevel);
			sources.add(() -> pokemon);

			ItemStack display = CommonUIComponents.pokemonDisplay(pokemon);
			display.offer(Keys.DISPLAY_NAME, TextParser.parse(TextParser.read(MsgConfigKeys.POKEMON_TITLE_SELECT), sources));
			display.offer(Keys.ITEM_LORE, TextParser.parse(TextParser.read(MsgConfigKeys.POKEMON_LORE_SELECT), sources));
			SpongeIcon icon = new SpongeIcon(display);
			if(!pokemon.isEgg()) {
				icon.addListener(clickable -> {
					this.display.close(this.viewer);

					if(this.party.getTeam().size() != 1) {
						Impactor.getInstance().getEventBus().post(
								DaycareEvent.AddPokemon.class,
								new TypeToken<Pokemon>(){},
								this.viewer.getUniqueId(),
								this.pen,
								pokemon
						);
						this.pen.addAtSlot(pokemon, slot);
						this.party.set(this.party.getPosition(pokemon), null);
						SpongeDaycarePlugin.getSpongeInstance().getService().getStorage().updateRanch(this.ranch);
						this.viewer.sendMessage(TextParser.parse(TextParser.read(MsgConfigKeys.ADD_POKEMON), sources));
					} else {
						this.viewer.sendMessage(TextParser.parse(TextParser.read(MsgConfigKeys.LAST_NON_EGG), sources));
					}
				});
			}
			slb.slot(icon, i);
		}

		ItemStack pc = ItemStack.builder()
				.itemType(SpongeItemTypeUtil.getOrDefaultItemTypeFromID("pixelmon:pc"))
				.add(Keys.DISPLAY_NAME, TextParser.parse(TextParser.read(MsgConfigKeys.PARTY_PC)))
				.build();
		SpongeIcon icon = new SpongeIcon(pc);
		icon.addListener(clickable -> {
			new PcUI(this.viewer, this.ranch, this.pen, this.slot).open();
		});
		slb.slot(icon, 17);

		return slb.build();
	}
}
