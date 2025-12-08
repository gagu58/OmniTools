package com.omnitools.omniTools.core;

import com.omnitools.omniTools.api.IUseHandler;
import com.omnitools.omniTools.api.UseContext;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class UseHandlerRegistry {
    private static final List<IUseHandler> HANDLERS = new ArrayList<>();

    public static void register(IUseHandler handler) {
        HANDLERS.add(handler);
    }

    public static InteractionResultHolder<ItemStack> handle(UseContext context) {
        for (IUseHandler handler : HANDLERS) {
            if (!handler.canHandle(context)) {
                continue;
            }
            InteractionResultHolder<ItemStack> result = handler.handle(context);
            if (result != null && result.getResult().consumesAction()) {
                return result;
            }
        }
        return InteractionResultHolder.pass(context.getStack());
    }
}
