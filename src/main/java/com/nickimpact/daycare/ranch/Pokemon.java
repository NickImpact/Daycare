package com.nickimpact.daycare.ranch;

import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.configuration.ConfigKeys;
import com.nickimpact.daycare.utils.GsonUtils;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.spongepowered.api.Sponge;

import java.util.Date;

/**
 * (Some note will go here)
 *
 * @author NickImpact
 */
public class Pokemon {

	private transient EntityPixelmon pokemon;
	private transient NBTTagCompound nbt;
	private String json;

	@Getter
	private final int startLvl;

	@Getter @Setter
	private int gainedLvls;

	@Getter @Setter
	private Date lastLvl;

	public static final long waitTime = DaycarePlugin.getInstance().getConfig().get(ConfigKeys.LVL_WAIT_TIME);

	public Pokemon(EntityPixelmon pokemon) {
		this.pokemon = pokemon;
		startLvl = pokemon.getLvl().getLevel();
		NBTTagCompound nbt = new NBTTagCompound();
		this.nbt = pokemon.writeToNBT(nbt);
		json = GsonUtils.serialize(this.nbt);
	}

	public EntityPixelmon getPokemon() {
		if(this.pokemon == null) {
			this.pokemon = (EntityPixelmon) PixelmonEntityList.createEntityFromNBT(
					decode(), (World) Sponge.getServer().getWorld(Sponge.getServer().getDefaultWorldName()).get()
			);
		}

		return this.pokemon;
	}

	private NBTTagCompound decode() {
		return nbt != null ? nbt : (nbt = GsonUtils.deserialize(json));
	}

	public void incrementGainedLvls() {
		++this.gainedLvls;
	}
}
