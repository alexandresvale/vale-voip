package com.valevoip.core.telecom

import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccountHandle
import android.util.Log

class ValeConnectionService : ConnectionService() {

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        Log.d("ValeTelecom", "Gerando Conexão para Chamada Recebida!")

        val connection = ValeConnection()
        connection.setInitializing()
        connection.setRinging()
        return connection
    }

    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        Log.d("ValeTelecom", "Gerando Conexão para Chamada Sucedida!")

        val connection = ValeConnection()
        // Ligação saindo já começa inicializando e logo "tocando" (chamando o outro lado)
        connection.setInitializing()
        connection.setDialing()

        return connection
    }
}