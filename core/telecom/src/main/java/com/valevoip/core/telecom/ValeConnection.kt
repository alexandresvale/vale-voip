package com.valevoip.core.telecom

import android.telecom.Connection
import android.telecom.DisconnectCause
import android.util.Log

class ValeConnection : Connection() {

    init {
        // Define as capacidades que nossa ligação suporta.
        // Estamos dizendo: Nós suportamos emudecer (Mute) e travar/segurar (Hold)
        connectionProperties = PROPERTY_SELF_MANAGED
        connectionCapabilities = CAPABILITY_MUTE or CAPABILITY_SUPPORT_HOLD
        audioModeIsVoip = true
    }

    /**
     * Quando o usuário aperta o botão verde de atender no Android
     */
    override fun onAnswer() {
        super.onAnswer()
        Log.d("ValeConnection", "Usuário Atendeu a Chamada!")
        setActive() // Dizemos pro Android que a chamada começou e a tela de duração pode contar

        // No futuro, chamaremos o SipClient para ele mandar o "200 OK" pro Linphone!
    }

    /**
     * Quando o usuário aperta o botão vermelho de rejeitar ou desligar
     */
    override fun onDisconnect() {
        super.onDisconnect()
        Log.d("ValeConnection", "Usuário Desligou!")
        // Avisamos o sistema que a chamada encerrou localmente
        setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
        destroy() // Limpa da memória

        // No futuro, chamaremos o SipClient para ele mandar o "BYE" ou "DECLINE"
    }

    /**
     * Quando o usuário muda o estado de áudio (Ex: Liga o Viva Voz, ou bota Mute)
     */
    override fun onCallAudioStateChanged(state: android.telecom.CallAudioState?) {
        super.onCallAudioStateChanged(state)
        Log.d("ValeConnection", "Áudio mudou: Mute = ${state?.isMuted}")
    }
}