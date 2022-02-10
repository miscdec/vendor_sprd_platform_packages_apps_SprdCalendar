package com.sprd.calendar.vcalendar;

import java.io.ByteArrayOutputStream;

/**
 * Binary to decimal conversion
 *
 */
public class HexadecimalConver {

    private static String hexString = "0123456789ABCDEF";

    public static String toStringHex(String s) {
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(
                        s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            s = new String(baKeyword, "GB2312");
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }

    /**
     * Encodes a string into sixteen digits, suitable for all characters (including Chinese)
     */
    public static String encode(String str) {
        // Gets the byte array based on the default encoding
        byte[] bytes = str.getBytes();
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        // Disassemble each byte in the byte array into two bits and sixteen decimal integers
        for (int i = 0; i < bytes.length; i++) {
            sb.append(hexString.charAt((bytes[i] & 0xf0) >> 4));
            sb.append(hexString.charAt((bytes[i] & 0x0f) >> 0));
        }
        return sb.toString();
    }

    /**
     * Decodes sixteen digit numbers into strings, suitable for all characters (including Chinese)
     */
    public static String decode(String bytes) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(
                bytes.length() / 2);
        // Assemble each two bit sixteen decimal integer into one byte
        for (int i = 0; i < bytes.length(); i += 2)
            baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString
                    .indexOf(bytes.charAt(i + 1))));
        String decodeString = "";
        try {
            decodeString = new String(baos.toByteArray(), "GB2312");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decodeString;
    }
}
