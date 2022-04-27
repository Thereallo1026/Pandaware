package dev.africa.pandaware.utils.client;

import dev.africa.pandaware.api.interfaces.MinecraftInstance;
import lombok.experimental.UtilityClass;
import lombok.var;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.RegistrySimple;

import static net.minecraft.client.resources.model.IBakedModel.DUMMY_MODEL;

@UtilityClass
public class PatcherUtils implements MinecraftInstance {
    public void patchTextureMap() {
        if (mc.getBlockRendererDispatcher() != null && mc.getBlockRendererDispatcher().getBlockModelShapes() != null) {
            BlockModelShapes shapes = mc.getBlockRendererDispatcher().getBlockModelShapes();
            ModelManager modelManager = shapes.getModelManager();

            var registry = (RegistrySimple<ModelResourceLocation, IBakedModel>) modelManager.getModelRegistry();

            Printer.consoleInfo("Clearing unnecessary model registry of size " + registry.getKeys().size());
            registry.getKeys().forEach(location -> registry.putObject(location, DUMMY_MODEL));

            var modelStore = shapes.getBakedModelStore();

            Printer.consoleInfo("Clearing unnecessary model store of size " + modelStore.size());
            modelStore.clear();
        }
    }
}
