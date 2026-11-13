-- Zadrinth Client: sincronização entre jogadores (ícone no TAB)
-- Cole tudo isto no Supabase em: SQL Editor > New query > Run.
--
-- Guarda só: endereço do servidor, nome do jogador, versão do client e a hora
-- do último aviso. Ninguém lê ou escreve a tabela direto: os clients só usam
-- as duas funções abaixo. Registros com mais de 10 minutos são apagados.

create table if not exists public.zadrinth_presence (
  server         text        not null,
  name           text        not null,
  client_version text,
  last_seen      timestamptz not null default now(),
  primary key (server, name)
);

alter table public.zadrinth_presence enable row level security;
-- (sem policies de propósito: a tabela fica fechada para acesso direto)

create index if not exists zadrinth_presence_last_seen on public.zadrinth_presence (last_seen);

-- "Estou jogando neste servidor" (o client chama a cada 60 s)
create or replace function public.zadrinth_heartbeat(p_server text, p_name text, p_version text)
returns void
language sql
security definer
set search_path = public
as $$
  insert into zadrinth_presence (server, name, client_version, last_seen)
  values (lower(left(p_server, 255)), lower(left(p_name, 16)), left(p_version, 32), now())
  on conflict (server, name)
  do update set last_seen = now(), client_version = excluded.client_version;

  delete from zadrinth_presence where last_seen < now() - interval '10 minutes';
$$;

-- "Destes nomes, quem está usando o Zadrinth agora?" (máx. 200 nomes)
create or replace function public.zadrinth_online(p_server text, p_names text[])
returns jsonb
language sql
stable
security definer
set search_path = public
as $$
  select coalesce(jsonb_agg(p.name), '[]'::jsonb)
  from zadrinth_presence p
  where p.server = lower(p_server)
    and p.name = any (select lower(n) from unnest(p_names[1:200]) as n)
    and p.last_seen > now() - interval '3 minutes';
$$;

revoke all on function public.zadrinth_heartbeat(text, text, text) from public;
revoke all on function public.zadrinth_online(text, text[]) from public;
grant execute on function public.zadrinth_heartbeat(text, text, text) to anon;
grant execute on function public.zadrinth_online(text, text[]) to anon;
