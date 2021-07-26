package tudbut.mod.polyfire.mods;

import de.tudbut.io.StreamReader;
import de.tudbut.io.StreamWriter;
import tudbut.mod.polyfire.PolyFire;
import tudbut.mod.polyfire.gui.GuiPF;
import tudbut.mod.polyfire.utils.ChatUtils;
import tudbut.mod.polyfire.utils.JSModule;
import tudbut.mod.polyfire.utils.Module;
import tudbut.obj.Save;
import tudbut.obj.TLMap;
import tudbut.parsing.JSON;
import tudbut.parsing.TCN;
import tudbut.tools.ConfigSaverTCN;

import java.io.*;
import java.util.ArrayList;

public class JSModules extends Module {

    @Save
    ArrayList<String> jsModules = new ArrayList<>();

    TLMap<String, Module> modules = new TLMap<>();

    @Override
    public void updateBinds() {
        subButtons.clear();
        subButtons.add(new GuiPF.Button(PolyFire.prefix + "jsmodules add [id]", s->{}));
        subButtons.add(new GuiPF.Button(PolyFire.prefix + "jsmodules remove [id]", s->{}));
        subButtons.add(new GuiPF.Button(PolyFire.prefix + "jsmodules reload [id]", s->{}));
    }

    @Override
    public void onEnable() {
        for (String module : jsModules) {
            loadModule(module);
        }
        if(mc.currentScreen instanceof GuiPF) {
            ((GuiPF) mc.currentScreen).resetButtons();
        }
    }

    @Override
    public void onDisable() {
        for (String module : jsModules) {
            unloadModule(module);
        }
        if(mc.currentScreen instanceof GuiPF) {
            ((GuiPF) mc.currentScreen).resetButtons();
        }
    }

    public Module loadModule(String s) {
        try {
            FileInputStream stream = new FileInputStream("config/pf/modules/" + s + ".pfmodule.js");
            String js = new StreamReader(stream).readAllAsString();
            Module module = JSModule.Loader.createFromJS(js, s);
            if(module == null)
                return null;
            modules.set(s, module);
            try {
                try {
                    TCN tcn = JSON.read(new StreamReader(new FileInputStream("config/pf/modules/config/" + s + ".pfmodulecfg.json")).readAllAsString());
                    ConfigSaverTCN.loadConfig(module, tcn);
                    try {
                        if (module.enabled)
                            module.onEnable();
                    } catch (NullPointerException ignored) {
                    }
                } catch (Exception ignored) { }
                module.onConfigLoad();
                module.updateBindsFull();
            } catch (Exception e) {
                e.printStackTrace();
            }
            PolyFire.addModule(module);
            return module;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void unloadModule(String s) {
        if(modules.get(s) != null) {
            modules.get(s).onDisable();
            modules.get(s).onConfigSave();
            try {
                StreamWriter writer = new StreamWriter(new FileOutputStream("config/pf/modules/config/" + ((JSModule) modules.get(s)).id + ".pfmodulecfg.json"));
                writer.writeChars(JSON.writeReadable(ConfigSaverTCN.saveConfig(modules.get(s))).toCharArray());
            } catch (Exception ignored) { }
            PolyFire.removeModule(modules.get(s));
            modules.set(s, null);
        }
    }

    public void reloadModule(String s) {
        unloadModule(s);
        if(loadModule(s) == null) {
            ChatUtils.print("Couldn't load module " + s);
        }
    }

    @Override
    public void onEveryChat(String s, String[] args) {
        if(new File("config/pf/modules/config").mkdirs()) {
            ChatUtils.print("Put JSModule files in your config/pf/modules folder!");
        }
        try {
            StreamWriter writer = new StreamWriter(new FileOutputStream("config/pf/modules/Example.pfmodule.js"));
            writer.writeChars(("" +
                    "return {\n" +
                    "  name: 'Example',\n" +
                    "  onEnable: function() {\n" +
                    "    this.jm.printChat('Example module enabled!') // jm = The module\n" +
                    "    this.mc.player.swingArm(Java.type('net.minecraft.util.EnumHand').MAIN_HAND)\n" +
                    "  }\n" +
                    "};\n" +
                    "").toCharArray());
            writer.stream.close();
        } catch (IOException ignored) {
        }
        if(args.length == 2) {
            if(args[0].equalsIgnoreCase("add")) {
                jsModules.remove(args[1]);
                jsModules.add(args[1]);
                unloadModule(args[1]);
                if(loadModule(args[1]) != null)
                    ChatUtils.print("Loaded!");
                else
                    ChatUtils.print("Failed to load module. It seems to be faulty!");
            }
            if(args[0].equalsIgnoreCase("remove")) {
                jsModules.remove(args[1]);
                unloadModule(args[1]);
                ChatUtils.print("Unloaded!");
            }
            if(args[0].equalsIgnoreCase("reload")) {
                reloadModule(args[1]);
                ChatUtils.print("Reloaded!");
            }
        }
        if(args.length == 1) {
            if(args[0].equalsIgnoreCase("reload")) {
                for (String module : jsModules) {
                    reloadModule(module);
                }
                ChatUtils.print("Reloaded!");
            }
        }
    }
}
