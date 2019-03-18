package com.simibubi.mightyarchitect.control.phase;

import net.minecraftforge.event.world.BlockEvent;

public interface IListenForBlockEvents {

	public void onBlockPlaced(BlockEvent.PlaceEvent event);
	public void onBlockBroken(BlockEvent.BreakEvent event);
	
}
