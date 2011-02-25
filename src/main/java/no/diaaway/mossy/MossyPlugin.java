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
        // TODO: find out why no cobblestones are found D:
        getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {

            public void run() {
                for (int w = 0; w < getServer().getWorlds().size(); w++) {
                    Chunk[] chunks = getServer().getWorlds().get(w).getLoadedChunks();
                    for (int i = 0; i < chunks.length; i++) {
                        for (int x = 0; x < 16; x++) {
                            for (int y = 0; y < 128; y++) {
                                for (int z = 0; z < 16; z++) {
                                    if (getServer().getWorlds().get(w).getBlockTypeIdAt(chunks[i].getX() + x, y, chunks[i].getZ() + z)
                                            == Material.COBBLESTONE.getId()) {
                                        addCobblestone(getServer().getWorlds().get(w).getBlockAt(x, y, z));
                                    }
                                }
                            }
                        }
                    }
                    System.out.println(cobblestones.size() + " cobblestone(s) loaded");
                }
            }
        }, 0L);

        // START THE TIMER
        // todo: add measure to ensure that the period isn't dangerously small
        growTimerId = getServer().getScheduler().scheduleAsyncRepeatingTask(
                this,
                new Runnable() { // 

                    public void run() {
                        if (cobblestones.size() > 0) { // dont need to work on empty list
                            growMoss();
                        }
                    }
                },
                0L, // start delay
                mossing.getLong(growrate)); // period

        // Notify admin that all went well
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
        System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is looking for cobblestones!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        return false;
    }

    protected Object addCobblestone(Block block) {
        System.out.println(this.getDescription().getName() + ": Adding block");
        return cobblestones.putIfAbsent(block, 0L);
    }

    protected Object removeCobblestone(Block block) {
        if (cobblestones.containsKey(block)) {
            System.out.println(this.getDescription().getName() + ": Removing block");
        }
        return cobblestones.remove(block);
    }

    /**
     * Finds a suitable cobblestone block and grows moss on it.
     */
    protected void growMoss() {
        // loop through the cobblestones
        Iterator i = cobblestones.keySet().iterator();
        Block block;

        while (i.hasNext()) {
            block = (Block) i.next();
            if (isNear(block, Material.WATER)
                    || isNear(block, Material.STATIONARY_WATER)
                    || isNear(block, Material.MOSSY_COBBLESTONE)) {
                long age = (Long) cobblestones.get(block);
                if (age > mossing.getLong(growthreshold)) {
                    java.util.Random r = new java.util.Random(); // find a better way to do this when icba
                    if (r.nextBoolean() && r.nextBoolean() && r.nextBoolean()) { // randumb chance (0.5*0.5*0.5=0,125 => 12.5 % growth chance
                        block.getWorld().getBlockAt(block.getLocation()).setType(Material.MOSSY_COBBLESTONE);
                        removeCobblestone(block);
                    } else {
                    }
                } else {
                    cobblestones.replace(block, age, age + 1);
                }
            }
        }
    }

    /**
     * checks if any blocks adjacent to the block is of the material provided
     * This method assumes that adjacent means blocks that in these cords:
     * x=x-1, y=y-1; z=z-1 to x=x+1, y=y+1; z=z+1, i.e. a cube with the block
     * in the middle.
     *
     * @param block
     * @param material
     * @return
     */
    protected boolean isNear(Block block, Material material) {
        // TODO rewrite isNear and isNearHelper
        boolean isNear = (isNearHelper(block, material)
                || isNearHelper(block.getFace(BlockFace.UP), material)
                || isNearHelper(block.getFace(BlockFace.DOWN), material));
        return isNear;
    }

    private boolean isNearHelper(Block block, Material material) {
        boolean isNear = false;
        // todo: rewrite this method so that it isn't shitty coded

        // if self is of material type, self is near material
        if (!isNear && block.getFace(BlockFace.SELF).getType().equals(material)) {
            isNear = true;
        }

        // check adjacent blocks, horizontal plane
        if (!isNear && block.getFace(BlockFace.NORTH).getType().equals(material)) {
            isNear = true;
        }
        if (!isNear && block.getFace(BlockFace.NORTH_EAST).getType().equals(material)) {
            isNear = true;
        }
        if (!isNear && block.getFace(BlockFace.NORTH_WEST).getType().equals(material)) {
            isNear = true;
        }
        if (!isNear && block.getFace(BlockFace.SOUTH).getType().equals(material)) {
            isNear = true;
        }
        if (!isNear && block.getFace(BlockFace.SOUTH_EAST).getType().equals(material)) {
            isNear = true;
        }
        if (!isNear && block.getFace(BlockFace.SOUTH_WEST).getType().equals(material)) {
            isNear = true;
        }
        if (!isNear && block.getFace(BlockFace.EAST).getType().equals(material)) {
            isNear = true;
        }
        if (!isNear && block.getFace(BlockFace.WEST).getType().equals(material)) {
            isNear = true;
        }
        return isNear;
    }
}
