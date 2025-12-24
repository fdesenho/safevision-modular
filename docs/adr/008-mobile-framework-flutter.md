# ADR 008: Framework Mobile (Flutter)

* **Status:** Planejado (Fase 3)
* **Data:** 2025-12-24

## Contexto e Problema
O projeto requer um aplicativo móvel ("Officer Companion") para uso em campo por oficiais. Manter duas bases de código nativas (Swift para iOS e Kotlin para Android) é inviável com os recursos atuais da equipe.

## Decisão
Utilizar **Flutter (Dart)** para desenvolvimento multiplataforma.

## Consequências
### Positivas
* **Single Codebase:** Um único código gera binários para Android e iOS.
* **Performance:** O Flutter compila para código nativo ARM (sem ponte JavaScript), essencial para mapas e alertas em tempo real.
* **UI Consistente:** O motor de renderização próprio garante que o app tenha a mesma aparência em qualquer versão do OS.

### Negativas
* **Tamanho do App:** O binário final tende a ser maior que apps nativos puros.
* **Integrações de Hardware:** Recursos muito específicos (ex: botões de hardware proprietários) podem exigir escrita de código nativo (Platform Channels).