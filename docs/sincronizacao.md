# Sincronização entre jogadores (ícone no TAB)

O ícone do Zadrinth Client aparece no TAB só para quem também usa o client.
Para isso os clients avisam uns aos outros por um projeto **Supabase** (grátis).

## Como funciona

```
 Seu celular (Zadrinth)                Supabase                 Celular do amigo (Zadrinth)
 ──────────────────────               ────────                  ───────────────────────────
 a cada 60 s:  "Joao está em    ──►   zadrinth_presence   ◄──   a cada 60 s: "Maria está em
 jogar.mush.com.br"                   (servidor, nome,          jogar.mush.com.br"
                                       última vez visto)
 a cada 30 s (10 s com o TAB
 aberto): "destes nomes do TAB,  ──►  zadrinth_online    ──►    resposta: ["maria"]
 quem usa o Zadrinth?"                                          → ícone ao lado de Maria
```

- Vale por servidor: só aparece o ícone de quem está no **mesmo endereço** que você.
- Quem fecha o jogo some da lista em até 3 minutos.
- O que vai para o Supabase: o endereço do servidor, o seu nome, os nomes da lista
  do TAB e a versão do client. Nada de senha, IP do jogador ou conversa.
- No menu do client (Right Shift) dá para desligar **Aparecer no TAB** (você para
  de avisar) e **Ícone no TAB** (você para de ver os ícones).

## Configurar (pelo celular)

1. Entre em **supabase.com**, crie uma conta e um projeto (plano grátis).
2. No projeto, abra **SQL Editor → New query**, cole todo o arquivo
   [`docs/supabase.sql`](supabase.sql) e toque em **Run**.
3. Toque em **Connect** no topo do projeto (ou abra **Project Settings → API Keys**) e copie:
   - **Project URL** (algo como `https://xxxx.supabase.co`)
   - a **Publishable key** (começa com `sb_publishable_`). Projetos antigos podem
     mostrar a chave **anon public** no lugar: as duas funcionam
4. No GitHub, abra o repositório → **Settings → Secrets and variables → Actions →
   New repository secret** e crie:
   - `SUPABASE_URL` = o Project URL
   - `SUPABASE_ANON_KEY` = a Publishable key (ou a anon public)
5. Publique uma nova release (ou rode o CI de novo). O mod passa a vir com a
   sincronização ligada, e no menu aparece **● Sincronização ligada**.

A Publishable key (ou a "anon public") pode ir dentro do app: ela só consegue chamar as duas
funções do `supabase.sql`, porque a tabela fica fechada para acesso direto.
