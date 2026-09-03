package me.geyserextensionists.geyserdisplayentity;

import me.geyserextensionists.geyserdisplayentity.entity.BlockDisplayEntity;
import me.geyserextensionists.geyserdisplayentity.entity.ItemDisplayEntity;
import me.geyserextensionists.geyserdisplayentity.entity.NativeDisplayEntity;
import me.geyserextensionists.geyserdisplayentity.entity.SlotDisplayEntity;
import me.geyserextensionists.geyserdisplayentity.managers.ConfigManager;
import me.geyserextensionists.geyserdisplayentity.util.EntityUtils;
import me.geyserextensionists.geyserdisplayentity.util.FileConfiguration;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.command.Command;
import org.geysermc.geyser.api.command.CommandSource;
import org.geysermc.geyser.api.entity.property.GeyserEntityProperty;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCommandsEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineEntitiesEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineEntityPropertiesEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserPreInitializeEvent;
import org.geysermc.geyser.api.extension.Extension;
import org.geysermc.geyser.api.util.Identifier;
import org.geysermc.geyser.entity.*;
import org.geysermc.geyser.entity.type.Entity;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.MetadataTypes;
import org.geysermc.mcprotocollib.protocol.data.game.entity.type.EntityType;

import java.util.Collection;

public class GeyserDisplayEntity implements Extension {

    private static GeyserDisplayEntity extension;

    private ConfigManager configManager;

    private static BedrockEntityDefinition ITEM_DISPLAY_BEDROCK;
    private static BedrockEntityDefinition BLOCK_DISPLAY_BEDROCK;

    private static VanillaEntityType<ItemDisplayEntity> ITEM_DISPLAY;
    private static VanillaEntityType<BlockDisplayEntity> BLOCK_DISPLAY;

    public static final Integer MAX_VALUE =
            1000000;

    public static final Integer MIN_VALUE =
            -1000000;

    /*
     * ========================================
     * PRE INITIALIZE
     * ========================================
     */

    @Subscribe
    public void onLoad(
            GeyserPreInitializeEvent event
    ) {

        ensureManagersLoaded();
    }

    /*
     * ========================================
     * DEFINE ENTITIES
     * ========================================
     */

    @Subscribe
    public void onDefineEntities(
            GeyserDefineEntitiesEvent event
    ) {

        ensureManagersLoaded();

        ITEM_DISPLAY_BEDROCK =
                EntityUtils.findOrRegisterCustomDefinition(
                        this,
                        event,
                        Identifier.of(
                                "geyser:item_display"
                        )
                );

        BLOCK_DISPLAY_BEDROCK =
                EntityUtils.findOrRegisterCustomDefinition(
                        this,
                        event,
                        Identifier.of(
                                "geyser:block_display"
                        )
                );
    }

    /*
     * ========================================
     * ENTITY PROPERTIES
     * ========================================
     */

    @Subscribe
    public void onEntityPropertiesEvent(
            GeyserDefineEntityPropertiesEvent event
    ) {

        /*
         * CRITICAL FIX:
         *
         * On some Geyser startup sequences this event
         * arrives before GeyserPreInitializeEvent has
         * initialized ConfigManager.
         *
         * The original extension dereferenced
         * configManager here and crashed.
         */
        ensureManagersLoaded();

        logger().info(
                "DEBUG MetadataTypes.INT = "
                        + MetadataTypes.INT
        );

        logger().info(
                "DEBUG MetadataTypes.BLOCK_STATE = "
                        + MetadataTypes.BLOCK_STATE
        );

        logger().info(
                "DEBUG MetadataTypes.OPTIONAL_BLOCK_STATE = "
                        + MetadataTypes.OPTIONAL_BLOCK_STATE
        );

        logger().info(
                "DEBUG MetadataTypes.INT id = "
                        + MetadataTypes.INT.getId()
        );

        logger().info(
                "DEBUG MetadataTypes.BLOCK_STATE id = "
                        + MetadataTypes.BLOCK_STATE.getId()
        );

        logger().info(
                "DEBUG MetadataTypes.OPTIONAL_BLOCK_STATE id = "
                        + MetadataTypes.OPTIONAL_BLOCK_STATE.getId()
        );

        try {

            registerDisplayProperties(
                    event,
                    Identifier.of(
                            "geyser:item_display"
                    )
            );

            registerDisplayProperties(
                    event,
                    Identifier.of(
                            "geyser:block_display"
                    )
            );

            EntityTypeBase<Entity> entityBase =
                    EntityTypeDefinition
                            .baseBuilder(
                                    Entity.class
                            )
                            .addTranslator(
                                    MetadataTypes.BYTE,
                                    Entity::setFlags
                            )
                            .addTranslator(
                                    MetadataTypes.INT,
                                    Entity::setAir
                            )
                            .addTranslator(
                                    MetadataTypes.OPTIONAL_COMPONENT,
                                    Entity::setCustomName
                            )
                            .addTranslator(
                                    MetadataTypes.BOOLEAN,
                                    Entity::setCustomNameVisible
                            )
                            .addTranslator(
                                    MetadataTypes.BOOLEAN,
                                    Entity::setSilent
                            )
                            .addTranslator(
                                    MetadataTypes.BOOLEAN,
                                    Entity::setGravity
                            )
                            .addTranslator(
                                    MetadataTypes.POSE,
                                    (
                                            entity,
                                            entityMetadata
                                    ) ->
                                            entity.setPose(
                                                    entityMetadata.getValue()
                                            )
                            )
                            .addTranslator(
                                    MetadataTypes.INT,
                                    Entity::setFreezing
                            )
                            .build();

            EntityTypeBase<SlotDisplayEntity> slotDisplayBase =
                    EntityTypeBase
                            .baseInherited(
                                    SlotDisplayEntity.class,
                                    entityBase
                            )

                            // ID 8
                            .addTranslator(null) // Interpolation delay

                            // ID 9
                            .addTranslator(null) // Transformation interpolation duration

                            // ID 10
                            .addTranslator(null) // Position/rotation interpolation duration

                            // ID 11
                            .addTranslator(
                                    MetadataTypes.VECTOR3,
                                    SlotDisplayEntity::setTranslation
                            )

                            // ID 12
                            .addTranslator(
                                    MetadataTypes.VECTOR3,
                                    SlotDisplayEntity::setScale
                            )

                            // ID 13
                            .addTranslator(
                                    MetadataTypes.QUATERNION,
                                    SlotDisplayEntity::setLeftRotation
                            )

                            // ID 14
                            .addTranslator(
                                    MetadataTypes.QUATERNION,
                                    SlotDisplayEntity::setRightRotation
                            )

                            // ID 15
                            .addTranslator(null) // Billboard

                            // ID 16
                            .addTranslator(null) // Brightness override

                            // ID 17
                            .addTranslator(null) // View range

                            // ID 18
                            .addTranslator(null) // Shadow radius

                            // ID 19
                            .addTranslator(null) // Shadow strength

                            // ID 20
                            .addTranslator(null) // Width

                            // ID 21
                            .addTranslator(null) // Height

                            // ID 22
                            .addTranslator(null) // Glow color override

                            .build();
            
            EntityTypeBase<NativeDisplayEntity> nativeBlockDisplayBase =
                    EntityTypeBase.baseInherited(
                                    NativeDisplayEntity.class,
                                    entityBase
                            )

                            /*
                             * Display metadata IDs 8-10
                             */
                            .addTranslator(null) // Interpolation delay
                            .addTranslator(null) // Transformation interpolation duration
                            .addTranslator(null) // Position/rotation interpolation duration

                            /*
                             * Display metadata IDs 11-14
                             */
                            .addTranslator(
                                    MetadataTypes.VECTOR3,
                                    NativeDisplayEntity::setTranslation
                            )

                            .addTranslator(
                                    MetadataTypes.VECTOR3,
                                    NativeDisplayEntity::setDisplayScale
                            )

                            .addTranslator(
                                    MetadataTypes.QUATERNION,
                                    NativeDisplayEntity::setLeftRotation
                            )

                            .addTranslator(
                                    MetadataTypes.QUATERNION,
                                    NativeDisplayEntity::setRightRotation
                            )

                            /*
                             * Remaining display-base metadata
                             */
                            .addTranslator(null) // Billboard
                            .addTranslator(null) // Brightness
                            .addTranslator(null) // View range
                            .addTranslator(null) // Shadow radius
                            .addTranslator(null) // Shadow strength
                            .addTranslator(null) // Width
                            .addTranslator(null) // Height
                            .addTranslator(null) // Glow color override

                            .build();

            BLOCK_DISPLAY =
                    VanillaEntityType.inherited(
                                    BlockDisplayEntity::new,
                                    slotDisplayBase
                            )
                            .type(
                                    EntityType.BLOCK_DISPLAY
                            )
                            .height(
                                    configManager
                                            .getConfig()
                                            .getFloat(
                                                    "general.height"
                                            )
                            )
                            .width(
                                    0.001f
                            )
                            .bedrockDefinition(
                                    BLOCK_DISPLAY_BEDROCK
                            )
                            .addTranslator(
                                    MetadataTypes.BLOCK_STATE,
                                    BlockDisplayEntity::setDisplayedBlockState
                            )
                            .build();

            ITEM_DISPLAY =
                    VanillaEntityType
                            .inherited(
                                    ItemDisplayEntity::new,
                                    slotDisplayBase
                            )
                            .type(
                                    EntityType.ITEM_DISPLAY
                            )
                            .height(
                                    configManager
                                            .getConfig()
                                            .getFloat(
                                                    "general.height"
                                            )
                            )
                            .width(
                                    0.001f
                            )
                            .bedrockDefinition(
                                    ITEM_DISPLAY_BEDROCK
                            )
                            .addTranslator(
                                    MetadataTypes.ITEM_STACK,
                                    ItemDisplayEntity::setDisplayedItem
                            )
                            .addTranslator(
                                    MetadataTypes.BYTE,
                                    ItemDisplayEntity::setDisplayType
                            )
                            .build();

            EntityUtils.replaceJavaDefinition(
                    EntityType.BLOCK_DISPLAY,
                    BLOCK_DISPLAY
            );

            EntityUtils.replaceJavaDefinition(
                    EntityType.ITEM_DISPLAY,
                    ITEM_DISPLAY
            );

        } catch (Throwable err) {

            logger().error(
                    "Error in load",
                    err
            );
        }

        logger().info(
                "Done"
        );
    }

    /*
     * ========================================
     * COMMANDS
     * ========================================
     */

    @Subscribe
    public void onDefineCommand(
            GeyserDefineCommandsEvent event
    ) {

        ensureManagersLoaded();

        event.register(
                Command.builder(
                                this
                        )
                        .name(
                                "reload"
                        )
                        .source(
                                CommandSource.class
                        )
                        .playerOnly(
                                false
                        )
                        .description(
                                "GeyserDisplayEntity Reload Command"
                        )
                        .permission(
                                "geyserdisplayentity.commands.reload"
                        )
                        .executor(
                                (
                                        source,
                                        command,
                                        args
                                ) -> {

                                    configManager.load();

                                    source.sendMessage(
                                            configManager
                                                    .getLang()
                                                    .getString(
                                                            "commands.geyserdisplayentity.reload.successfully-reloaded"
                                                    )
                                    );
                                }
                        )
                        .build()
        );
    }

    /*
     * ========================================
     * DISPLAY PROPERTIES
     * ========================================
     */

    private void registerDisplayProperties(
            GeyserDefineEntityPropertiesEvent event,
            Identifier entityIdentifier
    ) {

        ensureManagersLoaded();

        Collection<GeyserEntityProperty<?>> existing =
                event.properties(
                        entityIdentifier
                );

        FileConfiguration entityConfig =
                configManager
                        .getEntityTypesCache()
                        .get(
                                entityIdentifier
                        );

        /*
         * Defensive check:
         *
         * If an entity config somehow failed to load,
         * log it instead of throwing another NPE.
         */
        if (entityConfig == null) {

            logger().warning(
                    "No entity configuration found for "
                            +
                            entityIdentifier
            );

            return;
        }

        FileConfiguration properties =
                entityConfig
                        .getConfigurationSection(
                                "properties"
                        );

        if (properties == null) {

            logger().warning(
                    "No properties section found for "
                            +
                            entityIdentifier
            );

            return;
        }

        for (Object entityKey :
                properties
                        .getRootNode()
                        .childrenMap()
                        .keySet()) {

            String entityString =
                    entityKey.toString();

            FileConfiguration propertyConfig =
                    entityConfig
                            .getConfigurationSection(
                                    "properties."
                                            +
                                            entityString
                            );

            if (propertyConfig == null) {
                continue;
            }

            String propertyType =
                    propertyConfig
                            .getString(
                                    "property-type"
                            );

            if ("integer".equals(
                    propertyType
            )) {

                EntityUtils.registerInteger(
                        event,
                        existing,
                        entityIdentifier,
                        propertyConfig.getString(
                                "id"
                        ),
                        propertyConfig.getInt(
                                "min-value"
                        ),
                        propertyConfig.getInt(
                                "max-value"
                        ),
                        propertyConfig.getInt(
                                "default-value"
                        )
                );

            } else if ("float".equals(
                    propertyType
            )) {

                EntityUtils.registerFloat(
                        event,
                        existing,
                        entityIdentifier,
                        propertyConfig.getString(
                                "id"
                        ),
                        propertyConfig.getInt(
                                "min-value"
                        ),
                        propertyConfig.getInt(
                                "max-value"
                        ),
                        propertyConfig.getFloat(
                                "default-value"
                        )
                );
            }
        }
    }

    /*
     * ========================================
     * MANAGER INITIALIZATION FIX
     * ========================================
     */

    private synchronized void ensureManagersLoaded() {

        /*
         * FileConfiguration internally calls
         * GeyserDisplayEntity.getExtension(),
         * therefore the static extension reference
         * MUST be assigned before constructing
         * ConfigManager.
         */
        if (extension == null) {

            extension =
                    this;
        }

        if (configManager == null) {

            logger().info(
                    "Initializing GeyserDisplayEntity configuration manager..."
            );

            configManager =
                    new ConfigManager(
                            this
                    );

            logger().info(
                    "GeyserDisplayEntity configuration manager initialized."
            );
        }
    }

    public static GeyserDisplayEntity getExtension() {

        return extension;
    }

    public ConfigManager getConfigManager() {

        ensureManagersLoaded();

        return configManager;
    }
}