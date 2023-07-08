package org.itxtech.synapseapi.network.protocol.mod.data;

import cn.nukkit.math.Vector3;

import java.util.List;

public class NavPathResult {

    /**
     * -1：未知错误（返回的数据中没有path字段）
     * 0：成功
     * 1：参数错误
     * 2：玩家所在chunk未加载完毕
     * 3：终点为实心方块，无法寻路
     */
    public int code;
    public List<Vector3> path;

    public NavPathResult(int code, List<Vector3> path) {
        this.code = code;
        this.path = path;
    }

    public static NavPathResult ofSuccess(List<Vector3> path) {
        return new NavPathResult(0, path);
    }

    public static NavPathResult ofError(int code) {
        return new NavPathResult(code, null);
    }

    public boolean isSuccess() {
        return code == 0;
    }

}
