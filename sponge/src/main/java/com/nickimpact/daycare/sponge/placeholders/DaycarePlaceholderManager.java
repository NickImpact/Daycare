package com.nickimpact.daycare.sponge.placeholders;

import com.google.common.collect.BiMap;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.nickimpact.daycare.api.pens.Pen;
import com.nickimpact.daycare.api.pens.Ranch;
import com.nickimpact.daycare.api.pens.Statistics;
import com.nickimpact.daycare.sponge.SpongeDaycarePlugin;
import com.nickimpact.daycare.sponge.configuration.MsgConfigKeys;
import net.impactdev.impactor.api.Impactor;
import net.impactdev.impactor.api.configuration.Config;
import net.impactdev.impactor.api.configuration.ConfigKey;
import net.impactdev.impactor.api.services.text.MessageService;
import net.impactdev.impactor.api.utilities.Time;
import net.impactdev.impactor.api.utilities.mappings.Tuple;
import org.mariuszgromada.math.mxparser.Argument;
import org.mariuszgromada.math.mxparser.Expression;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.placeholder.PlaceholderContext;
import org.spongepowered.api.text.placeholder.PlaceholderParser;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DaycarePlaceholderManager {

    private static final DecimalFormat df = new DecimalFormat("#0.##");
    public static final DateTimeFormatter base = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss a z").withZone(ZoneId.systemDefault());

    private final List<PlaceholderParser> parsers = Lists.newArrayList();

    public DaycarePlaceholderManager() {
        this.populate();
    }

    public void register(PlaceholderParser parser) {
        this.parsers.add(parser);
    }

    public ImmutableList<PlaceholderParser> getAllParsers() {
        return ImmutableList.copyOf(this.parsers);
    }

    public void populate() {
        Config msgConf = SpongeDaycarePlugin.getSpongeInstance().getMsgConfig();
        MessageService<Text> processor = Impactor.getInstance().getRegistry().get(MessageService.class);
        PluginContainer container = SpongeDaycarePlugin.getSpongeInstance().getPluginContainer();

        this.register(this.create("prefix", "GTS Prefix", container, context -> processor.parse(msgConf.get(MsgConfigKeys.PLUGIN_PREFIX))));
        this.register(this.create("error", "GTS Error Prefix", container, context -> processor.parse(msgConf.get(MsgConfigKeys.PLUGIN_ERROR))));
        this.register(new SourceSpecificPlaceholderParser<>(
                Ranch.class,
                "stats_egg_ratio",
                "Statistics: Egg Collection Ratio",
                ranch -> Text.of(df.format(ranch.getStats().getEggCollectionRatio()), "% Collected")
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Ranch.class,
                "stats_egg_collected",
                "Statistics: Egg Collection Amount",
                ranch -> Text.of(df.format(ranch.getStats().getStat(Statistics.Stats.EGGS_COLLECTED)))
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Ranch.class,
                "stats_egg_dismissed",
                "Statistics: Egg Dismissed Amount",
                ranch -> Text.of(df.format(ranch.getStats().getStat(Statistics.Stats.EGGS_DELETED)))
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Ranch.class,
                "stats_num_gained_lvls",
                "Statistics: Number of Levels Gained",
                ranch -> Text.of(df.format(ranch.getStats().getStat(Statistics.Stats.NUM_GAINED_LVLS)))
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Pen.class,
                "pen_unlock_date",
                "Pen: Unlock Date",
                pen -> Text.of(base.format(pen.getDateUnlocked()))
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Integer.class,
                "pen_id",
                "Pen: ID",
                Text::of
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                String.class,
                "pen_price",
                "Pen: Unlock Price",
                Text::of
        ));
        this.register(new SourceSpecificPlaceholderParser<>(
                Integer.class,
                "slot_id",
                "Slot: ID",
                Text::of
        ));
    }

    private PlaceholderParser create(String id, String name, PluginContainer plugin, Function<PlaceholderContext, Text> parser) {
        return PlaceholderParser.builder()
                .id(id)
                .name(name)
                .plugin(plugin)
                .parser(parser)
                .build();
    }

    private Optional<String> getOptionFromSubject(Subject subject, String... options) {
        for (String option : options) {
            String o = option.toLowerCase();

            // Option for context.
            Optional<String> os = subject.getOption(subject.getActiveContexts(), o);
            if (os.isPresent()) {
                return os.map(r -> r.isEmpty() ? null : r);
            }

            // General option
            os = subject.getOption(o);
            if (os.isPresent()) {
                return os.map(r -> r.isEmpty() ? null : r);
            }
        }

        return Optional.empty();
    }

}
