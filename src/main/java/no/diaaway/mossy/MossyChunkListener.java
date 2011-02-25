/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package no.diaaway.mossy;

import org.bukkit.Chunk;
import org.bukkit.Material;
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
        int id;
        Chunk chunk = event.getChunk();
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 128; y++) {
                for (int z = 0; z < 16; z++) {
                    id = chunk.getWorld().getBlockTypeIdAt(chunk.getX()+x, y, chunk.getZ()+z);
                    if (id == Material.COBBLESTONE.getId())  {
                        plugin.addCobblestone(event.getWorld().getBlockAt(chunk.getX()+x, y, chunk.getZ()+z));
                    }
                }
            }
        }
    }
}
