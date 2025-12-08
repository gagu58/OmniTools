package com.omnitools.omniTools.core;

import com.omnitools.omniTools.api.UseContext;
import com.omnitools.omniTools.api.WrenchContext;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class OmniToolItem extends Item {
    public OmniToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player != null ? player.getItemInHand(hand) : ItemStack.EMPTY;
        if (player != null) {
            UseContext useContext = new UseContext(level, player, hand, stack);
            InteractionResultHolder<ItemStack> result = UseHandlerRegistry.handle(useContext);
            if (result.getResult().consumesAction()) {
                return result;
            }
        }
        return super.use(level, player, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        var player = context.getPlayer();
        var pos = context.getClickedPos();
        var face = context.getClickedFace();

        if (player != null) {
            WrenchContext wrenchContext = new WrenchContext(
                    level,
                    pos,
                    face,
                    player,
                    context.getItemInHand()
            );

            InteractionResult result = WrenchHandlerRegistry.handle(wrenchContext);
            if (result.consumesAction()) {
                return result;
            }
        }

        return super.useOn(context);
    }
}
