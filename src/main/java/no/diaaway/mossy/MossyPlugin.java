package no.diaaway.mossy;

import com.nijikokun.bukkit.iProperty;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * MossyPlugin - A plugin that grows moss!
 * @author DiaAWAY
 */
public class MossyPlugin extends JavaPlugin {

    public iProperty mossing = new iProperty(this.getClass().getSimpleName() + ".properties");
    private MossyBlockListener blockListener = new MossyBlockListener(this);
    private MossyChunkListener chunkListener = new MossyChunkListener(this);
    //private ArrayList<Block> cobblestone = new ArrayList<Block>();
    private ConcurrentHashMap cobblestones = new ConcurrentHashMap();
    private int growTimerId;
    private String growrate = "growrate";
    private String growdistance = "growdistance";
    private String growthreshold = "growagethreshold";

    public void onDisable() {
        getServer().getScheduler().cancelTask(growTimerId);
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " says goodbye!");
    }

    public void onEnable() {
        // LISTEN FOR EVENTS
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvent(Type.BLOCK_BREAK, blockListener, Priority.Low, this);
        pm.registerEvent(Type.BLOCK_PLACED, blockListener, Priority.Low, this);
        pm.registerEvent(Type.CHUNK_LOADED, chunkListener, Priority.Low, this);

        // SET DEFAULT VALUES ON FIRST RUN
        if (!mossing.keyExists(growrate)) {
            mossing.setLong(growrate, 1000L);
        }
        if (!mossing.keyExists(growdistance)) {
            mossing.setInt(growdistance, 1);
        }
        if (!mossing.keyExists(growthreshold)) {
            mossing.setLong(growthreshold, 100);
        }

        // LOAD ALL COBBLESTONE OBJECTS FROM THE WORLD INTO THE ARRAY
        getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {

            public void run() {
                int cobbles = 0;
                for (int w = 0; w < getServer().getWorlds().size(); w++) {
                    Chunk[] chunks = getServer().getWorlds().get(w).getLoadedChunks();
                    System.out.println("loaded chunks: " + chunks.length);
                    for (int i = 0; i < chunks.length; i++) {
                        for (int x = 0; x < 16; x++) {
                            for (int y = 0; y < 128; y++) {
                                for (int z = 0; z < 16; z++) {
                                    if (getServer().getWorlds().get(w).getBlockTypeIdAt(chunks[i].getX() + x, y, chunks[i].getZ() + z) != 0) {
                                        addCobblestone(getServer().getWorlds().get(w).getBlockAt(x, y, z));
                                        cobbles += 1;
                                        System.out.println("adding cobblestone");
                                    }
                                }
                            }
                        }
                    }
                    System.out.println(cobbles + " cobblestone(s) loaded");
                }
            }
        }, 0L);

        // START THE TIMER
        // todo: add measure to ensure that the period isn't dangerously small
        growTimerId = getServer().getScheduler().scheduleAsyncRepeatingTask(
                this,
                new Runnable() { // 

                    public void run() {
                        growMoss();
                    }
                },
                0L, // start delay
                mossing.getLong(growrate)); // period

        // Notify admin that all went well
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        // RELOAD PROPERTIES FILE
            /*
        getServer().getScheduler().cancelTask(growTimerId);
         */
        return false;
    }

    protected synchronized Object addCobblestone(Block block) {
        System.out.println(this.getDescription().getName() + ": Adding block");
        return cobblestones.putIfAbsent(block, 0L);
    }

    protected synchronized Object removeCobblestone(Block block) {
        System.out.println(this.getDescription().getName() + ": Removing block");
        return cobblestones.remove(block);
    }

    /**
     * Finds a suitable cobblestone block and grows moss on it.
     */
    protected synchronized void growMoss() {
        System.out.println(this.getDescription().getName() + ": Growing moss");
        // loop through the cobblestones
        Iterator i = cobblestones.keySet().iterator();
        Block block;

        while (i.hasNext()) {
            block = (Block) i.next();
            System.out.println("water:" + isNear(block, Material.WATER) + " moss:" + isNear(block, Material.MOSSY_COBBLESTONE));
            if (isNear(block, Material.WATER) || isNear(block, Material.MOSSY_COBBLESTONE)) {
                long age = (Long) cobblestones.get(block);
                if (age > mossing.getLong(growthreshold)) {
                    java.util.Random r = new java.util.Random();
                    if (r.nextBoolean() && r.nextBoolean() && r.nextBoolean()) { // randumb chance (0.5*0.5*0.5=0,125 => 12.5 % growth chance
                        block.getWorld().getBlockAt(block.getLocation()).setType(Material.MOSSY_COBBLESTONE);
                        removeCobblestone(block);
                        System.out.println("block was changed");
                    } else {
                        System.out.println("threshold met, but moss didn't grow");
                    }
                } else {
                    System.out.println("aging block");
                    cobblestones.replace(block, age, age + 1);
                }
            }
        }
    }

    private boolean isNear(Block block, Material material) {
        System.out.println(block.getFace(BlockFace.NORTH).getType().toString());
        System.out.println(block.getFace(BlockFace.SOUTH).getType().toString());
        System.out.println(block.getFace(BlockFace.EAST).getType().toString());
        System.out.println(block.getFace(BlockFace.WEST).getType().toString());
        System.out.println(block.getFace(BlockFace.UP).getType().toString());
        if (block.getFace(BlockFace.NORTH).getType().equals(material)) {
            return true;
        }
        if (block.getFace(BlockFace.SOUTH).getType().equals(material)) {
            return true;
        }
        if (block.getFace(BlockFace.EAST).getType().equals(material)) {
            return true;
        }
        if (block.getFace(BlockFace.WEST).getType().equals(material)) {
            return true;
        }
        if (block.getFace(BlockFace.UP).getType().equals(material)) {
            return true;
        }
        if (block.getFace(BlockFace.DOWN).getType().equals(material)) {
            return true;
        }
        return false;
    }
}
