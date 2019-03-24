package com.simibubi.mightyarchitect.item;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import com.simibubi.mightyarchitect.item.symmetry.SymmetryElement;
import com.simibubi.mightyarchitect.item.symmetry.SymmetryEmptySlot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.PlaceEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@EventBusSubscriber
public class SymmetryHandler {

	private static int tickCounter = 0;

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
				SymmetryElement mirror = ItemWandSymmetry.getMirror(stackInSlot);
				if (mirror instanceof SymmetryEmptySlot)
					continue;

				double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) event.getPartialTicks();
				double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) event.getPartialTicks();
				double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) event.getPartialTicks();
				BlockPos pos = new BlockPos(mirror.getPosition());

				float yShift = 0;
				double speed = 1 / 16d;
				yShift = MathHelper.sin((float) ((tickCounter + event.getPartialTicks()) * speed)) / 5f;

				BufferBuilder buffer = Tessellator.getInstance().getBuffer();
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
				GlStateManager.pushMatrix();
				GlStateManager.translate(-x, -y + yShift + .2f, -z);
				mc.getBlockRendererDispatcher().renderBlock(mirror.getModel(), pos, player.world, buffer);
				Tessellator.getInstance().draw();
				GlStateManager.popMatrix();

			}
		}

		GL11.glDisable(GL11.GL_BLEND);
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public static void onClientTick(ClientTickEvent event) {
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayerSP player = mc.player;

		if (mc.world == null)
			return;
		if (mc.isGamePaused())
			return;

		tickCounter++;

		if (tickCounter % 10 == 0) {
			for (int i = 0; i < InventoryPlayer.getHotbarSize(); i++) {
				ItemStack stackInSlot = player.inventory.getStackInSlot(i);

				SymmetryElement mirror = ItemWandSymmetry.getMirror(stackInSlot);
				if (mirror instanceof SymmetryEmptySlot)
					continue;

				if (stackInSlot != null && stackInSlot.getItem() == AllItems.wand_symmetry
						&& ItemWandSymmetry.isEnabled(stackInSlot)) {
					Random r = new Random();
					double offsetX = (r.nextDouble() - 0.5) * 0.3;
					double offsetZ = (r.nextDouble() - 0.5) * 0.3;

					Vec3d pos = mirror.getPosition().addVector(0.5 + offsetX, 1 / 4d, 0.5 + offsetZ);
					Vec3d speed = new Vec3d(0, r.nextDouble() * 1 / 8f, 0);
					mc.world.spawnParticle(EnumParticleTypes.END_ROD, false, pos.x, pos.y, pos.z, speed.x, speed.y,
							speed.z, 1);
				}
			}
		}

	}

	public static void drawEffect(BlockPos from, BlockPos to) {
		double density = 0.3f;
		Vec3d start = new Vec3d(from).addVector(0.5, 0.5, 0.5);
		Vec3d end = new Vec3d(to).addVector(0.5, 0.5, 0.5);
		Vec3d diff = end.subtract(start);

		Vec3d step = diff.normalize().scale(density);
		int steps = (int) (diff.lengthVector() / step.lengthVector());

		Random r = new Random();
		for (int i = 5; i < steps - 1; i++) {
			Vec3d pos = start.add(step.scale(i));
			Vec3d speed = new Vec3d(0, r.nextDouble() * -40f, 0);
			Minecraft.getMinecraft().world.spawnParticle(EnumParticleTypes.SPELL_WITCH, pos.x, pos.y, pos.z,
					speed.x, speed.y, speed.z);
		}
		
		Vec3d speed = new Vec3d(0, r.nextDouble() * 1 / 32f, 0);
		Vec3d pos = start.add(step.scale(5));
		Minecraft.getMinecraft().world.spawnParticle(EnumParticleTypes.END_ROD, pos.x, pos.y, pos.z,
				speed.x, speed.y, speed.z);
		
		speed = new Vec3d(0, r.nextDouble() * 1 / 32f, 0);
		pos = start.add(step.scale(steps));
		Minecraft.getMinecraft().world.spawnParticle(EnumParticleTypes.END_ROD, pos.x, pos.y, pos.z,
				speed.x, speed.y, speed.z);
	}

}
