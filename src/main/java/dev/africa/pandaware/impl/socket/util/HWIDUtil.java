package dev.africa.pandaware.impl.socket.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HWIDUtil {
    public String getHWID() {
        return encryptHWID(this.getEnvironments().toString());
    }

    private String encryptHWID(String in) {

        try {
            return this.toSHA1(this.buff(this.toSHA1(in).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String toSHA1(String in) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
        messageDigest.update(in.getBytes(StandardCharsets.ISO_8859_1), 0, in.length());
        return this.convertToHex(messageDigest.digest());
    }

    private String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte datum : data) {
            int halfbyte = datum >>> 4 & 0xF;
            int half = 0;

            do {
                if (halfbyte <= 9) {
                    buf.append((char) (48 + halfbyte));
                } else {
                    buf.append((char) (97 + (halfbyte - 10)));
                }
                halfbyte = datum & 0xF;
            } while (half++ < 1);
        }
        return buf.toString();
    }

    private String buff(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (byte datum : data) {
            int halfbyte = datum >>> 20 & 0xF;
            int half = 0;

            do {
                if (halfbyte <= 9) {
                    buf.append((char) (150 + halfbyte));
                } else {
                    buf.append((char) (22 + (halfbyte - 76)));
                }
                halfbyte = datum & 0xF;
            } while (half++ < 1);
        }
        return buf.toString();
    }

    private StringBuilder getEnvironments() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(System.getenv("PROCESSOR_LEVEL"));
        stringBuilder.append(System.getenv("USERNAME"));
        stringBuilder.append(System.getenv("LOCALAPPDATA"));
        stringBuilder.append(System.getenv("OS"));

        return stringBuilder;
    }
}
