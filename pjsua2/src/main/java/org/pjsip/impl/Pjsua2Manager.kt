package org.pjsip.impl

import android.util.Log
import org.pjsip.pjsua2.AccountConfig
import org.pjsip.pjsua2.AuthCredInfo
import org.pjsip.pjsua2.Endpoint
import org.pjsip.pjsua2.EpConfig
import org.pjsip.pjsua2.TransportConfig
import org.pjsip.pjsua2.pjsip_transport_type_e
import org.pjsip.pjsua2.pjsua_state
import java.sql.DriverManager.println

class Pjsua2Manager {

    private lateinit var endpoint: Endpoint
    private lateinit var account: MyAccount

    fun start() {
        try {
            endpoint = Endpoint()
            if (endpoint.libGetState() > pjsua_state.PJSUA_STATE_NULL) {
                return
            }
            endpoint.libCreate()
            endpoint.libInit(EpConfig())
            endpoint.libStart()
            Log.d("####","Pjsua2Manager start success")
        } catch (exception: Exception) {
            Log.d("####","Pjsua2Manager error = $exception")
        }
    }

    fun init() {
        // Inicializar a instância de Endpoint
        endpoint = Endpoint()
        endpoint.libCreate()

        // Configurações básicas da biblioteca
        val epConfig = EpConfig()
        endpoint.libInit(epConfig)

        // Cria e configura o transporte (ex: UDP)
        val sipTpConfig = TransportConfig()
        sipTpConfig.port = 5060 // Porta SIP padrão
        endpoint.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_UDP, sipTpConfig)

        // Inicia a biblioteca
        endpoint.libStart()

        // Registrar uma conta SIP
        val accConfig = AccountConfig()
        accConfig.idUri = "sip:5005@192.168.100.12"
        accConfig.regConfig.registrarUri = "sip:192.168.100.12"
        accConfig.sipConfig.authCreds.add(AuthCredInfo("digest", "*", "5005", 0, "5005"))

        account = MyAccount()
        account.create(accConfig)

        // Outros ajustes e configurações...

        System.out.println("PJSUA2 Iniciado com sucesso!")
    }

    fun destroy() {
        account.delete()
        endpoint.libDestroy()
        endpoint.delete()
    }
}
