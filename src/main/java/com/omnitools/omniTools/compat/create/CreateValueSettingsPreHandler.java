package com.omnitools.omniTools.compat.create;

import com.omnitools.omniTools.core.ModItems;
import com.omnitools.omniTools.core.OmniToolItem;
import com.omnitools.omniTools.core.ToolMode;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsInputHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * 让 omni 扳手在扳手模式下，优先走 Create 的 ValueSettingsInputHandler，
 * 这样当点击到可调节的 ValueBox 时，会先打开数值设置界面并取消事件，
 * 后续的 WrenchEventHandler 就不会再去 onWrenched 旋转方块了。
 */
@EventBusSubscriber(modid = "omnitools", bus = EventBusSubscriber.Bus.GAME)
public class CreateValueSettingsPreHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player == null) {
            return;
        }

        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || stack.getItem() != ModItems.OMNI_WRENCH.get()) {
            return;
        }

        // 只在普通扳手模式下启用 Create 的调节行为优先级
        if (OmniToolItem.getMode(stack) != ToolMode.WRENCH) {
            return;
        }

        if (event.isCanceled()) {
            return;
        }

        // 直接复用 Create 原版的判定逻辑
        ValueSettingsInputHandler.onBlockActivated(event);
        // 如果点击到了 ScrollValueBehaviour，onBlockActivated 会自己 cancel 事件并启动 GUI，
        // 后续的 Create WrenchEventHandler 会因为 event.isCanceled() 而什么也不做。
    }
}
