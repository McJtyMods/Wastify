package com.mcjty.wastify;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ForgeEventHandlers {

// @todo
//    @SubscribeEvent(priority = EventPriority.HIGH)
//    public void onBiomeLoad(BiomeLoadingEvent event) {
//        event.getGeneration().getFeatures(GenerationStage.Decoration.TOP_LAYER_MODIFICATION).add(() -> WastifyFeature.WASTIFY_CONFIGURED_FEATURE);
//    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(
                Commands.literal(Wastify.MODID)
                        .then(WastifyCommands.registerListBiomes(dispatcher))
                        .then(WastifyCommands.registerListBlocks(dispatcher)));
    }
}
