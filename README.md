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

Corpo do POST/PUT: `data, horaInicialVagner, horaFinalVagner, horaInicialFilipe, horaFinalFilipe, kmInicial, kmFinal, cargaPosto, valorVagner, valorFilipe`. O horário de cada pessoa é opcional, mas inicial e final devem vir juntos (ou ambos em branco).

## Cálculos (feitos no backend)
- Total de horas de cada pessoa = hora final − hora inicial (turno que vira a meia-noite é tratado); total do dia = Vagner + Filipe
- Total de Km = Km final − Km inicial
- Líquido Vagner = Valor Vagner − Carga Posto; Líquido Filipe = Valor Filipe − Carga Posto
- Resumo: horas do Vagner, do Filipe e dos dois juntos (soma), faturamento bruto e líquido por pessoa e total.
