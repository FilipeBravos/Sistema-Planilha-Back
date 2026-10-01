#!/usr/bin/env bash
# Lido (source) por iniciar.sh e atualizar.sh: traduz o .env.local para o que o compose precisa.
ler_env() { grep -E "^$1=" .env.local | tail -1 | cut -d= -f2- | tr -d '[:space:]'; }

PORTA=$(ler_env PORTA); PORTA=${PORTA:-8080}
case "$(ler_env ACESSO_REDE | tr '[:upper:]' '[:lower:]')" in
  sim|s|true|yes|1) export ENDERECO_REDE=0.0.0.0; ACESSO_REDE_ATIVO=1 ;;
  *)                export ENDERECO_REDE=127.0.0.1; ACESSO_REDE_ATIVO=0 ;;
esac

# Mostra o endereço para abrir nos outros aparelhos (prefere os de redes domésticas: 192.168.x.x e 10.x.x.x).
mostrar_enderecos() {
  local ips
  ips=$( (hostname -I 2>/dev/null || ipconfig getifaddr en0 2>/dev/null || ipconfig getifaddr en1 2>/dev/null) \
         | tr -s ' ' '\n' | grep -E '^[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+$' || true)
  local casa
  casa=$(echo "$ips" | grep -E '^(192\.168\.|10\.)' || true)
  if [ -z "$casa" ]; then   # sem rede doméstica clássica: descarta os endereços internos do Docker (172.x)
    casa=$(echo "$ips" | grep -vE '^172\.(1[6-9]|2[0-9]|3[01])\.' || true)
  fi
  [ -n "$casa" ] && ips="$casa"
  echo "Acesso pela rede LIGADO. Nos outros aparelhos da mesma rede Wi-Fi, abra:"
  if [ -n "$ips" ]; then
    echo "$ips" | while read -r ip; do echo "    http://$ip:$PORTA"; done
  else
    echo "    http://IP-DESTE-COMPUTADOR:$PORTA   (não consegui descobrir o IP; veja nas configurações de rede)"
  fi
  echo "Se não abrir, libere a porta $PORTA no firewall deste computador (veja LOCAL.md)."
  echo "Atenção: qualquer aparelho da sua rede consegue abrir a tela de login (a conexão é HTTP, sem criptografia)."
}
