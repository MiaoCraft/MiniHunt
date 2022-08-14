package net.mcxk.hjyhunt.replay;

import me.jumper251.replay.ReplaySystem;
import me.jumper251.replay.filesystem.saving.DefaultReplaySaver;
import me.jumper251.replay.filesystem.saving.IReplaySaver;
import me.jumper251.replay.replaysystem.Replay;
import me.jumper251.replay.replaysystem.data.ReplayData;
import me.jumper251.replay.utils.LogUtils;
import me.jumper251.replay.utils.fetcher.Acceptor;
import me.jumper251.replay.utils.fetcher.Consumer;
import net.mcxk.hjyhunt.HJYHunt;
import net.mcxk.hjyhunt.game.Game;
import org.bukkit.Bukkit;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class MHRecordSaver implements IReplaySaver {
    public static File DIR;
    String fileSuffix = ".replay";
    private boolean reformatting;

    public MHRecordSaver(Game game) {
        SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        DIR = new File(ReplaySystem.getInstance().getDataFolder() + "/replays/" + time.format(new Date()));
    }

    @Override
    public void saveReplay(Replay replay) {
        if (!DIR.exists()) {
            DIR.mkdirs();
        }

        File file = new File(DIR, replay.getId() + fileSuffix);

        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream fileOut = new FileOutputStream(file);
            GZIPOutputStream gOut = new GZIPOutputStream(fileOut);
            ObjectOutputStream objectOut = new ObjectOutputStream(gOut);
            objectOut.writeObject(replay.getData());
            objectOut.flush();
            gOut.close();
            fileOut.close();
            objectOut.close();
        } catch (Exception var6) {
            var6.printStackTrace();
        }

    }

    @Override
    public void loadReplay(final String replayName, Consumer<Replay> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(HJYHunt.getInstance(), new Acceptor<Replay>(consumer) {
            @Override
            public Replay getValue() {
                try {
                    File file = new File(DefaultReplaySaver.DIR, replayName + fileSuffix);
                    FileInputStream fileIn = new FileInputStream(file);
                    GZIPInputStream gIn = new GZIPInputStream(fileIn);
                    ObjectInputStream objectIn = new ObjectInputStream(gIn);
                    ReplayData data = (ReplayData) objectIn.readObject();
                    objectIn.close();
                    gIn.close();
                    fileIn.close();
                    return new Replay(replayName, data);
                } catch (Exception var6) {
                    if (!reformatting) {
                        var6.printStackTrace();
                    }

                    return null;
                }
            }
        });
    }

    @Override
    public boolean replayExists(String replayName) {
        File file = new File(DIR, replayName + fileSuffix);
        return file.exists();
    }

    @Override
    public void deleteReplay(String replayName) {
        File file = new File(DIR, replayName + fileSuffix);
        if (file.exists()) {
            file.delete();
        }

    }

    public void reformatAll() {
        this.reformatting = true;
        if (DIR.exists()) {
            Arrays.stream(Objects.requireNonNull(DIR.listFiles())).filter((file) -> file.isFile() && file.getName().endsWith(".replay")).map(File::getName).collect(Collectors.toList()).forEach((file) -> {
                this.reformat(file.replace("\\.replay", ""));
            });
        }

        this.reformatting = false;
    }

    private void reformat(final String replayName) {
        this.loadReplay(replayName, old -> {
            if (old == null) {
                LogUtils.log("Reformatting: " + replayName);
                try {
                    File file = new File(DefaultReplaySaver.DIR, replayName + fileSuffix);
                    FileInputStream fileIn = new FileInputStream(file);
                    ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                    ReplayData data = (ReplayData) objectIn.readObject();
                    objectIn.close();
                    fileIn.close();
                    deleteReplay(replayName);
                    saveReplay(new Replay(replayName, data));
                } catch (Exception var6) {
                    var6.printStackTrace();
                }
            }

        });
    }

    @Override
    public List<String> getReplays() {
        List<String> files = new ArrayList<>();
        if (DIR.exists()) {

            for (File file : Objects.requireNonNull(DIR.listFiles())) {
                if (file.isFile() && file.getName().endsWith(fileSuffix)) {
                    files.add(file.getName().replace("\\.replay", ""));
                }
            }
        }
        return files;
    }
}
