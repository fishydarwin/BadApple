package me.darwj.badapple;

import me.darwj.badapple.commands.BadAppleCommand;
import me.darwj.badapple.objects.XYPair;
import org.bukkit.plugin.java.JavaPlugin;
import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.common.io.IOUtils;
import org.jcodec.scale.AWTUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public final class BadApple extends JavaPlugin {

    private static File badAppleFile;
    public static final int BAD_APPLE_FRAME_COUNT = 13144; // magic number

    private static final List<List<XYPair>> frameData = new ArrayList<>();
    public static List<List<XYPair>> getFrameData() {
        return frameData;
    }

    public static BufferedImage getBadAppleFrame(int frame) throws JCodecException, IOException {
        return AWTUtil.toBufferedImage(FrameGrab.getFrameFromFile(badAppleFile, frame));
    }

    private static BadApple instance;
    public static BadApple getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {

        instance = this;

        badAppleFile = new File(getDataFolder(), "bad_apple.mp4");
        // Store BadApple
        if (!badAppleFile.exists()) {
            if (!badAppleFile.getParentFile().exists()) {
                badAppleFile.getParentFile().mkdirs();
            }
            InputStream badAppleInputStream = getClass().getClassLoader().getResourceAsStream("bad_apple.mp4");
            assert badAppleInputStream != null;
            //
            try {
                java.nio.file.Files.copy(
                        badAppleInputStream,
                        badAppleFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            //
            IOUtils.closeQuietly(badAppleInputStream);
        }

        // Load in memory!
        //TODO: this is bad, the server can't start til this is ready lol
        //TODO: could maybe load sequentially from a custom data file?
        getLogger().info("[RENDER] Parsing Bad Apple into memory...");
        for (int f = 0; f < BAD_APPLE_FRAME_COUNT; f++) {
            BufferedImage frame = null;
            try {
                frame = BadApple.getBadAppleFrame(f);
            } catch (JCodecException | IOException e) {
                throw new RuntimeException(e);
            }
            List<XYPair> points = new ArrayList<>();
            for (int x = 0; x < 120; x += 2) {
                for (int y = 0; y < 90; y += 2) {
                    // dark or light pixel?
                    java.awt.Color color = new java.awt.Color(frame.getRGB(x, y));
                    if (color.getRed() + color.getGreen() + color.getBlue() > 381) { // if light, store
                        points.add(new XYPair(x, y));
                    }
                }
            }
            frameData.add(points);
            frame.flush();
            getLogger().info("[RENDER] " + f + "/" + BAD_APPLE_FRAME_COUNT);
        }

        // Create /badapple command
        getCommand("badapple").setExecutor(new BadAppleCommand());

    }

}
