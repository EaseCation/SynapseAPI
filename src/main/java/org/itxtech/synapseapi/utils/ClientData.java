package org.itxtech.synapseapi.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by boybook on 16/6/25.
 */
public class ClientData {

    public final Map<String, Entry> clientList = new HashMap<>();

    public String getHashByDescription(String description) {
        return this.clientList.entrySet().stream()
                .filter(entry -> entry.getValue().getDescription().equals(description))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
        /*final String[] re = new String[1];
        this.clientList.forEach((hash, entry) -> {
            if (entry.getDescription().equals(description)) {
                re[0] = hash;
            }
        });
        return re[0];*/
    }

    public static class Entry {
        private final String ip;
        private final int port;
        private final int playerCount;
        private final int maxPlayers;
        private final String description;

        public Entry(String ip, int port, int playerCount, int maxPlayers, String description) {
            this.ip = ip;
            this.port = port;
            this.playerCount = playerCount;
            this.maxPlayers = maxPlayers;
            this.description = description;
        }

        public String getIp() {
            return ip;
        }

        public int getPort() {
            return port;
        }

        public int getMaxPlayers() {
            return maxPlayers;
        }

        public int getPlayerCount() {
            return playerCount;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry entry = (Entry) o;
            return Objects.equals(ip, entry.ip) &&
                    Objects.equals(description, entry.description);
        }

        @Override
        public int hashCode() {
            return Objects.hash(ip, description);
        }

        @Override
        public String toString() {
            return ip +
                    ":" +
                    port +
                    "[" +
                    playerCount +
                    "/" +
                    maxPlayers +
                    "]("
                    + getDescription() +
                    ")";
        }
    }

    @Override
    public String toString() {
        return this.clientList.entrySet().stream()
                .map(entry -> entry.getKey() +
                                " => " +
                                entry.getValue().ip +
                                ":" +
                                entry.getValue().port +
                                "[" +
                                entry.getValue().playerCount +
                                "/" +
                                entry.getValue().maxPlayers +
                                "]("
                                + entry.getValue().getDescription() +
                                ")"
                ).collect(Collectors.joining("\n"));
    }
}
