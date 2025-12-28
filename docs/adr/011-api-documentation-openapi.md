# ADR 011: Documentação API-First com OpenAPI/Swagger

* **Status:** Aceito
* **Data:** 2025-12-25
* **Decisores:** Fabio Desenho (Software Architect), Integration Lead

## Contexto e Problema
O SafeVision possui múltiplos consumidores (Frontend Web, futuro App Mobile e integrações externas). A ausência de um contrato formal de API gera:
1. **Desalinhamento entre Times:** Premissas conflitantes sobre payloads de entrada e saída.
2. **Onboarding Lento:** Dificuldade para novos desenvolvedores compreenderem os recursos disponíveis nos microsserviços.
3. **Inconsistência de Interface:** Endpoints com diferentes padrões de nomenclatura e tratamento de erros.

## Decisão
Adotar a especificação **OpenAPI 3.0** como a "Única Fonte de Verdade" para as interfaces de comunicação REST.
* **Ferramenta:** Uso de **SpringDoc OpenAPI** para geração dinâmica de documentação e exposição via Swagger UI.
* **Abordagem:** *Contract-First* para novos recursos, garantindo que o design da API preceda a implementação.

## Consequências
### Positivas
* **Interatividade:** Possibilidade de testar endpoints e fluxos de dados em tempo real via Swagger UI sem ferramentas externas.
* **Produtividade:** Permite a geração automática de clientes (SDKs) para o frontend Angular e futuro aplicativo Flutter.

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
- **Actual execution:** 0h (Aguardando implementação)