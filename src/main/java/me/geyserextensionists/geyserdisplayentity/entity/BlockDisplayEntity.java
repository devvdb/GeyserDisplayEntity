package me.geyserextensionists.geyserdisplayentity.entity;

import org.geysermc.geyser.api.util.Identifier;
import org.geysermc.geyser.entity.properties.type.IntProperty;
import org.geysermc.geyser.entity.spawn.EntitySpawnContext;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type.IntEntityMetadata;

import static me.geyserextensionists.geyserdisplayentity.GeyserDisplayEntity.MAX_VALUE;
import static me.geyserextensionists.geyserdisplayentity.GeyserDisplayEntity.MIN_VALUE;

public class BlockDisplayEntity extends SlotDisplayEntity {

    public BlockDisplayEntity(
            EntitySpawnContext entitySpawnContext
    ) {
        super(entitySpawnContext);
    }

    public void setDisplayedBlockState(
            IntEntityMetadata blockState
    ) {

        int javaBlockState =
                blockState.getPrimitiveValue();

        propertyManager.addProperty(
                new IntProperty(
                        Identifier.of(
                                "geyser:s_int"
                        ),
                        MAX_VALUE,
                        MIN_VALUE,
                        0
                ),
                javaBlockState
        );

        updateBedrockEntityProperties();
    }
}