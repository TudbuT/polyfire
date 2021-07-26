package tudbut.mod.polyfire.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import tudbut.mod.polyfire.gui.GuiPF;
import tudbut.obj.Save;

import java.util.ArrayList;

public class JSModule extends Module {

    private final Context context;
    private final Value jsModule;
    public final String id;
    private Class<? extends Event>[] events = new Class[0];

    public ArrayList<GuiPF.Button> sb = new ArrayList<>();


    @Override
    public String toString() {
        if(jsModule == null)
            return "JSModule (ERROR)";
        else {
            String s = "JSModule (ERROR)";
            try {
                s = jsModule.getMember("name").asString();
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return s;
        }
    }

    public JSModule(Context context, Value jsModuleInput, String id) {
        this.context = context;
        jsModule = jsModuleInput;
        try {
            jsModule.putMember("jm", this);
            context.eval("js", "jsModuleObj.cfg = {}");
            jsModule.putMember("mc", Minecraft.getMinecraft());
            System.out.println("JSModule has: " + jsModule.getMemberKeys());
        } catch (Throwable e) {
            e.printStackTrace();
        }
        this.id = id;
        if(jsModule.hasMember("eventListeners"))
            this.events = jsModule.getMember("eventListeners").asHostObject();

        key = new KeyBind(null, toString() + "::toggle", true);
        updateBinds();
    }

    @SubscribeEvent
    public void onEvent(Event event) {
        for (int i = 0; i < events.length; i++) {
            if(events[i].isInstance(event)) {
                if (jsModule.hasMember("onEvent"))
                    jsModule.getMember("onEvent").execute(event);
                return;
            }
        }
    }

    @Override
    public boolean onPacket(Packet<?> packet) {
        try {
            if (jsModule.hasMember("onPacket"))
                return jsModule.getMember("onPacket").execute(packet).asBoolean();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void updateBinds() {
        if(jsModule == null)
            return;
        try {
            if(jsModule.hasMember("updateBinds"))
                jsModule.getMember("updateBinds").execute();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        subButtons = sb;
    }

    @Override
    public void onEnable() {
        try {
            if(jsModule.hasMember("onEnable"))
                jsModule.getMember("onEnable").execute();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        try {
            if(jsModule.hasMember("onDisable"))
                jsModule.getMember("onDisable").execute();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTick() {
        try {
            if(jsModule.hasMember("onTick")) {
                jsModule.getMember("onTick").execute();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEveryTick() {
        try {
            if(jsModule.hasMember("onEveryTick"))
                jsModule.getMember("onEveryTick").execute();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSubTick() {
        try {
            if(jsModule.hasMember("onSubTick"))
                jsModule.getMember("onSubTick").execute();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEverySubTick() {
        try {
            if(jsModule.hasMember("onEverySubTick"))
                jsModule.getMember("onEverySubTick").execute();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onServerChat(String s, String formatted) {
        try {
            if (jsModule.hasMember("onServerChat"))
                return jsModule.getMember("onServerChat").execute(s, formatted).asBoolean();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onChat(String s, String[] args) {
        try {
            if(jsModule.hasMember("onChat"))
                jsModule.getMember("onChat").execute(s, args);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEveryChat(String s, String[] args) {
        try {
            if(jsModule.hasMember("onEveryChat"))
                jsModule.getMember("onEveryChat").execute(s, args);
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
        cfgStr = context.eval("js", "JSON.stringify(jsModuleObj.cfg)").asString();
    }

    @Override
    public void onConfigLoad() {
        context.eval("js", "jsModuleObj.cfg = " + cfgStr);
    }

    public static class Loader {

        public static JSModule createFromJS(String js, String id) {
            try {
                Context context = JSFieldMapper.createMapperContext();
                return new JSModule(context, context.eval("js",
                        "" +
                                "const jsModuleObj = (function(){" +
                                js +
                                "})(); jsModuleObj"
                ), id);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
