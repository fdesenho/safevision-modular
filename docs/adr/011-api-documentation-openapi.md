# ADR 011: Governança de APIs e Documentação Centralizada (OpenAPI/Swagger)

* **Status:** Implementado
* **Data:** 2025-12-31 (Atualizado)
* **Decisores:** Fabio Desenho (Software Architect), Integration Lead
* **Issue Vinculada:** #11

## 1. Contexto e Problema
O SafeVision opera numa arquitetura de microsserviços distribuídos com múltiplos consumidores (Frontend Angular, Mobile e Integrações). A ausência de contratos formais e centralizados gera:
1.  **Fragmentação de Conhecimento:** Desenvolvedores precisam conhecer a URL e porta específica de cada serviço para consultar sua API.
2.  **Problemas de CORS:** O consumo direto das APIs dos microsserviços pelo Frontend (para documentação) viola as políticas de *Same-Origin*, exigindo configurações de segurança frágeis.
3.  **Desalinhamento Frontend-Backend:** Risco de *drift* (divergência) entre os DTOs Java e as Interfaces TypeScript.

## 2. Decisão Arquitetural
Adotar a especificação **OpenAPI 3.0** como a "Única Fonte de Verdade", implementada através do padrão **API Gateway Aggregation**.

### 2.1. Componentes da Solução
* **Backend (Microserviços):** Uso de `springdoc-openapi-starter-webmvc-ui` para expor os contratos JSON (`/v3/api-docs`) em cada serviço de domínio (Auth, Alert, Recognition).
* **Edge (API Gateway):** Configuração do Spring Cloud Gateway como **Agregador de Documentação**. O Gateway centraliza a interface visual (Swagger UI) e roteia as chamadas para os JSONs dos serviços internos via Service Discovery (Eureka).
* **Frontend (Angular):** Adoção de **Compodoc/TSDoc** para governança de código e espelhamento estrito dos DTOs do Backend nas Interfaces TypeScript.

### 2.2. Estratégia de Segurança e CORS
Para viabilizar a agregação, foi decidido flexibilizar as regras de segurança especificamente para rotas de metadados, mantendo a proteção nos endpoints de negócio:
* **Gateway (WebFlux):** Configuração global de CORS (`CorsGlobalConfig`) permitindo origens controladas e liberação de rotas `/aggregate/**` e `/webjars/**` no `SecurityWebFilterChain`.
* **Serviços (MVC):** Liberação dos endpoints `/v3/api-docs` na `SecurityFilterChain` para permitir que o Gateway consuma os contratos sem necessidade de token de serviço.

## 3. Consequências

### ✅ Positivas
* **Ponto Único de Acesso:** Desenvolvedores acessam `gateway:8080/swagger-ui.html` e navegam por todos os serviços via menu *dropdown*, sem precisar saber portas internas.
* **Rastreabilidade End-to-End:** O fluxo `Controller Java (Swagger) -> Gateway (Aggregator) -> Frontend (TSDoc)` garante que a mudança em um DTO seja visível em toda a cadeia.
* **Isolamento de Segurança:** O Frontend interage apenas com o Gateway, simplificando a gestão de certificados e tokens JWT.

### ⚠️ Negativas / Riscos Mitigados
* **Complexidade de Dependências:** Exige gerenciamento cuidadoso no Maven (`dependencyManagement`) para evitar conflitos entre bibliotecas Reativas (Gateway/Netty) e Imperativas (Serviços/Tomcat).
* **Exposição de Metadados:** As rotas de documentação são públicas. Em ambiente produtivo, deve-se avaliar o bloqueio externo dessas rotas via Firewall ou perfil de Spring (`@Profile("!prod")`).

## 4. Referências Técnicas
* **URL Central:** `http://localhost:8080/swagger-ui.html`
* **Padrão de Agregação:** Configurado via `springdoc.swagger-ui.urls` no `application.yml` do Gateway.
* **Governança Frontend:** Dashboard disponível via `npm run doc:generate`.
### Negativas
* **Sobrecarga de Manutenção:** Exige disciplina contínua para manter anotações de código e esquemas de dados sincronizados com a evolução da API.

## 📊 Estimativa de Esforço e Custos (Referência: Fabio Desenho)
**Taxa Horária:** $60.00/h (Especialista em Eng. de Software)

| Atividade | Estimativa (h) | Custo ($) |
| :--- | :---: | :---: |
| Configuração springdoc (Java 21) | 2h | $120.00 |
| Instrumentação de Controllers | 4h | $240.00 |
| Documentação de Schemas DTO | 4h | $240.00 |
| Validação e Testes (Swagger) | 2h | $120.00 |
| **TOTAL** | **12h** | **$720.00** |

**Notas de Governança:**
- **Estimated execution:** 12h
- **Actual execution:** 5h 
