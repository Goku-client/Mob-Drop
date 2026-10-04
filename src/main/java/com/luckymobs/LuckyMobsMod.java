package com.luckymobs;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.ThreadLocalRandom;

public class LuckyMobsMod implements ModInitializer {

	// Chance (0-100) that a kill gives OP loot. The rest is a bad surprise.
	private static final int GOOD_CHANCE = 50;

	@Override
	public void onInitialize() {
		ServerLivingEntityEvents.AFTER_DEATH.register(LuckyMobsMod::onDeath);
	}

	private static void onDeath(LivingEntity victim, DamageSource source) {
		if (!(victim instanceof Mob)) return;
		if (!(source.getEntity() instanceof ServerPlayer player)) return;
		if (!(victim.level() instanceof ServerLevel level)) return;

		double x = victim.getX(), y = victim.getY(), z = victim.getZ();

		if (ThreadLocalRandom.current().nextInt(100) < GOOD_CHANCE) {
			ItemStack loot = makeLoot(level);
			ItemEntity drop = new ItemEntity(level, x, y + 0.5, z, loot);
			level.addFreshEntity(drop);
			level.playSound(null, x, y, z, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
			level.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y + 1, z, 25, 0.6, 0.6, 0.6, 0.1);
			player.displayClientMessage(Component.literal("\u00a76\u00a7lLUCKY!"), true);
		} else {
			badLuck(level, player, x, y, z);
			level.sendParticles(ParticleTypes.SMOKE, x, y + 1, z, 25, 0.6, 0.6, 0.6, 0.05);
			player.displayClientMessage(Component.literal("\u00a7c\u00a7lUNLUCKY!"), true);
		}
	}

	private static void badLuck(ServerLevel level, ServerPlayer player, double x, double y, double z) {
		switch (ThreadLocalRandom.current().nextInt(7)) {
			case 0 -> level.explode(null, x, y, z, 3.0F, Level.ExplosionInteraction.MOB);
			case 1 -> {
				LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.EVENT);
				if (bolt != null) {
					bolt.setPos(player.getX(), player.getY(), player.getZ());
					level.addFreshEntity(bolt);
				}
			}
			case 2 -> player.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1));
			case 3 -> player.addEffect(new MobEffectInstance(MobEffects.WITHER, 160, 1));
			case 4 -> player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0));
			case 5 -> {
				player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 300, 0));
				player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 300, 2));
			}
			default -> {
				BlockPos pos = BlockPos.containing(x, y, z);
				if (level.getBlockState(pos).isAir()) {
					level.setBlockAndUpdate(pos, Blocks.LAVA.defaultBlockState());
				}
			}
		}
	}

	private static ItemStack makeLoot(ServerLevel level) {
		ThreadLocalRandom r = ThreadLocalRandom.current();
		switch (r.nextInt(12)) {
			case 0 -> {
				ItemStack s = new ItemStack(Items.NETHERITE_SWORD);
				ench(level, s, Enchantments.SHARPNESS, 5);
				ench(level, s, Enchantments.FIRE_ASPECT, 2);
				ench(level, s, Enchantments.LOOTING, 3);
				ench(level, s, Enchantments.UNBREAKING, 3);
				ench(level, s, Enchantments.MENDING, 1);
				return s;
			}
			case 1 -> {
				ItemStack s = new ItemStack(Items.NETHERITE_PICKAXE);
				ench(level, s, Enchantments.EFFICIENCY, 5);
				ench(level, s, Enchantments.FORTUNE, 3);
				ench(level, s, Enchantments.UNBREAKING, 3);
				ench(level, s, Enchantments.MENDING, 1);
				return s;
			}
			case 2 -> {
				ItemStack s = new ItemStack(Items.NETHERITE_AXE);
				ench(level, s, Enchantments.SHARPNESS, 5);
				ench(level, s, Enchantments.EFFICIENCY, 5);
				ench(level, s, Enchantments.UNBREAKING, 3);
				ench(level, s, Enchantments.MENDING, 1);
				return s;
			}
			case 3 -> {
				ItemStack s = new ItemStack(Items.NETHERITE_CHESTPLATE);
				ench(level, s, Enchantments.PROTECTION, 4);
				ench(level, s, Enchantments.UNBREAKING, 3);
				ench(level, s, Enchantments.MENDING, 1);
				return s;
			}
			case 4 -> {
				ItemStack s = new ItemStack(Items.NETHERITE_BOOTS);
				ench(level, s, Enchantments.PROTECTION, 4);
				ench(level, s, Enchantments.FEATHER_FALLING, 4);
				ench(level, s, Enchantments.UNBREAKING, 3);
				ench(level, s, Enchantments.MENDING, 1);
				return s;
			}
			case 5 -> { return new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, r.nextInt(2, 6)); }
			case 6 -> { return new ItemStack(Items.TOTEM_OF_UNDYING, 1); }
			case 7 -> { return new ItemStack(Items.DIAMOND_BLOCK, r.nextInt(2, 9)); }
			case 8 -> { return new ItemStack(Items.NETHERITE_INGOT, r.nextInt(2, 6)); }
			case 9 -> { return new ItemStack(Items.ELYTRA, 1); }
			case 10 -> { return new ItemStack(Items.EXPERIENCE_BOTTLE, r.nextInt(32, 65)); }
			default -> { return new ItemStack(Items.ENDER_PEARL, r.nextInt(8, 17)); }
		}
	}

	private static void ench(ServerLevel level, ItemStack stack, ResourceKey<Enchantment> key, int lvl) {
		Holder<Enchantment> holder = level.registryAccess()
				.lookupOrThrow(Registries.ENCHANTMENT)
				.getOrThrow(key);
		stack.enchant(holder, lvl);
	}
}
