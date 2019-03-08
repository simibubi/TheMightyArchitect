package com.simibubi.mightyarchitect.symmetry;

import org.lwjgl.opengl.GL11;

import com.simibubi.mightyarchitect.block.AllBlocks;
import com.simibubi.mightyarchitect.item.AllItems;
import com.simibubi.mightyarchitect.item.ItemWandSymmetry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@EventBusSubscriber
public class SymmetryElementEventHandler {
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onBlockPlaced(PlaceEvent event) {
		if (!event.getWorld().isRemote) {
			EntityPlayer player = event.getPlayer();
			InventoryPlayer inv = player.inventory;
			for (int i = 0; i < InventoryPlayer.getHotbarSize(); i++) {
				if (!inv.getStackInSlot(i).isEmpty() && inv.getStackInSlot(i).getItem() == AllItems.wand_symmetry) {
					ItemWandSymmetry.apply(event.getWorld(), inv.getStackInSlot(i), player, event.getPos(),
							event.getPlacedBlock());
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onBlockDestroyed(BreakEvent event) {
		if (!event.getWorld().isRemote) {
			EntityPlayer player = event.getPlayer();
			InventoryPlayer inv = player.inventory;
			for (int i = 0; i < InventoryPlayer.getHotbarSize(); i++) {
				if (!inv.getStackInSlot(i).isEmpty() && inv.getStackInSlot(i).getItem() == AllItems.wand_symmetry) {
					ItemWandSymmetry.remove(event.getWorld(), inv.getStackInSlot(i), player, event.getPos());
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void render(RenderWorldLastEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayerSP player = mc.player;

		GL11.glEnable(GL11.GL_BLEND);

		for (int i = 0; i < InventoryPlayer.getHotbarSize(); i++) {
			ItemStack stackInSlot = player.inventory.getStackInSlot(i);
			if (stackInSlot != null && stackInSlot.getItem() == AllItems.wand_symmetry
					&& ItemWandSymmetry.isEnabled(stackInSlot)) {
				for (SymmetryElement mirror : ItemWandSymmetry.getMirrors(stackInSlot)) {
					if (mirror instanceof SymmetryEmptySlot)
						continue;

					double x = player.lastTickPosX
							+ (player.posX - player.lastTickPosX) * (double) event.getPartialTicks();
					double y = player.lastTickPosY
							+ (player.posY - player.lastTickPosY) * (double) event.getPartialTicks();
					double z = player.lastTickPosZ
							+ (player.posZ - player.lastTickPosZ) * (double) event.getPartialTicks();
					BlockPos pos = new BlockPos(mirror.getPosition());

					BufferBuilder buffer = Tessellator.getInstance().getBuffer();
					buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
					GlStateManager.pushMatrix();
					GlStateManager.translate(-x, -y, -z);
					mc.getBlockRendererDispatcher().renderBlock(mirror.getModel(), pos,
							player.world, buffer);
					Tessellator.getInstance().draw();
					GlStateManager.popMatrix();

				}
			}
		}
		
		GL11.glDisable(GL11.GL_BLEND);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void blockHighlightEvent(DrawBlockHighlightEvent event) {
		if (event.getPlayer().getHeldItemMainhand() != null
				&& event.getPlayer().getHeldItemMainhand().getItem() instanceof ItemWandSymmetry
				&& event.getTarget() != null && event.getTarget().typeOfHit == Type.BLOCK
				&& event.getPlayer().isSneaking()) {
			EntityPlayer player = event.getPlayer();

			GlStateManager.disableTexture2D();
			GlStateManager.enableAlpha();
			GlStateManager.enableDepth();
			GlStateManager.color(1f, 1f, 1f, .5f);

			double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) event.getPartialTicks();
			double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) event.getPartialTicks();
			double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) event.getPartialTicks();
			BlockPos pos = event.getTarget().getBlockPos().offset(event.getTarget().sideHit);

			BufferBuilder buffer = Tessellator.getInstance().getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
			GlStateManager.pushMatrix();
			GlStateManager.translate(-x, -y, -z);
			Minecraft.getMinecraft().getBlockRendererDispatcher()
					.renderBlock(AllBlocks.symmetry_point.getDefaultState(), pos, player.world, buffer);
			Tessellator.getInstance().draw();
			GlStateManager.popMatrix();

			GlStateManager.enableTexture2D();
			GlStateManager.disableAlpha();
			GlStateManager.disableBlend();
			GlStateManager.resetColor();
		}
	}

}
