package me.geyserextensionists.geyserdisplayentity.entity;

import org.cloudburstmc.math.imaginary.Quaternionf;
import org.cloudburstmc.math.vector.Vector3f;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataTypes;
import org.cloudburstmc.protocol.bedrock.data.entity.EntityFlag;
import org.geysermc.geyser.entity.spawn.EntitySpawnContext;
import org.geysermc.geyser.entity.type.Entity;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.EntityMetadata;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type.BooleanEntityMetadata;
import org.geysermc.mcprotocollib.protocol.data.game.entity.metadata.type.IntEntityMetadata;

public class NativeDisplayEntity extends Entity {

    private Quaternionf lastLeft =
            Quaternionf.IDENTITY;

    private Quaternionf lastRight =
            Quaternionf.IDENTITY;

    private Vector3f displayScale =
            Vector3f.from(
                    1.0f,
                    1.0f,
                    1.0f
            );

    public NativeDisplayEntity(
            EntitySpawnContext context
    ) {
        super(context);
    }

    /*
     * Java BlockDisplay translation.
     *
     * Our Shifter logs proved that these values are
     * currently always (0,0,0), and the actual model
     * coordinates are already stored in the entity's
     * world position.
     *
     * Therefore do NOT move the Bedrock entity here.
     */
    public void setTranslation(
            EntityMetadata<Vector3f, ?> entityMetadata
    ) {
        // Intentionally ignored.
    }

    /*
     * Shifter uses uniform scales:
     *
     * 15x15x15
     * 16x16x16
     * 17x17x17
     *
     * Bedrock's native falling-block entity has a
     * normal SCALE metadata property, so use that
     * instead of custom Molang properties.
     */
    public void setDisplayScale(
            EntityMetadata<Vector3f, ?> entityMetadata
    ) {

        Vector3f value =
                entityMetadata.getValue();

        if (value == null) {
            value =
                    Vector3f.from(
                            1.0f,
                            1.0f,
                            1.0f
                    );
        }

        this.displayScale =
                value;

        /*
         * Native Bedrock entity scaling is scalar.
         *
         * Shifter uses uniform XYZ scale, so X is
         * sufficient.
         */
        this.metadata.put(
                EntityDataTypes.SCALE,
                value.getX()
        );

        updateBedrockMetadata();
    }

    public void setLeftRotation(
            EntityMetadata<Quaternionf, ?> entityMetadata
    ) {

        Quaternionf value =
                entityMetadata.getValue();

        this.lastLeft =
                value != null
                        ? value
                        : Quaternionf.IDENTITY;

        applyCombinedYaw();
    }

    public void setRightRotation(
            EntityMetadata<Quaternionf, ?> entityMetadata
    ) {

        Quaternionf value =
                entityMetadata.getValue();

        this.lastRight =
                value != null
                        ? value
                        : Quaternionf.IDENTITY;

        applyCombinedYaw();
    }

    /*
     * This is the same quaternion combination that
     * produced the correct V8 yaw values.
     *
     * Do NOT negate javaYaw.
     */
    private float getCombinedYaw() {

        Quaternionf left =
                lastLeft != null
                        ? lastLeft.normalize()
                        : Quaternionf.IDENTITY;

        Quaternionf right =
                lastRight != null
                        ? lastRight.normalize()
                        : Quaternionf.IDENTITY;

        float lw = left.getW();
        float lx = left.getX();
        float ly = left.getY();
        float lz = left.getZ();

        float rw = right.getW();
        float rx = right.getX();
        float ry = right.getY();
        float rz = right.getZ();

        /*
         * combined = left * right
         */
        float w =
                lw * rw
                        - lx * rx
                        - ly * ry
                        - lz * rz;

        float x =
                lw * rx
                        + lx * rw
                        + ly * rz
                        - lz * ry;

        float y =
                lw * ry
                        - lx * rz
                        + ly * rw
                        + lz * rx;

        float z =
                lw * rz
                        + lx * ry
                        - ly * rx
                        + lz * rw;

        double sinYaw =
                2.0 * (
                        w * y
                                + x * z
                );

        double cosYaw =
                1.0
                        - 2.0 * (
                        y * y
                                + z * z
                );

        float javaYaw =
                (float) Math.toDegrees(
                        Math.atan2(
                                sinYaw,
                                cosYaw
                        )
                );

        return wrapDegrees(
                javaYaw
        );
    }

    private void applyCombinedYaw() {

        float yaw =
                getCombinedYaw();

        /*
         * moveAbsolute() safely updates the stored yaw
         * even before the entity has spawned.
         *
         * Once valid, it also sends the native Bedrock
         * movement/rotation packet.
         */
        moveAbsolute(
                position,
                yaw,
                0.0f,
                false,
                true
        );
    }

    /*
     * The Java BlockDisplay's actual block-state metadata
     * is translated directly into the equivalent Bedrock
     * block state.
     */
    public void setDisplayedBlockState(
            IntEntityMetadata blockState
    ) {

        int javaBlockState =
                blockState.getPrimitiveValue();

        var bedrockBlock =
                session
                        .getBlockMappings()
                        .getBedrockBlock(
                                javaBlockState
                        );

        this.metadata.put(
                EntityDataTypes.BLOCK,
                bedrockBlock
        );

        /*
         * Important:
         *
         * BlockDisplay gets its block state from metadata
         * AFTER the entity can already have spawned.
         *
         * Therefore immediately push the metadata update.
         */
        updateBedrockMetadata();

        System.out.println(
                "[GeyserDisplayEntity] NATIVE BLOCK "
                        + "javaState="
                        + javaBlockState
                        + " bedrock="
                        + bedrockBlock
                        + " scale="
                        + displayScale
                        + " yaw="
                        + getCombinedYaw()
        );
    }

    /*
     * Match Geyser's FallingBlockEntity behavior.
     *
     * When gravity is disabled, NO_AI prevents the
     * Bedrock falling-block entity from attempting its
     * own client-side movement.
     */
    @Override
    public void setGravity(
            BooleanEntityMetadata entityMetadata
    ) {

        super.setGravity(
                entityMetadata
        );

        setFlag(
                EntityFlag.NO_AI,
                entityMetadata.getPrimitiveValue()
        );
    }

    private float wrapDegrees(
            float degrees
    ) {

        float wrapped =
                degrees % 360.0f;

        if (wrapped >= 180.0f) {
            wrapped -=
                    360.0f;
        }

        if (wrapped < -180.0f) {
            wrapped +=
                    360.0f;
        }

        return wrapped;
    }
}