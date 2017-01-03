package com.example.config

import org.springframework.security.jwt.crypto.sign.SignatureVerifier

class NoOpVerifier(val algoType:String = "RSA") : SignatureVerifier {
    override fun verify(content: ByteArray?, signature: ByteArray?) {}
    override fun algorithm(): String = algoType
}

