package com.omnitools.omniTools.compat.entangled;

import com.omnitools.omniTools.core.ModItems;
import com.omnitools.omniTools.core.OmniToolItem;
import com.omnitools.omniTools.core.ToolMode;
import com.supermartijn642.core.render.RenderUtils;
import com.supermartijn642.core.render.RenderWorldEvent;
import com.supermartijn642.entangled.EntangledBinderItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

//纠缠方块模组的选择方块渲染
public class EntangledHighlightHandler {
    public static void onRenderWorld(RenderWorldEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Level world = mc.level;
        if (player == null || world == null) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        if (stack.getItem() != ModItems.OMNI_WRENCH.get()) {
            return;
        }

        if (OmniToolItem.getMode(stack) != ToolMode.LINK) {
            return;
        }

        EntangledBinderItem.BinderTarget target = stack.get(EntangledBinderItem.BINDER_TARGET);
        if (target == null || !target.dimension().equals(world.dimension().location())) {
            return;
        }

        var pos = target.pos();

        event.getPoseStack().pushPose();
        Vec3 camera = RenderUtils.getCameraPosition();
        event.getPoseStack().translate(-camera.x, -camera.y, -camera.z);
        event.getPoseStack().translate(pos.getX(), pos.getY(), pos.getZ());

        var shape = world.getBlockState(pos).getOcclusionShape(world, pos);
        RenderUtils.renderShape(event.getPoseStack(), shape, 235 / 255f, 210 / 255f, 52 / 255f, false);
        RenderUtils.renderShapeSides(event.getPoseStack(), shape, 235 / 255f, 210 / 255f, 52 / 255f, 30 / 255f, false);

        event.getPoseStack().popPose();
    }
}
