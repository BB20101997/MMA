package de.webtwob.mma.core.common.packet;

import io.netty.buffer.ByteBuf;

import de.webtwob.mma.api.capability.APICapabilities;
import de.webtwob.mma.api.interfaces.capability.ICraftingRequest;
import de.webtwob.mma.core.common.CoreLog;
import de.webtwob.mma.core.common.inventory.TokenContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;

/**
 * Created by bennet on 21.03.17.
 */
public class TokenUpdatePacket implements IMessage {

    private ItemStack request;
    private int       quantity;

    /**
     * Used to generate TokenUpdatePackets from an incoming ByteStream
     */
    @SuppressWarnings("unused")
    public TokenUpdatePacket() {
    }

    /**
     * Creates a TokenUpdatePacket
     *
     * @param request  the ItemStack to request
     * @param quantity the quantity to request
     */
    public TokenUpdatePacket(ItemStack request, int quantity) {
        this.request = request;
        this.quantity = quantity;
    }

    @Override
    public void fromBytes(ByteBuf buf) {

        PacketBuffer packetBuffer = new PacketBuffer(buf);
        try {
            request = packetBuffer.readItemStack();
            quantity = packetBuffer.readInt();
        } catch (IOException e) {
            CoreLog.warn("Could not read ItemStack or quantity form TokenUpdatePacket");
        }

    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        packetBuffer.writeItemStack(request);
        packetBuffer.writeInt(quantity);
    }

    public static class TokenUpdatePacketHandler implements IMessageHandler<TokenUpdatePacket, IMessage> {

        @Override
        public IMessage onMessage(TokenUpdatePacket message, MessageContext ctx) {
            EntityPlayer playerEntity = ctx.getServerHandler().player;
            if (playerEntity.openContainer instanceof TokenContainer) {
                ItemStack        held = ((TokenContainer) playerEntity.openContainer).stack;
                ICraftingRequest icr;
                if ((icr = held.getCapability(APICapabilities.CAPABILITY_CRAFTING_REQUEST, null)) != null) {
                    if (message.request != null) {
                        CoreLog.debug("Got TokenUpdatePacket for {}!" + message.request.getDisplayName());
                        icr.setRequest(message.request);
                        icr.setQuantity(message.quantity);
                    } else {
                        CoreLog.debug("Got TokenUpdatePacket to clear request!");
                        icr.setRequest(ItemStack.EMPTY);
                        icr.setQuantity(0);
                    }
                }
            }
            return null;
        }
    }
}
