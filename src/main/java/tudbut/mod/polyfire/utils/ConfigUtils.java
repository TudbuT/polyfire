package tudbut.mod.polyfire.utils;

import de.tudbut.io.StreamWriter;
import de.tudbut.tools.Tools;
import tudbut.debug.DebugProfiler;
import tudbut.mod.polyfire.PolyFire;
import tudbut.mod.polyfire.utils.isbpl.ISBPLModule;
import tudbut.parsing.JSON;
import tudbut.parsing.TCN;
import tudbut.tools.ConfigSaverTCN;

import java.io.FileOutputStream;

public class ConfigUtils {
    
    public static String make(PolyFire pf) {
        return Tools.mapToString(makeTCN(pf).toMap());
    }
    
    public static TCN makeTCN(PolyFire pf) {
        TCN tcn = new TCN();
    
        tcn.set("init", "true");
        
        makeClient(pf, tcn);
        makeModules(tcn);
        
        return tcn;
    }
    
    private static void makeClient(PolyFire pf, TCN tcn) {
        try {
            TCN cfg = ConfigSaverTCN.saveConfig(pf);
            
            tcn.set("client", cfg);
        } catch (Exception e) {
            System.err.println("Couldn't save config of client");
            e.printStackTrace();
            tcn.set("init", null);
        }
    }
    
    private static void makeModules(TCN tcn) {
        TCN cfg = new TCN();
    
        for (int i = 0; i < PolyFire.modules.length; i++) {
            Module module = PolyFire.modules[i];

            try {
                module.onConfigSave();
                if(module instanceof JSModule) {
                    StreamWriter writer = new StreamWriter(new FileOutputStream("config/pf/modules/config/" + ((JSModule) module).id + ".jsmodulecfg.json"));
                    writer.writeChars(JSON.writeReadable(ConfigSaverTCN.saveConfig(module)).toCharArray());
                    continue;
                }
                if(module instanceof ISBPLModule) {
                    StreamWriter writer = new StreamWriter(new FileOutputStream("config/pf/modules/config/" + ((ISBPLModule) module).id + ".isbplmodulecfg.json"));
                    writer.writeChars(JSON.writeReadable(ConfigSaverTCN.saveConfig(module)).toCharArray());
                    continue;
                }

                TCN moduleTCN = ConfigSaverTCN.saveConfig(module);
                cfg.set(module.toString(), moduleTCN);
            } catch (Exception e) {
                System.err.println("Couldn't save config of module " + module.toString());
                e.printStackTrace();
                tcn.set("init", null);
            }
        }
        
        tcn.set("modules", cfg);
    }
    
    public static void load(PolyFire ttcp, String config) {
        try {
            System.out.println("Reading as TCNMap...");
            TCN tcn = TCN.readMap(Tools.stringToMap(config));
            if (!tcn.getBoolean("init"))
                throw new Exception();
            System.out.println("Done");
            loadTCN(ttcp, tcn);
        }
        catch (Exception e0) {
            System.err.println("Couldn't load config as TCNMap");
            try {
                System.out.println("Reading as TCN...");
                TCN tcn = TCN.read(config);
                System.out.println("Done");
                loadTCN(ttcp, tcn);
            }
            catch (Exception e1) {
                System.err.println("Couldn't load config");
            }
        }
    }
    
    public static void loadTCN(PolyFire pf, TCN tcn) {
        loadClient(pf, tcn);
        loadModules(tcn);
    }
    
    private static void loadClient(PolyFire pf, TCN tcn) {
        try {
            ConfigSaverTCN.loadConfig(pf, tcn.getSub("client"));
        } catch (Exception e) {
            System.err.println("Couldn't load config of client");
            e.printStackTrace();
        }
    }
    
    private static void loadModules(TCN tcn) {
        tcn = tcn.getSub("modules");
    
        DebugProfiler profiler = new DebugProfiler("ConfigLoadProfiler", "init");
        
        for (int i = 0; i < PolyFire.modules.length; i++) {
            Module module = PolyFire.modules[i];
            profiler.next(module.toString());

            if(module instanceof JSModule || module instanceof ISBPLModule)
                continue;

            if(module.enabled) {
                module.enabled = false;
                module.onDisable();
            }
            
            try {
                ConfigSaverTCN.loadConfig(module, tcn.getSub(module.toString()));
                try {
                    if (module.enabled)
                        module.onEnable();
                } catch (NullPointerException ignored) { }
                module.onConfigLoad();
                module.updateBindsFull();
            }
            catch (Exception e) {
                module.enabled = module.defaultEnabled();
                System.err.println("Couldn't load config of module " + module.toString());
                e.printStackTrace();
            }
        }
        
        profiler.endAll();
        System.out.println(profiler.getResults());
        profiler.delete();
    }
}
