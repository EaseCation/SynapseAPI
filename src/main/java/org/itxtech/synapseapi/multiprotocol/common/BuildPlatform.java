package org.itxtech.synapseapi.multiprotocol.common;

public interface BuildPlatform {
    int UNKNOWN = -1;

    /**
     * Android
     */
    int GOOGLE = 1;
    /**
     * iPhone/iPod
     */
    int IOS = 2;
    /**
     * Mac (MEE)
     */
    int OSX = 3;
    int AMAZON = 4;
    int GEAR_VR = 5;

    int UWP = 7;
    int WIN32 = 8;
    /**
     * Bedrock Dedicated Server
     */
    int DEDICATED = 9;
    int TV_OS = 10;
    /**
     * Play Station
     */
    int ORBIS = 11;
    /**
     * Nintendo Switch
     */
    int NX = 12;
    int XBOX = 13;
    int WINDOWS_PHONE = 14;
    int LINUX = 15;
}
