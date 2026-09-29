# Sistema Planilha - Back

API (Java 21 + Spring Boot 3) do controle financeiro diário de motorista de aplicativo (Vagner e Filipe).

## Executar
```bash
mvn spring-boot:run     # http://localhost:8080
mvn test
```
Banco H2 em arquivo (`./data`). CORS liberado para `http://localhost:4200` (`app.cors.allowed-origins`).

## Endpoints
| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/registros?inicio=&fim=` | Linhas da planilha (datas ISO, opcionais) |
| POST | `/api/registros` | Cria linha |
| PUT | `/api/registros/{id}` | Atualiza linha |
| DELETE | `/api/registros/{id}` | Remove linha |
| GET | `/api/resumo?inicio=&fim=` | Totais de horas e faturamento |

Corpo do POST/PUT: `data, horaInicial, horaFinal, kmInicial, kmFinal, cargaPosto, valorVagner, valorFilipe`.

## Cálculos (feitos no backend)
- Total de horas = hora final − hora inicial (turno que vira a meia-noite é tratado)
- Total de Km = Km final − Km inicial
- Líquido Vagner = Valor Vagner − Carga Posto; Líquido Filipe = Valor Filipe − Carga Posto
- Horas individuais: como a planilha tem uma única faixa de horário por dia, as horas do dia contam para quem tem valor > 0 naquele dia; o total conjunto conta cada dia uma vez.
