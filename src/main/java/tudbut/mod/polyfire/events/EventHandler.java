package tudbut.mod.polyfire.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import tudbut.mod.polyfire.PolyFire;
import tudbut.mod.polyfire.utils.ChatUtils;
import tudbut.mod.polyfire.utils.ThreadManager;
import tudbut.mod.polyfire.utils.Utils;

import java.util.Date;

public class EventHandler {

    public static float tps = 20.0f;
    private static long lastTick = -1;
    private static long joinTime = 0;

    public static boolean onPacket(Packet<?> packet) {
        boolean b = false;

        if(packet instanceof SPacketTimeUpdate) {
            long time = System.currentTimeMillis();
            if(lastTick != -1 && new Date().getTime() - joinTime > 5000) {
                long diff = time - lastTick;
                if(diff > 50) {
                    tps = (tps + ((1000f / diff) * 20f)) / 2;
                }
            }
            else {
                tps = 20.0f;
            }
            lastTick = time;
        }

        for (int i = 0; i < PolyFire.modules.length ; i++) {
            if (PolyFire.modules[i].enabled)
                try {
                    if (PolyFire.modules[i].onPacket(packet))
                        b = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }

        return b;
    }

    // Fired when enter is pressed in chat
    @SubscribeEvent
    public void onChat(ClientChatEvent event) {
        // Only for PF commands
        if (event.getOriginalMessage().startsWith(PolyFire.prefix)) {
            // Don't send
            event.setCanceled(true);
            ChatUtils.print("Blocked message");
            // When canceled, the event blocks adding the message to the chat history,
            // so it'll cause confusion if this line doesn't exist
            ChatUtils.history(event.getOriginalMessage());

            // The command without the prefix
            String s = event.getOriginalMessage().substring(PolyFire.prefix.length());

            try {
                // Toggle a module
                if (s.startsWith("t ")) {
                    for (int i = 0; i < PolyFire.modules.length ; i++) {
                        if (PolyFire.modules[i].toString().equalsIgnoreCase(s.substring("t ".length()))) {
                            ChatUtils.print(String.valueOf(!PolyFire.modules[i].enabled));

                            if (PolyFire.modules[i].enabled = !PolyFire.modules[i].enabled)
                                PolyFire.modules[i].onEnable();
                            else
                                PolyFire.modules[i].onDisable();
                        }
                    }
                }

                // Ignore any commands and say something
                if (s.startsWith("say ")) {
                    PolyFire.player.sendChatMessage(s.substring("say ".length()));
                    ChatUtils.history(event.getOriginalMessage());
                }

                if (s.equals("help")) {
                    //String help = Utils.getRemote("help.chat.txt", false);
                    //if (help == null) {
                    ChatUtils.print("Unable retrieve help message! Check your connection!");
                    //} else {
                    //help = help.replaceAll("%p", TTCp.prefix);
                    //ChatUtils.print(help);
                    //}
                }

                // Ignore any commands and say something
                if (s.startsWith("say ")) {
                    PolyFire.player.sendChatMessage(s.substring("say ".length()));
                    ChatUtils.history(event.getOriginalMessage());
                }

                // Module-specific commands
                for (int i = 0; i < PolyFire.modules.length ; i++) {
                    if (s.toLowerCase().startsWith(PolyFire.modules[i].toString().toLowerCase())) {
                        System.out.println("Passing command to " + PolyFire.modules[i].toString());
                        try {
                            String args = s.substring(PolyFire.modules[i].toString().length() + 1);
                            if (PolyFire.modules[i].enabled)
                                PolyFire.modules[i].onChat(args, args.split(" "));
                            PolyFire.modules[i].onEveryChat(args, args.split(" "));
                        }
                        catch (StringIndexOutOfBoundsException e) {
                            String args = "";
                            if (PolyFire.modules[i].enabled)
                                PolyFire.modules[i].onChat(args, new String[0]);
                            PolyFire.modules[i].onEveryChat(args, new String[0]);
                        }
                    }
                }
            }
            catch (Exception e) {
                ChatUtils.print("Command failed!");
                e.printStackTrace();
            }
        }
    }

    // When a message is received, those will often require parsing
    @SubscribeEvent
    public void onServerChat(ClientChatReceivedEvent event) {
        // Trigger module event for server chat, the modules can cancel display of the message
        for (int i = 0; i < PolyFire.modules.length ; i++) {
            if (PolyFire.modules[i].enabled) {
                if (PolyFire.modules[i].onServerChat(event.getMessage().getUnformattedText(), event.getMessage().getFormattedText()))
                    event.setCanceled(true);
            }
        }

    }

    // When the client joins a server
    @SubscribeEvent
    public void onJoinServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        ChatUtils.print("§a§lPolyFire has a Discord server: https://discord.gg/2WsVCQDpwy!");

        tps = 20.0f;
        lastTick = -1;
        joinTime = System.currentTimeMillis();

        // Check for a new version
        ThreadManager.run(() -> {
            try {
                Thread.sleep(10000);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (PolyFire.mc.world != null) {
                String s = Utils.getLatestVersion();
                if (s == null) {
                    ChatUtils.print("Unable to check for a new version! Check your connection!");
                } else if (!s.equals(PolyFire.VERSION)) {
                    ChatUtils.print(
                            "§a§lA new PolyFire version was found! Current: " +
                                    PolyFire.VERSION +
                                    ", New: " +
                                    s
                    );
                }
                try {
                    for (int i = 0; i < 60; i++) {
                        Thread.sleep(1000);
                        if(PolyFire.mc.world == null)
                            break;
                    }
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // When any entity appears on screen, useful for setting player and world
    @SubscribeEvent
    public void onJoin(EntityJoinWorldEvent event) {
        // Setting player and world
        PolyFire.player = Minecraft.getMinecraft().player;
        PolyFire.world = Minecraft.getMinecraft().world;
    }

    // When the player dies, NOT called by FML
    public void onDeath(EntityPlayer player) {
        BlockPos pos = player.getPosition();
        ChatUtils.print("§c§l§k|||§c§l You died at " + pos.getX() + " " + pos.getY() + " " + pos.getZ());
    }

    @SubscribeEvent
    public void onOverlay(RenderBlockOverlayEvent event) {
        event.setCanceled(true);
    }

    // Fired every tick
    @SubscribeEvent
    public void onSubTick(TickEvent event) {
        try {
            if (PolyFire.mc.world == null || PolyFire.mc.player == null)
                return;
            EntityPlayerSP player = PolyFire.player;
            if (player == null || event.side == Side.SERVER)
                return;

            for (int i = 0; i < PolyFire.modules.length ; i++) {
                PolyFire.modules[i].player = player;
                if (PolyFire.modules[i].enabled)
                    try {
                        PolyFire.modules[i].onSubTick();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                PolyFire.modules[i].onEverySubTick();
            }
        } catch (Exception ignored) { }
    }

    // Fired every tick
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        try {
            if (PolyFire.mc.world == null || PolyFire.mc.player == null)
                return;

            if (event.phase != TickEvent.Phase.START)
                return;

            if (event.type != TickEvent.Type.CLIENT)
                return;


            EntityPlayerSP player = PolyFire.player;
            if (player == null || event.side == Side.SERVER)
                return;

            long time = System.currentTimeMillis();
            long diff = time - lastTick;
            float f = ((1000f / diff) * 20f);
            if(f < tps / 1.25) {
                tps = (tps + f) / 2;
            }

            for (int i = 0; i < PolyFire.modules.length ; i++) {
                PolyFire.modules[i].player = player;
                PolyFire.modules[i].key.onTick();

                try {
                    for (String key : PolyFire.modules[i].customKeyBinds.keys()) {
                        if (PolyFire.modules[i].enabled || PolyFire.modules[i].customKeyBinds.get(key).alwaysOn) {
                            PolyFire.modules[i].customKeyBinds.get(key).onTick();
                        }
                    }
                    if (PolyFire.modules[i].enabled) {
                        PolyFire.modules[i].onTick();
                    }
                    PolyFire.modules[i].onEveryTick();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception ignored) { }
    }
}
