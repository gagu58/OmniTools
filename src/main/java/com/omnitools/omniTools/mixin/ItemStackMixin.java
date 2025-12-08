package com.omnitools.omniTools.mixin;

import com.omnitools.omniTools.core.ModItems;
import com.omnitools.omniTools.core.OmniToolItem;
import com.omnitools.omniTools.core.ToolMode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Inject(method = "is(Lnet/minecraft/tags/TagKey;)Z", at = @At("HEAD"), cancellable = true)
    private void omnitools$hideWrenchTags(TagKey<Item> tag, CallbackInfoReturnable<Boolean> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.isEmpty()) {
            return;
        }
        if (self.getItem() != ModItems.OMNI_WRENCH.get()) {
            return;
        }
        if (!omnitools$isWrenchTag(tag)) {
            return;
        }
        ToolMode mode = OmniToolItem.getMode(self);
        if (mode == ToolMode.LINK) {
            cir.setReturnValue(false);
        }
    }

    @Unique
    private static boolean omnitools$isWrenchTag(TagKey<Item> tag) {
        ResourceLocation id = tag.location();
        // 只判断 path 是否为 tools/wrench，以兼容 c:tools/wrench、forge:tools/wrench 等不同命名空间
        return "tools/wrench".equals(id.getPath());
    }
}
