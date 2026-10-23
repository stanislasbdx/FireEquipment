package fr.stan1712.wetston.fireequipment.events;

import fr.stan1712.wetston.fireequipment.fire.BlockKey;
import fr.stan1712.wetston.fireequipment.fire.BurningBlock;
import fr.stan1712.wetston.fireequipment.fire.FireManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class FireListener implements Listener {
	private final FireManager fire;

	public FireListener(FireManager fire) {
		this.fire = fire;
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onIgnite(BlockIgniteEvent event) {
		final Block block = event.getBlock();
		if(!fire.isWorldEnabled(block.getWorld())) {
			return;
		}

		if(event.getCause() == BlockIgniteEvent.IgniteCause.SPREAD) {
			event.setCancelled(true);
			return;
		}

		fire.igniteAround(block);
	}

	@EventHandler(ignoreCancelled = true)
	public void onBurn(BlockBurnEvent event) {
		if(fire.isWorldEnabled(event.getBlock().getWorld())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onSpread(BlockSpreadEvent event) {
		if(event.getSource().getType() == Material.FIRE && fire.isWorldEnabled(event.getBlock().getWorld())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onFade(BlockFadeEvent event) {
		final Block block = event.getBlock();
		if(block.getType() == Material.FIRE && fire.isWorldEnabled(block.getWorld()) && fire.supportsFlame(block)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBreak(BlockBreakEvent event) {
		final BurningBlock burningBlock = fire.get(BlockKey.of(event.getBlock()));
		if(burningBlock != null) {
			fire.extinguish(burningBlock);
		}
	}

	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event) {
		if(fire.isWorldEnabled(event.getWorld())) {
			fire.loadChunk(event.getChunk());
		}
	}

	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent event) {
		fire.unloadChunk(event.getChunk());
	}
}
