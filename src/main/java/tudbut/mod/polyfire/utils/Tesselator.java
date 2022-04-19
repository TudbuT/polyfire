package tudbut.mod.polyfire.utils;

import de.tudbut.type.Vector3d;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import tudbut.net.ic.PBIC;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class Tesselator {
    
    static int mode;
    static int color;
    static Vector3d translated;
    static boolean depth;

    public static void ready() {
        glPushMatrix();
    }
    public static void translate(double x, double y, double z) {
        glTranslated(x,y,z);
        translated = new Vector3d(x,y,z);
    }
    public static void begin(int modeIn) {
        glBegin(mode = modeIn);
    }
    public static void color(int argb) {
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_LIGHTING);
        glEnable(GL_BLEND);
        glDisable(GL_CULL_FACE);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        byte[] bytes = PBIC.putInt(argb);
        glColor4ub(bytes[1], bytes[2], bytes[3], bytes[0]);
        color = argb;
    }
    public static void depth(boolean b) {
        depth = b;
        if(b)
            glEnable(GL_DEPTH_TEST);
        else
            glClear(GL_DEPTH_BUFFER_BIT);
    }
    public static void put(double x, double y, double z) {
        glVertex3d(x,y,z);
    }
    public static void end() {
        translated = null;
        color = 0;
        depth = false;
        mode = 0;
        glEnd();
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glEnable(GL_CULL_FACE);
        glPopMatrix();
    }
    public static void next() {
        // end current
        glEnd();
        glEnable(GL_TEXTURE_2D);
        glDisable(GL_BLEND);
        glEnable(GL_CULL_FACE);
        glPopMatrix();
        
        // start new
        glPushMatrix();
        glTranslated(translated.getX(), translated.getY(), translated.getZ());
        color(color);
        depth(depth);
        glBegin(mode);
    }

    /**
     * Draws an outline around a block.
     * @param pos The BlockPos of the block you want do draw an outline around.
     * @param color The RGB colour of the outline. Either as an int or Color.
     * @param eyePos The Eye Position of the player.
     * @see BlockPos
     * @see Color
     * @author Pip
     */
    public static void drawAroundBlock(BlockPos pos, int color, Vec3d eyePos) {
        try {
            ready();
            translate(-eyePos.x, -eyePos.y, -eyePos.z);
            color(color);
            depth(false);
            begin(GL11.GL_LINES);

            // bottom - just like tud
            put(pos.getX(), pos.getY(), pos.getZ());
            put(pos.getX() + 1, pos.getY(), pos.getZ());

            put(pos.getX(), pos.getY(), pos.getZ());
            put(pos.getX(), pos.getY(), pos.getZ() + 1);

            put(pos.getX() + 1, pos.getY(), pos.getZ());
            put(pos.getX() + 1, pos.getY(), pos.getZ() + 1);

            put(pos.getX(), pos.getY(), pos.getZ() + 1);
            put(pos.getX() + 1, pos.getY(), pos.getZ() + 1);

            //sides
            put(pos.getX(), pos.getY(), pos.getZ());
            put(pos.getX(), pos.getY() + 1, pos.getZ());

            put(pos.getX() + 1, pos.getY(), pos.getZ());
            put(pos.getX() + 1, pos.getY() + 1, pos.getZ());

            put(pos.getX(), pos.getY(), pos.getZ() + 1);
            put(pos.getX(), pos.getY() + 1, pos.getZ() + 1);

            put(pos.getX() + 1, pos.getY(), pos.getZ() + 1);
            put(pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);

            put(pos.getX() + 1, pos.getY(), pos.getZ());
            put(pos.getX() + 1, pos.getY() + 1, pos.getZ());

            // top - like Pip
            put(pos.getX(), pos.getY() + 1, pos.getZ());
            put(pos.getX() + 1, pos.getY() + 1, pos.getZ());

            put(pos.getX(), pos.getY() + 1, pos.getZ());
            put(pos.getX(), pos.getY() + 1, pos.getZ() + 1);

            put(pos.getX() + 1, pos.getY() + 1, pos.getZ());
            put(pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);

            put(pos.getX(), pos.getY() + 1, pos.getZ() + 1);
            put(pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);

            end();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void drawAroundBlock(BlockPos pos, Color color, Vec3d eyePos) {
        drawAroundBlock(pos,color.getRGB(),eyePos);
    }
}
