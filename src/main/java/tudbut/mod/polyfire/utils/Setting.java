package tudbut.mod.polyfire.utils;

import org.lwjgl.input.Keyboard;
import tudbut.mod.polyfire.gui.GuiPF;

import java.lang.reflect.Field;

public class Setting {
    
    public static GuiPF.Button createInt(int min, int max, int step, String string, Module module, String field, Runnable onClick) {
        final int[] locVal = {(Integer) field(module, field)};
        return new GuiPF.Button(
                string.replaceAll("\\$val", String.valueOf(locVal[0])),
                text -> {
                    if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                        locVal[0] -= step;
                    }
                    else {
                        locVal[0] += step;
                    }
                    
                    if(locVal[0] < min) {
                        locVal[0] = max;
                    }
                    if(locVal[0] > max) {
                        locVal[0] = min;
                    }
                    
                    
                    field(module, field, locVal[0]);
                    text.set(string.replaceAll("\\$val", String.valueOf(locVal[0])));
                    onClick.run();
                }
        );
    }
    
    public static GuiPF.Button createEnum(Class<? extends Enum<?>> theEnum, String string, Module module, String field, Runnable onClick) {
        final int[] locVal = {field(module, field) == null ? 0 : ((Enum<?>) field(module, field)).ordinal()};
        return new GuiPF.Button(
                string.replaceAll("\\$val", theEnum.getEnumConstants()[locVal[0]].name()),
                text -> {
                    if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                        locVal[0] --;
                    }
                    else {
                        locVal[0] ++;
                    }
                    
                    if(locVal[0] < 0) {
                        locVal[0] = theEnum.getEnumConstants().length - 1;
                    }
                    if(locVal[0] > theEnum.getEnumConstants().length - 1) {
                        locVal[0] = 0;
                    }
                    
                    
                    field(module, field, theEnum.getEnumConstants()[locVal[0]]);
                    text.set(string.replaceAll("\\$val", theEnum.getEnumConstants()[locVal[0]].name()));
                    onClick.run();
                }
        );
    }
    
    public static GuiPF.Button createInt(int min, int max, int step, String string, Module module, String field) {
        return Setting.createInt(min, max, step, string, module, field, () -> {});
    }
    
    public static GuiPF.Button createEnum(Class<? extends Enum<?>> theEnum, String string, Module module, String field) {
        return Setting.createEnum(theEnum, string, module, field, () -> {});
    }
    
    public static GuiPF.Button createFloat(float min, float max, float step, String string, Module module, String field) {
        final float[] locVal = {(Float) field(module, field)};
        return new GuiPF.Button(
                string.replaceAll("\\$val", String.valueOf(locVal[0])),
                text -> {
                    if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                        locVal[0] -= step;
                    }
                    else {
                        locVal[0] += step;
                    }
                    
                    if(locVal[0] < min) {
                        locVal[0] = max;
                    }
                    if(locVal[0] > max) {
                        locVal[0] = min;
                    }
                    
                    field(module, field, locVal[0]);
                    text.set(string.replaceAll("\\$val", String.valueOf(locVal[0])));
                }
        );
    }
    
    public static GuiPF.Button createSecureFloat(int min, int max, int step, int dec, String string, Module module, String field) {
        final int[] locVal = {(int) ((Float) field(module, field) * dec)};
        return new GuiPF.Button(
                string.replaceAll("\\$val", String.valueOf((float) locVal[0] / dec)),
                text -> {
                    if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                        locVal[0] -= step;
                    }
                    else {
                        locVal[0] += step;
                    }
                    
                    if(locVal[0] < min) {
                        locVal[0] = max;
                    }
                    if(locVal[0] > max) {
                        locVal[0] = min;
                    }
                    
                    field(module, field, (float) locVal[0] / dec);
                    text.set(string.replaceAll("\\$val", String.valueOf((float) locVal[0] / dec)));
                }
        );
    }
    
    public static GuiPF.Button createBoolean(String string, Module module, String field) {
        final boolean[] locVal = {(Boolean) field(module, field)};
        return new GuiPF.Button(
                string.replaceAll("\\$val", String.valueOf(locVal[0])),
                text -> {
                    locVal[0] = !locVal[0];
                    
                    field(module, field, locVal[0]);
                    text.set(string.replaceAll("\\$val", String.valueOf(locVal[0])));
                }
        );
    }
    
    public static GuiPF.Button createKey(String string, Module.KeyBind keyBind) {
        return new GuiPF.Button(
                string.replaceAll("\\$val", keyBind.key == null ? "NONE" : Keyboard.getKeyName(keyBind.key)),
                text -> {
                    int i;
                    if ((i = getKeyPress()) != -1) {
                        keyBind.key = i;
                        text.set(string.replaceAll("\\$val", Keyboard.getKeyName(keyBind.key)));
                    }
                    else {
                        keyBind.key = null;
                        text.set(string.replaceAll("\\$val", "NONE (Hold)"));
                        new Thread(() -> {
                            try {
                                Thread.sleep(1000);
                            }
                            catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            text.set(string.replaceAll("\\$val", "NONE"));
                        }).start();
                    }
                }
        );
    }
    
    private static int getKeyPress() {
        for (int i = 0 ; i <= 0xff ; i++) {
            if(Keyboard.isKeyDown(i))
                return i;
        }
        return -1;
    }
    
    private static Object field(Module m, String s) {
        try {
            Field f = m.getClass().getDeclaredField(s);
            f.setAccessible(true);
            return f.get(m);
        }
        catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static void field(Module m, String s, Object o) {
        try {
            Field f = m.getClass().getDeclaredField(s);
            f.setAccessible(true);
            f.set(m, o);
        }
        catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
