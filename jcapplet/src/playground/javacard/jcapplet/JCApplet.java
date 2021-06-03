package playground.javacard.jcapplet;

import javacard.framework.*;
import javacard.security.*;
import javacardx.crypto.Cipher;

public class JCApplet extends Applet {
    private final byte[] greeting;
    private final KeyPair keyPair;
    private final Cipher cipher;

    private JCApplet() {
        greeting = new byte[16];
        // supported by CREF
        // keyPair = new KeyPair(KeyPair.ALG_RSA, KeyBuilder.LENGTH_RSA_512);
        // supported by real smart card
        keyPair = new KeyPair(KeyPair.ALG_RSA_CRT, KeyBuilder.LENGTH_RSA_1024);
        cipher = Cipher.getInstance(Cipher.ALG_RSA_PKCS1, false);
    }

    public static void install(byte bArray[], short bOffset, byte bLength) {
        (new JCApplet()).register();
    }

    private static final byte[] GREETING = new byte[] { 'H', 'e', 'l', 'l', 'o' };

    private static final byte GET_GREETING = (byte) 0xA0;
    private static final byte SET_GREETING = (byte) 0xA2;
    private static final byte ADD = (byte) 0xB0;
    private static final byte GENERATE_KEY_PAIR = (byte) 0xC0;
    private static final byte READ_PUBLIC_KEY = (byte) 0xC2;
    private static final byte USE_PRIVATE_KEY = (byte) 0xD0;

    public void process(APDU apdu) {
        if (selectingApplet()) { return; }

        byte[] buffer = apdu.getBuffer();

        short len;

        switch ((buffer[ISO7816.OFFSET_INS])) {
            case GET_GREETING:
                // APDU: CLA INS P1 P2
                if (buffer[ISO7816.OFFSET_P1] == 0x01) {
                    Util.arrayCopy(GREETING, (byte)0, buffer, (short) 0, (short) GREETING.length);
                    apdu.setOutgoingAndSend((short) 0, (short) GREETING.length);
                } else {
                    Util.arrayCopy(greeting, (byte)0, buffer, (short) 0, (short) greeting.length);
                    apdu.setOutgoingAndSend((short) 0, (short) greeting.length);
                }
                // RAPDU: GREETING SW1 SW2
                break;
            case SET_GREETING:
                // APDU: CLA INS P1 P2 LEN GREETING
                if ((buffer[ISO7816.OFFSET_LC] & 0xff) > greeting.length) {
                    ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
                }
                Util.arrayFillNonAtomic(greeting, (short) 0, (short) greeting.length, (byte) 0x00);
                Util.arrayCopyNonAtomic(buffer, ISO7816.OFFSET_CDATA, greeting, (short) 0, buffer[ISO7816.OFFSET_LC]);
                // RAPDU: SW1 SW2
                break;
            case ADD:
                // APDU: CLA INS P1 P2 04 A B
                short a = Util.getShort(buffer, ISO7816.OFFSET_CDATA);
                short b = Util.getShort(buffer, (short) (ISO7816.OFFSET_CDATA + 2));
                Util.setShort(buffer, (short) 0, (short) (a + b));
                apdu.setOutgoingAndSend((short) 0, (short) 2);
                // RAPDU: A+B SW1 SW2
                break;
            case GENERATE_KEY_PAIR:
                // APDU: CLA INS P1 P2
                // keyPair = new KeyPair(KeyPair.ALG_RSA_CRT, KeyBuilder.LENGTH_RSA_1024);
                // JCSystem.requestObjectDeletion();
                keyPair.genKeyPair();
                // RAPDU: SW1 SW2
                break;
            case READ_PUBLIC_KEY:
                // APDU: CLA INS P1 P2
                RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
                len = publicKey.getModulus(buffer, (short) 0);
                apdu.setOutgoingAndSend((short) 0, len);
                // RAPDU: PUBLIC KEY SW1 SW2
                break;
            case USE_PRIVATE_KEY:
                // APDU: CLA INS P1 P2 LC DATA
                cipher.init(keyPair.getPrivate(), Cipher.MODE_ENCRYPT);
                len = cipher.doFinal(buffer, ISO7816.OFFSET_CDATA,
                        (short) (buffer[ISO7816.OFFSET_LC] & 0xff),
                        buffer, (short) 0);
                apdu.setOutgoingAndSend((short) 0, len);
                // RAPDU: SIGNED DATA SW1 SW2
                break;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }
}
