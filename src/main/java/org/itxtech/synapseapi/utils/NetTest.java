package org.itxtech.synapseapi.utils;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.PluginCommand;
import lombok.extern.log4j.Log4j2;
import org.itxtech.synapseapi.SynapseAPI;
import org.itxtech.synapseapi.SynapseEntry;
import org.itxtech.synapseapi.messaging.PluginMessageListener;

import java.io.File;
import java.io.FileWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Log4j2
public class NetTest implements PluginMessageListener {

    private final SynapseAPI plugin;

    private boolean isTesting = false;
    private NetTestThread thread = null;

    private String data;
    private int count;
    private DataEntry[] dataEntries;
    private long sleepTime;

    public NetTest(SynapseAPI plugin) {
        this.plugin = plugin;
        plugin.getServer().getCommandMap().register("NetTest", new NetTestCommand(plugin));
    }

    public void startTest(SynapseEntry target, int dataLen, int count, int sleepTime) {
        if (isTesting) stopTest();
        this.data = getRandomString(dataLen);
        this.count = count;
        this.dataEntries = new DataEntry[count];
        this.sleepTime = sleepTime;
        this.thread = new NetTestThread();
        this.thread.target = target;
        this.thread.start();
        this.isTesting = true;
    }

    public void stopTest() {
        if (isTesting) {
            if (thread != null) {
                thread.isRunning = false;
            }
            this.endTest();
            plugin.getLogger().info("[网络测试] 上一次测试已终止");
        }
    }

    private void endTest() {
        plugin.getLogger().info("[网络测试] 测试完成，正在导出结果...");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh_mm_ss_SS");
        String fileName = df.format(new Date()) + ".csv";
        File file = new File(plugin.getDataFolder().toString() + "/" + fileName);
        try {
            if (!file.exists()) {
                if (file.createNewFile()) {
                    FileWriter fw = new FileWriter(file);
                    fw.write("ID,\"Send Time\",\"Recv Time\",\"Time Full\",\"Time Send\",\"Time Recv\"\n");
                    for (DataEntry entry : this.dataEntries) {
                        fw.write(
                                entry.id + ",\"" +
                                        new Timestamp(entry.sendTime).toString() + "\"," +
                                        (entry.recvTime == -1 ? "\"\"" : ("\"" + new Timestamp(entry.recvTime).toString() + "\",")) +
                                        entry.getTimeFull() + "," +
                                        entry.getTimeSend() + "," +
                                        entry.getTimeRecv() +
                                        "\n");
                    }
                    fw.flush();
                    fw.close();
                }
            }
            plugin.getLogger().info("[网络测试] 测试完成，已导出结果数据至文件：" + fileName);
            this.isTesting = false;
        } catch (Exception e) {
            plugin.getLogger().warning("文件导出失败");
            Server.getInstance().getLogger().logException(e);
        }
    }

    public class NetTestThread extends Thread {
        public boolean isRunning = true;
        public SynapseEntry target = null;
        @Override
        public void run() {
            super.run();
            for (int i = 0; i < count; i++) {
                try {
                    if (!isTesting) break;
                    SynapseEntry entry;
                    if (target != null) entry = target;
                    else entry = getPlugin().getSynapseEntries().values().stream().findAny().orElseThrow(() -> new RuntimeException("当前无Nemisys连接"));
                    entry.sendPluginMessage(getPlugin(), "nettest", (i + ":" + data).getBytes());
                    dataEntries[i] = new DataEntry(i);
                    System.out.println("当前发送数据[" + (i + 1) + "/" + count + "]");
                } catch (Exception e) {
                    System.out.println("发送数据[" + (i + 1) + "/" + count + "]时发生错误");
                    log.throwing(e);
                }
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    //ignore
                }
            }
            try {
                Thread.sleep(1000); //等待1秒
            } catch (InterruptedException e) {
                //ignore
            }
            endTest();
        }
    }

    public SynapseAPI getPlugin() {
        return plugin;
    }

    @Override
    public void onPluginMessageReceived(SynapseEntry entry, String channel, byte[] message) {
        try {
            String msg = new String(message);
            int indexTime = msg.indexOf('#');
            int indexData = msg.indexOf(':');
            //String[] split = msg.split(":", 1);
            if (indexTime > 0 && indexData >= 0) {
                plugin.getLogger().info("[网络测试] 返回数据" + msg.substring(indexTime + 1, indexData) + ": " + msg.substring(0, indexTime));
                long time = Long.parseLong(msg.substring(0, indexTime));
                int id = Integer.parseInt(msg.substring(indexTime + 1, indexData));
                dataEntries[id].recvTime = System.currentTimeMillis();
                dataEntries[id].proxyRecvTime = time;
            }
        } catch (Exception e) {
            Server.getInstance().getLogger().logException(e);
        }
    }

    public static String getRandomString(int length) { //length表示生成字符串的长度
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    public static class DataEntry {

        private final int id;
        private final long sendTime = System.currentTimeMillis();
        private long proxyRecvTime = -1;
        private long recvTime = -1;

        public DataEntry(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public long getSendTime() {
            return sendTime;
        }

        public long getProxyRecvTime() {
            return proxyRecvTime;
        }

        public long getRecvTime() {
            return recvTime;
        }

        public long getTimeSend() {
            if (proxyRecvTime == -1) return -1;
            return proxyRecvTime - sendTime;
        }

        public long getTimeRecv() {
            if (proxyRecvTime == -1 || recvTime == -1) return -1;
            return recvTime - proxyRecvTime;
        }

        public long getTimeFull() {
            if (recvTime == -1) return -1;
            return recvTime - sendTime;
        }
    }

    public class NetTestCommand extends PluginCommand<SynapseAPI> {
        public NetTestCommand(SynapseAPI owner) {
            super("nettest", owner);
            this.setPermission("console.command.nettest");
        }

        @Override
        public boolean execute(CommandSender sender, String commandLabel, String[] args) {
            if (!(sender instanceof Player)) {
                if (args.length > 0) {
                    if (args[0].equals("start")) {
                        if (args.length > 3) {
                            try {
                                int dataLen = Integer.parseInt(args[1]);
                                int count = Integer.parseInt(args[2]);
                                int sleep = Integer.parseInt(args[3]);
                                if (args.length > 4) {
                                    SynapseEntry entry = plugin.getSynapseEntries().get(args[4]);
                                    if (entry != null) {
                                        startTest(entry, dataLen, count, sleep);
                                    } else {
                                        sender.sendMessage("Can't find target Nemisys: " + args[4]);
                                        return true;
                                    }
                                } else {
                                    startTest(null, dataLen, count, sleep);
                                }
                            } catch (Exception e) {
                                sender.sendMessage("Usage: nettest start <dataLen> <count> <sleep>");
                            }
                        }
                    } else if (args[0].equals("stop")) {
                        stopTest();
                    } else {
                        sender.sendMessage("Usage: nettest <start|stop>");
                    }
                } else {
                    sender.sendMessage("Usage: nettest <start|stop>");
                }
            }
            return true;
        }
    }
}
