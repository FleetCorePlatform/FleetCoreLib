package io.fleetcoreplatform.Generators;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Generates highly collision-resistant, temporally-sorted unique identifiers. The output relies on
 * a combination of hardware MAC addresses, secure random nonces, and timestamping.
 */
public class ChronoHashID {
    private static String getMacSegment() throws SocketException {
        NetworkInterface ni = NetworkInterface.getNetworkInterfaces().nextElement();
        byte[] mac = ni.getHardwareAddress();

        String[] hexadecimal = new String[mac.length];
        for (int i = 0; i < mac.length; i++) {
            hexadecimal[i] = String.format("%02X", mac[i]);
        }

        return String.join("-", hexadecimal).substring(0, 7);
    }

    private static String hash(String value) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = md.digest(value.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hashBytes);
    }

    private static String getSecureString() {
        String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
        String CHAR_UPPER = CHAR_LOWER.toUpperCase();
        String NUMBER = "0123456789";
        String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;

        SecureRandom sr = new SecureRandom();
        StringBuilder sb = new StringBuilder(15);

        for (int i = 0; i < 15; i++) {
            int rndCharAt = sr.nextInt(DATA_FOR_RANDOM_STRING.length());
            sb.append(DATA_FOR_RANDOM_STRING.charAt(rndCharAt));
        }

        return sb.toString();
    }

    /**
     * Synthesizes a chronologically sortable hash ID string. The prefix contains the current epoch
     * time, while the suffix is a SHA-256 hash of the local hardware MAC combined with a
     * 15-character secure random sequence.
     *
     * @return The generated unique identifier.
     * @throws SocketException If hardware network interfaces cannot be accessed.
     * @throws NoSuchAlgorithmException If the runtime environment lacks SHA-256 support.
     */
    public static String generateCHID() throws SocketException, NoSuchAlgorithmException {
        String timeMillis = String.valueOf(System.currentTimeMillis());
        String macSegment = getMacSegment();
        String secureString = getSecureString();

        String rawId = macSegment + secureString;
        String hashedId = hash(rawId);

        return timeMillis + hashedId;
    }
}
