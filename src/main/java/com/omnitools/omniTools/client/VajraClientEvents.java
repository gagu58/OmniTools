package com.omnitools.omniTools.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.omnitools.omniTools.core.ModItems;
import com.omnitools.omniTools.core.OmniVajraItem;
import com.omnitools.omniTools.network.SyncVajraMiningSpeedPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = "omnitools", value = Dist.CLIENT)
public class VajraClientEvents {
    private static final float SPEED_STEP = 10.0F;

    public static KeyMapping TOGGLE_VAJRA_MINING_SPEED_UP_KEYBIND;
    public static KeyMapping TOGGLE_VAJRA_MINING_SPEED_DOWN_KEYBIND;

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        TOGGLE_VAJRA_MINING_SPEED_UP_KEYBIND = new KeyMapping(
                "key.omnitools.vajra_toggle_mining_speed_up",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_PAGE_UP,
                "key.categories.omnitools"
        );
        event.register(TOGGLE_VAJRA_MINING_SPEED_UP_KEYBIND);

        TOGGLE_VAJRA_MINING_SPEED_DOWN_KEYBIND = new KeyMapping(
                "key.omnitools.vajra_toggle_mining_speed_down",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_PAGE_DOWN,
                "key.categories.omnitools"
        );
        event.register(TOGGLE_VAJRA_MINING_SPEED_DOWN_KEYBIND);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.screen != null) {
            return;
        }

        while (TOGGLE_VAJRA_MINING_SPEED_UP_KEYBIND != null && TOGGLE_VAJRA_MINING_SPEED_UP_KEYBIND.consumeClick()) {
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() == ModItems.OMNI_VAJRA.get()) {
                float newSpeed = OmniVajraItem.getMiningSpeed(stack) + SPEED_STEP;
                OmniVajraItem.setMiningSpeed(stack, newSpeed);
                PacketDistributor.sendToServer(new SyncVajraMiningSpeedPacket(OmniVajraItem.getMiningSpeed(stack)));
                player.displayClientMessage(Component.translatable("message.omnitools.vajra_mining_speed", OmniVajraItem.getMiningSpeed(stack)), true);
            }
        }

        while (TOGGLE_VAJRA_MINING_SPEED_DOWN_KEYBIND != null && TOGGLE_VAJRA_MINING_SPEED_DOWN_KEYBIND.consumeClick()) {
            ItemStack stack = player.getMainHandItem();
            if (stack.getItem() == ModItems.OMNI_VAJRA.get()) {
                float newSpeed = OmniVajraItem.getMiningSpeed(stack) - SPEED_STEP;
                OmniVajraItem.setMiningSpeed(stack, newSpeed);
                PacketDistributor.sendToServer(new SyncVajraMiningSpeedPacket(OmniVajraItem.getMiningSpeed(stack)));
                player.displayClientMessage(Component.translatable("message.omnitools.vajra_mining_speed", OmniVajraItem.getMiningSpeed(stack)), true);
            }
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        return;
    }
}
