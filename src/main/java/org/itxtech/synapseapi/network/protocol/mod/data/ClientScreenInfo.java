package org.itxtech.synapseapi.network.protocol.mod.data;

import lombok.ToString;

@ToString
public class ClientScreenInfo {

    @ToString
    public static class ScreenInfo {
        public int width;
        public int height;
    }

    @ToString
    public static class ViewInfo {
        public int width;
        public int height;
        public int offsetX;
        public int offsetY;
    }

    public ScreenInfo screen;
    public ViewInfo view;

}
