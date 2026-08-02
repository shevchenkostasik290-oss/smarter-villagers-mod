package com.villagermod;

import net.minecraft.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "villagerbehaviormod", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VillagerEventHandler {

    private static final String FEAR_TIMER_KEY = "VillagerFearTimer";
    private static final String SAFE_REACHED_KEY = "VillagerSafeReached";

    @SubscribeEvent
    public static void onVillagerAttacked(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof VillagerEntity)) return;
        if (!(event.getSource().getEntity() instanceof PlayerEntity)) return;

        VillagerEntity villager = (VillagerEntity) event.getEntity();
        PlayerEntity player = (PlayerEntity) event.getSource().getEntity();

        boolean hasHeavyArmor = isWearingHeavyArmor(player);
        float currentHealth = villager.getHealth();

        if (hasHeavyArmor || currentHealth <= 6.0f) {
            startFleeing(villager, player);
        } else {
            startAttacking(villager, player);
        }
    }

    @SubscribeEvent
    public static void onVillagerUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntity() instanceof VillagerEntity)) return;
        VillagerEntity villager = (VillagerEntity) event.getEntity();
        CompoundNBT nbt = villager.getPersistentData();

        if (nbt.contains(FEAR_TIMER_KEY)) {
            int timer = nbt.getInt(FEAR_TIMER_KEY);
            PlayerEntity nearestPlayer = villager.level.getNearestPlayer(villager, 30.0D);

            if (!nbt.getBoolean(SAFE_REACHED_KEY)) {
                if (nearestPlayer == null || villager.distanceToSqr(nearestPlayer) >= 900.0D) {
                    nbt.putBoolean(SAFE_REACHED_KEY, true);
                }
            } else {
                timer--;
                if (timer <= 0) {
                    nbt.remove(FEAR_TIMER_KEY);
                    nbt.remove(SAFE_REACHED_KEY);
                    clearGoals(villager);
                } else {
                    nbt.putInt(FEAR_TIMER_KEY, timer);
                }
            }
        }
    }

    private static void startFleeing(VillagerEntity villager, PlayerEntity player) {
        clearGoals(villager);
        CompoundNBT nbt = villager.getPersistentData();
        nbt.putInt(FEAR_TIMER_KEY, 1200);
        nbt.putBoolean(SAFE_REACHED_KEY, false);

        villager.goalSelector.addGoal(1, new AvoidEntityGoal<>(villager, PlayerEntity.class, 30.0F, 1.2D, 1.4D));
    }

    private static void startAttacking(VillagerEntity villager, PlayerEntity player) {
        clearGoals(villager);
        villager.setTarget(player);
        villager.goalSelector.addGoal(1, new MeleeAttackGoal(villager, 1.2D, false));
    }

    private static void clearGoals(VillagerEntity villager) {
        villager.setTarget(null);
        villager.goalSelector.getRunningGoals().forEach(goal -> {
            if (goal.getGoal() instanceof AvoidEntityGoal || goal.getGoal() instanceof MeleeAttackGoal) {
                villager.goalSelector.removeGoal(goal.getGoal());
            }
        });
    }

    private static boolean isWearingHeavyArmor(PlayerEntity player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlotType.CHEST);
        if (!chest.isEmpty() && chest.getItem() instanceof ArmorItem) {
            IArmorMaterial mat = ((ArmorItem) chest.getItem()).getMaterial();
            return mat == ArmorMaterial.IRON || mat == ArmorMaterial.DIAMOND || mat == ArmorMaterial.NETHERITE;
        }
        return false;
    }
}