package ru.whitebeef.beeflibrary.commands;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractCommand extends BukkitCommand {

    private static final List<AbstractCommand> registeredCommands = new ArrayList<>();

    public static void unregisterAllCommands(Plugin plugin) {
        registeredCommands.forEach(abstractCommand -> abstractCommand.unregister(plugin));
    }

    public static Builder builder(String name, Class<? extends AbstractCommand> clazz) {
        return new Builder(name, clazz);
    }

    private final Map<CommandSender, Cooldown> cooldowns = new HashMap<>();
    private final String name;
    private final Map<String, AbstractCommand> subCommands;
    private final BiConsumer<CommandSender, String[]> onCommand;
    private final BiFunction<CommandSender, String[], List<String>> onTabComplete;
    private final List<Alias> aliases;
    private final int minArgsCount;
    private boolean onlyForPlayers;
    private String cooldownSkipPermission;
    private Cooldown.Type cooldownType;
    private long cooldown;

    public AbstractCommand(@NotNull String name, @Nullable String permission, @Nullable String description, @Nullable String usageMessage, boolean onlyForPlayers,
                           BiConsumer<CommandSender, String[]> onCommand,
                           BiFunction<CommandSender, String[], List<String>> onTabComplete,
                           Map<String, AbstractCommand> subCommands,
                           List<Alias> aliases, int minArgsCount) {
        super(name, Objects.requireNonNullElseGet(description, String::new),
                Objects.requireNonNullElseGet(usageMessage, String::new),
                aliases.stream().map(Alias::getName).collect(Collectors.toList()));
        setPermission(Objects.requireNonNullElseGet(permission, String::new));
        this.name = name.toLowerCase();
        this.onCommand = onCommand;
        this.aliases = aliases;
        this.onlyForPlayers = onlyForPlayers;
        this.onTabComplete = onTabComplete;
        this.subCommands = subCommands;
        this.minArgsCount = minArgsCount;
    }

    public AbstractCommand addSubCommand(AbstractCommand abstractCommand) {
        subCommands.put(abstractCommand.getName(), abstractCommand);
        for (String alias : abstractCommand.getAliases()) {
            subCommands.put(alias, abstractCommand);
        }
        return this;
    }

    public Collection<AbstractCommand> getSubCommands() {
        return subCommands.values();
    }

    public String getName() {
        return name;
    }

    public boolean isOnlyForPlayers() {
        return onlyForPlayers;
    }

    public int getMinArgsCount() {
        return minArgsCount;
    }

    @Nullable
    public BiConsumer<CommandSender, String[]> getOnCommand() {
        return onCommand;
    }

    public BiFunction<CommandSender, String[], List<String>> getOnTabComplete() {
        return onTabComplete;
    }

    @Nullable
    public AbstractCommand getSubcommand(String str) {
        return subCommands.get(str.toLowerCase());
    }

    public void setCooldown(Cooldown.Type type, long cooldown, String cooldownSkipPermission) {
        this.cooldownType = type;
        this.cooldown = cooldown;
        this.cooldownSkipPermission = cooldownSkipPermission;
    }

    protected void onCommand(CommandSender sender, String[] args) {
        StandardConsumers.NO_ARGS.getConsumer().accept(sender, args);
    }

    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    private Set<String> getAllAvailableAliases(CommandSender sender) {
        boolean senderIsPlayer = sender instanceof Player;
        return Stream.concat(
                        subCommands.values().stream()
                                .filter(abstractCommand -> !abstractCommand.isOnlyForPlayers() || senderIsPlayer)
                                .filter(abstractCommand -> abstractCommand.testPermissionSilent(sender))
                                .map(AbstractCommand::getName),
                        subCommands.values().stream()
                                .filter(abstractCommand -> !abstractCommand.isOnlyForPlayers() || senderIsPlayer)
                                .filter(abstractCommand -> abstractCommand.testPermissionSilent(sender))
                                .flatMap(abstractCommand -> abstractCommand.aliases.stream()
                                        .filter(Alias::isShown)
                                        .map(Alias::getName)))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        Pair<Integer, AbstractCommand> pair = getCurrentCommand(args);
        AbstractCommand currentCommand = pair.right();

        args = Arrays.stream(args).skip(pair.left()).toArray(String[]::new);

        if (args.length < currentCommand.getMinArgsCount()) {
            StandardConsumers.NO_ARGS.getConsumer().accept(sender, args);
            return true;
        }

        if (!currentCommand.testPermissionSilent(sender)) {
            StandardConsumers.NO_PERMISSION.getConsumer().accept(sender, args);
            return true;
        }
        if (!(sender instanceof Player) && isOnlyForPlayers()) {
            StandardConsumers.ONLY_FOR_PLAYERS.getConsumer().accept(sender, args);
            return true;
        }

        if (cooldowns.get(sender) != null && !cooldowns.get(sender).isCooldownPassed(sender, cooldownSkipPermission)) {
            StandardConsumers.COOLDOWN.getConsumer().accept(sender, args);
            return true;
        }

        if (currentCommand.getOnCommand() != null) {
            currentCommand.getOnCommand().accept(sender, args);
            return true;
        }
        currentCommand.onCommand(sender, args);
        return true;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if (args.length == 0) {
            return Collections.emptyList();
        }

        ArrayList<String> retList = new ArrayList<>();

        if (args.length == 1) {
            if (onTabComplete != null) {
                retList.addAll(onTabComplete.apply(sender, args));
            } else {
                retList.addAll(this.onTabComplete(sender, args));
            }
            retList.addAll(getAllAvailableAliases(sender));
        } else {
            AbstractCommand subcommand = this.getSubcommand(args[0]);
            if (subcommand != null) {
                retList.addAll(subcommand.tabComplete(sender, args[0], Arrays.copyOfRange(args, 1, args.length)));
            } else {
                if (onTabComplete != null) {
                    retList.addAll(onTabComplete.apply(sender, args));
                } else {
                    retList.addAll(this.onTabComplete(sender, args));
                }
            }
        }
        return retList.stream().filter(str -> str.startsWith(args[args.length - 1])).toList();
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args, @Nullable Location location) throws IllegalArgumentException {
        return this.tabComplete(sender, alias, args);
    }

    private Pair<Integer, AbstractCommand> getCurrentCommand(String[] args) {
        int index = -1;
        AbstractCommand currentCommand = this;
        while (args.length > ++index) {
            String arg = args[index].toLowerCase();

            AbstractCommand lastCommand = currentCommand.getSubcommand(arg);

            if (lastCommand == null) {
                break;
            }

            currentCommand = lastCommand;
        }
        return Pair.of(index, currentCommand);
    }

    private void loadTree(AbstractCommand parent, AbstractCommand current) {
        if (current.getPermission().isEmpty()) {
            current.setPermission(parent.getPermission() + "." + current.getName());
        }
        current.onlyForPlayers = parent.onlyForPlayers;

        current.getSubCommands().forEach(command -> loadTree(current, command));
    }


    public void register(Plugin plugin) {
        loadTree(this, this);

        try {
            final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
            commandMap.register(plugin.getName().toLowerCase(), this);
            registeredCommands.add(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void unregister(Plugin plugin) {
        try {
            final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());
            HashMap<String, Command> knownCommands = (HashMap<String, Command>) commandMap.getKnownCommands();
            knownCommands.remove(plugin.getName().toLowerCase() + ":" + getName());
            knownCommands.remove(getName());
            for (String alias : getAliases()) {
                knownCommands.remove(plugin.getName().toLowerCase() + ":" + alias);
                knownCommands.remove(alias);
            }
            super.unregister(commandMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class Builder {
        private final Class<? extends AbstractCommand> clazz;
        private final String name;
        private final Map<String, AbstractCommand> subCommands = new HashMap<>();
        private final List<Alias> aliases = new ArrayList<>();
        private BiConsumer<CommandSender, String[]> onCommand = null;
        private BiFunction<CommandSender, String[], List<String>> onTabComplete = null;
        private String permission = "";
        private String description = "";
        private String usageMessage = "";
        private boolean onlyForPlayers = false;
        private int minArgsCount = 0;
        private String cooldownSkipPermission = "";
        private Cooldown.Type cooldownType = Cooldown.Type.MILLIS;
        private long cooldown = 0;

        public Builder(String name, Class<? extends AbstractCommand> clazz) {
            this.name = name;
            this.clazz = clazz;
        }

        public Builder addSubCommand(AbstractCommand command) {
            subCommands.put(command.getName(), command);
            return this;
        }

        public Builder setOnCommand(BiConsumer<CommandSender, String[]> onCommand) {
            this.onCommand = onCommand;
            return this;
        }

        public Builder setOnTabComplete(BiFunction<CommandSender, String[], List<String>> onTabComplete) {
            this.onTabComplete = onTabComplete;
            return this;
        }

        public Builder setPermission(String permission) {
            this.permission = permission;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setUsageMessage(String usageMessage) {
            this.usageMessage = usageMessage;
            return this;
        }

        public Builder addAlias(Alias alias) {
            this.aliases.add(alias);
            return this;
        }

        public Builder addAliases(Alias... alias) {
            this.aliases.addAll(List.of(alias));
            return this;
        }

        public Builder setOnlyForPlayers(boolean value) {
            this.onlyForPlayers = value;
            return this;
        }

        public Builder setMinArgsCount(int minArgsCount) {
            this.minArgsCount = minArgsCount;
            return this;
        }

        /**
         * @param skipPermission Empty string is no permission to skip. Nobody can't skip with that
         */
        public Builder setCooldown(Cooldown.Type type, long cooldown, @NotNull String skipPermission) {
            this.cooldownType = type;
            this.cooldown = cooldown;
            this.cooldownSkipPermission = skipPermission;
            return this;
        }

        public AbstractCommand build() {
            try {
                AbstractCommand command = clazz.getDeclaredConstructor(String.class, String.class, String.class, String.class, boolean.class, BiConsumer.class, BiFunction.class, Map.class, List.class, int.class)
                        .newInstance(name, permission, description, usageMessage, onlyForPlayers, onCommand, onTabComplete, subCommands, aliases, minArgsCount);
                command.setCooldown(cooldownType, cooldown, cooldownSkipPermission); // Костыль, чтобы не переписывать все остальные плагины
                return command;
            } catch (Exception e) {
                throw new RuntimeException();
            }
        }
    }
}
