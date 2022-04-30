package tudbut.mod.polyfire.utils.isbpl;

import de.tudbut.io.StreamReader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import tudbut.mod.polyfire.utils.ChatUtils;
import tudbut.mod.polyfire.utils.Module;
import tudbut.obj.Save;
import tudbut.parsing.JSON;
import tudbut.parsing.TCN;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ISBPLModule extends Module {

    private final ISBPL context;
    public final String id;
    
    public TCN config = new TCN();

    public Stack<ISBPLObject> run(String code, Object... args) {
        Stack<ISBPLObject> stack = new Stack<>();
        for (int i = 0 ; i < args.length ; i++) {
            stack.push(context.toISBPL(args[i]));
        }
        context.interpret(new File("_eval"), code, stack);
        return stack;
    }
    
    public boolean functionExists(String fn) {
        return context.frameStack.get().peek().all().containsKey(fn);
    }
    
    @Override
    public String toString() {
        String s = "ISBPLModule (ERROR)";
        try {
            s = context.toJavaString(run("name").pop());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return s;
    }

    public ISBPLModule(ISBPL context, String id) {
        this.context = context;
        try {
            context.natives.put("jm", stack -> stack.push(context.toISBPL(this)));
            context.natives.put("mc", stack -> stack.push(context.toISBPL(Minecraft.getMinecraft())));
            context.interpret(new File("_pf"), "native mc native jm", new Stack<>());
            run("def config jm config =config");
            run("def sb jm subComponents =sb");
            run("def Setting \"tudbut.mod.client.ttcp.utils.Setting\" JIO class =Setting");
        } catch (Throwable e) {
            e.printStackTrace();
        }
        this.id = id;

        key = new KeyBind(null, toString() + "::toggle", true);
        updateBinds();
    }

    @Override
    public boolean onPacket(Packet<?> packet) {
        try {
            if (functionExists("onPacket"))
                return run("onPacket", packet).pop().isTruthy();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void updateBinds() {
        try {
            if(functionExists("updateBinds"))
                run("updateBinds");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        try {
            if(functionExists("onEnable"))
                run("onEnable");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        try {
            if(functionExists("onDisable"))
                run("onDisable");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTick() {
        try {
            if(functionExists("onTick"))
                run("onTick");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEveryTick() {
        try {
            if(functionExists("onEveryTick"))
                run("onEveryTick");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSubTick() {
        try {
            if(functionExists("onSubTick"))
                run("onSubTick");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEverySubTick() {
        try {
            if(functionExists("onEverySubTick"))
                run("onEverySubTick");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onServerChat(String s, String formatted) {
        try {
            if (functionExists("onServerChat"))
                return run("onServerChat", s, formatted).pop().isTruthy();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onChat(String s, String[] args) {
        try {
            if(functionExists("onChat"))
                run("onChat", s, args);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEveryChat(String s, String[] args) {
        try {
            if(functionExists("onEveryChat"))
                run("onEveryChat", s, args);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void printChat(String toPrint) {
        ChatUtils.print(toPrint);
    }

    @Save
    String cfgStr = "{}";

    @Override
    public void onConfigSave() {
        cfgStr = JSON.write(config);
    }

    @Override
    public void onConfigLoad() {
        try {
            config = JSON.read(cfgStr);
        }
        catch (JSON.JSONFormatException e) {
            e.printStackTrace();
        }
    }

    public static class Loader {

        static {
            try {
                URL url = new URL("https://codeload.github.com/TudbuT/isbpl/zip/refs/heads/master");
                ZipInputStream inputStream = new ZipInputStream(url.openStream());
                ZipEntry entry;
                while ((entry = inputStream.getNextEntry()) != null) {
                    File associatedFile = new File("config/pf/isbpl", entry.getName().substring("isbpl-master/".length()));
                    if(entry.isDirectory()) {
                        associatedFile.mkdirs();
                    }
                    else {
                        associatedFile.getParentFile().mkdirs();
                        FileOutputStream stream = new FileOutputStream(associatedFile);
                        stream.write(new StreamReader(inputStream).readAllAsBytes());
                        stream.close();
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Unable to download ISBPL");
            }
        }
        
        public static ISBPLModule create(String id) {
            try {
                Stack<ISBPLObject> stack = new Stack<>();
                ISBPL context = new ISBPL();
                File std = new File("config/pf/isbpl/std.isbpl");
                context.interpret(std, ISBPL.readFile(std), stack);
                File file = new File("config/pf/modules/" + id + ".pfmodule.isbpl").getAbsoluteFile();
                context.interpret(file, ISBPL.readFile(file), stack);
                return new ISBPLModule(context, id);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
