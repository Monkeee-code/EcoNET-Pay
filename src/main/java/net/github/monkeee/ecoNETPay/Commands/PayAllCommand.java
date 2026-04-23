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

public class PayAllCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("Only players can use this command").color(NamedTextColor.RED));
            return true;
        }

        if (!(player.hasPermission("econet.payall") || player.isOp())) {
            player.sendMessage(Component.text("You are missing permissions!").color(NamedTextColor.RED));
            return true;
        }
        
        Economy economy = EcoNETPay.getEconomy();

        if (args.length == 0) {
            player.sendMessage(Component.text("Please mention a player to pay them!").color(NamedTextColor.RED));
            return false;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            player.sendMessage(Component.text("The player isn't online or does not exist!").color(NamedTextColor.RED));
            return false;
        }

        if (player == target) {
            player.sendMessage(Component.text("You cannot pay your self!").color(NamedTextColor.RED));
            return true;
        }

        double playerBalance = economy.getBalance(player);

        if (playerBalance == 0.00) {
            player.sendMessage(Component.text("You do not have any money to give!").color(NamedTextColor.RED));
            return true;
        }

        EconomyResponse r = economy.withdrawPlayer(player, playerBalance);
        if (r.transactionSuccess()) {
            player.sendMessage(Component.text("$"+EcoNETPay.formatCurrency(playerBalance)).color(NamedTextColor.GREEN)
                    .append(Component.text(" has been paid to ").color(NamedTextColor.DARK_AQUA))
                    .append(target.displayName().color(NamedTextColor.WHITE))
                    .append(Component.text(" ("+EcoNETPay.formatCurrency(r.balance)+")").color(NamedTextColor.DARK_GRAY)));
            EconomyResponse r1 = economy.depositPlayer(target, playerBalance);
            if (r1.transactionSuccess()) {
                target.sendMessage(Component.text("You have been paid ").color(NamedTextColor.DARK_AQUA)
                        .append(Component.text("$"+EcoNETPay.formatCurrency(playerBalance)).color(NamedTextColor.GREEN))
                        .append(Component.text(" by ").color(NamedTextColor.DARK_AQUA))
                        .append(Component.text(player.getName()).color(NamedTextColor.WHITE))
                        .append(Component.text(" ("+EcoNETPay.formatCurrency(r1.balance)+")").color(NamedTextColor.DARK_GRAY)));
                target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1, 0);
            }
        } else {
            player.sendMessage(Component.text(String.format("An error occurred: %s", r.errorMessage)).color(NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, SoundCategory.MASTER, 1, 0);
        }


        return false;
        }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) return List.of();
        @Nullable List<String> playerNames = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            playerNames.add(player.getName());
        }
        playerNames.remove(p.getName());
        if (args.length == 1) return EcoNETPay.GetBetterList(playerNames, args, 0);

        return List.of();
    }
}
