package com.omnitools.omniTools.core;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.holdersets.AnyHolderSet;

public class OmniVajraItem extends Item {
    private static final String MINING_SPEED_TAG = "VajraMiningSpeed";
    private static final float MIN_MINING_SPEED = 1.0F;
    private static final float MAX_MINING_SPEED = 1000.0F;

    public OmniVajraItem(Properties properties) {
        super(properties.component(DataComponents.TOOL, createToolProperties()));
    }

    private static Tool createToolProperties() {
        return new Tool(List.of(
                Tool.Rule.deniesDrops(Tiers.NETHERITE.getIncorrectBlocksForDrops()),
                new Tool.Rule(new AnyHolderSet<>(BuiltInRegistries.BLOCK.asLookup()), Optional.empty(), Optional.of(true))
        ), Tiers.NETHERITE.getSpeed(), 0);
    }

    public static float getMiningSpeed(ItemStack stack) {
        if (stack.isEmpty()) {
            return Tiers.NETHERITE.getSpeed();
        }
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains(MINING_SPEED_TAG)) {
                return tag.getFloat(MINING_SPEED_TAG);
            }
        }
        return Tiers.NETHERITE.getSpeed();
    }

    public static void setMiningSpeed(ItemStack stack, float speed) {
        if (stack.isEmpty()) {
            return;
        }
        float clamped = Math.max(MIN_MINING_SPEED, Math.min(MAX_MINING_SPEED, speed));
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        tag.putFloat(MINING_SPEED_TAG, clamped);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return getMiningSpeed(stack);
    }
}
