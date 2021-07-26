package tudbut.mod.polyfire.mods;

import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import tudbut.mod.polyfire.PolyFire;
import tudbut.mod.polyfire.gui.GuiPF;
import tudbut.mod.polyfire.utils.ChatUtils;
import tudbut.mod.polyfire.utils.Module;
import tudbut.mod.polyfire.utils.Setting;
import tudbut.obj.Save;

import java.io.IOException;

public class ClickGUI extends Module {
    
    static ClickGUI instance;
    // TMP fix for mouse not showing
    @Save
    public boolean mouseFix = false;
    
    @Save
    public boolean flipButtons = false;
    
    @Save
    public int themeID = 0;
    
    public GuiPF.ITheme customTheme = null;
    
    public GuiPF.ITheme getTheme() {
        if(customTheme != null)
            return customTheme;
        return GuiPF.Theme.values()[themeID];
    }
    
    private int confirmInstance = 0;
    
    public ClickGUI() {
        instance = this;
        clickGuiShow = true;
    }
    
    public static ClickGUI getInstance() {
        return instance;
    }
    
    @Save
    public ScrollDirection sd = ScrollDirection.Vertical;
    
    public enum ScrollDirection {
        Vertical,
        Horizontal
    }
    
    public void updateBinds() {
        subButtons.clear();
        subButtons.add(new GuiPF.Button("Flip buttons: " + flipButtons, text -> {
            flipButtons = !flipButtons;
            text.set("Flip buttons: " + flipButtons);
        }));
        subButtons.add(new GuiPF.Button("Theme: " + getTheme(), text -> {
            if(customTheme == null) {
                if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
                    themeID--;
                else
                    themeID++;
    
                if (themeID < 0)
                    themeID = GuiPF.Theme.values().length - 1;
                if (themeID > GuiPF.Theme.values().length - 1)
                    themeID = 0;
    
                text.set("Theme: " + getTheme());
            }
        }));
        subButtons.add(Setting.createEnum(ScrollDirection.class, "Scroll: $val", this, "sd"));
        subButtons.add(new GuiPF.Button("Reset layout", text -> {
            displayConfirmation = true;
            confirmInstance = 0;
        }));
        subButtons.add(new GuiPF.Button("Mouse fix: " + mouseFix, text -> {
            mouseFix = !mouseFix;
            text.set("Mouse fix: " + mouseFix);
        }));
        subButtons.add(new GuiPF.Button("Reset client", text -> {
            displayConfirmation = true;
            confirmInstance = 1;
        }));
    }
    
    @Override
    public void onEnable() {
        // Show the GUI
        try {
            ChatUtils.print("Showing ClickGUI");
            PolyFire.mc.displayGuiScreen(new GuiPF());
        } catch (Exception e) {
            e.printStackTrace();
            enabled = false;
        }
    }
    
    @Override
    public void onConfirm(boolean result) {
        if (result)
            switch (confirmInstance) {
                case 0:
                    // Reset ClickGUI by closing it, resetting its values, and opening it
                    enabled = false;
                    onDisable();
                    for (Module module : PolyFire.modules) {
                        module.clickGuiX = null;
                        module.clickGuiY = null;
                    }
                    enabled = true;
                    onEnable();
                    break;
                case 1:
                    displayConfirmation = true;
                    confirmInstance = 2;
                    break;
                case 2:
                    enabled = false;
                    onDisable();
    
                    // Saving file
                    try {
                        PolyFire.file.setContent("");
                        PolyFire.file = null;
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    Minecraft.getMinecraft().shutdown();
                    break;
            }
    }
    
    @Override
    public void onDisable() {
        System.out.println("Hiding ClickGUI");
        // Kill the GUI
        if (PolyFire.mc.currentScreen != null && PolyFire.mc.currentScreen.getClass() == GuiPF.class)
            PolyFire.mc.displayGuiScreen(null);
    }
    
    @Override
    public void onEveryTick() {
        if(key.key == null) {
            key.key = Keyboard.KEY_0;
            updateBindsFull();
        }
    }
    
    @Override
    public void onChat(String s, String[] args) {
    
    }
}
