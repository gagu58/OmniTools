package com.omnitools.omniTools.core;

import com.omnitools.omniTools.omniTools;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(omniTools.MODID);

    public static final DeferredItem<OmniToolItem> OMNI_WRENCH = ITEMS.register("omni_wrench",
            () -> new OmniToolItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<OmniVajraItem> OMNI_VAJRA = ITEMS.register("omni_vajra",
            () -> new OmniVajraItem(new Item.Properties().stacksTo(1).attributes(ItemAttributeModifiers.builder().add(
                    Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 20.0D, Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND
            ).add(
                    Attributes.ATTACK_SPEED,
                    new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -0.0D, Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND
            ).build())));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }

}
