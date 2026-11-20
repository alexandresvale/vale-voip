# Vale VoIP - Contexto e Especificações Técnicas

## Contexto
O **Vale VoIP** é um aplicativo Android nativo focado em comunicação corporativa por voz sobre IP (**SIP/VoIP**), desenvolvido como Trabalho de Conclusão de Curso (TCC). Ele foi projetado para atuar como um ramal móvel inteligente e seguro, integrando-se com servidores SIP e centrais PABX (Asterisk, FreePBX, Issabel, OpenSIPS).

A arquitetura foi construída para isolar integralmente as regras de negócio e a interface gráfica do motor de telefonia C++, utilizando **Clean Architecture**, princípios **SOLID** e modularização estrita (**15 módulos**). O motor SIP adotado na camada de infraestrutura é a **liblinphone (`org.linphone:linphone-sdk-android`)**, encapsulada por interfaces de domínio que permitem sua substituição (ex.: PJSIP/PJSUA2) sem impacto nas camadas de domínio ou apresentação.

---

## 1. Atores (Actors)
- **Usuário:** Pessoa que utiliza o aplicativo para autenticar seu ramal, originar, receber e gerenciar chamadas de voz.
- **Servidor SIP / PABX:** Infraestrutura responsável pelo registro (`REGISTER`), roteamento de chamadas (`INVITE`, `re-INVITE`, `BYE`), travessia de NAT (`Outbound Proxy` / `STUN`) e negociação de mídia (`SDP`).
- **Sistema Operacional Android:** Gerencia o ciclo de vida do *Foreground Service* (`phoneCall` / `microphone`), foco e rotas de áudio (Auricular, Viva-voz, Bluetooth SCO, Fones com fio/USB), sensores de proximidade, notificações e políticas de energia (*Doze Mode*).

---

## 2. Requisitos Funcionais (RFs)
- **RF01 - Autenticação SIP Flexível:** O usuário deve conseguir registrar sua conta informando **Usuário/Ramal**, **Senha**, **Domínio ou IPv4**, **Porta customizável** (`5060` por padrão, validada entre `1` e `65535`) e o protocolo de transporte (**UDP**, **TCP** ou **TLS**).
- **RF02 - Discador Híbrido (Numérico e Alfanumérico):** O usuário deve conseguir iniciar chamadas de áudio tanto para ramais numéricos (via teclado T9 `123`) quanto para usuários ou URIs SIP alfanuméricas (ex.: `valevoip`, `usuario@dominio` via modo `ABC`), com suporte a colagem rápida da área de transferência.
- **RF03 - Recebimento Contínuo de Chamadas:** O aplicativo deve manter o registro SIP ativo em segundo plano por meio de um *Foreground Service* resiliente, exibindo notificações interativas (`Atender`, `Recusar`, `Desligar`) com navegação sob clique.
- **RF04 - Roteamento de Áudio Multi-Dispositivo:** Permitir `Mute/Unmute` do microfone, alternância rápida para `Viva-voz` e seleção dedicada de dispositivo de áudio (**Bluetooth**, **Fone com fio/USB**, **Viva-voz** e **Telefone/Auricular**), sincronizando automaticamente a saída e o microfone correspondente.
- **RF05 - Controle de Chamada, Espera e DTMF:** Permitir atender, rejeitar ou encerrar chamadas, colocar e retomar chamadas em espera (**Hold/Resume** via `re-INVITE`) e enviar dígitos **DTMF** (RFC 4733 / SIP INFO) para interação com URAs.
- **RF06 - Histórico de Chamadas:** Visualizar o registro detalhado de chamadas (efetuadas, recebidas e perdidas), com busca por ramal/contato, filtro de chamadas perdidas, rediscagem rápida, exclusão individual e limpeza total.
- **RF07 - Gerenciamento de Codecs de Áudio (SDP/RTP):** Permitir visualizar e habilitar/desabilitar codecs de áudio suportados pelo motor (`OPUS`, `PCMU G.711 µ-law`, `PCMA G.711 A-law`, `G722`, `GSM`, `Speex`, `L16`), impedindo a desativação do último codec ativo e exibindo na tela de chamada um indicador em tempo real do codec negociado via SDP (ex.: `OPUS • 48 kHz`).
- **RF08 - Configurações de Rede, Segurança (SRTP), Bateria e Diagnóstico:** Permitir configurar Cancelamento de Eco (AEC), Vibração, restrição para **Usar Apenas Wi-Fi**, **Criptografia de Mídia (`SRTP`)**, consultar/solicitar isenção de otimização de bateria (*Doze Mode*) e exportar um **Relatório de Diagnóstico Técnico para Suporte** sem exposição de senhas ou dados sensíveis (LGPD).

---

## 3. Requisitos Não Funcionais (RNFs)
- **RNF01 - Clean Architecture & Modularização Estrita (15 Módulos):**
  - **Domínio Puro (`:core:domain`):** 100% Kotlin puro, sem dependências do Android SDK ou de bibliotecas externas.
  - **Infraestrutura (`:core:*`):** `:core:sip`, `:core:telecom`, `:core:data`, `:core:designsystem`, `:core:navigation` e `:core:analytics`.
  - **Features (`:feature:*`):** `:feature:splash`, `:feature:login`, `:feature:home`, `:feature:dialer`, `:feature:call`, `:feature:history` e `:feature:config`, orquestradas por `:app`.
- **RNF02 - Build System Moderno:** Gerenciamento centralizado via Version Catalog (`gradle/libs.versions.toml`) e *Gradle Convention Plugins* (`:build-logic`), compilando com **API 35 (Android 15)** e JDK 17.
- **RNF03 - Stack Reativa & Confinamento JNI:** Uso de Kotlin Coroutines, `StateFlow` e `Channel`/`SharedFlow` garantindo execução *Main-safe* e confinamento seguro dos ponteiros nativos C++ da `liblinphone`.
- **RNF04 - Interface Declarativa:** Construída integralmente em **Jetpack Compose** com Material 3 (suporte completo a **Light Mode** e **Dark Mode**) e fluxo unidirecional de dados (MVI / UDF).
- **RNF05 - Resiliência de Serviço & Sensor de Proximidade Inteligente:** Uso de *Foreground Service* adequado às políticas do Android 14/15 (`FOREGROUND_SERVICE_TYPE_PHONE_CALL` e promoção dinâmica de `FOREGROUND_SERVICE_TYPE_MICROPHONE`), travessia de NAT (`STUN`/`ICE` + `Outbound Proxy`) e ativação do `PROXIMITY_SCREEN_OFF_WAKE_LOCK` exclusivamente quando o áudio está no auricular (`EARPIECE`).
- **RNF06 - Segurança em Repouso e em Trânsito:**
  - **Em Repouso (*Data at Rest*):** Credenciais SIP criptografadas no DataStore utilizando **Google Tink (`AES256-GCM`)** integrado ao **Android Keystore**, com exclusão explícita de backup em nuvem (`backup_rules.xml` e `data_extraction_rules.xml`).
  - **Em Trânsito (*Data in Transit*):** Suporte a sinalização **SIP/TLS** com validação de certificado e criptografia de pacotes de voz via **SRTP** (`RTP/SAVP`).
- **RNF07 - Hardening de Produção:** Build `release` com minificação e ofuscação **R8/ProGuard** customizada (`proguard-rules.pro`), higienização automática de logs SIP nativos em produção e bloqueio de tráfego HTTP aberto (`android:usesCleartextTraffic="false"`).
- **RNF08 - Observabilidade & CI/CD Automatizado:** Monitoramento via Firebase Analytics e Crashlytics (`Timber`) e pipeline de integração e entrega contínua no **GitHub Actions** (Lint, Testes Unitários, Testes Integrados em Emulador Android KVM e geração automatizada de builds de Homologação e Produção).
