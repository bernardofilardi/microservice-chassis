# ADR-0002: Padronização de Observabilidade com OpenTelemetry, Prometheus e Jaeger 

- **Status:** Aprovado 
- **Data:** 2026-11-07 
- **Autor:** Bernardo Filardi 
- **Contexto de Aplicação:** `microservice-chassis`

---
## 1. Contexto e Problema 

Em sistemas distribuídos baseados em microsserviços, a capacidade de monitorar a saúde do sistema e diagnosticar falhas em tempo real é indispensável para garantir os pilares de **Confiabilidade** (*Reliability*) e **Manutenibilidade / Operabilidade** (*Maintainability / Operability*) [2, 4, 5]. Quando uma requisição atravessa múltiplos serviços, *logs* isolados em arquivos texto não são suficientes para entender a causa raiz de degradações de latência ou falhas em cascata [4, 6]. Sem uma estratégia de observabilidade padronizada: 
1. **Falta de Visibilidade em Cauda Longa (p99):** Dificuldade para diagnosticar latências nos percentis mais altos (p99/p99.9), fundamentais para garantir o desempenho e conter gargalos sob alta carga [3]. 
2. **Acoplamento com Fornecedores (*Vendor Lock-in*):** Dependências de SDKs proprietários de APM (*Application Performance Monitoring*) introduzem travas de código e dificultam a migração entre ferramentas de observabilidade. 
3. **Descentralização da Telemetria:** Métricas, *logs* estruturados em JSON e rastreamento distribuído (*Distributed Tracing*) operam em silos sem correlação entre o identificador da requisição (*Trace ID*) e os alertas operacionais [1, 4]. 

---
## 2. Decisão 

Decidimos padronizar a telemetria do *chassis* corporativo adotando o **OpenTelemetry (OTEL)** como padrão aberto de instrumentação de código, combinado com **Prometheus** para coleta de métricas e **Jaeger** para rastreamento distribuído [1]. 

### Diretrizes de Implementação: 
1. **Instrumentação Vendor-Agnostic:** Utilizar as pontes de telemetria do Spring Boot 3 (`micrometer-tracing-bridge-otel` e `opentelemetry-exporter-otlp`) para emitir dados via protocolo OTLP (*OpenTelemetry Protocol*), garantindo desacoplamento total de fornecedores de monitoramento [1].
2. **Correlação de Contexto em Logs:** Injetar automaticamente `trace_id` e `span_id` no MDC (*Mapped Diagnostic Context*) do Logback. Todo *log* emitido em formato JSON conterá essas chaves para correlação imediata entre eventos e rastros de execução.
3. **Métricas Padronizadas (RED / USE):** Exportar métricas operacionais via endpoint do Prometheus (`/actuator/prometheus`) organizadas sob os padrões **RED** (*Rate, Errors, Duration*) para requisições HTTP e **USE** (*Utilization, Saturation, Errors*) para recursos do sistema e da JVM [1, 4].
4. **Agente/Collector Desacoplado:** Enviar os spans via OTLP gRPC/HTTP para um **OpenTelemetry Collector** central, permitindo rotear a telemetria para múltiplos destinos (Prometheus, Jaeger, Grafana Tempo) sem modificar o código da aplicação [1]. 

---
## 3. Consequências e Trade-Offs 

### 🟢 Pontos Positivos (Ganhos): 
- **Operabilidade Aprimorada:** Diagnóstico rápido de falhas em produção, permitindo associar um erro de aplicação a um *Trace ID* específico em segundos [4, 6]. 
- **Fim do Vendor Lock-in:** Emissão nativa no padrão OTLP mantido pela *Cloud Native Computing Foundation (CNCF)*. A troca do backend de visualização exige apenas a reconfiguração do Collector [1]. 
- **Monitoramento de Latência p99:** Coleta precisa de distribuições e percentis de tempo de resposta, facilitando o controle de latências extremas sob aumento de carga [3]. 

### 🔴 Pontos Negativos / Desafios (Custos): 
- **Overhead de Processamento:** A geração e exportação de traces consome recursos de CPU/RAM e banda de rede (mitigado pela aplicação de amostragem probabilística em produção). 
- **Complexidade do Ambiente Local:** Requer manter o ecossistema OpenTelemetry Collector + Prometheus + Jaeger rodando no `docker-compose.yml` durante o desenvolvimento local. 

---
## 4. Alternativas Consideradas e Descartadas 
- **SDKs Proprietários de APM (Datadog / New Relic Agent):** Descartados para evitar acoplamento direto do código-fonte a um fornecedor pago [1]. 
- **Spring Cloud Sleuth (Legado):** Descartado por ter sido substituído pelo Micrometer Tracing no Spring Boot 3. 
- **Parsing Manual de Logs via Regex:** Descartado por ser ineficiente, frágil contra mudanças de formato de código e incapaz de fornecer contexto encadeado de rastreamento distribuído [4]. 

---
## 5. Referências e Padrões 
- Martin Kleppmann — *Designing Data-Intensive Applications* (Capítulo 1: Reliability, Maintainability &amp; Operability) [2, 4, 5] 
- Plano de Estudos Staff Software Engineer — *Fase 2: Observabilidade Avançada (OpenTelemetry, RED/USE, Tracing)* [1] 
- CNCF OpenTelemetry Specification &amp; Protocol (OTLP)