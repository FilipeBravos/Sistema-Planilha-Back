# Sistema Planilha - Back

API (Java 21 + Spring Boot 3 + PostgreSQL) do controle financeiro diário de motorista de aplicativo (Vagner e Filipe).

## Executar
```bash
APP_USUARIOS_INICIAIS="filipe:minha-senha-1,vagner:minha-senha-2" mvn spring-boot:run     # http://localhost:8080
mvn test
```
Sem `APP_USUARIOS_INICIAIS` ninguém consegue entrar (veja Login abaixo). Para colocar o sistema online, veja **[DEPLOY.md](DEPLOY.md)**.
Banco PostgreSQL. Suba um local com `docker compose up -d` (usa `docker-compose.yml`) ou aponte para o seu
servidor com as variáveis `DB_URL`, `DB_USER` e `DB_PASSWORD` (padrão: `jdbc:postgresql://localhost:5432/planilha`, usuário/senha `planilha`).
As tabelas são criadas automaticamente. Os testes usam H2 em memória e não precisam do PostgreSQL.
CORS liberado para `http://localhost:4200` (variável `CORS_ORIGINS`).

## Endpoints
| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/registros?inicio=&fim=` | Linhas da planilha (datas ISO, opcionais) |
| POST | `/api/registros` | Cria linha |
| PUT | `/api/registros/{id}` | Atualiza linha |
| DELETE | `/api/registros/{id}` | Remove linha |
| GET | `/api/resumo?inicio=&fim=` | Totais de horas e faturamento |
| GET | `/api/despesas?categoria=&inicio=&fim=` | Vencimentos de despesas no período (filtros opcionais) |
| GET | `/api/despesas/resumo?inicio=&fim=` | Total a vencer no período e total por categoria |
| POST / PUT | `/api/despesas`, `/api/despesas/{id}` | Cria / atualiza despesa |
| DELETE | `/api/despesas/{id}` | Remove despesa |
| GET | `/api/emprestimos?credor=` | Empréstimos (filtro opcional por credor) |
| GET | `/api/emprestimos/resumo` | Total emprestado, pago e saldo, geral e por credor |
| POST / PUT | `/api/emprestimos`, `/api/emprestimos/{id}` | Cria / atualiza empréstimo |
| DELETE | `/api/emprestimos/{id}` | Remove empréstimo |
| GET | `/api/relatorios/dashboard?inicio=&fim=` | Relatório consolidado por mês (receita, despesas, empréstimos) |

Corpo do POST/PUT: `data, horaInicialVagner, horaFinalVagner, horaInicialFilipe, horaFinalFilipe, kmInicial, kmFinal, cargaPostoVagner, cargaPostoFilipe, valorVagner, valorFilipe`. O horário de cada pessoa é opcional, mas inicial e final devem vir juntos (ou ambos em branco).

## Cálculos (feitos no backend)
- Total de horas de cada pessoa = hora final − hora inicial (turno que vira a meia-noite é tratado); total do dia = Vagner + Filipe
- Total de Km = Km final − Km inicial
- Líquido Vagner = Valor Vagner − Carga Posto do Vagner; Líquido Filipe = Valor Filipe − Carga Posto do Filipe (cada um informa a sua carga)
- Resumo: horas do Vagner, do Filipe e dos dois juntos (soma), faturamento bruto e líquido por pessoa e total.

## Despesas
Corpo do POST/PUT: `categoria, nome, data, valor, formaPagamento, parcelas`.
- `categoria`: `CARRO`, `ENERGIA_ELETRICA`, `MOTO`, `PLANO_DE_SAUDE`, `FARMACIA`, `LORD_E_AMORA`, `FILIPE`, `VAGNER`, `ROMILDA`, `INTERNET_E_TELEFONE`
- `formaPagamento`: `DINHEIRO`, `CARTAO`, `CHEQUE`, `BOLETO`
- `parcelas` (1 a 60) é obrigatório no `CARTAO` e ignorado nas outras formas. `valor` é o total; a resposta traz `valorParcela` (valor ÷ parcelas) para cartão.
- `nome` é texto livre (o front sugere os nomes de cada categoria).

### Vencimentos (parcelas por mês)
`GET /api/despesas` e `/api/despesas/resumo` trabalham com **vencimentos**, não com a data da compra:
- Fora do cartão há um vencimento só, na data da compra.
- No cartão há um vencimento por parcela: a 1ª na data da compra e as demais nos meses seguintes, no mesmo dia (ou no último dia do mês, se o dia não existir).
- O valor das parcelas é `valor ÷ parcelas` arredondado; a última parcela absorve a diferença, então a soma é sempre o valor total.
- Cada item traz `despesaId`, `dataCompra`, `valorTotal`, `parcelas`, `numeroParcela`, `vencimento` e `valor` (o que vence naquela data). Edição e exclusão (`PUT`/`DELETE /api/despesas/{id}`) usam o `despesaId`.

## Empréstimos
Corpo do POST/PUT: `credor, nomeTerceiro, data, valor, parcelas, valorPago`.
- `credor`: `ROMILDA`, `VERONICA`, `BANCO_DO_BRASIL`, `BANCO_ITAU` ou `TERCEIROS`. Em `TERCEIROS` o `nomeTerceiro` (quem emprestou) é obrigatório; nos demais é ignorado.
- `parcelas` (1 a 600): em quantas vezes o empréstimo foi dividido. `valorPago`: quanto já foi pago.
- A resposta traz `valorParcela` (valor ÷ parcelas), `saldo` (valor − pago, nunca negativo) e `quitado` (saldo zerado). O resumo soma os saldos de cada empréstimo, então pagar a mais em um não abate o saldo de outro.

## Relatórios
`GET /api/relatorios/dashboard?inicio=&fim=` consolida o intervalo em meses completos (padrão: últimos 12 meses; máximo 120). Resposta:
- `meses[]` e `totais`: `faturamentoBruto`, `cargaPosto`, `faturamentoLiquido` (bruto − carga), `despesas` (o que vence no mês, com parcelas do cartão), `parcelasEmprestimos` e `resultado` (líquido − despesas − parcelas).
- `despesasPorCategoria[]` e `maioresDespesas[]` (top 10 por nome dentro da categoria), do maior para o menor, com `percentual` do total de despesas.
- `emprestimos`: situação atual (total emprestado, pago e saldo devedor), independente do intervalo.
- **Parcelas de empréstimos são previstas**: como o sistema guarda só o total pago (sem datas de pagamento), cada empréstimo gera uma parcela por mês (`valor ÷ parcelas`), a 1ª um mês depois da data do empréstimo; a última absorve o arredondamento.

## Login e segurança
- Tudo em `/api` exige login, exceto `POST /api/auth/login` e `GET /api/saude` (usado pelo healthcheck).
- Sessão por cookie `HttpOnly` (`SameSite=Lax`; `Secure` quando `COOKIE_SEGURO=true`) e proteção CSRF (cookie `XSRF-TOKEN` + cabeçalho `X-XSRF-TOKEN`, que o Angular envia sozinho).
- `POST /api/auth/login` (`usuario`, `senha`), `GET /api/auth/eu`, `POST /api/auth/logout`, `POST /api/auth/senha` (`senhaAtual`, `novaSenha` com 8+ caracteres).
- Senhas guardadas com BCrypt. Depois de 5 erros seguidos do mesmo IP e usuário, o login é bloqueado por 15 minutos.
- Os usuários são criados na inicialização a partir de `APP_USUARIOS_INICIAIS` (`login:senha,login2:senha2`), só se ainda não existirem; cada pessoa troca a própria senha pela tela.

## Hospedagem
`Dockerfile` (backend), `docker-compose.prod.yml` (banco + backend + site com HTTPS), `.env.example` e `scripts/backup.sh`: passo a passo em [DEPLOY.md](DEPLOY.md).
