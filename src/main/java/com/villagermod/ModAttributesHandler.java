package com.villagermod;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "villagerbehaviormod", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModAttributesHandler {

    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        // Официально регистрируем урон атаки для жителей, чтобы Forge не выдавал исключение Can't find attribute
        event.add(EntityType.VILLAGER, Attributes.ATTACK_DAMAGE, 2.0D);
    }
}