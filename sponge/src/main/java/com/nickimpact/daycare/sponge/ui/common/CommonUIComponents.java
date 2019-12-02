package com.nickimpact.daycare.sponge.ui.common;

import com.nickimpact.daycare.sponge.SpongeDaycarePlugin;
import com.nickimpact.daycare.sponge.configuration.MsgConfigKeys;
import com.nickimpact.impactor.sponge.ui.SpongeIcon;
import com.nickimpact.impactor.sponge.ui.SpongeLayout;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.config.PixelmonItems;
import com.pixelmonmod.pixelmon.entities.pixelmon.EnumSpecialTexture;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.forms.EnumGreninja;
import com.pixelmonmod.pixelmon.enums.forms.EnumNoForm;
import com.pixelmonmod.pixelmon.enums.forms.IEnumForm;
import com.pixelmonmod.pixelmon.storage.NbtKeys;
import lombok.AllArgsConstructor;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

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
        net.minecraft.item.ItemStack nativeItem = new net.minecraft.item.ItemStack(PixelmonItems.itemPixelmonSprite);
        NBTTagCompound nbt = new NBTTagCompound();
        String idValue = String.format("%03d", pokemon.getBaseStats().nationalPokedexNumber);
        if (pokemon.isEgg()) {
            switch(pokemon.getSpecies()) {
                case Manaphy:
                    nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/eggs/manaphy1");
                    break;
                case Togepi:
                    nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/eggs/togepi1");
                    break;
                default:
                    nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/eggs/egg1");
                    break;
            }
        } else {
            if (pokemon.isShiny()) {
                nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/shinypokemon/" + idValue + getSpriteExtraProperly(pokemon.getSpecies(), pokemon.getFormEnum(), pokemon.getGender(), pokemon.getSpecialTexture()));
            } else {
                nbt.setString(NbtKeys.SPRITE_NAME, "pixelmon:sprites/pokemon/" + idValue + getSpriteExtraProperly(pokemon.getSpecies(), pokemon.getFormEnum(), pokemon.getGender(), pokemon.getSpecialTexture()));
            }
        }
        nativeItem.setTagCompound(nbt);
        return (ItemStack) (Object) (nativeItem);
    }

    private static String getSpriteExtraProperly(EnumSpecies species, IEnumForm form, Gender gender, EnumSpecialTexture specialTexture) {
        if (species == EnumSpecies.Greninja && (form == EnumGreninja.BASE || form == EnumGreninja.BATTLE_BOND) && specialTexture.id > 0 && species.hasSpecialTexture()) {
            return "-special";
        }

        if(form != EnumNoForm.NoForm) {
            return species.getFormEnum(form.getForm()).getSpriteSuffix();
        }

        if(EnumSpecies.mfSprite.contains(species)) {
            return "-" + gender.name().toLowerCase();
        }

        if(specialTexture.id > 0 && species.hasSpecialTexture()) {
            return "-special";
        }

        return "";
    }

    @AllArgsConstructor
    public static class CommonConfirmComponent {
        private List<Text> lore;
        private BiConsumer<Player, ClickInventoryEvent> clickable;
    }
}
