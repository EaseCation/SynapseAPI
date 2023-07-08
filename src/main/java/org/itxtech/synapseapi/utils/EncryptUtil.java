package org.itxtech.synapseapi.utils;

import cn.nukkit.Server;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class EncryptUtil {

    /**
     * 加密公钥
     * */
    private static final String SERVER_PUBLIC_KEY =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxtAqXwIdP3Irh5UF5NFe" +
            "SMTeMSWd6hC/mMfLkdONyYg1IRVAZ2sih29oske5iWYnskDp9wtmDK7bu0qBC2u/" +
            "qJoi5rsiyrggWS65FnTQqKTP4ohZ2KTNvPdFwjdTrfui6aZccoAd9sEYKnlyypA+" +
            "GfZDwpK1s06ROVdywHwaUVS65wNquWbQGBO2SGkLo3X7pqnmvBcDjo93r6A6W6iu" +
            "GqDkCPKRrHFljIxi0/krrezAVKZQ/D4vpdB62f/7Is/W169RG7duk1+EvFq2CGC3" +
            "OrOHc6hilfubKSg/3+p9HPbxsFD6I36T7TB0EgcMGzVZymCifYPu0sBkXs1OsC+p" +
            "8wIDAQAB";

    /**
     * 解密私钥
     * */
    private static final String CLIENT_PRIVATE_KEY =
            "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDHF2vXPdCB/5/b" +
            "thLgdTOqiqs3k9FcAMtLimR8imkji4Yp7WG13oOwe/B8jUlgm6eLA8Hd4PAb0xfl" +
            "IqlG/p2habS9OfBP3LvFrZrI/DXrSPqaOYOb9mKuIIIjsmBosR41askZ6inuaWi2" +
            "dnaWx6TEd0/8OAtknRWLYbee9aWYWGS0NQQTP7qx9TpbiVWJYaK1AOlmIUE94xEi" +
            "n46aIY6f4GzWXxaOwjPaBzVBBpGnirSK+KZFHiVWmRSIX4H1Vi591Pzacokbsnxp" +
            "AFqL/4jxJ1sXNdEquM/eXZtBd/XgYY84/lz0sLRfuEkKiIIjxKKr1934ZxR0oMXr" +
            "l976VeZ7AgMBAAECggEAGGvYD5wGgpu/mSzZXe0ifE3sOLIKIVX7mfWM4/X6daC8" +
            "wVHcBXoY/0IEutJ96LrPL28zjar06QTYzoOU7h93aIBV6kw8HR+/bL0h2nuNhaH7" +
            "E4XwjeYILsHQFN7hkpX/VaAWsPYXVKZbcN5RJ6evn2XfStn5E8ttBB7lbjtFp80g" +
            "j7kawzX6bIdnrKrcl4V1DbGLYt/juZ/EeNg0Y6adRmcxOwjkGk/kPc8jt7wlLf4R" +
            "7dvxe1GQLFr3PkHOPud3QmAF3DNF5zhnQslXzO5IPE9yRUdFCN3MSvj6IhoziSlO" +
            "5tM5yJ+dcyyO+OkwpdG4t+eNhDlp+egTvwFPsSICiQKBgQDmOq5tGfkehLByDF9u" +
            "0NNJbd5Dv5EVpEHEV3d5qDvRg01yB+OSgVmLiOkvOUWPj4a4e7f4jsEzOsDl2w5G" +
            "kXHMPmAQj/+cHCXpMdenev511IKcoOudEUWsOoGDz/DEEBk5tFpLyS9s01ptjlaI" +
            "/EsMxw0wo5WJDmOxbPYnvAtIJQKBgQDdYHl8b9+7NZXqmo4TnB37bTPfXzC2ZSJd" +
            "HEaEmOQmGmz3/zuhgJdreJe3qhIu5R6kI3I//7M6t6gLvHlO++V2F5wl9F5JfdXu" +
            "GCehQakM6kZ3CwT+sQEZG1Updw711p/ntpHEz57D8M4CizBc/Lo+vxxYcLd9N971" +
            "QJimUdJiHwKBgQDc5Ep8dSIWzNp54jMiIIvH9Pj+6WrrZ7iOCBmiiO4zu5XvpXAM" +
            "HbzrasXIQFvkPKHWmSVO/rCRVXrETqNow516iBIlcfOeNUeOcPrnPtOr++QBIUhO" +
            "X8cIUu32qOjpRETfn5HPCs1MzZACc0HFlsLvFIAsPZd4c2zlS5T1VK9CXQKBgQC3" +
            "Uy1H311nqvh7T4JYuvAZRlWupSWx5g6PDNtY80P8SeXzeWnoVJUQas05S53DN/on" +
            "8zKpzRafiXegQHQ09GURZnvo8nUiZxLCGT64S6ezCk9QRc/dGrfOEtRzgiThcNr7" +
            "CDWuxkC0AraXZKyoVKHnVFwk4TMSfzIj51qCYT/YAQKBgCHvHvD25QkLahPIlLC+" +
            "gCgm1Hz1kQgKOFUn57QybS/S/X5rbXzkeHt0bDgWjHJ+Vm3gJ/aIc62PRBYOtWSE" +
            "AdCX8wriOVMQ+fKSsbpmEFqe5RtboWM5tiSx4SemZon+ax41tE5VNOIS+Dxfono0" +
            "UIRYA+mHAPgy9jmaj1tYcKZn";

    /**
     * 加密算法RSA
     */
    public static final String KEY_ALGORITHM = "RSA";

    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 245;

    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 256;

    private static final KeyFactory KEY_FACTORY;
    private static final PublicKey PUBLIC_KEY;
    private static final PrivateKey PRIVATE_KEY;

    static {
        try {
            KEY_FACTORY = KeyFactory.getInstance(KEY_ALGORITHM);
            // 将Base64编码后的公钥转换成PublicKey对象
            PUBLIC_KEY = KEY_FACTORY.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(SERVER_PUBLIC_KEY)));
            PRIVATE_KEY = KEY_FACTORY.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(CLIENT_PRIVATE_KEY)));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * RSA公钥加密
     *
     * @param str       加密字符串
     * @return 密文
     * @throws Exception 加密过程中的异常信息
     */
    public static String encrypt(String str) {
        String result = "";
        try {
            // 加密
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, PUBLIC_KEY);
            byte[] inputArray = str.getBytes();
            int inputLength = inputArray.length;
            // 标识
            int offSet = 0;
            byte[] resultBytes = {};
            byte[] cache;
            while (inputLength - offSet > 0) {
                if (inputLength - offSet > MAX_ENCRYPT_BLOCK) {
                    cache = cipher.doFinal(inputArray, offSet, MAX_ENCRYPT_BLOCK);
                    offSet += MAX_ENCRYPT_BLOCK;
                } else {
                    cache = cipher.doFinal(inputArray, offSet, inputLength - offSet);
                    offSet = inputLength;
                }
                resultBytes = Arrays.copyOf(resultBytes, resultBytes.length + cache.length);
                System.arraycopy(cache, 0, resultBytes, resultBytes.length - cache.length, cache.length);
            }
            result = Base64.getEncoder().encodeToString(resultBytes);
        } catch (Exception e) {
            Server.getInstance().getLogger().alert("rsaEncrypt error: " + e.getMessage(), e);
        }
        return result;
    }

    /**
     * RSA私钥解密
     *
     * @param str        加密字符串
     * @return 明文
     * @throws Exception 解密过程中的异常信息
     */
    public static String decrypt(String str) throws Exception {
        byte[] inputByte = Base64.getDecoder().decode(str.getBytes(StandardCharsets.UTF_8));
        Cipher cipher = Cipher.getInstance(KEY_FACTORY.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, PRIVATE_KEY);
        int inputLen = inputByte.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(inputByte, offSet, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(inputByte, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_DECRYPT_BLOCK;
        }
        String result = out.toString();
        out.close();

        return result;
    }

}
