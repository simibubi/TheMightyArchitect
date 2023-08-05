package com.simibubi.mightyarchitect.foundation;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.BufferBuilder;

import net.minecraft.client.renderer.MultiBufferSource.BufferSource;

public class BufferSourceAccess extends BufferSource {

	public BufferSourceAccess(BufferBuilder pBuilder) {
		super(pBuilder, ImmutableMap.of());
	}
	
}
