package com.example.domain.model

enum class CallStatus {
    IDLE,
    /**
     * O convite foi enviado, aguardando resposta do servidor ou rede.
     * UI: Mostrar "Conectando..."
     */
    DIALING,

    /**
     * O telefone do outro lado está tocando (recebemos um 180 Ringing).
     * UI: Mostrar "Tocando..."
     */
    RINGING,

    /**
     * O telefone do outro lado está tocando (recebemos um 180 Ringing).
     * UI: Mostrar "Tocando...(Alguém está ligando)"
     */
    INCOMING,

    /**
     * A chamada foi atendida e o áudio está fluindo.
     * UI: Mostrar o cronômetro (00:00) e botões de controle ativados.
     */
    ACTIVE,

    /**
     * A chamada foi finalizada, rejeitada ou houve erro de conexão.
     * UI: Mostrar "Encerrado" e fechar a tela.
     */
    ENDED
}