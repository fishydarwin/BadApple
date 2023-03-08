package me.darwj.badapple.commands;

import me.darwj.badapple.BadApple;
import me.darwj.badapple.objects.XYPair;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jcodec.api.JCodecException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class BadAppleCommand implements CommandExecutor {

    private static final Map<Player, Integer> frameCount = new HashMap<>();
    private static final Map<Player, BukkitTask> tasks = new HashMap<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Please run this command as a player.");
            return true;
        }

        Player player = (Player) sender;

        if (frameCount.containsKey(player)) {
            sender.sendMessage(ChatColor.RED + "Please wait for Bad Apple to finish on your screen.");
            return true;
        }

        // turn player around...
        Location turnLocation = player.getLocation();
        turnLocation.setPitch(0);
        turnLocation.setYaw(0);
        player.teleport(turnLocation);

        // reset frame counter
        frameCount.put(player, 0);

        // play async
        Bukkit.getScheduler().runTaskTimerAsynchronously(BadApple.getInstance(), () -> {
            if (frameCount.get(player) >= BadApple.BAD_APPLE_FRAME_COUNT) {
                cancelTask(player);
                return;
            }
            try {
                playBadApple(player);
            } catch (IOException | JCodecException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, 0, 1);

        return true;
    }

    private static void cancelTask(Player player) {
        tasks.get(player).cancel();
        tasks.remove(player);
    }

    private static final float SPACING = 0.125f;
    private void playBadApple(Player player)
            throws IOException, JCodecException, InterruptedException {

        Particle.DustOptions dustOptions = new Particle.DustOptions(
                Color.fromRGB(255, 255, 255), 1.0F);

        // define target width/height
        final int w = 120;
        final int h = 90;

        // define offset
        final Location offset = player.getLocation()
                .add(new Vector((w / 2f) * SPACING, (h / 2f) * SPACING, 16));

        if (!player.isOnline()) { // stop if quits
            cancelTask(player);
            return;
        }

        int f = frameCount.get(player);

        // render frame, 120p
        for (XYPair point : BadApple.getFrameData().get(f)) {
            player.spawnParticle(Particle.REDSTONE, offset.clone()
                    .subtract(new Vector(point.x * SPACING, point.y * SPACING, 0)), 1, dustOptions);
        }

        frameCount.put(player, ++f);
    }

}
