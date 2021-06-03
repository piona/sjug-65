package playground.javacard.jcterminal;

public class HexString {
    /**
     * Convert a byte array into a String.
     *
     * @param bytes Byte array.
     * @return A String from the byte array.
     */
    public static final String byteArrayToHex(byte[] bytes) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            buffer.append(Character.toUpperCase(Character.forDigit((bytes[i] >> 4) & 0x0f, 16)));
            buffer.append(Character.toUpperCase(Character.forDigit((bytes[i] & 0x0f), 16)));
        }
        return buffer.toString();
    }
}
