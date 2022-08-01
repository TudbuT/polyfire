package tudbut.mod.polyfire;

import de.tudbut.async.Task;
import de.tudbut.io.StreamReader;
import de.tudbut.pluginapi.Plugin;
import de.tudbut.pluginapi.PluginManager;
import de.tudbut.tools.FileRW;
import de.tudbut.tools.Tools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tudbut.mod.polyfire.events.EventHandler;
import tudbut.mod.polyfire.mods.*;
import tudbut.mod.polyfire.utils.*;
import tudbut.obj.Save;
import tudbut.parsing.TCN;
import tudbut.tools.Lock;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Date;

@Mod(modid = PolyFire.MODID, name = PolyFire.NAME, version = PolyFire.VERSION)
public class PolyFire {
    // FML stuff and version
    public static final String MODID = "polyfire";
    public static final String NAME = "PolyFire";
    public static final String VERSION = "v1.1.5a";
    // TODO: PLEASE change this when skidding or rebranding.
    //  It is used for analytics and doesn't affect gameplay
    public static final String BRAND = "TudbuT/polyfire:master";
    
    // Registered modules, will make an api for it later
    public static Module[] modules;
    // Plugins
    public static Plugin[] plugins;
    // Player and current World(/Dimension), updated regularly in FMLEventHandler
    public static EntityPlayerSP player;
    public static World world;
    // Current Minecraft instance running
    public static Minecraft mc;
    // Config
    public static FileRW file;
    // Data
    public static TCN data;
    // Prefix for chat-commands
    @Save
    public static String prefix = "-";

    public static HashMap<String, String> obfMap = new HashMap<>();
    public static HashMap<String, String> deobfMap = new HashMap<>();
    
    // Logger, provided by Forge
    public static Logger logger = LogManager.getLogger("polyfire");
    
    
    public static Task<Void> deobfTask;
    
    private static PolyFire instance;
    
    public static PolyFire getInstance() {
        return instance;
    }

    static {
        // Preload and exclude graaljs because minecraft.

        try {
            ClassLoader.getSystemClassLoader().loadClass("org.graalvm.polyglot.Context");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Launch.classLoader.addClassLoaderExclusion("com.oracle.");
        Launch.classLoader.addClassLoaderExclusion("org.graalvm.");
        Launch.classLoader.addClassLoaderExclusion("com.ibm.icu.");
        Launch.classLoader.addTransformerExclusion("com.oracle.");
        Launch.classLoader.addTransformerExclusion("org.graalvm.");
        Launch.classLoader.addTransformerExclusion("com.ibm.icu.");
    }
    
    {instance = this;}
    
    // Runs a slight moment after the game is started, not all mods are initialized yet
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {

        logger = event.getModLog();
        try {
            new File("config/pf/").mkdirs();
            file = new FileRW("config/pf/main.cfg");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    
        deobfTask = createDeobfMap();
        deobfTask
                .err(Throwable::printStackTrace)
                .ok();
    }
    
    private Task<Void> createDeobfMap() {
        return new Task<>((res, rej) -> {
            if (!isObfEnv())
                return;
            try {
                String[] srg = new StreamReader(ClassLoader.getSystemResourceAsStream("minecraft_obf.srg")).readAllAsString().split("\n");
                
                for (int i = 0 ; i < srg.length ; i+=1000) {
                    int finalI = i;
                    new Task<Void>((res1, rej1) -> {
                        for(int n = finalI; n < srg.length && n - finalI < 1000; n++) {
                            if (srg[n].isEmpty()) {
                                continue;
                            }
                            String[] srgLine = srg[n].split(" ");
                            if (srgLine[0].equals("FD:") || srgLine[0].equals("CL:")) {
                                String out = srgLine[1];
                                String in = srgLine[srgLine.length - 1];
                                obfMap.put(out, in);
                                deobfMap.put(in, out);
                            }
                            if (srgLine[0].equals("MD:")) {
                                String out = srgLine[1];
                                String in = srgLine[3];
                                obfMap.put(out, in);
                                deobfMap.put(in, out);
                            }
                        }
                    }).ok().await();
                }
                res.call(null);
            }
            catch (Exception e) {
                rej.call(e);
            }
        });
    }

    // Runs when all important info is loaded and all mods are pre-initialized,
    // most game objects exist already when this is called
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        mc = Minecraft.getMinecraft();
        logger.info("PolyFire by TudbuT");
        
        mc.gameSettings.autoJump = false; // Fuck AutoJump, disable it on startup
        
        long sa; // For time measurements

        System.out.println("Init...");
        sa = new Date().getTime();
        
        data = Utils.getData();
        
        sa = new Date().getTime() - sa;
        System.out.println("Done in " + sa + "ms");
        
        System.out.println("Waiting for deobfTask");
        sa = new Date().getTime();
        deobfTask.await();
        sa = new Date().getTime() - sa;
        System.out.println("Done in " + sa + "ms");
        
        System.out.println("Constructing modules...");
        sa = new Date().getTime();
        // Constructing modules to be usable
        modules = new Module[] {
                new ClickGUI(),
                new JSModules(),
                new ISBPLModules()
        };
        sa = new Date().getTime() - sa;
        System.out.println("Done in " + sa + "ms");
        
        // Registering event handlers
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        
        System.out.println("Loading config...");
        sa = new Date().getTime();
    
        // Loading config from config/pf/main.cfg
        loadConfig();
        
        sa = new Date().getTime() - sa;
        System.out.println("Done in " + sa + "ms");
        
        System.out.println("Starting threads...");
        sa = new Date().getTime();
        
        boolean[] b = {true, true};
    
        // Starting thread to regularly save config
        Thread saveThread = ThreadManager.run(() -> {
            Lock lock = new Lock();
            while (b[0]) {
                lock.lock(5000);
                try {
                    saveConfig();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                lock.waitHere();
            }
            b[1] = false;
        });
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            b[0] = false;
            Lock timer = new Lock();
            timer.lock(5000);
            while(saveThread.isAlive() && b[1] && timer.isLocked());
            try {
                saveConfig();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        sa = new Date().getTime() - sa;
        System.out.println("Done in " + sa + "ms");
        
        System.out.println("Loading plugins...");
        sa = new Date().getTime();
    
        sa = new Date().getTime() - sa;
        System.out.println("Done in " + sa + "ms");

    
        System.out.println("Initializing modules...");
        sa = new Date().getTime();
    
        for (int i = 0 ; i < modules.length ; i++) {
            modules[i].init();
        }
        
        sa = new Date().getTime() - sa;
        System.out.println("Done in " + sa + "ms");
    }
    
    TCN cfg = null;
    
    public void saveConfig() throws IOException {
        setConfig();
        
        // Saving file
        file.setContent(Tools.mapToString(cfg.toMap()));
    }
    
    public void saveConfig(String file) throws IOException {
        PolyFire.file = new FileRW("config/pf/" + file + ".cfg");
        saveConfig();
    }
    
    public void setConfig(String file) throws IOException {
        saveConfig();
        PolyFire.file = new FileRW("config/pf/" + file + ".cfg");
        loadConfig();
        setConfig();
    }
    
    public void setConfig() {
        cfg = ConfigUtils.makeTCN(this);
    }
    
    public void loadConfig() {
        ConfigUtils.load(this, file.getContent().join("\n"));
    }
    
    public static boolean isIngame() {
        if(mc == null)
            return false;
        return mc.world != null && mc.player != null && mc.playerController != null ;
    }

    public static void addModule(Module module) {
        ArrayList<Module> list = new ArrayList<>(Arrays.asList(modules));
        list.add(module);
        modules = list.toArray(new Module[0]);
    }
    
    public static void removeModule(Module module) {
        ArrayList<Module> list = new ArrayList<>(Arrays.asList(modules));
        list.remove(module);
        modules = list.toArray(new Module[0]);
    }
    
    public static <T extends Module> T getModule(Class<? extends T> module) {
        for (int i = 0; i < modules.length; i++) {
            if(modules[i].getClass() == module) {
                return (T) modules[i];
            }
        }
        throw new IllegalArgumentException();
    }
    public static <T extends Module> T getModule(String module) {
        for (int i = 0; i < modules.length; i++) {
            if(modules[i].toString().equals(module)) {
                return (T) modules[i];
            }
        }
        return null;
    }
    
    public static Class<? extends Module> getModuleClass(String s) {
        for (int i = 0; i < modules.length; i++) {
            if(modules[i].toString().equals(s)) {
                return modules[i].getClass();
            }
        }
        return Module.class;
    }

    static Boolean obfEnvCached;
    public static boolean isObfEnv() {
        if(obfEnvCached == null) {
            try {
                Minecraft.class.getDeclaredField("world");
                obfEnvCached = false;
            } catch (NoSuchFieldException e) {
                obfEnvCached = true;
            }
        }
        return obfEnvCached;
    }
}
