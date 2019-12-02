package com.nickimpact.daycare.spigot.ui;

import com.nickimpact.impactor.spigot.ui.SpigotIcon;
import com.nickimpact.impactor.spigot.ui.SpigotLayout;
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
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.function.BiConsumer;

public class CommonUIComponents {

    public static SpigotLayout confirmBase(SpigotIcon focus, CommonConfirmComponent confirm, BiConsumer<Player, InventoryClickEvent> cancel) {
        ItemStack c = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 5);
        ItemMeta cm = c.getItemMeta();
        cm.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aConfirm Action"));
        cm.setLore(confirm.lore);
        c.setItemMeta(cm);
        SpigotIcon ci = new SpigotIcon(c);
        ci.addListener(clickable -> confirm.clickable.accept(clickable.getPlayer(), clickable.getEvent()));

        ItemStack ca = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short) 14);
        ItemMeta cameta = ca.getItemMeta();
        cameta.setDisplayName(ChatColor.RED + "Cancel Action");
        ca.setItemMeta(cameta);
        SpigotIcon can = new SpigotIcon(ca);
        can.addListener(clickable -> cancel.accept(clickable.getPlayer(), clickable.getEvent()));

        return SpigotLayout.builder()
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
        return CraftItemStack.asBukkitCopy((net.minecraft.server.v1_12_R1.ItemStack) (Object) nativeItem);
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
        private List<String> lore;
        private BiConsumer<Player, InventoryClickEvent> clickable;
    }
}
