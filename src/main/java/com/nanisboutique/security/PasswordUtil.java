package com.nanisboutique.security;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Utilidades para almacenar y verificar contraseñas con PBKDF2-HMAC-SHA256. */
public final class PasswordUtil {
    private static final int ITERACIONES = 120_000;
    private static final int LONGITUD_BITS = 256;
    private static final int LONGITUD_SALT = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() { }

    public static String hash(char[] password) {
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        }
        byte[] salt = new byte[LONGITUD_SALT];
        RANDOM.nextBytes(salt);
        byte[] derived = derivar(password, salt, ITERACIONES);
        Base64.Encoder encoder = Base64.getEncoder().withoutPadding();
        return "PBKDF2$" + ITERACIONES + "$" + encoder.encodeToString(salt)
                + "$" + encoder.encodeToString(derived);
    }

    public static boolean verificar(char[] password, String almacenada) {
        if (password == null || almacenada == null) return false;
        String[] partes = almacenada.split("\\$", -1);
        if (partes.length != 4 || !"PBKDF2".equals(partes[0])) return false;
        try {
            int iteraciones = Integer.parseInt(partes[1]);
            if (iteraciones < 10_000 || iteraciones > 2_000_000) return false;
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] salt = decoder.decode(partes[2]);
            byte[] hashEsperado = decoder.decode(partes[3]);
            byte[] calculado = derivar(password, salt, iteraciones);
            return MessageDigest.isEqual(hashEsperado, calculado);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static byte[] derivar(char[] password, byte[] salt, int iteraciones) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iteraciones, LONGITUD_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("No fue posible procesar la contraseña.", ex);
        } finally {
            spec.clearPassword();
        }
    }
}
