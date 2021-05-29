package com.nickimpact.daycare.sponge.utils;

import com.nickimpact.daycare.sponge.SpongeDaycarePlugin;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.api.services.text.MessageService;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class TextParser {

    public static Text parse(String in) {
        return parse(in, Collections.emptyList());
    }

    public static Text parse(String in, List<Supplier<Object>> sources) {
        return ((MessageService<Text>) Impactor.getInstance().getRegistry().get(MessageService.class))
                .parse(in, sources);
    }

    public static List<Text> parse(List<String> in) {
        return parse(in, Collections.emptyList());
    }

    public static List<Text> parse(List<String> in, List<Supplier<Object>> sources) {
        return ((MessageService<Text>) Impactor.getInstance().getRegistry().get(MessageService.class))
                .parse(in, sources);
    }

    public static <T> T read(ConfigKey<T> key) {
        return SpongeDaycarePlugin.getSpongeInstance().getMsgConfig().get(key);
    }
}
