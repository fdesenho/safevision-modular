# ADR 004: Gestão de Segredos com Spring Cloud Vault

* **Status:** Proposto (Fase 2)
* **Data:** 2025-12-24

## Contexto e Problema
Atualmente, as credenciais (DB, RabbitMQ, Tokens) são gerenciadas via variáveis de ambiente ou arquivos `.env`. Isso expõe riscos de vazamento em logs, histórico de Git e dificulta a rotação periódica de senhas (compliance de segurança).

## Decisão
Adotar **HashiCorp Vault** integrado via **Spring Cloud Vault**.
As aplicações Java não terão mais senhas configuradas localmente; elas solicitarão credenciais dinâmicas ao Vault durante a inicialização.

## Consequências
### Positivas
* **Segurança Zero Trust:** Senhas não ficam armazenadas em texto plano nos servidores de aplicação.
* **Rotação Automática:** Facilita a troca de senhas de banco sem redeploy da aplicação.
* **Auditoria:** Registro centralizado de qual serviço acessou qual segredo e quando.

### Negativas
* **Single Point of Failure:** Se o Vault cair, nenhuma aplicação nova consegue iniciar.
* **Overhead de Inicialização:** Aumenta ligeiramente o tempo de startup da aplicação.