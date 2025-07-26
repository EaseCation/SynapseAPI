package org.itxtech.synapseapi.filtertext;

import cn.nukkit.Player;
import cn.nukkit.utils.Utils;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseAPI;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

@Log4j2
public class FilterTextService implements Runnable {
    private static final FilterTextService INSTANCE = Utils.make(new FilterTextService(), service -> {
        Thread thread = new Thread(service, "Filter Text Service");
        thread.setDaemon(true);
        thread.start();
    });

    private final Byte2ObjectMap<TextFilter> filters = new Byte2ObjectAVLTreeMap<>();

    private final BlockingQueue<Input> incoming = new LinkedBlockingQueue<>();
    private final Queue<Runnable> outgoing = new ConcurrentLinkedQueue<>();

    private FilterTextService() {
        addFilter((byte) -100, new LengthFilter());
        addFilter((byte) 0, VanillaFilter.getInstance());
    }

    @Override
    public void run() {
        INPUT:
        while (SynapseAPI.getInstance().isEnabled()) {
            Input input;
            try {
                input = incoming.take();
            } catch (InterruptedException e) {
                continue;
            }

            Player player = input.player;
            boolean chat = input.chat;
            String text = input.text;
            try {
                for (TextFilter filter : filters.values()) {
                    if (!chat && filter.isChatOnly()) {
                        continue;
                    }
                    text = filter.filter(text, player);
                    if (text == null) {
                        continue INPUT;
                    }
                }
                String result = text;
                outgoing.offer(() -> input.callback.accept(result));
            } catch (Exception e) {
                log.throwing(e);
            }
        }
    }

    public void tick() {
        Runnable callback;
        while ((callback = outgoing.poll()) != null) {
            try {
                callback.run();
            } catch (Exception e) {
                log.throwing(e);
            }
        }
    }

    public static void filter(String text, Player player, boolean chat, Consumer<String> callback) {
        INSTANCE.incoming.offer(new Input(text, player, chat, callback));
    }

    public boolean addFilter(byte priority, TextFilter filter) {
        Objects.requireNonNull(filter, "filter");
        return filters.put(priority, filter) == null;
    }

    public static FilterTextService getInstance() {
        return INSTANCE;
    }

    private record Input(String text, Player player, boolean chat, Consumer<String> callback) {
    }
}
