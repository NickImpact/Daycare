package com.nickimpact.daycare.ui;

import com.google.common.collect.Lists;
import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.api.gui.Icon;
import com.nickimpact.daycare.api.gui.InventoryBase;
import com.nickimpact.daycare.configuration.ConfigKeys;
import com.nickimpact.daycare.exceptions.AlreadyUnlockedException;
import com.nickimpact.daycare.ranch.Pen;
import com.nickimpact.daycare.ranch.Ranch;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * (Some note will appear here)
 *
 * @author NickImpact (Nick DeGruccio)
 */
public class RanchUI extends InventoryBase {

	private Ranch ranch;

	public RanchUI(Player player, Ranch ranch) {
		super(player);
		this.ranch = ranch;

		this.drawDesign();
		this.drawPens();
	}

	@Override
	public Text getTitle() {
		return Text.of(TextColors.RED, "Daycare ", TextColors.GRAY, "\u00BB ", TextColors.DARK_AQUA, "Main Menu");
	}

	@Override
	public int getSize() {
		return 6;
	}

	private void drawDesign() {
		this.drawBorder(this.getSize(), DyeColors.BLACK);
	}

	private void drawPens() {
		List<Pen> pens = ranch.getPens();
		int slot = 10;
		int penID = 1;
		for(Pen pen : pens) {
			if(slot == 17) {
				slot = 19;
			}
			Icon icon = new Icon(slot++, ItemStack.builder()
					.itemType(Sponge.getRegistry().getType(ItemType.class, "pixelmon:ranch").orElse(ItemTypes.BARRIER))
					.add(Keys.DISPLAY_NAME, Text.of(TextColors.YELLOW, "Ranch ", penID++))
					.build()
			);

			if(pen.isUnlocked()) {
				List<Text> lore = Lists.newArrayList();
				lore.add(this.penInfo(pen, 1));
				lore.add(this.penInfo(pen, 2));

				icon.getDisplay().offer(Keys.ITEM_LORE, lore);
				icon.addListener(clickable -> {
					Sponge.getScheduler().createTaskBuilder().execute(() -> {
						clickable.getPlayer().closeInventory();
						clickable.getPlayer().openInventory(new PenUI(clickable.getPlayer(), pen).getInventory());
					}).delayTicks(1).submit(DaycarePlugin.getInstance());
				});
			} else {
				BigDecimal price = new BigDecimal(DaycarePlugin.getInstance().getConfig().get(ConfigKeys.BASE_PEN_PRICE) +
						(penID - 1) * DaycarePlugin.getInstance().getConfig().get(ConfigKeys.INCREMENT_PEN_PRICE));
				icon.getDisplay().offer(Keys.ITEM_LORE, Lists.newArrayList(
						Text.of(TextColors.RED, "Currently locked..."),
						Text.EMPTY,
						Text.of(TextColors.RED, "Unlock price:"),
						Text.of(TextColors.GREEN, "  - " + DaycarePlugin.getInstance().getEconomy().getDefaultCurrency().format(price).toPlain())
				));
				final int id = penID - 1;
				final int sl = slot - 1;
				icon.addListener(clickable -> {
					try {
						boolean unlock = ranch.unlock(id);
						if(!unlock) {
							Sponge.getScheduler().createTaskBuilder().execute(() -> {
								Icon ic = new Icon(sl, ItemStack.builder()
										.itemType(ItemTypes.BARRIER)
										.add(Keys.DISPLAY_NAME, Text.of(TextColors.RED, "Insufficient funds..."))
										.build()
								);
								this.addIcon(ic);
								this.updateContents(sl);
							}).delayTicks(1).submit(DaycarePlugin.getInstance());

							Sponge.getScheduler().createTaskBuilder().execute(() -> {
								this.addIcon(icon);
								this.updateContents(sl);
							}).delay(5, TimeUnit.SECONDS).submit(DaycarePlugin.getInstance());
						}
					} catch (AlreadyUnlockedException e) {
						e.printStackTrace();
					}
				});
			}

			this.addIcon(icon);
		}
	}

	private Text penInfo(Pen pen, int id) {
		return Text.of(
				TextColors.DARK_AQUA, "Slot ", id, ": ",
				(pen.getAtPosition(id).map(pokemon -> Text.of(
						TextColors.YELLOW, pokemon.getPokemon().getName(), " ", TextColors.GRAY, "(",
						TextColors.GREEN, "Lvl ", pokemon.getStartLvl() + pokemon.getGainedLvls(),
						TextColors.GRAY, ")"
				)).orElse(Text.of(TextColors.RED, "Empty"))));
	}

	private void playerIcon() {

	}

	private void settingsIcon() {

	}

	private void statisticsDisplay() {

	}
}
