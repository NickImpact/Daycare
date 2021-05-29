package com.nickimpact.daycare.sponge.ui.common;

import com.nickimpact.daycare.sponge.SpongeDaycarePlugin;
import com.nickimpact.daycare.sponge.configuration.MsgConfigKeys;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.config.PixelmonItems;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.items.ItemPixelmonSprite;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import lombok.AllArgsConstructor;
import net.impactdev.impactor.sponge.ui.SpongeIcon;
import net.impactdev.impactor.sponge.ui.SpongeLayout;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Calendar;
import java.util.List;
import java.util.function.BiConsumer;

public class CommonUIComponents {

    public static SpongeLayout confirmBase(SpongeIcon focus, CommonConfirmComponent confirm, BiConsumer<Player, ClickInventoryEvent> cancel) {
        ItemStack c = ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .add(Keys.DYE_COLOR, DyeColors.LIME)
                .add(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize(SpongeDaycarePlugin.getSpongeInstance().getMsgConfig().get(MsgConfigKeys.CONFIRM)))
                .add(Keys.ITEM_LORE, confirm.lore)
                .build();
        SpongeIcon ci = new SpongeIcon(c);
        ci.addListener(clickable -> confirm.clickable.accept(clickable.getPlayer(), clickable.getEvent()));

        ItemStack ca = ItemStack.builder()
                .itemType(ItemTypes.STAINED_GLASS_PANE)
                .add(Keys.DYE_COLOR, DyeColors.RED)
                .add(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize(SpongeDaycarePlugin.getSpongeInstance().getMsgConfig().get(MsgConfigKeys.CANCEL)))
                .build();
        SpongeIcon can = new SpongeIcon(ca);
        can.addListener(clickable -> cancel.accept(clickable.getPlayer(), clickable.getEvent()));

        return SpongeLayout.builder()
                .border()
                .slot(focus, 13)
                .slots(ci, 29, 30, 38, 39)
                .slots(can, 32, 33, 41, 42)
                .build();
    }

    public static ItemStack pokemonDisplay(Pokemon pokemon) {
        Calendar calendar = Calendar.getInstance();
        boolean aprilFools = false;
        if(calendar.get(Calendar.MONTH) == Calendar.APRIL && calendar.get(Calendar.DAY_OF_MONTH) == 1) {
            aprilFools = true;
        }

        if(pokemon.isEgg()) {
            net.minecraft.item.ItemStack item = new net.minecraft.item.ItemStack(PixelmonItems.itemPixelmonSprite);
            NBTTagCompound nbt = new NBTTagCompound();
            switch (pokemon.getSpecies()) {
                case Manaphy:
                case Togepi:
                    nbt.setString(NbtKeys.SPRITE_NAME,
                            String.format("pixelmon:sprites/eggs/%s1", pokemon.getSpecies().name.toLowerCase()));
                    break;
                default:
                    nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/eggs/egg1");
                    break;
            }
            item.setTagCompound(nbt);
            return (ItemStack) (Object) item;
        } else {
            return (ItemStack) (Object) (aprilFools ? ItemPixelmonSprite.getPhoto(Pixelmon.pokemonFactory.create(EnumSpecies.Bidoof)) : ItemPixelmonSprite.getPhoto(pokemon));
        }
    }

    @AllArgsConstructor
    public static class CommonConfirmComponent {
        private List<Text> lore;
        private BiConsumer<Player, ClickInventoryEvent> clickable;
    }
}
