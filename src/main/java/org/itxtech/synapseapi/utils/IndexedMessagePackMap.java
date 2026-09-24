package org.itxtech.synapseapi.utils;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.msgpack.value.Value;
import org.msgpack.value.impl.ImmutableMapValueImpl;

import javax.annotation.Nullable;

/** 保留原始键类型与顺序，仅复用解包时构建的文本索引；构造参数由解包器独占。 */
final class IndexedMessagePackMap extends ImmutableMapValueImpl {
    private final Value[] entries;
    private final Object2IntMap<String> offsets;

    IndexedMessagePackMap(Value[] entries, Object2IntMap<String> offsets) {
        super(entries);
        this.entries = entries;
        this.offsets = offsets;
    }

    @Nullable
    Value getTextField(String key) {
        int offset = offsets.getInt(key);
        // 整数键参与重复键检查，但不能冒充协议中的字符串字段名。
        return offset >= 0 && entries[offset].isRawValue() ? entries[offset + 1] : null;
    }
}
