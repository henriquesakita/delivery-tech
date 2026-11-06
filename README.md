# Delivery Tech API

[![CI](https://github.com/henriquesakita/delivery-tech/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/henriquesakita/delivery-tech/actions/workflows/ci-cd.yml)
[![Java 21](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Licença MIT](https://img.shields.io/badge/licen%C3%A7a-MIT-3fb950)](LICENSE)

**API REST de delivery com autenticação JWT, Swagger, Prometheus e entrega em Docker.**

Marketplace de delivery com clientes, restaurantes, produtos, pedidos, pagamentos
e relatórios. Construída com **Java 21**, **Spring Boot 3**, **Spring Security + JWT**,
**JPA/Hibernate** e documentação automática com **OpenAPI/Swagger**.

O projeto vai além do CRUD: tem autenticação stateless, validação de entrada,
tratamento global de erros, testes, build em Docker multi-stage e pipeline de
CI/CD no GitHub Actions.

`Java 21` `Spring Boot` `Spring Security` `JWT` `JPA` `Docker` `Prometheus` `GitHub Actions`

## Contexto do projeto

Este repositório é a entrega prática do curso **Arquitetura de Sistemas Online** (120 horas), do [Programa Qualifica SP](https://fundacaofat.org.br/cursos-gratuitos-de-tecnologia), promovido pela Secretaria de Desenvolvimento Econômico do Estado de São Paulo e executado pela Fundação FAT. Concluído em setembro de 2025, o curso usa um projeto-base de delivery para percorrer modelagem de dados, APIs RESTful, segurança, controle de transações, documentação com Swagger, testes automatizados, CI/CD, escalabilidade e observabilidade.

## Stack

| Área | Tecnologia |
| --- | --- |
| Linguagem | Java 21 (records, pattern matching, virtual threads disponíveis) |
| Framework | Spring Boot 3.5.4 (Web, Data JPA, Validation, Security, Actuator) |
| Autenticação | JWT (`jjwt` 0.12) + BCrypt, sessões stateless |
| Banco | H2 em memória (desenvolvimento) / MySQL (configurável) |
| Documentação | springdoc-openapi + Swagger UI |
| Observabilidade | Actuator, Micrometer e endpoint Prometheus |
| Mapeamento | ModelMapper + DTOs de entrada e saída |
| Build | Maven Wrapper + Docker multi-stage (distroless) |
| CI/CD | GitHub Actions (testes, build e smoke test do container) |

## Arquitetura

```mermaid
flowchart LR
  A[Cliente HTTP<br/>Swagger / curl] --> B[Filtro JWT]
  B --> C[Controllers REST]
  C --> D[Services]
  D --> E[Repositories Spring Data JPA]
  E --> F[(H2 / MySQL)]
  C --> G[DTOs + Bean Validation]
  D --> H[ModelMapper]
  C --> I[GlobalExceptionHandler]
  B --> J[Actuator / Prometheus]
```

O fluxo de uma requisição autenticada:

1. O cliente envia `Authorization: Bearer <token>`.
2. `JwtAuthenticationFilter` valida o token, carrega o usuário e popula o
   `SecurityContext`.
3. O controller recebe DTOs validados com Jakarta Bean Validation.
4. O service aplica as regras de negócio e o repository persiste via JPA.
5. Erros de negócio e validação são convertidos em respostas JSON padronizadas
   pelo `GlobalExceptionHandler`.

## Como rodar

### Pré-requisitos

- JDK 21
- Maven (ou use o wrapper incluído)

```bash
cd delivery-api
./mvnw spring-boot:run        # Linux/macOS
mvnw.cmd spring-boot:run      # Windows
```

A API sobe em `http://localhost:8080`. O `DataLoader` popula clientes,
restaurantes e produtos de exemplo na primeira execução.

| Recurso | URL |
| --- | --- |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/api-docs |
| Health | http://localhost:8080/health |
| Actuator | http://localhost:8080/actuator/health |
| Prometheus | http://localhost:8080/actuator/prometheus |
| H2 Console | http://localhost:8080/h2-console |

H2 Console: JDBC URL `jdbc:h2:mem:delivery`, usuário `sa`, senha vazia.

### Docker

```bash
cd delivery-api
docker compose up --build
```

A API fica em `http://localhost:8080` e o Adminer em `http://localhost:8083`.
A imagem final usa `gcr.io/distroless/java21-debian12:nonroot`, roda como usuário
sem privilégios e persiste o banco H2 em volume.

## Autenticação

O login usa o **e-mail** cadastrado como `username`.

```bash
# 1. Cadastrar
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"nome":"Ana Souza","email":"ana@email.com","senha":"123456","role":"CLIENTE"}'

# 2. Entrar e obter o token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"ana@email.com","password":"123456"}'
```

```json
{ "token": "eyJhbGciOiJIUzI1NiJ9..." }
```

```bash
# 3. Usar o token
curl http://localhost:8080/api/clientes \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

Rotas públicas: `/api/auth/**`, `/health`, `/info`, `/swagger-ui/**`,
`/api-docs/**` e `/actuator/**`. As demais exigem token válido.

## Endpoints principais

### Autenticação

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/api/auth/register` | Cadastra um usuário |
| `POST` | `/api/auth/login` | Autentica e devolve o JWT |

### Clientes

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/api/clientes` | Cria um cliente |
| `GET` | `/api/clientes` | Lista clientes |
| `GET` | `/api/clientes/{id}` | Busca por ID |
| `GET` | `/api/clientes/email/{email}` | Busca por e-mail |
| `GET` | `/api/clientes/buscar?nome=` | Busca por nome |
| `PUT` | `/api/clientes/{id}` | Atualiza |
| `PATCH` | `/api/clientes/{id}/status` | Ativa/inativa |
| `DELETE` | `/api/clientes/{id}` | Remove |

### Restaurantes

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/api/restaurantes` | Cria um restaurante |
| `GET` | `/api/restaurantes` | Lista restaurantes |
| `GET` | `/api/restaurantes/disponiveis` | Lista os ativos |
| `GET` | `/api/restaurantes/categoria/{categoria}` | Filtra por categoria |
| `GET` | `/api/restaurantes/proximos/{cep}` | Busca por proximidade |
| `GET` | `/api/restaurantes/{id}/produtos` | Cardápio do restaurante |
| `PATCH` | `/api/restaurantes/{id}/status` | Ativa/inativa |
| `POST` | `/api/restaurantes/{id}/taxa-entrega` | Define a taxa de entrega |

### Produtos

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/api/produtos` | Cria um produto |
| `GET` | `/api/produtos` | Lista produtos |
| `GET` | `/api/produtos/restaurante/{id}` | Produtos de um restaurante |
| `GET` | `/api/produtos/categoria/{categoria}` | Filtra por categoria |
| `GET` | `/api/produtos/buscar?nome=` | Busca por nome |
| `PATCH` | `/api/produtos/{id}/disponibilidade` | Liga/desliga disponibilidade |
| `DELETE` | `/api/produtos/{id}/inativar` | Inativa sem apagar |

### Pedidos

| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/api/pedidos` | Cria um pedido com itens |
| `GET` | `/api/pedidos` | Lista pedidos |
| `GET` | `/api/pedidos/{id}` | Detalha um pedido |
| `GET` | `/api/pedidos/cliente/{clienteId}` | Pedidos de um cliente |
| `GET` | `/api/pedidos/restaurante/{restauranteId}` | Pedidos de um restaurante |
| `POST` | `/api/pedidos/{id}/itens` | Adiciona item |
| `PUT` | `/api/pedidos/{id}/confirmar` | Confirma o pedido |
| `PATCH` | `/api/pedidos/{id}/status` | Atualiza o status |
| `DELETE` | `/api/pedidos/{id}/cancelar` | Cancela |
| `POST` | `/api/pedidos/calcular` | Calcula o total |

Status disponíveis: `CRIADO`, `PENDENTE`, `CONFIRMADO`, `PREPARANDO`,
`SAIU_PARA_ENTREGA`, `ENTREGUE` e `CANCELADO`.

### Relatórios

| Método | Rota | Descrição |
| --- | --- | --- |
| `GET` | `/api/relatorios/vendas-por-restaurante` | Vendas por restaurante |
| `GET` | `/api/relatorios/produtos-mais-vendidos` | Ranking de produtos |
| `GET` | `/api/relatorios/clientes-ativos` | Clientes ativos |
| `GET` | `/api/relatorios/pedidos-por-periodo` | Pedidos por período |
| `GET` | `/api/relatorios/resumo-vendas` | Resumo geral |

## Decisões técnicas

- **API stateless com JWT.** Nenhuma sessão no servidor; o filtro injeta o
  usuário autenticado a cada requisição.
- **DTOs separados do modelo.** As entidades JPA não vazam para a API;
  `ModelMapper` converte e o contrato fica estável.
- **Validação declarativa.** `@NotBlank`, `@Email` e `@NotNull` barram entradas
  inválidas antes da regra de negócio.
- **Erros padronizados.** `GlobalExceptionHandler` traduz exceções de negócio,
  conflito e entidade inexistente em respostas consistentes.
- **Observabilidade desde o início.** Health checks customizados, Actuator e
  métricas Prometheus para monitorar a aplicação.
- **Docker enxuto e seguro.** Build multi-stage separa Maven do runtime
  distroless, sem shell e sem usuário root.
- **Pipeline confiável.** O CI roda os testes, empacota o JAR, sobe o container
  e só passa quando `/actuator/health` responde.

## Testes

```bash
cd delivery-api
./mvnw test
```

O relatório de cobertura do JaCoCo é gerado em
`delivery-api/target/site/jacoco/index.html`.

## Estrutura

```
delivery-api/
  src/main/java/com/deliverytech/delivery_api/
    config/       segurança, OpenAPI, ModelMapper e carga inicial
    controller/   endpoints REST
    dto/          contratos de entrada e saída
    exception/    exceções e tratamento global
    health/       health checks customizados
    model/        entidades JPA e enums
    repository/   interfaces Spring Data
    security/     JWT e filtro de autenticação
    service/      regras de negócio
  src/test/       testes automatizados
  Dockerfile      build multi-stage com runtime distroless
  docker-compose.yml
.github/workflows/ci-cd.yml
```

## Autor

**Henriques Sakita** — [github.com/henriquesakita](https://github.com/henriquesakita)

Licenciado sob a [MIT](LICENSE).
