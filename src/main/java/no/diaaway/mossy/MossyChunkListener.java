/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package no.diaaway.mossy;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldListener;

/**
 *
 * @author DiaAWAY
 */
public class MossyChunkListener extends WorldListener {

    private MossyPlugin plugin;

    public MossyChunkListener(MossyPlugin instance) {
        plugin = instance;
    }

    @Override
    public void onChunkLoaded(ChunkLoadEvent event) {
        System.out.println("MossyPlugin: ChunkLoadEvent!");
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 128; y++) {
                for (int z = 0; z < 16; z++) {
                    int id = event.getChunk().getWorld().getBlockTypeIdAt(event.getChunk().getX()+x, y, event.getChunk().getZ()+z);
                    if (id == Material.COBBLESTONE.getId())  {
                        plugin.addCobblestone(event.getWorld().getBlockAt(event.getChunk().getX()+x, y, event.getChunk().getZ()+z));
                    }
                }
            }
        }
    }
}
