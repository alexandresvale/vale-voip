package com.valevoip.core.domain.client

/**
 * Abstração pura de domínio para prover informações do dispositivo e do aplicativo.
 * Isola a regra de negócio do framework Android (android.os.Build / PackageManager).
 */
interface DeviceInfoProvider {
    val appVersion: String
    val manufacturer: String
    val model: String
    val osVersion: String
    val apiLevel: Int
}
