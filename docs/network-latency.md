# 网络延迟诊断与入队唤醒

在 `plugins/SynapseAPI/config.yml` 设置以下选项，修改后重启：

```yaml
network-event-driven: false
network-latency-trace-enabled: false
network-latency-trace-directory: latency-traces
network-latency-trace-duration-seconds: 300
```

两个开关默认关闭且互相独立。关闭唤醒优化时保留原有轮询逻辑；开启后入队通知网络线程，保留 FIFO 和定时回退。对端可独立开启或关闭，网络协议不变。

Trace 关闭时不采样、不计算指纹、不分配追踪包或创建文件线程。开启会增加摘要计算和写文件开销，建议只用于限时诊断。时长范围 1–3600 秒，有界队列容量 32768，最多 1,000,000 条记录，到期自动停止。输出相对于插件数据目录，采用唯一 CSV 文件名；`trace.dropped` 为累计丢记录数量，分析前必须检查。输出失败关闭诊断，不影响服务启动。

本机运行时必须包含本次配套 Network 版本；由宿主 Nukkit 打包该依赖。旧实验 JVM 参数不再生效。完整测量边界与分析工具见 CodeFunCore 的 `scripts/network-latency/README.md`。
