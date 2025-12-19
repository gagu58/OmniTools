package com.omnitools.omniTools.compat.ae2;

import com.omnitools.omniTools.api.IWrenchHandler;
import com.omnitools.omniTools.api.WrenchContext;
import com.omnitools.omniTools.mixin.ae2.P2PTunnelPartInvoker;
import com.omnitools.omniTools.core.ToolMode;
import appeng.api.ids.AEComponents;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartItem;
import appeng.api.parts.SelectedPart;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEParts;
import appeng.core.localization.PlayerMessages;
import appeng.core.localization.Tooltips;
import appeng.items.tools.MemoryCardItem;
import appeng.parts.AEBasePart;
import appeng.parts.p2p.P2PTunnelPart;
import appeng.util.InteractionUtil;
import appeng.util.SettingsFrom;
import appeng.me.service.P2PService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Set;
import java.util.stream.Collectors;

public class Ae2MemoryCardWrenchHandler implements IWrenchHandler {

    private static final Component PREFIX = Component.translatable("omnitools.compat.ae2").append(" ");

    @Override
    public boolean canHandle(WrenchContext context) {
        if (context.getCurrentMode() != ToolMode.CONFIGURATION) {
            return false;
        }
        Level level = context.getLevel();
        if (level == null) {
            return false;
        }
        return resolveTarget(level, context.getPos(), context.getClickLocation()) != null;
    }

    @Override
    public InteractionResult handle(WrenchContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level == null || player == null) {
            return InteractionResult.PASS;
        }

        Target target = resolveTarget(level, context.getPos(), context.getClickLocation());
        if (target == null) {
            return InteractionResult.PASS;
        }

        ItemStack stack = context.getStack();
        boolean alt = InteractionUtil.isInAlternateUseMode(player);

        if (target.kind == Kind.BLOCK) {
            return handleBlock(level, player, stack, alt, target.block);
        } else {
            return handlePart(level, player, stack, alt, target.part);
        }
    }

    private InteractionResult handleBlock(Level level, Player player, ItemStack stack, boolean alt, AEBaseBlockEntity block) {
        if (alt) {
            var builder = DataComponentMap.builder();
            block.exportSettings(SettingsFrom.MEMORY_CARD, builder, player);
            var settings = builder.build();
            if (!settings.isEmpty()) {
                MemoryCardItem.clearCard(stack);
                stack.applyComponents(settings);
                player.displayClientMessage(PREFIX.copy().append(PlayerMessages.SavedSettings.text()), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        Component storedName = stack.get(AEComponents.EXPORTED_SETTINGS_SOURCE);
        // 原版：AEBaseEntityBlock#useItemOn 比对的是 block.getName()（Block），这里用 blockState 的 block 名称保持一致
        Component blockName = block.getBlockState().getBlock().getName();
        if (storedName != null && blockName.equals(storedName)) {
            block.importSettings(SettingsFrom.MEMORY_CARD, stack.getComponents(), player);
            player.displayClientMessage(PREFIX.copy().append(PlayerMessages.LoadedSettings.text()), true);
        } else {
            importGenericSettingsAndNotifyPrefixed(block, stack.getComponents(), player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private InteractionResult handleP2P(Level level, Player player, ItemStack stack, boolean alt, P2PTunnelPart<?> tunnel) {
        if (tunnel.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (alt) {
            short newFreq = tunnel.getFrequency();
            boolean wasOutput = tunnel.isOutput();
            ((P2PTunnelPartInvoker) (Object) tunnel).omnitools$invokeSetOutput(false);

            boolean needsNewFrequency = wasOutput || newFreq == 0;
            var grid = tunnel.getMainNode().getGrid();
            if (grid != null) {
                var p2p = P2PService.get(grid);
                if (needsNewFrequency) {
                    newFreq = p2p.newFrequency();
                }
                p2p.updateFreq(tunnel, newFreq);
            }

            tunnel.onTunnelConfigChange();

            MemoryCardItem.clearCard(stack);
            stack.set(AEComponents.EXPORTED_SETTINGS_SOURCE, tunnel.getPartItem().asItem().getDescription());
            stack.applyComponents(tunnel.exportSettings(SettingsFrom.MEMORY_CARD));

            if (needsNewFrequency) {
                player.displayClientMessage(PREFIX.copy().append(PlayerMessages.ResetSettings.text()), true);
            } else {
                player.displayClientMessage(PREFIX.copy().append(PlayerMessages.SavedSettings.text()), true);
            }
            return InteractionResult.SUCCESS;
        }

        var p2pTunnelItem = stack.get(AEComponents.EXPORTED_P2P_TYPE);
        if (p2pTunnelItem instanceof IPartItem<?> partItem
                && P2PTunnelPart.class.isAssignableFrom(partItem.getPartClass())) {
            appeng.api.parts.IPart newBus = tunnel;
            if (newBus.getPartItem() != partItem) {
                newBus = tunnel.getHost().replacePart(partItem, tunnel.getSide(), player, InteractionHand.MAIN_HAND);
            }

            if (newBus instanceof P2PTunnelPart<?> newTunnel) {
                newTunnel.importSettings(SettingsFrom.MEMORY_CARD, stack.getComponents(), player);
            }

            player.displayClientMessage(PREFIX.copy().append(PlayerMessages.LoadedSettings.text()), true);
            return InteractionResult.SUCCESS;
        }

        player.displayClientMessage(PREFIX.copy().append(PlayerMessages.InvalidMachine.text()), true);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handlePart(Level level, Player player, ItemStack stack, boolean alt, AEBasePart part) {
        if (part instanceof P2PTunnelPart<?> p2p) {
            return handleP2P(level, player, stack, alt, p2p);
        }
        if (!part.useStandardMemoryCard()) {
            return InteractionResult.PASS;
        }

        // 原版：AEBasePart#useMemoryCard 对 Interface / Pattern Provider 进行 block/part 映射
        var partItem = part.getPartItem();
        var partAsItem = partItem == null ? null : partItem.asItem();
        if (partAsItem == AEParts.INTERFACE.asItem()) {
            partAsItem = AEBlocks.INTERFACE.asItem();
        } else if (partAsItem == AEParts.PATTERN_PROVIDER.asItem()) {
            partAsItem = AEBlocks.PATTERN_PROVIDER.asItem();
        }
        Component name = partAsItem == null ? Component.literal("part") : partAsItem.getDescription();

        if (alt) {
            var settings = part.exportSettings(SettingsFrom.MEMORY_CARD);
            if (!settings.isEmpty()) {
                MemoryCardItem.clearCard(stack);
                stack.applyComponents(settings);
                player.displayClientMessage(PREFIX.copy().append(PlayerMessages.SavedSettings.text()), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        var storedName = stack.get(AEComponents.EXPORTED_SETTINGS_SOURCE);
        if (storedName != null && name.equals(storedName)) {
            part.importSettings(SettingsFrom.MEMORY_CARD, stack.getComponents(), player);
            player.displayClientMessage(PREFIX.copy().append(PlayerMessages.LoadedSettings.text()), true);
        } else {
            importGenericSettingsAndNotifyPrefixed(part, stack.getComponents(), player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private void importGenericSettingsAndNotifyPrefixed(Object importTo, DataComponentMap input, Player player) {
        Set<DataComponentType<?>> imported = MemoryCardItem.importGenericSettings(importTo, input, player);
        if (player != null && !player.getCommandSenderWorld().isClientSide()) {
            if (imported.isEmpty()) {
                player.displayClientMessage(PREFIX.copy().append(PlayerMessages.InvalidMachine.text()), true);
            } else {
                var restored = Tooltips.conjunction(imported.stream()
                        .map(type -> (Component) Component.translatable(MemoryCardItem.getSettingTranslationKey(type)))
                        .distinct()
                        .collect(Collectors.toList()));
                player.displayClientMessage(PREFIX.copy().append(PlayerMessages.InvalidMachinePartiallyRestored.text(restored)), true);
            }
        }
    }

    private Target resolveTarget(Level level, BlockPos pos, Vec3 click) {
        BlockEntity be = level.getBlockEntity(pos);
        // 原版：CableBusBlock 会优先把交互转给 cb.useItemOn -> selectPartLocal -> part.onUseItemOn
        // 因为 CableBusBlockEntity 也继承 AEBaseBlockEntity，所以这里必须先按 IPartHost 的命中选择 Part，避免误走方块分支
        if (be instanceof IPartHost host) {
            SelectedPart sp = host.selectPartWorld(click);
            if (sp != null && sp.part instanceof AEBasePart aePart) {
                return new Target(Kind.PART, null, aePart, aePart.getPartItem().asItem().getDescription());
            }
            // 关键：如果这是 PartHost，但没命中 Part，就必须返回 null，让交互继续走原版行为
            // 否则会误走 AEBaseBlockEntity 分支（例如 CableBusBlockEntity），导致“提示成功但实际没效果”。
            return null;
        }

        if (be instanceof AEBaseBlockEntity ae) {
            Component name = ae.getName();
            return new Target(Kind.BLOCK, ae, null, name);
        }
        return null;
    }

    private enum Kind {BLOCK, PART}

    private record Target(Kind kind, AEBaseBlockEntity block, AEBasePart part, Component name) {
    }
}
