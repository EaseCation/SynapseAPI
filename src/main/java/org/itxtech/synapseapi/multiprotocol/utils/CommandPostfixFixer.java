package org.itxtech.synapseapi.multiprotocol.utils;

import cn.nukkit.SharedConstants;
import cn.nukkit.command.data.CommandEnum;
import com.google.common.base.Ascii;
import lombok.extern.log4j.Log4j2;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Builds command tables that avoid the client's postfix symbol remapping defect.
 *
 * <p>The affected {@code CommandRegistry::loadRemoteCommands} implementation validates a postfix
 * index against the postfix mapping, but then reads that index from the hard-enum mapping before
 * setting {@code PostfixBit}. A valid postfix wire index can consequently become an unrelated local
 * symbol. Invalid grammar can crash the client during command completion, before the player submits
 * a command.</p>
 *
 * <p>The native command-parser setup restores its saved registry state before loading a new
 * command table. Its constructor provides a verified prefix of twelve hard enums in the examined
 * 1.21.130 through 1.26.30 clients. Advertising those enum names in their native order makes the
 * corresponding enum mapping entries equal their postfix counterparts. An existing native enum
 * accepts an empty value update without losing its original values or grammar rules.</p>
 *
 * <p>Only these constructor-defined slots are used. Additional native registrations outside the
 * constructor can affect subsequent enum IDs, so inventing synthetic enums beyond this prefix
 * does not establish a safe mapping. Excess postfixes are omitted from the returned table; the
 * packet writer must advertise parameters using those suffixes as {@code STRING} instead. Their
 * server-side execution remains unchanged. When {@link SharedConstants#ENABLE_TEST_COMMAND} is
 * enabled, this fallback is reported at most once per minute, without logging command text or
 * suffix contents.</p>
 *
 * <p>This class preserves every original enum object's identity. After reordering, callers must
 * resolve alias and parameter references against the returned enum list and remap the enum-index
 * portion of existing constraints. The enum-value pool is only extended, so its existing indices
 * remain valid. This workaround addresses table remapping; it does not make arbitrary suffix
 * strings valid tokens in the client's command lexer.</p>
 */
@Log4j2
public final class CommandPostfixFixer {
    private static final long REPORT_INTERVAL_NANOS = TimeUnit.MINUTES.toNanos(1);
    private static final AtomicLong NEXT_REPORT_NANOS = new AtomicLong();

    // Boolean, Item and EntityEquipmentSlot use typed native registration templates.
    // Counting only string-enum registration calls misses those entries and shifts subsequent suffixes.
    private static final List<String> NATIVE_ENUMS = List.of(
            "GameMode",
            "RValueParam",
            "ValueParam",
            "IntParam",
            "DigitsParam",
            "Boolean",
            "PermissionCategory",
            "PermissionState",
            "EntityPropertySelector",
            "Item",
            "EntityEquipmentSlot",
            "EntityTypeSelector"
    );

    private CommandPostfixFixer() {
    }

    /**
     * Prepares the hard-enum prefix and the supported postfix table for the modern packet writer.
     *
     * <p>Enum names are matched exactly, while enum objects are retained by identity. Different
     * original objects with the same name are deliberately not merged: command parameters and
     * constraints may still refer to each object separately. The client resolves those names to
     * its own registry, but the packet writer must retain the original network references.</p>
     *
     * @param originalEnums the collected hard enums in their original network order
     * @param enumValues the existing enum-value pool; missing prefix values are appended only
     * @param normalizedPostfixes suffixes already normalized by {@link #normalizePostfix(String)} and deduplicated in their intended network order
     * @param protocol the recipient protocol handled by the 1.21.130-or-later packet codec
     * @return the enum order and supported postfixes; absent postfixes require STRING metadata
     */
    public static Layout prepare(List<CommandEnum> originalEnums, List<String> enumValues, List<String> normalizedPostfixes, int protocol) {
        if (normalizedPostfixes.isEmpty()) {
            return new Layout(originalEnums, normalizedPostfixes);
        }

        List<String> nativeNames = NATIVE_ENUMS;
        List<CommandEnum> reorderedEnums = new ArrayList<>(nativeNames.size() + originalEnums.size());
        Set<CommandEnum> prefixEnums = Collections.newSetFromMap(new IdentityHashMap<>());

        for (String nativeName : nativeNames) {
            CommandEnum prefixEnum = null;
            for (CommandEnum originalEnum : originalEnums) {
                if (nativeName.equals(originalEnum.getName())) {
                    prefixEnum = originalEnum;
                    break;
                }
            }
            if (prefixEnum == null) {
                prefixEnum = "GameMode".equals(nativeName) ? CommandEnum.ENUM_GAMEMODE : new CommandEnum(nativeName, Collections.emptyMap());
            }
            reorderedEnums.add(prefixEnum);
            prefixEnums.add(prefixEnum);
            for (String value : prefixEnum.getValues().keySet()) {
                if (!enumValues.contains(value)) {
                    enumValues.add(value);
                }
            }
        }

        for (CommandEnum originalEnum : originalEnums) {
            if (!prefixEnums.contains(originalEnum)) {
                reorderedEnums.add(originalEnum);
            }
        }

        int supportedCount = Math.min(normalizedPostfixes.size(), nativeNames.size());
        List<String> supportedPostfixes = new ArrayList<>(supportedCount);
        for (String postfix : normalizedPostfixes) {
            if (supportedPostfixes.size() == supportedCount) {
                break;
            }
            supportedPostfixes.add(postfix);
        }

        int overflowCount = normalizedPostfixes.size() - supportedCount;
        if (overflowCount > 0) {
            reportOverflow(protocol, supportedCount, overflowCount);
        }
        return new Layout(reorderedEnums, supportedPostfixes);
    }

    /**
     * Returns the suffix identity used by the client's UTF-8 postfix registry.
     *
     * <p>The native registry only folds ASCII A-Z to a-z; it preserves non-ASCII bytes, whitespace,
     * and embedded NUL bytes. Unicode-wide lowercasing would therefore merge distinct native
     * suffixes. Malformed Java UTF-16 needs one additional step: the packet writer's UTF-8 encoder
     * replaces unpaired surrogates with {@code '?'}. Matching that replacement before deduplication
     * prevents two Java strings from becoming one native suffix and shifting subsequent indices.</p>
     *
     * <p>Well-formed strings do not perform an encode/decode round trip. Callers must use this same
     * normalization both when collecting suffixes and when looking up a parameter's table index.</p>
     *
     * @param postfix the non-null suffix stored in a command parameter
     * @return the suffix after wire-compatible UTF-16 repair and ASCII case folding
     */
    public static String normalizePostfix(String postfix) {
        for (int index = 0; index < postfix.length(); index++) {
            char character = postfix.charAt(index);
            if (Character.isHighSurrogate(character)) {
                if (index + 1 < postfix.length() && Character.isLowSurrogate(postfix.charAt(index + 1))) {
                    index++;
                    continue;
                }
                return Ascii.toLowerCase(new String(postfix.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
            }
            if (Character.isLowSurrogate(character)) {
                return Ascii.toLowerCase(new String(postfix.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
            }
        }
        return Ascii.toLowerCase(postfix);
    }

    private static void reportOverflow(int protocol, int supportedCount, int overflowCount) {
        if (!SharedConstants.ENABLE_TEST_COMMAND) {
            return;
        }
        long now = System.nanoTime();
        long nextReport = NEXT_REPORT_NANOS.get();
        if ((nextReport != 0 && now - nextReport < 0) || !NEXT_REPORT_NANOS.compareAndSet(nextReport, now + REPORT_INTERVAL_NANOS)) {
            return;
        }
        log.warn("Command postfix metadata fallback: protocol={}, supportedSuffixes={}, overflowSuffixes={}, metadataType=STRING", protocol, supportedCount, overflowCount);
    }

    /**
     * Final network table order. Suffixes omitted from {@code postfixes} require STRING metadata.
     */
    public record Layout(List<CommandEnum> enums, List<String> postfixes) {
    }
}
