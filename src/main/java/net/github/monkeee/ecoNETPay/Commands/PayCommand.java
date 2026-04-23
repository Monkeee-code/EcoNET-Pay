package net.github.monkeee.ecoNETPay.Commands;

import net.github.monkeee.ecoNETPay.EcoNETPay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PayCommand implements CommandExecutor, TabCompleter {

    private final EcoNETPay plugin = EcoNETPay.getInstance();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("Only players can use this command").color(NamedTextColor.RED));
            return true;
        }

        if (!(player.hasPermission("econet.pay") || player.isOp())) {
            player.sendMessage(Component.text("You are missing permissions!").color(NamedTextColor.RED));
            return true;
        }

        Economy economy = EcoNETPay.getEconomy();

        if (args.length == 0) {
            player.sendMessage(Component.text("Please mention a player and the value to pay them!").color(NamedTextColor.RED));
            return false;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(Component.text("The player isn't online or does not exist!").color(NamedTextColor.RED));
            return false;
        }

        if (player == target) {
            player.sendMessage(Component.text("You cannot pay yourself!").color(NamedTextColor.RED));
            return true;
        }

        if (args.length == 1) {
            player.sendMessage(Component.text("Please specify a value to pay the player " + args[0]).color(NamedTextColor.RED));
            return false;
        }

        double value = currencyFormater(args[1], player);
        if (value <= 0) return false;

        double playerBalance = economy.getBalance(player);

        if (playerBalance < value) {
            player.sendMessage(Component.text("You do not have enough money for this transaction!").color(NamedTextColor.RED));
            return false;
        }


        EconomyResponse r = economy.withdrawPlayer(player, value);
        if (r.transactionSuccess()) {
            player.sendMessage(Component.text("$"+ EcoNETPay.formatCurrency(value)).color(NamedTextColor.GREEN)
                    .append(Component.text(" has been paid to ").color(NamedTextColor.DARK_AQUA))
                    .append(target.displayName().color(NamedTextColor.WHITE))
                    .append(Component.text(" ("+EcoNETPay.formatCurrency(r.balance)+")").color(NamedTextColor.DARK_GRAY)));

            EconomyResponse r1 = economy.depositPlayer(target, value);
            if (r1.transactionSuccess()) {
                target.sendMessage(Component.text("You have been paid ").color(NamedTextColor.DARK_AQUA)
                        .append(Component.text("$"+EcoNETPay.formatCurrency(value)).color(NamedTextColor.GREEN))
                        .append(Component.text(" by ").color(NamedTextColor.DARK_AQUA))
                        .append(Component.text(player.getName()).color(NamedTextColor.WHITE))
                        .append(Component.text(" ("+EcoNETPay.formatCurrency(r1.balance)+")").color(NamedTextColor.DARK_GRAY)));
                target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1, 0);
            }
        } else {
            player.sendMessage(Component.text(String.format("An error occurred: %s", r.errorMessage)).color(NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, SoundCategory.MASTER, 1, 0);
        }

        return true;
    }

    private double currencyFormater(String number, Player player) {
        int length = number.length();
        try {
            switch (number.charAt(length - 1)) {
                case 'k' -> {
                    number = number.replace('k', ' ');
                    double parsed = Double.parseDouble(number);
                    parsed = parsed * 1000;
                    return parsed;
                }
                case 'm' -> {
                    number = number.replace('m', ' ');
                    double parsed = Double.parseDouble(number);
                    parsed = parsed * 1000000;
                    return parsed;
                }
                case 'b' -> {
                    number = number.replace('b', ' ');
                    double parsed = Double.parseDouble(number);
                    parsed = parsed * 1000000000;
                    return parsed;
                }
                default -> {
                    return Double.parseDouble(number);
                }
            }
        } catch (NumberFormatException e) {
            plugin.getLogger().severe("There was an error while executing /pay command: " + e.getMessage());
            player.sendMessage(Component.text("There was an error. Please ensure that you inputted a correct value!").color(NamedTextColor.RED));
            return 0;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) return List.of();
        @Nullable List<String> playerNames = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerNames.add(player.getName());
        }
        playerNames.remove(p.getName());
        List<String> valueSuggestion = List.of("1", "1k", "10k", "100k", "1m", "1b");

        if (args.length == 1) return EcoNETPay.GetBetterList(playerNames, args, 0);
        if (args.length == 2) return valueSuggestion;
        return List.of();
        }
    }
