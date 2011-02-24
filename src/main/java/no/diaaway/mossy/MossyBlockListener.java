/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package no.diaaway.mossy;

import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 *
 * @author DiaAWAY
 */
public class MossyBlockListener extends BlockListener {

    MossyPlugin plugin;

    public MossyBlockListener(MossyPlugin instance) {
        plugin = instance;
    }

    @Override
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType().equals(Material.COBBLESTONE)) {
            plugin.removeCobblestone(event.getBlock());
        }
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType().equals(Material.COBBLESTONE)) {
            plugin.addCobblestone(event.getBlock());
        }
    }
}
