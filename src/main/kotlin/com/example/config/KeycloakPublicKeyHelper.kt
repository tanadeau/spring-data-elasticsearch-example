package com.example.config

import org.apache.commons.codec.binary.Base64
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.X509EncodedKeySpec


/**
 * Decode a Public Key from a PEM string
 *
 * Keycloak gives the key in a 'PEM' format minus the BEGIN/END strings
 * And the TokenVerifier expects it in the open-ssh format (rsa-ssh)
 *
 * @param pem
 * *
 * @return
 * *
 * @throws Exception
 */
fun decodePublicKey(pem: String): RSAPublicKey = decodePublicKey(pemToDer(pem))


private fun decodePublicKey(der: ByteArray): RSAPublicKey {
    val spec = X509EncodedKeySpec(der)
    val kf = KeyFactory.getInstance("RSA")
    return kf.generatePublic(spec) as RSAPublicKey
}

private fun pemToDer(pem: String): ByteArray = Base64.decodeBase64(removeBeginEnd(pem))


private fun removeBeginEnd(pem: String): String = pem.replace("-----BEGIN (.*)-----".toRegex(), "")
            .replace("-----END (.*)----".toRegex(), "")
            .replace("\r\n", "")
            .replace("\n", "")
            .trim { it <= ' ' }


