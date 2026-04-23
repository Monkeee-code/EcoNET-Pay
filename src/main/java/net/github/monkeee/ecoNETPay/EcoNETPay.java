package net.github.monkeee.ecoNETPay;

import net.github.monkeee.ecoNETPay.Commands.PayAllCommand;
import net.github.monkeee.ecoNETPay.Commands.PayCommand;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.bukkit.Bukkit.getPluginManager;

public final class EcoNETPay extends JavaPlugin {

    private static EcoNETPay instance;
    public static Economy eco = null;

    @Override
    public void onEnable() {
        instance = this;
        if (!enableEconomy()) {
            getLogger().severe("Disabling EcoNETPay due to missing dependency!");
            getServer().getPluginManager().disablePlugin(this);
        }

        Objects.requireNonNull(getCommand("pay")).setExecutor(new PayCommand());
        Objects.requireNonNull(getCommand("pay")).setTabCompleter(new PayCommand());
        Objects.requireNonNull(getCommand("payall")).setExecutor(new PayAllCommand());
        Objects.requireNonNull(getCommand("payall")).setTabCompleter(new PayAllCommand());
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling EcoNETPay!");
    }

    private static boolean enableEconomy() {
        if (getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        eco = rsp.getProvider();
        return eco != null;
    }

    public static EcoNETPay getInstance() { return instance; }
    public static Economy getEconomy() { return eco; }

    public static String formatCurrency(double value) {
        if (value >= 1_000_000_000) return String.format("%.2fb", value / 1_000_000_000);
        if (value >= 1_000_000) return  String.format("%.2fm", value / 1_000_000);
        if (value >= 1_000) return String.format("%.2fk", value / 1_000);
        return String.format("%.2f", value);
    }

    public static List<String> GetBetterList(List<String> list, String[] args, int argStage) {
        if (argStage >= args.length) return List.of();
        List<String> completions = null;
        String input = args[argStage].toLowerCase();
        for (String s : list) {
            if (s.toLowerCase().startsWith(input)) {
                if (completions == null) {
                    completions = new ArrayList<>();
                }
                completions.add(s);
            }
        }
        if (completions != null) Collections.sort(completions);
        return completions != null ? completions : List.of();
    }
}
