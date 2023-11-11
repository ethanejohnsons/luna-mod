package dev.bluevista.lunamod.entity;

import dev.bluevista.lunamod.LunaMod;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemSteerable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.SaddledComponent;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class LunaEntity extends AnimalEntity implements ItemSteerable, Saddleable {

	public static final TrackedData<Boolean> SADDLED = DataTracker.registerData(LunaEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	public static final TrackedData<Integer> BOOST_TIME = DataTracker.registerData(LunaEntity.class, TrackedDataHandlerRegistry.INTEGER);
	public static final Ingredient ALL_FOOD = Ingredient.ofItems(Registries.ITEM.stream().filter(Item::isFood).toArray(Item[]::new));
	public final SaddledComponent saddledComponent;

	public LunaEntity(EntityType<? extends AnimalEntity> entityType, World world) {
		super(entityType, world);
		this.saddledComponent = new SaddledComponent(dataTracker, BOOST_TIME, SADDLED);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(0, new SwimGoal(this));
		this.goalSelector.add(1, new EscapeDangerGoal(this, 1.25));
		this.goalSelector.add(3, new AnimalMateGoal(this, 1.0));
		this.goalSelector.add(4, new TemptGoal(this, 1.2, ALL_FOOD, false));
		this.goalSelector.add(4, new TemptGoal(this, 1.2, ALL_FOOD, false));
		this.goalSelector.add(5, new FollowParentGoal(this, 1.1));
		this.goalSelector.add(6, new WanderAroundFarGoal(this, 1.0));
		this.goalSelector.add(7, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
		this.goalSelector.add(8, new LookAroundGoal(this));
	}

	public static DefaultAttributeContainer.Builder createAttributes() {
		return MobEntity.createMobAttributes()
			.add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0)
			.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25);
	}

	@Override
	@Nullable
	public LivingEntity getControllingPassenger() {
		if (isSaddled() && getFirstPassenger() instanceof PlayerEntity player && player.isHolding(Items.CARROT_ON_A_STICK)) {
			return player;
		}
		return super.getControllingPassenger();
	}

	@Override
	public void onTrackedDataSet(TrackedData<?> data) {
		if (BOOST_TIME.equals(data) && getWorld().isClient) {
			this.saddledComponent.boost();
		}
		super.onTrackedDataSet(data);
	}

	@Override
	protected void initDataTracker() {
		super.initDataTracker();
		getDataTracker().startTracking(SADDLED, false);
		getDataTracker().startTracking(BOOST_TIME, 0);
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);
		this.saddledComponent.writeNbt(nbt);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);
		this.saddledComponent.readNbt(nbt);
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
	public ActionResult interactMob(PlayerEntity player, Hand hand) {
		if (!isBreedingItem(player.getStackInHand(hand)) && isSaddled() && !hasPassengers() && !player.shouldCancelInteraction()) {
			if (!getWorld().isClient) {
				player.startRiding(this);
			}
			return ActionResult.success(getWorld().isClient);
		}
		var actionResult = super.interactMob(player, hand);
		if (!actionResult.isAccepted()) {
			var itemStack = player.getStackInHand(hand);
			if (itemStack.isOf(Items.SADDLE)) {
				return itemStack.useOnEntity(player, this, hand);
			}
			return ActionResult.PASS;
		}
		return actionResult;
	}

	@Override
	public boolean canBeSaddled() {
		return isAlive() && !isBaby();
	}

	@Override
	protected void dropInventory() {
		super.dropInventory();
		if (this.isSaddled()) {
			this.dropItem(Items.SADDLE);
		}
	}

	@Override
	public boolean isSaddled() {
		return this.saddledComponent.isSaddled();
	}

	@Override
	public void saddle(@Nullable SoundCategory sound) {
		this.saddledComponent.setSaddled(true);
		if (sound != null) {
			this.getWorld().playSoundFromEntity(null, this, SoundEvents.ENTITY_PIG_SADDLE, sound, 0.5f, 1.0f);
		}
	}

	@Override
	public Vec3d updatePassengerForDismount(LivingEntity passenger) {
		Direction direction = this.getMovementDirection();
		if (direction.getAxis() == Direction.Axis.Y) {
			return super.updatePassengerForDismount(passenger);
		}
		int[][] is = Dismounting.getDismountOffsets(direction);
		BlockPos blockPos = this.getBlockPos();
		BlockPos.Mutable mutable = new BlockPos.Mutable();
		for (EntityPose entityPose : passenger.getPoses()) {
			Box box = passenger.getBoundingBox(entityPose);
			for (int[] js : is) {
				mutable.set(blockPos.getX() + js[0], blockPos.getY(), blockPos.getZ() + js[1]);
				double d = this.getWorld().getDismountHeight(mutable);
				if (!Dismounting.canDismountInBlock(d)) continue;
				Vec3d vec3d = Vec3d.ofCenter(mutable, d);
				if (!Dismounting.canPlaceEntityAt(this.getWorld(), passenger, box.offset(vec3d))) continue;
				passenger.setPose(entityPose);
				return vec3d;
			}
		}
		return super.updatePassengerForDismount(passenger);
	}

	@Override
	protected void tickControlled(PlayerEntity controllingPlayer, Vec3d movementInput) {
		super.tickControlled(controllingPlayer, movementInput);
		this.setRotation(controllingPlayer.getYaw(), controllingPlayer.getPitch() * 0.5f);
		this.bodyYaw = this.headYaw = this.getYaw();
		this.prevYaw = this.headYaw;
		this.saddledComponent.tickBoost();
	}

	@Override
	protected Vec3d getControlledMovementInput(PlayerEntity controllingPlayer, Vec3d movementInput) {
		return new Vec3d(0.0, 0.0, 1.0);
	}

	@Override
	protected float getSaddledSpeed(PlayerEntity controllingPlayer) {
		return (float)(this.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED) * 0.5 * (double)this.saddledComponent.getMovementSpeedMultiplier());
	}

	@Override
	public boolean consumeOnAStickItem() {
		return this.saddledComponent.boost(this.getRandom());
	}

	@Override @Nullable
	public LunaEntity createChild(ServerWorld world, PassiveEntity entity) {
		return LunaMod.LUNA.create(world);
	}

	@Override
	public boolean isBreedingItem(ItemStack stack) {
		return ALL_FOOD.test(stack);
	}

	@Override
	public Vec3d getLeashOffset() {
		return new Vec3d(0.0, 0.6f * this.getStandingEyeHeight(), this.getWidth() * 0.4f);
	}

	@Override
	protected Vector3f getPassengerAttachmentPos(Entity passenger, EntityDimensions dimensions, float scaleFactor) {
		return new Vector3f(0.0f, dimensions.height - 0.03125f * scaleFactor, 0.0f);
	}

}