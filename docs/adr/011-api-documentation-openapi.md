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