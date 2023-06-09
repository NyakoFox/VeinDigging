package gay.nyako.veindigging;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.lwjgl.glfw.GLFW;

public class VeinDiggingClientMod implements ClientModInitializer {
    public static boolean VEINDIGGING_HELD = false;

    @Override
    public void onInitializeClient() {
        KeyBinding keybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.veindigging.grave",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_GRAVE_ACCENT,
            "category.veindigging.veindigging"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            if (keybind.isPressed()) {
                if (!VEINDIGGING_HELD) {
                    VEINDIGGING_HELD = true;
                    PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
                    passedData.writeBoolean(true);
                    ClientPlayNetworking.send(VeinDiggingMod.CHANGE_VEINDIGGING_STATE_PACKET, passedData);
                }
            } else if (VEINDIGGING_HELD) {
                VEINDIGGING_HELD = false;
                PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
                passedData.writeBoolean(false);
                ClientPlayNetworking.send(VeinDiggingMod.CHANGE_VEINDIGGING_STATE_PACKET, passedData);
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            ClientPlayNetworking.send(VeinDiggingMod.USING_CLIENT_MOD_PACKET, PacketByteBufs.empty());
        });
    }
}
