package com.omnitools.omniTools.client;

import com.omnitools.omniTools.core.ModItems;
import com.omnitools.omniTools.core.OmniVajraItem;
import com.omnitools.omniTools.network.SyncVajraMiningSpeedPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = "omnitools", value = Dist.CLIENT)
public class VajraClientEvents {
    private static final float SPEED_STEP = 10.0F;

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (event.getScrollDeltaY() == 0) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.screen != null) {
            return;
        }

        if (!player.isShiftKeyDown()) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() != ModItems.OMNI_VAJRA.get()) {
            return;
        }

        float oldSpeed = OmniVajraItem.getMiningSpeed(stack);
        float delta = event.getScrollDeltaY() > 0 ? SPEED_STEP : -SPEED_STEP;
        float newSpeed = oldSpeed + delta;

        OmniVajraItem.setMiningSpeed(stack, newSpeed);
        PacketDistributor.sendToServer(new SyncVajraMiningSpeedPacket(OmniVajraItem.getMiningSpeed(stack)));

        player.displayClientMessage(Component.translatable("message.omnitools.vajra_mining_speed", OmniVajraItem.getMiningSpeed(stack)), true);
        event.setCanceled(true);
    }
}
