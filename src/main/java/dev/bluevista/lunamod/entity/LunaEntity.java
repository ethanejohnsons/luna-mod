package dev.bluevista.lunamod.entity;

import dev.bluevista.lunamod.LunaMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemSteerable;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class LunaEntity extends PathAwareEntity implements GeoEntity, ItemSteerable, Saddleable {

	public static final Ingredient ALL_FOOD = Ingredient.ofItems(Registries.ITEM.stream().filter(Item::isFood).toArray(Item[]::new));
	public static final RawAnimation IDLE_ANIMATION = RawAnimation.begin().thenLoop("idle");
	public static final RawAnimation WALK_ANIMATION = RawAnimation.begin().thenLoop("walk");
	public static final RawAnimation RUN_ANIMATION = RawAnimation.begin().thenLoop("run");

	private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

	public LunaEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
		super(entityType, world);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(0, new SwimGoal(this));
		this.goalSelector.add(1, new TemptGoal(this, 1.2, ALL_FOOD, false));
		this.goalSelector.add(2, new WanderAroundFarGoal(this, 1.0));
		this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
		this.goalSelector.add(4, new LookAroundGoal(this));
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return MobEntity.createMobAttributes()
			.add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0)
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return LunaMod.LUNA_WOOF_SOUND_EVENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return LunaMod.LUNA_HURT_SOUND_EVENT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return LunaMod.LUNA_DEATH_SOUND_EVENT;
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
		this.playSound(SoundEvents.ENTITY_PIG_STEP, 0.15f, 1.0f);
	}

	@Override
	public Vec3d getLeashOffset() {
		return new Vec3d(0.0, 0.6f * this.getStandingEyeHeight(), this.getWidth() * 0.4f);
	}

	@Override
	public boolean consumeOnAStickItem() {
		return false;
	}

	@Override
	public boolean canBeSaddled() {
		return false;
	}

	@Override
	public void saddle(@Nullable SoundCategory sound) {
	}

	@Override
	public boolean isSaddled() {
		return false;
	}

	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return this.cache;
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
		controllerRegistrar.add((new AnimationController<>(this, "luna_controller", 3, this::predicate)));
	}

	@Environment(EnvType.CLIENT)
	private PlayState predicate(AnimationState<LunaEntity> state) {
		var vel2 = getVelocity().lengthSquared();
		if (state.isMoving()) {
			if (vel2 > 0.008) {
				return state.setAndContinue(RUN_ANIMATION);
			} else if (vel2 > 0.001) {
				return state.setAndContinue(WALK_ANIMATION);
			}
		}
		return state.setAndContinue(IDLE_ANIMATION);
	}

}