package playground.javacard.jcterminal;

import net.sourceforge.scuba.smartcards.CREFTerminalProvider;

import javax.smartcardio.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.List;

public class JCTerminal {
    public static void main(String[] args) throws CardException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        // CREF
        Security.addProvider(new CREFTerminalProvider());
        // TerminalFactory terminalFactory = TerminalFactory.getInstance("CREF", "localhost:9025");

        // PC/SC
        TerminalFactory terminalFactory = TerminalFactory.getDefault();

        List<CardTerminal> terminals = terminalFactory.terminals().list();
        System.out.println("Terminals: " + terminals);
        CardTerminal terminal = terminals.get(0);

        Card card = terminal.connect("*");
        CardChannel channel = card.getBasicChannel();
        // ATR
        System.out.println("ATR -> " + HexString.byteArrayToHex(card.getATR().getBytes()));

        // SELECT FILE
        System.out.println("SELECT FILE");
        byte[] aid = {(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE,
                      (byte) 0x01, (byte) 0x01};
        CommandAPDU apdu = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, aid);
        System.out.println("-> " + HexString.byteArrayToHex(apdu.getBytes()));
        ResponseAPDU rapdu = channel.transmit(apdu);
        System.out.println("<- " + HexString.byteArrayToHex(rapdu.getBytes()));

        // GET GREETING
        System.out.println("GET GREETING");
        apdu = new CommandAPDU(0x00, 0xA0, 0x01, 0x00, 0xFF);
        System.out.println("-> " + HexString.byteArrayToHex(apdu.getBytes()));
        rapdu = channel.transmit(apdu);
        System.out.println("<- " + HexString.byteArrayToHex(rapdu.getBytes()));
        System.out.println(new String(rapdu.getData()));

        // SET GREETING
        System.out.println("SET GREETING");
        byte[] newGreeting = "New hello!".getBytes(StandardCharsets.UTF_8);
        apdu = new CommandAPDU(0x00, 0xA2, 0x00, 0x00, newGreeting);
        System.out.println("-> " + HexString.byteArrayToHex(apdu.getBytes()));
        rapdu = channel.transmit(apdu);
        System.out.println("<- " + HexString.byteArrayToHex(rapdu.getBytes()));

        // ADD
        System.out.println("ADD");
        byte[] data = new byte[4];
        short a = 5;
        short b = 6;
        data[0] = (byte) (a & 0xff);
        data[1] = (byte) (a >> 8 & 0xff);
        data[2] = (byte) (b & 0xff);
        data[3] = (byte) (b >> 8 & 0xff);
        apdu = new CommandAPDU(0x00, 0xB0, 0x00, 0x00, data);
        System.out.println("-> " + HexString.byteArrayToHex(apdu.getBytes()));
        rapdu = channel.transmit(apdu);
        System.out.println("<- " + HexString.byteArrayToHex(rapdu.getBytes()));
        System.out.println("SUM: " + ((rapdu.getData()[0] & 0xff) +
                                     ((rapdu.getData()[1] & 0xff) << 8)));

        // GENERATE KEY PAIR
        System.out.println("GENERATE KEY PAIR");
        apdu = new CommandAPDU(0x00, 0xC0, 0x00, 0x00, 0xFF);
        System.out.println("-> " + HexString.byteArrayToHex(apdu.getBytes()));
        rapdu = channel.transmit(apdu);
        System.out.println("<- " + HexString.byteArrayToHex(rapdu.getBytes()));

        // READ PUBLIC KEY
        System.out.println("READ PUBLIC KEY");
        apdu = new CommandAPDU(0x00, 0xC2, 0x00, 0x00, 0xFF);
        System.out.println("-> " + HexString.byteArrayToHex(apdu.getBytes()));
        rapdu = channel.transmit(apdu);
        System.out.println("<- " + HexString.byteArrayToHex(rapdu.getBytes()));

        // build public key from data
        RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(
                new BigInteger(1, rapdu.getData()), BigInteger.valueOf(65537));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(rsaPublicKeySpec);

        // USE PRIVATE KEY
        System.out.println("USE PRIVATE KEY");
        String document = "SJUG #65: Karty Java Cards";
        byte[] documentHash = MessageDigest.getInstance("SHA-256").digest(document.getBytes());
        byte[] hashPrefix = new byte[]{(byte) 0x30, (byte) 0x31, (byte) 0x30, (byte) 0x0D,
                (byte) 0x06, (byte) 0x09, (byte) 0x60, (byte) 0x86, (byte) 0x48, (byte) 0x01,
                (byte) 0x65, (byte) 0x03, (byte) 0x04, (byte) 0x02, (byte) 0x01, (byte) 0x05,
                (byte) 0x00, (byte) 0x04, (byte) 0x20};
        byte[] hashAndPrefix = new byte[hashPrefix.length + documentHash.length];
        System.arraycopy(hashPrefix, 0, hashAndPrefix, 0, hashPrefix.length);
        System.arraycopy(documentHash, 0, hashAndPrefix, hashPrefix.length,
                         documentHash.length);

        apdu = new CommandAPDU(0x00, 0xD0, 0x00, 0x00, hashAndPrefix);
        System.out.println("-> " + HexString.byteArrayToHex(apdu.getBytes()));
        rapdu = channel.transmit(apdu);
        System.out.println("<- " + HexString.byteArrayToHex(rapdu.getBytes()));

        card.disconnect(false);

        // verify signature on host side
        document = "SJUG #65: Karty Java Cards";
        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(publicKey);
        verifier.update(document.getBytes());
        boolean status = verifier.verify(rapdu.getData());

        System.out.println(status ? "Signature OK" : "Signature NOT OK");
    }
}
