package com.nickimpact.daycare.api.json;

import com.google.gson.*;
import com.nickimpact.daycare.DaycarePlugin;
import com.nickimpact.daycare.PluginInfo;

import org.spongepowered.api.text.Text;

import java.lang.reflect.Type;

/**
 * This class serves as a way to serialize as well as deserialize any object with GSON.
 * To use this class properly, we must alert GSON that the adapter for this class in
 * question exists, and register it.
 *
 * <p>Note: The main purpose behind this class is to help serialize and deserialize
 * an abstract class. With GSON, we can't serialize and deserialize an abstract extending
 * class properly due to the absence of variables known to the scope. By offering the base
 * class as the adapter element, any inheriting class will be able to know of the variables
 * contained in the higher scope, and have all fields filled properly.</p>
 *
 * @author NickImpact
 */
public abstract class Adapter<E> implements JsonSerializer<E>, JsonDeserializer<E> {

	@Override
	@SuppressWarnings({"unchecked", "ConstantConditions"})
	public E deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) throws JsonParseException
	{
		JsonObject obj = (JsonObject)json;
		try {
			return (E) getGson().fromJson(obj, getRegistry().get(obj.get("id").getAsString()));
		} catch (Exception e) {
			DaycarePlugin.getInstance().getConsole().ifPresent(console -> console.sendMessages(
					Text.of(PluginInfo.ERROR_PREFIX, "========== JSON Error =========="),
					Text.of(PluginInfo.ERROR_PREFIX, "Failed to deserialize JSON data"),
					Text.of(PluginInfo.ERROR_PREFIX, "Exception: " + e.getClass().getSimpleName()),
					Text.of(PluginInfo.ERROR_PREFIX, "================================")
			));
			throw new JsonParseException(e.getMessage());
		}
	}

	@Override
	public JsonElement serialize(E src, Type type, JsonSerializationContext ctx) {
		return ctx.serialize(src);
	}

	public abstract Gson getGson();

	public abstract Registry getRegistry();
}
