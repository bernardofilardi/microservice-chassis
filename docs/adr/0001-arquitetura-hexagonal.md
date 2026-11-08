# ADR-0001: Adoção da Arquitetura Hexagonal (Ports &amp; Adapters) 

- **Status:** Aprovado 
- **Data:** 2026-11-07 
- **Autor:** Bernardo Filardi 
- **Contexto de Aplicação:** `microservice-chassis` 

--- 

## 1. Contexto e Problema 

Ao projetar um *chassis* corporativo para microsserviços, um dos principais riscos é a **complexidade acidental** gerada pelo acoplamento forte entre regras de negócio e dependências de infraestrutura (Spring Framework, Spring Data JPA, HTTP, clientes Kafka) [2]. 

Em arquiteturas convencionais de 3 camadas (Controller-Service-Repository), é comum que anotações de ORM/JPA e lógicas de entrega vazem para as entidades de negócio. Isso prejudica a **manutenibilidade** e a **evolutividade** do sistema — dois pilares fundamentais para o ciclo de vida do software [2, 4] —, tornando os testes de unidade lentos (por dependerem do contexto do Spring) e dificultando a troca ou atualização de tecnologias de infraestrutura. 

--- 

## 2. Decisão 

Decidimos adotar a **Arquitetura Hexagonal (*Ports &amp; Adapters*)** como padrão arquitetural obrigatório para o *chassis* de microsserviços [5]. 

### Diretrizes de Implementação: 

1. **Camada de Domínio (`domain`):** Deve conter exclusivamente lógicas de negócio, entidades e regras do domínio expressas em **Java Puro (POJOs/Records)**. É estritamente proibido importar anotações de frameworks (Spring, JPA, Jackson) dentro deste pacote [5]. 
2. **Camada de Aplicação (`application`):** Define os Casos de Uso (*Use Cases*) e as interfaces de **Portas de Entrada** (*Inbound Ports*) e **Portas de Saída** (*Outbound Ports*). 
3. **Camada de Infraestrutura (`infrastructure`):** Contém os **Adaptadores** (*Adapters*) de entrada (Controllers REST, Consumidores Kafka) e de saída (Repositórios Spring Data JPA, Clientes OTLP, Exporters Prometheus) [5]. 
4. **Governança Automatizada:** A integridade dessas fronteiras arquiteturais será validada continuamente na esteira de CI/CD via testes automatizados com **ArchUnit**. 

--- 

## 3. Consequências e Trade-Offs 

### 🟢 Pontos Positivos (Ganhos): 
- **Alta Testabilidade:** As regras de negócio do núcleo de domínio podem ser testadas através de testes unitários extremamente rápidos em Java puro (executados em milissegundos, sem subir o contexto da JVM/Spring). 
- **Independência de Framework:** O modelo de domínio fica protegido de alterações de versão ou substituição de frameworks de persistência/mensageria [2]. 
- **Evolutividade &amp; Simplicidade Abstraída:** Facilita a substituição de um adaptador (ex.: migrar de PostgreSQL para MongoDB ou de Kafka para RabbitMQ) alterando apenas a camada de infraestrutura, mantendo o domínio intocado [2, 4]. 

### 🔴 Pontos Negativos / Desafios (Custos): 
- **Boilerplate e Mapeadores:** Exige o mapeamento explícito entre DTOs de API, Entidades de Domínio e Entidades JPA de Banco de Dados. 
- **Curva de Aprendizado:** Exige disciplina da equipe de engenharia para não violar as fronteiras de pacotes (mitigado pelo uso do ArchUnit). 

--- 

## 4. Alternativas Consideradas e Descartadas 
- **Arquitetura Tradicional em 3 Camadas (Controller-Service-Repository):** Descartada por promover acoplamento direto das entidades com anotações `@Entity` do JPA e injeção do Spring dentro das regras de negócio, reduzindo a flexibilidade de manutenção [2]. 
- **Clean Architecture (Uncle Bob):** Considerada muito similar. Optamos pela nomenclatura **Hexagonal (Ports &amp; Adapters)** por ser mais direta no mapeamento dos componentes de infraestrutura do *chassis* [5]. 

--- 

## 5. Referências e Padrões 
- Alistair Cockburn — *Hexagonal Architecture (Ports and Adapters)* 
- Martin Kleppmann — *Designing Data-Intensive Applications (Maintainability &amp; Evolutivity)* 
- Testes de Regra Arquitetural via ArchUnit (`com.tngtech.archunit`)