package dev.bluevista.lunamod.client;

import dev.bluevista.lunamod.LunaMod;
import dev.bluevista.lunamod.entity.LunaEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * lets us see luna 🥺
 */
@Environment(EnvType.CLIENT)
public class LunaEntityRenderer extends GeoEntityRenderer<LunaEntity> {

	public LunaEntityRenderer(EntityRendererFactory.Context context) {
		super(context, new DefaultedEntityGeoModel<>(new Identifier(LunaMod.MODID, "luna"), true));
	}

}
