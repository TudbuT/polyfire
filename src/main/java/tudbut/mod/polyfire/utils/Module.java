package tudbut.mod.polyfire.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.Packet;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;
import tudbut.mod.polyfire.PolyFire;
import tudbut.mod.polyfire.gui.GuiPF;
import tudbut.obj.Save;
import tudbut.obj.TLMap;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

public abstract class Module {
    // Collection of event listeners and config loader/saver
    
    // Stuff for the construction of the module
    private static int cIndex = 0;
    public int index;

    protected Minecraft mc = Minecraft.getMinecraft();
    public EntityPlayerSP player = null;
    
    @Save
    public boolean enabled = defaultEnabled();
    @Save
    public boolean clickGuiShow = false;
    @Save
    public Integer clickGuiX;
    @Save
    public Integer clickGuiY;
    @Save
    public KeyBind key = new KeyBind(null, toString() + "::toggle", true);
    public ArrayList<GuiPF.Button> subButtons = new ArrayList<>();
    
    {
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @Save
    public TLMap<String, KeyBind> customKeyBinds = new TLMap<>();
    
    private GuiPF.Button[] confirmationButtons = new GuiPF.Button[3];
    
    {
        confirmationButtons[0] = new GuiPF.Button("Are you sure?", text -> {});
        confirmationButtons[1] = new GuiPF.Button("Yes", text -> {
            //noinspection UnusedAssignment no, it is!
            displayConfirmation = false;
            onConfirm(true);
        });
        confirmationButtons[2] = new GuiPF.Button("No", text -> {
            //noinspection UnusedAssignment no, it is!
            displayConfirmation = false;
            onConfirm(false);
        });
    }
    GuiPF.Button keyButton = Setting.createKey("KeyBind: $val", key);
    
    public Module() {
        index = cIndex;
        cIndex++;
    }
    
    public void updateBindsFull() {
        keyButton = Setting.createKey("KeyBind: $val", key);
        updateBinds();
    }
    
    public void updateBinds() {
    
    }
    
    public void toggle() {
        enabled = !enabled;
        if (enabled) {
            onEnable();
            ChatUtils.print("§a" + toString() + " ON");
        } else {
            onDisable();
            ChatUtils.print("§c" + toString() + " OFF");
        }
    }
    
    protected boolean displayConfirmation = false;
    
    public final GuiPF.Button[] getSubButtons() {
        if(displayConfirmation)
            return confirmationButtons;
        ArrayList<GuiPF.Button> buttons = (ArrayList<GuiPF.Button>) subButtons.clone();
        buttons.add(keyButton);
        return buttons.toArray(new GuiPF.Button[0]);
    }
    
    // Defaults to override
    public boolean defaultEnabled() {
        return false;
    }
    
    public boolean doStoreEnabled() {
        return true;
    }
    
    public boolean displayOnClickGUI() {
        return true;
    }
    
    // Event listeners
    public void onSubTick() { }
    
    public void onEverySubTick() { }
    
    public void onTick() { }
    
    public void onEveryTick() { }
    
    public void onConfirm(boolean result) { }
    
    public void onChat(String s, String[] args) { }
    
    public void onEveryChat(String s, String[] args) { }
    
    public void onEnable() { }
    
    public void onDisable() { }
    
    public boolean onServerChat(String s, String formatted) {
        return false;
    }
    
    public void onConfigLoad() {
    }

    public void onConfigSave() {
    }

    public void init() {
    }
    
    public int danger() {
        return 0;
    }
    
    // Return the module name
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
    
    private Module get() {
        return this;
    }
    
    public boolean onPacket(Packet<?> packet) {
        return false;
    }
    
    public static class KeyBind {
        public Integer key = null;
        public boolean down = false;
        public String onPress;
        public boolean alwaysOn;
    
        public KeyBind() {
        }
        
        public KeyBind(Integer key, String onPress, boolean alwaysOn) {
            this.key = key;
            this.onPress = onPress;
            this.alwaysOn = alwaysOn;
        }
        
        public void onTick() {
            if(key != null && PolyFire.mc.currentScreen == null) {
                if (Keyboard.isKeyDown(key)) {
                    if(!down) {
                        down = true;
                        if(onPress != null) {
                            try {
                                Module m = PolyFire.getModule(onPress.split("::")[0]);
                                m.getClass().getMethod(onPress.split("::")[1]).invoke(m);
                            }
                            catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                else
                    down = false;
            }
            else
                down = false;
        }
    }
}
