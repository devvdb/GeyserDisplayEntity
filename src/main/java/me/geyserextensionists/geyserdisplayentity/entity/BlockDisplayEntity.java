package me.geyserextensionists.geyserdisplayentity.entity;

import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataTypes;
import org.geysermc.geyser.api.util.Identifier;
import org.geysermc.geyser.entity.properties.type.IntProperty;
import org.geysermc.geyser.entity.spawn.EntitySpawnContext;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type.IntEntityMetadata;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static me.geyserextensionists.geyserdisplayentity.GeyserDisplayEntity.MAX_VALUE;
import static me.geyserextensionists.geyserdisplayentity.GeyserDisplayEntity.MIN_VALUE;

public class BlockDisplayEntity extends SlotDisplayEntity {

    /*
     * Only log each Java block-state ID once.
     */
    private static final Set<Integer> SEEN_BLOCK_STATES =
            ConcurrentHashMap.newKeySet();

    public BlockDisplayEntity(EntitySpawnContext entitySpawnContext) {
        super(entitySpawnContext);
    }

    @Override
    public void initializeMetadata() {
        super.initializeMetadata();

        /*
         * We are going to use the extension's already-registered
         * geyser:s_int property as the block/material selector
         * for the Bedrock resource pack.
         */
        propertyManager.addProperty(
                new IntProperty(
                        Identifier.of("geyser:s_int"),
                        MAX_VALUE,
                        MIN_VALUE,
                        0
                ),
                0
        );
    }

    public void setDisplayedBlockState(IntEntityMetadata blockState) {

        int javaBlockStateId = blockState.getPrimitiveValue();

        /*
         * Existing GeyserDisplayEntity behavior:
         * translate the Java block state into its Bedrock block definition.
         */
        this.metadata.put(
                EntityDataTypes.DISPLAY_BLOCK_STATE,
                this.session
                        .getBlockMappings()
                        .getBedrockBlock(javaBlockStateId)
        );

        /*
         * Send the raw Java state ID to Bedrock.
         *
         * The resource pack can read this through:
         *
         * query.property('geyser:s_int')
         */
        propertyManager.addProperty(
                new IntProperty(
                        Identifier.of("geyser:s_int"),
                        MAX_VALUE,
                        MIN_VALUE,
                        0
                ),
                javaBlockStateId
        );

        updateBedrockEntityProperties();

        /*
         * Diagnostic output. Because Shifter only uses a handful of
         * different blocks, we should only get a few lines.
         */
        if (SEEN_BLOCK_STATES.add(javaBlockStateId)) {
            System.out.println(
                    "[GeyserDisplayEntity] BlockDisplay Java block-state ID = "
                            + javaBlockStateId
                            + " Bedrock definition = "
                            + this.session
                            .getBlockMappings()
                            .getBedrockBlock(javaBlockStateId)
            );
        }
    }
}