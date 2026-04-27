package org.itxtech.synapseapi.command;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginIdentifiableCommand;
import cn.nukkit.command.data.CommandFlag;
import cn.nukkit.plugin.Plugin;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.network.latency.NetworkLatencyTraceManager;
import org.itxtech.synapseapi.network.latency.NetworkLatencyTraceManager.SegmentStats;
import org.itxtech.synapseapi.network.latency.NetworkLatencyTraceManager.Summary;

public class LatencyTraceCommand extends Command implements PluginIdentifiableCommand {
    private final SynapseAPI plugin;

    public LatencyTraceCommand(SynapseAPI plugin) {
        super("latencytrace", "Network latency trace telemetry", "/latencytrace <summary|player|reset>");
        setPermission("synapseapi.command.latencytrace");
        commandData.flags.add(CommandFlag.TEST);
        commandData.flags.add(CommandFlag.HIDDEN_FROM_COMMAND_BLOCK_ORIGIN);
        commandData.flags.add(CommandFlag.NOT_CHEAT);
        commandParameters.clear();
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!testPermission(sender)) {
            return false;
        }

        NetworkLatencyTraceManager manager = plugin.getLatencyTraceManager();
        if (!manager.isEnabled()) {
            sender.sendMessage("Latency trace is disabled. Set network-stack-latency-trace: true in SynapseAPI config.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("summary")) {
            sendSummary(sender, "Global", manager.getGlobalSummary(), manager);
            return true;
        }

        if (args[0].equalsIgnoreCase("reset")) {
            manager.reset();
            sender.sendMessage("Latency trace stats have been reset.");
            return true;
        }

        if (args[0].equalsIgnoreCase("player")) {
            if (args.length < 2) {
                sender.sendMessage("Usage: /latencytrace player <name>");
                return true;
            }
            Player player = Server.getInstance().getPlayerExact(args[1]);
            if (player == null) {
                sender.sendMessage("Player not found: " + args[1]);
                return true;
            }
            sendSummary(sender, "Player " + player.getName(), manager.getPlayerSummary(manager.getTracePlayerId(player)), manager);
            return true;
        }

        sender.sendMessage("Usage: /latencytrace <summary|player <name>|reset>");
        return true;
    }

    private void sendSummary(CommandSender sender, String title, Summary summary, NetworkLatencyTraceManager manager) {
        sender.sendMessage(title + " latency trace samples=" + summary.sampleCount()
                + " completed=" + manager.getCompletedCount()
                + " incomplete=" + manager.getIncompleteCount());
        SegmentStats[] stats = summary.stats();
        for (int i = 0; i < NetworkLatencyTraceManager.SEGMENT_NAMES.length; i++) {
            SegmentStats stat = stats[i];
            if (stat.count() == 0) {
                sender.sendMessage(NetworkLatencyTraceManager.SEGMENT_NAMES[i] + ": no data");
                continue;
            }
            sender.sendMessage(NetworkLatencyTraceManager.SEGMENT_NAMES[i]
                    + ": count=" + stat.count()
                    + " avg=" + NetworkLatencyTraceManager.toMs(stat.avgNs()) + "ms"
                    + " p50=" + NetworkLatencyTraceManager.toMs(stat.p50Ns()) + "ms"
                    + " p95=" + NetworkLatencyTraceManager.toMs(stat.p95Ns()) + "ms"
                    + " max=" + NetworkLatencyTraceManager.toMs(stat.maxNs()) + "ms");
        }
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }
}
