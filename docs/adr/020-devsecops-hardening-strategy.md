# ADR 020: Estratégia de DevSecOps e Hardening de Infraestrutura

* **Status:** Aceito
* **Data:** 2025-08-30
* **Decisores:** Fabio Desenho (Software Architect), Security Engineer

## Contexto e Problema
Por lidar com segurança pública, o SafeVision é um alvo potencial para ataques. A superfície de ataque inclui desde o dispositivo Edge até a API Gateway. Precisamos de uma "Cadeia de Custódia" de segurança no desenvolvimento.

## Decisão
Implementar um pipeline de **DevSecOps** integrado ao GitHub Actions.
* **SAST/DAST:** Uso de SonarQube e scans de vulnerabilidades de bibliotecas.
* **Image Scanning:** Uso de **Trivy** para escanear imagens Docker antes do push.
* **Hardening:** Aplicação de políticas de "Least Privilege" nos containers e segregação de redes.

## Consequências
### Positivas
* **Segurança por Design:** Identifica vulnerabilidades antes mesmo do deploy.
* **Compliance:** Facilita auditorias de segurança e certificações.

### Negativas
* **Fricção no Dev:** Pode impedir o merge de PRs se vulnerabilidades críticas forem encontradas, exigindo correção imediata.