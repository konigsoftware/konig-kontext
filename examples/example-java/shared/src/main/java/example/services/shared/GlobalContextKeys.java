package example.services.shared;

import org.konigsoftware.kontext.KonigKontextKey;
import org.konigsoftware.kontext.KonigKontextProtobufKey;

import java.nio.charset.StandardCharsets;

public class GlobalContextKeys {
    public static final KonigKontextProtobufKey<AuthContext> AUTH_CONTEXT_KEY = KonigKontextProtobufKey.fromJavaClass(AuthContext.class);

    public static final KonigKontextKey<String> MY_CONTEXT_KEY = new KonigKontextKey<>() {
        @Override
        public String getDefaultValue() {
            return "";
        }

        @Override
        public byte[] valueToBinary(String s) {
            return s.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public String valueFromBinary(byte[] bytes) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
    };
}