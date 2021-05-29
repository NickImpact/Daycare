package com.nickimpact.daycare.generations.ui;

import com.google.common.collect.Maps;
import com.nickimpact.daycare.sponge.SpongeDaycarePlugin;
import com.nickimpact.daycare.sponge.configuration.MsgConfigKeys;
import com.nickimpact.daycare.generations.implementation.GenerationsPen;
import com.nickimpact.daycare.sponge.implementation.SpongeRanch;
import com.nickimpact.daycare.sponge.utils.SpongeItemTypeUtil;
import com.nickimpact.impactor.sponge.ui.SpongeIcon;
import com.nickimpact.impactor.sponge.ui.SpongeLayout;
import com.nickimpact.impactor.sponge.ui.SpongeUI;
import com.pixelmongenerations.common.entity.pixelmon.EntityPixelmon;
import com.pixelmongenerations.core.config.PixelmonEntityList;
import com.pixelmongenerations.core.storage.PixelmonStorage;
import com.pixelmongenerations.core.storage.PlayerStorage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.text.Text;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class PartyUI {

	private SpongeUI display;
	private Player viewer;

	private SpongeRanch ranch;
	private GenerationsPen pen;
	private int slot;

	private PlayerStorage party;

	public PartyUI(Player viewer, SpongeRanch ranch, GenerationsPen pen, int slot) {
		this.viewer = viewer;
		this.ranch = ranch;
		this.pen = pen;
		this.slot = slot;

		this.party = PixelmonStorage.pokeBallManager.getPlayerStorageFromUUID(viewer.getUniqueId()).get();

		this.display = this.createDisplay();
	}

	public void open() {
		this.display.open(this.viewer);
	}

	private SpongeUI createDisplay() {
		return SpongeUI.builder()
				.title(SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsg(this.viewer, MsgConfigKeys.PARTY_TITLE, null, null))
				.dimension(InventoryDimension.of(9, 3))
				.build().define(this.design());
	}

	private SpongeLayout design() {
		SpongeLayout.SpongeLayoutBuilder slb = SpongeLayout.builder();
		slb.rows(SpongeIcon.BORDER, 0, 2);
		slb.slots(SpongeIcon.BORDER, 9, 16);

		for(int i = 10; i < 16; i++) {
			NBTTagCompound nbt = this.party.partyPokemon[i - 10];
			if(nbt == null) continue;
			EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(nbt, (World) this.viewer.getWorld());

			Map<String, Function<CommandSource, Optional<Text>>> tokens = Maps.newHashMap();
			tokens.put("calced_lvl", src -> Optional.of(Text.of(pokemon.getLvl().getLevel())));

			Map<String, Object> variables = Maps.newHashMap();
			variables.put("poke", pokemon);

			ItemStack display = GenerationsIcons.pokemonDisplay(pokemon);
			display.offer(Keys.DISPLAY_NAME, SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsg(this.viewer, MsgConfigKeys.POKEMON_TITLE_PEN, tokens, variables));
			display.offer(Keys.ITEM_LORE, SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsgs(this.viewer, MsgConfigKeys.POKEMON_LORE_SELECT, tokens, variables));
			SpongeIcon icon = new SpongeIcon(display);
			if(!pokemon.isEgg) {
				icon.addListener(clickable -> {
					this.display.close(this.viewer);
					this.pen.addAtSlot(pokemon, slot);
					this.party.removeFromPartyPlayer(this.party.getPosition(pokemon.getPokemonId()));
					SpongeDaycarePlugin.getSpongeInstance().getService().getStorage().updateRanch(this.ranch);
					this.viewer.sendMessage(SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsg(this.viewer, MsgConfigKeys.ADD_POKEMON, tokens, variables));
				});
			}
			slb.slot(icon, i);
		}

		ItemStack pc = ItemStack.builder()
				.itemType(SpongeItemTypeUtil.getOrDefaultItemTypeFromID("pixelmon:pc"))
				.add(Keys.DISPLAY_NAME, SpongeDaycarePlugin.getSpongeInstance().getTextParsingUtils().fetchAndParseMsg(this.viewer, MsgConfigKeys.PARTY_PC, null, null))
				.build();
		SpongeIcon icon = new SpongeIcon(pc);
		icon.addListener(clickable -> {
			new PcUI(this.viewer, this.ranch, this.pen, this.slot).open();
		});
		slb.slot(icon, 17);

		return slb.build();
	}
}
