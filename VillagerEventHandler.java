package com.villagermod;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "villagerbehaviormod", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VillagerEventHandler {

    @SubscribeEvent
    public static void onVillagerAttacked(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof VillagerEntity)) {
            return;
        }

        VillagerEntity villager = (VillagerEntity) event.getEntity();
        
        if (!(event.getSource().getTrueSource() instanceof PlayerEntity)) {
            return;
        }

        PlayerEntity player = (PlayerEntity) event.getSource().getTrueSource();
        
        // Check if it's a melee attack (not ranged)
        if (!event.getSource().isMagicDamage() && !event.getSource().isProjectile()) {
            // Give change to player (emerald)
            giveChangeToPlayer(player);
            
            // Deal 1 heart damage if player has no armor
            if (!hasArmor(player)) {
                player.hurtResistantTime = 0;
                player.attackEntityFrom(net.minecraft.util.DamageSource.GENERIC, 2.0f);
            }
        }
    }

    @SubscribeEvent
    public static void onVillagerHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof VillagerEntity)) {
            return;
        }

        VillagerEntity villager = (VillagerEntity) event.getEntity();
        
        // Check if health is below 6 HP (3 hearts)
        if (villager.getHealth() < 6.0f) {
            // Make villager flee from nearby players
            makeVillagerFlee(villager);
        }
    }

    private static void giveChangeToPlayer(PlayerEntity player) {
        PlayerInventory inventory = player.inventory;
        
        // Try to add an emerald to the player's inventory
        ItemStack emerald = new ItemStack(Items.EMERALD);
        
        // Try to add to main inventory first
        if (inventory.addItemStackToInventory(emerald)) {
            return;
        }
        
        // If inventory is full, drop the item
        if (!player.world.isRemote) {
            player.dropItem(emerald, false);
        }
    }

    private static boolean hasArmor(PlayerEntity player) {
        PlayerInventory inventory = player.inventory;
        
        // Check all armor slots
        for (ItemStack armorStack : inventory.armorInventory) {
            if (!armorStack.isEmpty() && armorStack.getItem() instanceof ArmorItem) {
                return true;
            }
        }
        
        return false;
    }

    private static void makeVillagerFlee(VillagerEntity villager) {
        // Find nearest player to flee from
        PlayerEntity nearestPlayer = villager.world.getClosestPlayer(
            villager, 
            16.0d // Search within 16 blocks
        );
        
        if (nearestPlayer != null) {
            // Calculate direction away from player
            double dx = villager.getPosX() - nearestPlayer.getPosX();
            double dz = villager.getPosZ() - nearestPlayer.getPosZ();
            
            // Normalize and set movement
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance > 0) {
                dx /= distance;
                dz /= distance;
                
                // Set villager to move away from player
                villager.setMotion(dx * 0.5, villager.getMotion().y, dz * 0.5);
            }
        }
    }
}
