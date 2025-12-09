package com.omnitools.omniTools.mixin.ae2;

import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.implementations.menuobjects.ItemMenuHost;
import appeng.menu.locator.ItemMenuHostLocator;
import com.omnitools.omniTools.core.OmniToolItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(OmniToolItem.class)
public abstract class OmniToolItemAEMenuItemMixin implements IMenuItem {

    public ItemMenuHost<?> getMenuHost(Player player, ItemMenuHostLocator locator,
            BlockHitResult hitResult) {
        return new ItemMenuHost<>((OmniToolItem) (Object) this, player, locator);
    }
}
