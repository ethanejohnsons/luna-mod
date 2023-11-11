package dev.bluevista.lunamod.client;

import dev.bluevista.lunamod.LunaMod;
import dev.bluevista.lunamod.entity.LunaEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.feature.SaddleFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PigEntityModel;
import net.minecraft.util.Identifier;

/**
 * lets us see luna 🥺
 */
@Environment(EnvType.CLIENT)
public class LunaEntityRenderer extends MobEntityRenderer<LunaEntity, PigEntityModel<LunaEntity>> {

	public static final Identifier TEXTURE = new Identifier(LunaMod.MODID, "textures/entity/luna.png");

	public LunaEntityRenderer(EntityRendererFactory.Context context) {
		super(context, new PigEntityModel<>(context.getPart(EntityModelLayers.PIG)), 0.7F);
		this.addFeature(new SaddleFeatureRenderer<>(
			this,
			new PigEntityModel<>(context.getPart(EntityModelLayers.PIG_SADDLE)),
			new Identifier("textures/entity/pig/pig_saddle.png")
		));
	}

	public Identifier getTexture(LunaEntity luna) {
		return TEXTURE;
	}

}
