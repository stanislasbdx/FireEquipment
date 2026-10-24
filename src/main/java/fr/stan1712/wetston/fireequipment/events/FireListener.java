package fr.stan1712.wetston.fireequipment.events;

import fr.stan1712.wetston.fireequipment.fire.AshManager;
import fr.stan1712.wetston.fireequipment.fire.BlockKey;
import fr.stan1712.wetston.fireequipment.fire.BurningBlock;
import fr.stan1712.wetston.fireequipment.fire.FireEnvironment;
import fr.stan1712.wetston.fireequipment.fire.FireManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.Bukkit;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Gate;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.FallingBlock;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.Iterator;
import java.util.List;

public class FireListener implements Listener {
	private final Plugin plugin;
	private final FireManager fire;
	private final AshManager ash;

	public FireListener(Plugin plugin, FireManager fire) {
		this.plugin = plugin;
		this.fire = fire;
		this.ash = fire.getAsh();
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
		if(!fire.isDispatching() && fire.isWorldEnabled(event.getBlock().getWorld())) {
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

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onAshBreak(BlockBreakEvent event) {
		if(ash.isAsh(event.getBlock())) {
			event.setDropItems(false);
			event.setExpToDrop(0);
			ash.unmark(event.getBlock());
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onAshForm(BlockFormEvent event) {
		if(ash.isAsh(event.getBlock())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onFallingBlock(EntityChangeBlockEvent event) {
		if(!(event.getEntity() instanceof FallingBlock fallingBlock)) {
			return;
		}

		final Block block = event.getBlock();
		if(event.getTo() == Material.AIR) {
			if(ash.isAsh(block)) {
				ash.unmark(block);
				ash.tag(fallingBlock);
			}
		}
		else if(ash.isTagged(fallingBlock)) {
			ash.mark(block);
		}
		else {
			fire.getCollapse().land(fallingBlock, block);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent event) {
		clearExplodedAsh(event.blockList());
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onBlockExplode(BlockExplodeEvent event) {
		clearExplodedAsh(event.blockList());
	}

	@EventHandler(ignoreCancelled = true)
	public void onPistonExtend(BlockPistonExtendEvent event) {
		if(event.getBlocks().stream().anyMatch(ash::isAsh)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPistonRetract(BlockPistonRetractEvent event) {
		if(event.getBlocks().stream().anyMatch(ash::isAsh)) {
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

	@EventHandler(priority = EventPriority.MONITOR)
	public void onOpen(PlayerInteractEvent event) {
		final Block block = event.getClickedBlock();
		if(event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getHand() != EquipmentSlot.HAND || block == null
				|| !(block.getBlockData() instanceof Openable openable) || openable.isOpen() || !fire.isWorldEnabled(block.getWorld())
				|| !(openable instanceof Door || openable instanceof TrapDoor || openable instanceof Gate)) {
			return;
		}

		Bukkit.getScheduler().runTask(plugin, () -> {
			if(block.getBlockData() instanceof Openable opened && opened.isOpen()) {
				fire.getBackdraft().onOpened(block);
			}
		});
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBreach(BlockBreakEvent event) {
		final Block block = event.getBlock();
		if(!fire.isWorldEnabled(block.getWorld())) {
			return;
		}

		Bukkit.getScheduler().runTask(plugin, () -> {
			if(FireEnvironment.isTraversable(block)) {
				fire.getBackdraft().onOpened(block);
			}
		});
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlace(BlockPlaceEvent event) {
		fire.tryAutoIgnite(event.getBlockPlaced());
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

	private void clearExplodedAsh(List<Block> blocks) {
		final Iterator<Block> iterator = blocks.iterator();

		while(iterator.hasNext()) {
			final Block block = iterator.next();
			if(ash.isAsh(block)) {
				iterator.remove();
				ash.unmark(block);
				block.setType(Material.AIR);
			}
		}
	}
}
