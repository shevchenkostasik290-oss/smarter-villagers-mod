package com.villagermod;

import net.minecraft.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.EnumSet;

@Mod.EventBusSubscriber(modid = "villagerbehaviormod", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VillagerEventHandler {

    private static final String FEAR_TIMER_KEY = "VillagerFearTimer";
    private static final String SAFE_REACHED_KEY = "VillagerSafeReached";

    @SubscribeEvent
    public static void onVillagerAttacked(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof VillagerEntity)) return;
        if (!(event.getSource().getTrueSource() instanceof PlayerEntity)) return;

        VillagerEntity villager = (VillagerEntity) event.getEntity();
        PlayerEntity player = (PlayerEntity) event.getSource().getTrueSource();

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
            PlayerEntity nearestPlayer = villager.world.getNearestPlayer(villager, 30.0D);

            if (!nbt.getBoolean(SAFE_REACHED_KEY)) {
                if (nearestPlayer == null || villager.getDistanceSq(nearestPlayer) >= 900.0D) {
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

        villager.goalSelector.addGoal(1, new AvoidEntityGoal<>(villager, PlayerEntity.class, 30.0F, 1.2D, 1.2D));
    }

    private static void startAttacking(VillagerEntity villager, PlayerEntity player) {
        clearGoals(villager);
        villager.setAttackTarget(player);
        villager.goalSelector.addGoal(1, new VillagerAttackGoal(villager, 1.2D));
    }

    private static void clearGoals(VillagerEntity villager) {
        villager.setAttackTarget(null);
        villager.goalSelector.getRunningGoals().forEach(goal -> {
            if (goal.getGoal() instanceof AvoidEntityGoal || goal.getGoal() instanceof VillagerAttackGoal) {
                villager.goalSelector.removeGoal(goal.getGoal());
            }
        });
    }

    private static boolean isWearingHeavyArmor(PlayerEntity player) {
        ItemStack chest = player.getItemStackFromSlot(EquipmentSlotType.CHEST);
        if (!chest.isEmpty() && chest.getItem() instanceof ArmorItem) {
            IArmorMaterial mat = ((ArmorItem) chest.getItem()).getArmorMaterial();
            return mat == ArmorMaterial.IRON || mat == ArmorMaterial.DIAMOND || mat == ArmorMaterial.NETHERITE;
        }
        return false;
    }

    private static class VillagerAttackGoal extends Goal {
        private final VillagerEntity villager;
        private final double speed;
        private int attackCooldown = 0;

        public VillagerAttackGoal(VillagerEntity villager, double speed) {
            this.villager = villager;
            this.speed = speed;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            PlayerEntity target = villager.getAttackTarget() instanceof PlayerEntity ? (PlayerEntity) villager.getAttackTarget() : null;
            return target != null && target.isAlive();
        }

        @Override
        public void start() {
            this.attackCooldown = 0;
        }

        @Override
        public void stop() {
            this.villager.getNavigator().clearPath();
        }

        @Override
        public void tick() {
            PlayerEntity target = (PlayerEntity) villager.getAttackTarget();
            if (target == null) return;

            villager.getLookController().setLookPositionWithEntity(target, 30.0F, 30.0F);
            villager.getNavigator().tryMoveToEntityLiving(target, this.speed);

            if (this.attackCooldown > 0) {
                this.attackCooldown--;
            }

            double distanceSqr = villager.getDistanceSq(target);
            if (distanceSqr <= 4.0D && this.attackCooldown <= 0) {
                this.attackCooldown = 20;
                villager.swingArm(Hand.MAIN_HAND);
                target.attackEntityFrom(DamageSource.causeMobDamage(villager), 2.0F);
            }
        }
    }
}