package org.itxtech.synapseapi.multiprotocol.protocol126.protocol;

import cn.nukkit.network.protocol.BookEditPacket;
import cn.nukkit.network.protocol.BookEditPacket.Action;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import lombok.ToString;

@ToString
public class BookEditPacket126 extends Packet126 {
    public static final int NETWORK_ID = ProtocolInfo.BOOK_EDIT_PACKET;

    public Action action;
    public int inventorySlot;
    public int pageNumber;
    public int secondaryPageNumber;

    public String text;
    public String photoName;

    public String title;
    public String author;
    public String xuid;

    @Override
    public int pid() {
        return NETWORK_ID;
    }

    @Override
    public void decode() {
        this.inventorySlot = this.getVarInt();

        this.action = Action.getValues()[this.getByte()];
        switch (this.action) {
            case REPLACE_PAGE:
            case ADD_PAGE:
                this.pageNumber = this.getVarInt();
                this.text = this.getString();
                this.photoName = this.getString();
                break;
            case DELETE_PAGE:
                this.pageNumber = this.getVarInt();
                break;
            case SWAP_PAGES:
                this.pageNumber = this.getVarInt();
                this.secondaryPageNumber = this.getVarInt();
                break;
            case SIGN_BOOK:
                this.title = this.getString();
                this.author = this.getString();
                this.xuid = this.getString();
                break;
        }
    }

    @Override
    public void encode() {
    }

    @Override
    public DataPacket toDefault() {
        BookEditPacket pk = new BookEditPacket();
        pk.action = this.action;
        pk.inventorySlot = this.inventorySlot;
        pk.pageNumber = this.pageNumber;
        pk.secondaryPageNumber = this.secondaryPageNumber;
        pk.text = this.text;
        pk.photoName = this.photoName;
        pk.title = this.title;
        pk.author = this.author;
        pk.xuid = this.xuid;
        return pk;
    }

    public static Class<? extends DataPacket> getDefaultPacket() {
        return BookEditPacket.class;
    }
}
