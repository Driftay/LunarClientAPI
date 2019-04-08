package gg.manny.lunar.listener;

import gg.manny.lunar.LunarClientAPI;
import gg.manny.lunar.type.ClientType;
import gg.manny.lunar.util.ReflectionUtil;
import lombok.RequiredArgsConstructor;
import net.minecraft.util.io.netty.buffer.ByteBuf;
import net.minecraft.util.io.netty.buffer.Unpooled;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Constructor;

@RequiredArgsConstructor
public class PlayerListener implements Listener {

    private final LunarClientAPI instance;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        //String ByteBuf
        //Packet packet = new PacketPlayOutCustomPayload("REGISTER", PivotUtil.serializeBuf("Lunar-Client"));
        this.instance.getClientMap().put(player.getUniqueId(), ClientType.NONE);
        this.instance.getServer().getScheduler().runTaskAsynchronously(this.instance, () -> {
            //messy af ¯\_(ツ)_/¯
            try {
                Constructor constructor = ReflectionUtil.getClass("PacketPlayOutCustomPayload").getConstructor(String.class, ByteBuf.class);
                Constructor serializerConstructor = ReflectionUtil.getClass("PacketDataSerializer").getConstructor(ByteBuf.class);
                Object packet = constructor.newInstance("REGISTER", serializerConstructor.newInstance(Unpooled.wrappedBuffer("Lunar-Client".getBytes())));
                ReflectionUtil.sendPacket(player, packet);
                ReflectionUtil.inject(player);

            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        try {
            ReflectionUtil.eject(event.getPlayer());
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.instance.getClientMap().remove(event.getPlayer().getUniqueId());
    }

}