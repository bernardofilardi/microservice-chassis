# ADR-0003: Adoção de Virtual Threads (Java 21 / Project Loom) para Concorrência de Alto Throughput 

- **Status:** Aprovado 
- **Data:** 2026-11-07 
- **Autor:** Bernardo Filardi 
- **Contexto de Aplicação:** `microservice-chassis`

---
## 1. Contexto e Problema 

No desenvolvimento de microsserviços modernos voltados para I/O (APIs REST que realizam chamadas HTTP síncronas, consultas a bancos de dados relacionais e comunicação com *brokers* de mensageria), o modelo tradicional de concorrência do Java — baseado em **Platform Threads** (onde 1 thread do Java mapeia diretamente para 1 thread do Sistema Operacional) — impõe limitações físicas e operacionais severas: 
1. **Alto Custo de Memória e Troca de Contexto:** *Platform threads* consomem um espaço significativo de memória (stack contígua tipicamente de ~1MB por thread) e geram sobrecarga (*overhead*) de CPU nas trocas de contexto (*context switching*) realizadas pelo kernel do SO sob alta concorrência [1]. 
2. **Dilema da Programação Reativa vs. Imperativa:** Para contornar o gargalo de I/O bloqueante sem esgotar as threads do SO, muitas equipes migram para frameworks reativos (ex.: Spring WebFlux, Project Reactor). Embora eficientes, sistemas reativos introduzem uma alta **complexidade acidental** (*accidental complexity*), prejudicando a legibilidade do código, o rastreamento de pilha (*stack traces* e *debugging*) e a evolutividade do sistema [3]. 
3. **Elevação da Latência p99 sob Carga:** Em ambientes de alta vazão (*throughput*), requisições que aguardam I/O retendo threads físicas geram filas de espera no servidor web, degradando a latência dos percentis mais altos (p99/p99.9) [1, 2]. 

---
## 2. Decisão 

Decidimos adotar as **Virtual Threads (Project Loom, introduzido oficialmente no Java 21)** como o modelo primário de concorrência e execução de tarefas no *chassis* corporativo [1, 4]. 

### Diretrizes de Implementação: 
1. **Habilitação Nativa no Spring Boot 3:** Ativar as Virtual Threads nativamente via propriedade de configuração no `application.yml`: 
```yaml 
spring: 
    threads: 
        virtual: 
            enabled: true
```
## 3. Consequências e Trade-Offs

### 🟢 Pontos Positivos (Ganhos):

* **Escalabilidade &amp; High Throughput:** Capacidade de processar centenas de milhares de requisições concorrentes sob I/O sem estourar o limite de memória da JVM ou exigir escalamento físico agressivo de hardware.
* **Simplicidade &amp; Operabilidade:** Manutenção do paradigma imperativo clássico, simplificando testes unitários, *debugging*, rastreabilidade via logs e reduzindo o atrito na manutenção por novos engenheiros.
* **Transparência de Telemetria:** Suporte nativo a *stack traces* legíveis e rastreamento distribuído com OpenTelemetry (contexto de MDC preservado por requisição).

### 🔴 Pontos Negativos / Desafios (Custos):

* **Risco de** **Thread Pinning** **:** Exige monitoramento e profiling de código (via IntelliJ Profiler ou flags como `-Djdk.traceVirtualThreadLocals`) para detectar bibliotecas legadas que travem as *carrier threads*.
* **Sem Ganho para Operações CPU-Bound:** Virtual Threads não aceleram cálculos matemáticos ou algoritmos intensivos de CPU; o limite nesses casos permanece atrelado à quantidade de *cores* físicos do processador.

---

## 4. Alternativas Consideradas e Descartadas

* **Platform Threads Tradicionais (Thread Pool por Requisição):** Descartadas por imporem um teto baixo de escalabilidade, consumo excessivo de memória por thread (~1MB) e degradação da latência p99 sob alta concorrência.
* **Programação Reativa (Spring WebFlux / Project Reactor):** Descartada por introduzir complexidade acidental desnecessária, comprometer a legibilidade das regras de negócio, dificultar o *debugging* e prejudicar a evolutividade da base de código.

---

## 5. Referências e Padrões

* **JEP 444:** Virtual Threads (JDK 21 Release Specification).
* **Kleppmann, Martin:** *Designing Data-Intensive Applications* — Capítulo 1: Confiabilidade, Escalabilidade (Parâmetros de Carga e Latência p99) e Manutenibilidade (Operabilidade e Simplicidade via Abstrações).
* **Plano de Estudos Staff Software Engineer:** *Fase 1: Concorrência e Performance (Virtual Threads &amp; minimização de latência p99)*.
