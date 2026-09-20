# Vale VoIP - Contexto e Especificações

## Contexto
O Vale VoIP é um aplicativo Android focado em comunicação por voz (SIP/VoIP). Ele foi projetado para atuar como um ramal móvel inteligente. A arquitetura foi construída para isolar o motor SIP da interface, utilizando *Feature Modules* e Clean Architecture. O motor SIP inicial adotado é o **Linphone**, sendo futuramente substituível por PJSIP/PJSUA2 sem impacto na interface.

## 1. Atores (Actors)
- **Usuário:** Pessoa que utiliza o app para originar, receber e gerenciar chamadas.
- **Servidor SIP / PBX:** Infraestrutura (ex: Asterisk, FreePBX) responsável pelo roteamento, registro e autenticação.
- **Sistema Operacional Android:** Gerencia a interface do sistema, foreground services e o roteamento de áudio padrão (TelecomManager).

## 2. Requisitos Funcionais (RFs)
- **RF01 - Autenticação SIP:** O usuário deve conseguir registrar sua conta (Usuário/Ramal, Senha, Domínio).
- **RF02 - Efetuar Chamadas:** O usuário deve conseguir iniciar uma chamada de áudio digitando o número.
- **RF03 - Receber Chamadas:** O app deve notificar e permitir atender chamadas ativas operando em um Foreground Service contínuo.
- **RF04 - Controle de Áudio:** Permitir Mute/Unmute do microfone e alternar para Viva-voz.
- **RF05 - Desligar/Rejeitar:** Desligar uma chamada em andamento ou rejeitar chamadas recebidas.
- **RF06 - Histórico de Chamadas:** Visualizar registro das atividades (recebidas, efetuadas, perdidas).

## 3. Requisitos Não Funcionais (RNFs)
- **RNF01 - Clean Architecture & Multi-Módulo:** Separação entre `core:domain`, infraestrutura (`core:sip-network`) e módulos de features (`feature:dialer`, `feature:call`).
- **RNF02 - Build Moderno:** Uso de `libs.versions.toml` com *Convention Plugins* (`build-logic`) para previsibilidade de compilação.
- **RNF03 - Stack Reativa:** Kotlin, Coroutines e Flows para *Main-safe* UI e assincronismo.
- **RNF04 - Interface:** Construída integralmente com Jetpack Compose (MVVM).
- **RNF05 - Serviço Contínuo:** Utilização de Foreground Service para a persistência de conexão sem depender de Push Notifications (FCM).