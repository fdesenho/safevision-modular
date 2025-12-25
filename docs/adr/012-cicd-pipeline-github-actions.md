# ADR 012: Pipeline de CI/CD com GitHub Actions

* **Status:** Aceito
* **Data:** 2025-12-25
* **Decisores:** Fabio Desenho (Software Architect), DevOps Engineer

## Contexto e Problema
A gestão manual de múltiplos microsserviços é inviável e insegura para um ambiente de vigilância crítica. Os desafios incluem:
1. **Consistência de Deploy:** Garantir que apenas código que passou em todos os testes seja promovido para produção.
2. **Gestão de Artefatos:** Necessidade de automatizar o build, versionamento e publicação de imagens Docker.
3. **Visibilidade:** Necessidade de rastrear o status de cada entrega diretamente vinculada às issues e commits.

## Decisão
Padronizar a orquestração de entregas através do **GitHub Actions**.
* **Fluxo:** Workflows automatizados para `Pull Requests` (testes e linting) e `Push` na `main` (build e deploy).
* **Registry:** Utilização do **GitHub Container Registry (GHCR)** para armazenamento privado de imagens Docker.
* **Segurança:** Integração de scans de vulnerabilidade (Trivy) nas imagens antes da publicação.

## Consequências
### Positivas
* **Maturidade DevOps:** Integração nativa com o repositório, boards de projeto e sistema de revisão de código do GitHub.
* **Eficiência Operacional:** Redução drástica do tempo entre o commit e a disponibilidade do artefato buildado.

### Negativas
* **Dependência de Plataforma:** A lógica da pipeline fica vinculada à sintaxe do GitHub Actions (.yaml), dificultando uma eventual migração para Jenkins ou GitLab CI.