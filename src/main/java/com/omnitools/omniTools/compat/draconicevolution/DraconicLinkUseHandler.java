package com.omnitools.omniTools.compat.draconicevolution;

import com.brandon3055.draconicevolution.api.energy.ICrystalBinder;
import com.omnitools.omniTools.api.IUseHandler;
import com.omnitools.omniTools.api.UseContext;
import com.omnitools.omniTools.core.ToolMode;
import com.brandon3055.draconicevolution.init.ItemData;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;

/**
 * 潜行对空气右键清除绑定（仿原版 Crystal Binder）。
 */
public class DraconicLinkUseHandler implements IUseHandler {
    @Override
    public boolean canHandle(UseContext context) {
        if (context.getCurrentMode() != ToolMode.LINK) {
            return false;
        }
        var player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) {
            return false;
        }
        ItemStack stack = context.getStack();
        return stack.has(ItemData.BINDER_POS);
    }

    @Override
    public InteractionResultHolder<ItemStack> handle(UseContext context) {
        ItemStack stack = context.getStack();
        if (stack.has(ItemData.BINDER_POS)) {
            stack.remove(ItemData.BINDER_POS);
        }
        return InteractionResultHolder.success(stack);
    }
}
