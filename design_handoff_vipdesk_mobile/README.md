# Handoff: VipDesk Mobile (app de atendimento omnichannel · Helpdesk · CRM)

## Overview
Design do aplicativo mobile do VipDesk — versão mobile (390×844) da plataforma web de atendimento omnichannel, Helpdesk, CRM, agendamentos e campanhas. 42 telas/quadros cobrindo shell, autenticação, módulos principais, telas públicas, biblioteca de componentes, estados de erro e o fluxo ponta a ponta. Tema claro, cor primária white-label (padrão VipDesk roxo `#7e3e97`), menu inferior fixo com 5 itens.

## About the Design Files
Os arquivos neste pacote são **referências de design criadas em HTML** — protótipos que mostram aparência e comportamento pretendidos, **não código de produção para copiar**. A tarefa é **recriar estas telas no ambiente do app** (React Native / Expo, Flutter, SwiftUI + Kotlin, ou o framework que o projeto já usa) seguindo seus padrões e bibliotecas. Se ainda não existe codebase, escolha o framework mais adequado (recomendação: React Native + Expo, TypeScript, navegação por tabs + stack) e implemente lá.

Abra `VipDesk Mobile v2.dc.html` no navegador para ver todas as telas em um canvas (pan/zoom). Cada tela tem um rótulo numerado ("04 – Login – Mobile"). Os post-its amarelos são anotações de produto/UX e não fazem parte da UI.

## Fidelity
**Média-alta fidelidade.** Cores, tipografia, espaçamentos, raios, ícones e textos são finais e devem ser seguidos. Os dados exibidos (nomes, números, mensagens) são exemplos — substitua por dados reais da API. Onde o HTML usa "placeholders" (QR code em padrão listrado, "logo do cliente"), use os assets reais.

## Princípios mobile (aplicar em todas as telas)
- Alvo de toque mínimo **44×44px**; texto de corpo mínimo **14px**; rótulos do menu **10px/500**.
- Padrões de conversão web → mobile: **DataTable → cards**, **modal → bottom sheet**, **dropdown → sheet de seleção**, **hover → long-press / swipe**.
- **Navegação empilhada** (stack) dentro de cada aba; o menu inferior permanece visível em listas e hubs e desaparece no chat/detalhe de ticket (tela cheia com composer).
- Cor primária vem de um **token único `--primary`** (white-label): botões, links, ícones ativos, badges "Novo", superfícies claras (`primary-surface`) derivam dele. Logo do cliente é um slot; VipDesk aparece como "Powered by VipDesk" nas telas públicas.
- Telas públicas (36–39) **não** têm menu inferior nem exigem login.

## Screens / Views

### Shell e navegação
**01 – App shell.** Status bar 44px branca. Header branco 46px, borda inferior `#e5e7eb`: logo 30×30 (raio 8, fundo primary, letra branca 13/700), título 16/600, chip de status "Disponível" (fundo `#ecfdf5`, texto `#047857`, 11/600, ponto verde 7px), ícone busca 22px `#4b5563`, sino 22px com badge vermelho (`#ef4444`, 9/700, raio 8). Menu inferior branco, borda superior `#e5e7eb`, 5 itens flex:1 (ícone 24px + rótulo 10/500; ativo `#7e3e97`, inativo `#6b7280`): Conversas · Tickets · CRM · Agenda · Mais. Conversas carrega 3 badges (vermelho `#ef4444` = empresa sem resposta, verde `#22c55e` = minhas com novas, laranja `#f59e0b` = menções "@1"). Home indicator 134×5 `#111827`.
**02 – Gaveta "Mais".** Bottom sheet sobre backdrop `rgba(17,24,39,.45)`; sheet branca raio 20 20 0 0, handle 40×4 `#d1d5db`. Cabeçalho do usuário (avatar 40 iniciais em `primary-surface`, nome 600, empresa + link "trocar empresa", chip XP `#fef3c7`/`#92400e`). Grade 4 colunas de atalhos (ícone 22px em quadrado 48×48 raio 12 `primary-surface`, rótulo 11px `#374151`): Dashboard, Campanhas, DeskFlow, Relatórios, Confirmações, Cobranças, Msgs. Classif., Base Legal (badge ADD-ON `#3b82f6` 8/700), Pesquisas, Chat interno, Reuniões, Arena; em cinza `#f3f4f6`: Configurações, Administração, Observab., Plano. Lista inferior (linhas 44px): Entrar em pausa (motivo ›), Post-its, Perfil/MFA/treinamentos, Ajuda e tour + versão.
**03 – Sitemap.** Diagrama de referência da hierarquia de navegação (não é tela).

### Conta e segurança (§6.1)
**04 – Login.** Fundo branco. Logo VipDesk 64px de altura centralizada (`logo-vipdesk.png`); chip cinza `#f3f4f6` raio 20 com mini-logo do cliente 24×24 e "Loja Vida Saudável · logo do cliente" (slot white-label). Subtítulo 13px `#6b7280` centralizado. Formulário padding 24: título "Entrar" 20/600; campos E-mail e Senha (label 12/500 `#374151`; input 44px, borda `#d1d5db`, raio 8, padding 0 12, 14px; ícone olho 18px à direita); linha "Manter conectado" (checkbox 16px preenchido primary) + link "Esqueci a senha" (primary, 500); botão Entrar 46px primary branco 15/600 raio 8; divisor "ou"; botão SSO 46px borda `#d1d5db` com ícone chave "Entrar com SSO da empresa"; rodapé "Ainda não tem conta? Teste grátis por 14 dias" e "Termos · Privacidade" 11px `#9ca3af`.
**05 – Escolha de empresa.** Header branco com logo 28px + "VipDesk" 16/600, borda inferior. Título 20/600 "Em qual empresa você quer entrar?", subtítulo 13 cinza. Cards 64px mín., raio 10: selecionado borda primary + fundo `primary-surface`; padrão borda `#e5e7eb`; bloqueado opacity .6, sub-texto vermelho "Fatura vencida · acesso bloqueado", ícone cadeado. Avatar 40 raio 8 com inicial. Botão "Usar outro e-mail" 44px secundário no rodapé.
**06 – MFA.** Mesmo header. Ícone escudo em quadrado 52 raio 14 `primary-surface`. Título 20/600, texto 13 cinza. 6 inputs 56px flex:1, 22/600 centralizado; ativo borda 2px primary. Checkbox "Confiar neste dispositivo por 30 dias". Botão Verificar 46px. Link "Usar código de recuperação". Alerta inferior `#fef3c7`/borda `#fcd34d`/texto `#78350f` 12px com ícone aviso ("Sua senha expira em 3 dias…").
**07 – Cadastro self-service (1/3).** Stepper 3 passos (círculo 24px: ativo primary preenchido, inativo borda `#d1d5db`; conectores 2px `#e5e7eb`). Campos: Nome da empresa, CNPJ (máscara `00.000.000/0000-00`), Segmento (select → sheet), Tamanho da equipe (chips), Subdomínio (sufixo `.vipdesk.app`). Botão Continuar 46px fixo no rodapé.

### Dashboard (§6.2) — **08**
Header padrão. Conteúdo scroll: seletor de período (chips), cards de KPI em grade 2 colunas (raio 10, borda `#e5e7eb`, valor 22/600, delta com seta verde/vermelha, meta em cinza): SLA hoje, NPS, Horas faturáveis, Tickets abertos; lista "Minha fila" e gráfico simplificado de barras (`primary` / `primary-surface`). Cards com título 12/600 cinza uppercase.

### Conversas (§6.3) — **09–12**
**09 – Lista.** Barra de filtros chips scroll horizontal (ativo fundo primary texto branco; inativo branco borda `#d1d5db`): Minhas · 3, Fila · 7, Todas, Departamento. Itens 72px: avatar 40 (iniciais em `primary-surface`) com ícone do canal sobreposto 16px (WhatsApp verde `#25d366`, Instagram `#e1306c`, e-mail cinza), nome 14/600, prévia 12 `#6b7280` uma linha (ellipsis), hora 11px, badge não lidas vermelho, etiqueta de SLA quando estourado (`#fee2e2`/`#b91c1c`). Swipe direita = assumir, esquerda = arquivar. FAB 44px primary "+" (nova conversa) acima do menu.
**10 – Chat (tela cheia).** Header 56px: voltar, avatar, nome + "WhatsApp · online", ícones telefone/vídeo/mais. Área de mensagens fundo `#f5f6f8`: bolhas raio 12 (canto oposto 2px) — cliente branco borda `#e5e7eb`; agente `#ede4f2` borda `#e0d0e8`; comentário interno `#fef3c7` borda `#fcd34d` com prefixo "Comentário interno ·". Hora 10px cinza, ✓✓ lidas. Composer: alternador segmentado Cliente/Interno (interno = `#f59e0b`, muda cor do composer para `#fffbeb`), ícone "+" 26px (sheet de anexos), textarea 44px raio 22, botão enviar 40px primary; dica 10px "segurar p/ áudio · / msgs rápidas · @ menciona". Tocar no nome abre o **painel do contato** (bottom sheet ~80% da altura, abas Detalhes / Ticket / Histórico / Timeline com indicador 2px primary).
**11 – Sheet de ações.** Bottom sheet: Transferir, Criar ticket, Classificar, Agendar retorno, Encerrar (destrutivo `#b91c1c`); linhas 52px com ícone 22px.
**12 – Estado vazio + janela flutuante.** Vazio: ícone 40px primary, título 15/600, texto 13 cinza, CTA. Janela flutuante (PiP) 120×160 no canto para o webphone/reunião em andamento.

### Helpdesk (§6.4) — **13–15**
**13 – Hub.** Cards de acesso (Meus tickets, Fila do depto, SLA em risco, Criar ticket) + contadores.
**14 – Lista.** Barra de filtros chips scroll horizontal (Meus abertos, Status, Depto, Prioridade); cards de ticket (raio 10): linha `#1102 · há 20 min` + badge de status; título 14/600; rodapé com responsável e SLA (ícone timer; vermelho se estourado). Long-press ativa **seleção em massa**: barra superior primary com contador e ações (Atribuir, Status, Fechar). FAB "+".
**15 – Detalhe.** Header compacto com número e status; título 16/600; abas Atividades / Propriedades / Horas (indicador primary). Atividades = timeline vertical (linha 2px `#e5e7eb`, pontos 10px coloridos por tipo, cartões de comentário). Propriedades = lista chave/valor com linhas 44px que abrem sheet de edição. Horas = worklog com botão iniciar/pausar. Composer fixo igual ao chat (público/interno). Botão "Resolver" verde `#22c55e` no header.
Badges de status: Em andamento `#dbeafe`/`#1d4ed8`; Pendente `#fef3c7`/`#92400e`; Resolvido `#dcfce7`/`#166534`; SLA estourado `#fee2e2`/`#b91c1c`; Novo `primary-surface`/primary; Arquivado `#f3f4f6`/`#4b5563` (todos 10/600, raio 10, padding 2–3×8).

### CRM "Vendas" (§6.5) — **16–19**
**16 – Hub.** KPIs do mês (meta, atingido, % no ritmo, barra de progresso primary), atalhos Kanban / Negócios / Contatos / Por vendedor.
**17 – Kanban.** Scroll horizontal com colunas 290px (cabeçalho "Etapa · n" + soma R$; cards raio 10 com título, valor 14/600, empresa, avatar do dono, dias na etapa). Arrastar card entre colunas (long-press). Chips de filtro no topo (Vendedor, Período, Origem, Produto).
**18 – Negócio.** Cabeçalho com valor e etapa (stepper horizontal das etapas do funil); seções: Contato, Produtos, Atividades (timeline), Anexos; barra fixa inferior "Avançar etapa" primary + "Perder" ghost vermelho.
**19 – Visão por vendedor.** Lista de vendedores com meta/realizado (barra), ranking, filtros.

### Contato / Cliente 360 (§6.6) — **20–21**
**20 – Contato 360.** Cabeçalho avatar 56, nome 18/600, empresa, chips de tags; linha de ações redondas 44px (WhatsApp, ligar, e-mail, agendar); cards: Dados, Conversas recentes, Tickets, Negócios, Agendamentos, NPS (nota + histórico), Consentimentos LGPD.
**21 – Lista de contatos.** Busca 42px, filtros chips, lista alfabética com avatar 40 e canal principal; FAB "+"; ação "Importar contatos" no menu ⋯.

### Agendamentos (§6.7) — **22–24**
**22 – Calendário.** Alternador Dia / Semana / Mês (segmentado); faixa de dias com bolinhas de ocupação; lista de horários do dia (cards com hora 14/600, cliente, serviço, status confirmado/pendente). FAB "+".
**23 – Página pública de agendamento.** Sem menu; barra de URL com cadeado; logo do cliente; escolher serviço → profissional → data (grade de dias) → horário (chips 44px) → dados do cliente → confirmar. "Powered by VipDesk".
**24 – Confirmações / Cobranças.** Lista de agendamentos aguardando confirmação (botões Confirmar verde / Reagendar / Cancelar) e cobranças vinculadas (Pix/cartão, status).

### Campanhas (§6.8) — **25–27**
**25 – Lista.** Cards: nome, canal, status (Agendada / Enviando com barra de progresso / Concluída), métricas enviados / entregues / lidos / respostas. Toggle ativo/inativo (40×22, verde `#22c55e` ligado / `#d1d5db` desligado).
**26 – Wizard passo 2 (mensagem).** Stepper 4 passos (Público · Mensagem · Agendamento · Revisão). Select "Template aprovado (Meta)"; corpo com variáveis `{{1}}`, `{{2}}`, `{{3}}` mapeadas a campos do contato (nome, pedido, prazo); **prévia estilo WhatsApp** (fundo `#efeae6`, bolha branca, hora). Rodapé Voltar / Continuar.
**27 – Público e agendamento.** Passo 1: segmentos (chips), filtros, contador "1.240 contatos"; aviso de opt-out. Passo 3: data/hora, janela de envio, limite por hora.

### DeskFlow, Relatórios, Classificação (§6.9–6.10) — **28–30**
**28 – DeskFlow (automação).** Lista de fluxos (nome, gatilho, status toggle, execuções 24h) e visualização **somente leitura** do diagrama (nós 160px raio 8, nó ativo borda 2px primary, conectores `#9ca3af`) — edição só no desktop (post-it).
**29 – Relatórios.** Abas scroll (Dashboard, Service desk, Análise…), cards de KPI e gráficos simplificados; exportar (sheet: PDF / CSV / enviar por e-mail).
**30 – Mensagens classificadas / Pesquisas.** Lista com etiqueta de classificação (chips coloridos) e resultados de pesquisas NPS/CSAT (nota média, distribuição promotor/neutro/detrator).

### Configurações e Administração (§6.11–6.12) — **31–35**
**31 – Hub de configurações.** Lista agrupada (linhas 52px, ícone 22 + caret): Canais, Usuários e permissões, Departamentos, Horários, Mensagens rápidas, Tags, Integrações, Faturamento/Plano.
**32 – Canais.** Cards por canal (ícone 26px da marca, nome/número, provedor, badge Conectado/Renovar/Desconectado); fluxo **QR code do WhatsApp** (card com QR 200px, contador de expiração, "Gerar novo").
**33 – Usuários e permissões.** Lista de usuários (avatar, nome, perfil, status); detalhe com Perfil (role → sheet), Departamentos (multi-chip), matriz de permissões (linhas com toggle), MFA obrigatório.
**34 – Administração (multiempresa).** Lista de empresas, plano, uso (licenças, canais, armazenamento com barras), fatura, ações.
**35 – Observabilidade.** Cards de saúde por serviço/parceiro (ponto verde/amarelo/vermelho), latência, fila de mensagens, últimos incidentes; link para status oficial do parceiro.

### Telas públicas (§6.13) — **36–39** (sem login, sem menu, subdomínio do cliente, rodapé "Powered by VipDesk")
**36 – Portal do cliente.** Barra de URL cinza com cadeado 11px; header com logo 34 do cliente, "Central de suporte", saudação; busca 42px; chips de assuntos; "Meus tickets" + botão "Abrir ticket" 34px primary; cards de ticket com última atualização e ações Responder/Anexar; "Artigos úteis" (lista com ícone documento).
**37 – Canal confidencial.** Aviso verde (`#ecfdf5`/`#a7f3d0`/`#065f46`, ícone escudo) "Seu relato é anônimo"; chips de tipo (Assédio, Fraude, Discriminação, Segurança, Outro); textarea 120px; campo Envolvidos; anexos (borda tracejada `#c4b5fd`, texto primary "metadados removidos"); checkbox contato seguro por protocolo; botão 46px "Enviar relato anônimo"; link "Acompanhar relato".
**38 – Pesquisa NPS.** Logo 52 centralizada; contexto do ticket 12px; pergunta 18/600; **escala 0–10** em grade 11 colunas, botões 44px raio 8 (selecionado fundo primary, texto branco; padrão branco borda `#d1d5db`); legendas "Nada provável / Muito provável"; após seleção aparece pergunta de follow-up que depende da faixa (0–6 "O que podemos melhorar?", 7–8 "O que faltou para uma nota 10?", 9–10 "O que mais gostou no atendimento?"), textarea e chips de motivo; botão "Enviar avaliação" desabilitado (`#e5e7eb`/`#9ca3af`) até haver nota. Uma resposta por ticket.
**39 – Bio link · contrato · pagamento.** Avatar 64 redondo, nome 16/600, descrição/horário; botões 46px brancos com ícone (WhatsApp, Agendar consulta, Central de suporte); seção "Pendências para você" (identificadas por token no link): card Contrato (badge Pendente, área de assinatura tracejada "Assinar com o dedo", botão "Ler e assinar" 40px primary); card Cobrança (valor 13/600, vencimento, badge Pix, QR 72px, botões "Copiar código Pix" e "Cartão / boleto" 36px secundários).

### Referência — **40–42**
**40 – Biblioteca de componentes.** Painel com tokens de cor, tipografia, botões (primário, secundário, ghost, destrutivo, desabilitado, FAB), campos (padrão, foco borda 2px primary, erro borda `#ef4444` + mensagem 11px), chips, segmentado, toggle, badges, chips de presença (Disponível/Em pausa/Offline), cards de lista, bottom sheet, toast (fundo `#111827`, texto branco, ação em `#a76bc0`), estado vazio.
**41a – 403.** Círculo 88px `#fee2e2` com cadeado `#b91c1c`; título 18/600 "Você não tem acesso a esta área"; texto explicando perfil e permissão; chip técnico "erro 403 · perm: reports.view"; botões "Solicitar acesso" (primary) e "Voltar ao Dashboard" (ghost).
**41b – Add-on não contratado.** Conteúdo ao fundo com `blur(3px)` opacity .55; bottom sheet de venda: ícone em quadrado 64 `primary-surface`, título, descrição, 3 benefícios com check verde, preço "R$ 149/mês", botão "Solicitar ativação"; nota "Somente administradores contratam".
**41c – Parceiro indisponível / offline.** Barra persistente escura `#111827` no topo ("Sem conexão · mostrando dados de 09:38 · Tentar"); card do canal com borda `#fecaca`, badge Desconectado, caixa de erro `#fef2f2`/`#991b1b` (erro 502, fila de retry), botões Reconectar / Status Meta; bloco offline com "2 mensagens aguardando envio".
**42 – Fluxo ponta a ponta.** Diagrama: mensagem chega (09) → atendente responde (10) → converte em ticket (11) → tratamento (14–15) → resolvido e comunicado (15) → NPS enviada (38) → nota volta ao Dashboard e ao 360 (08 · 20). DeskFlow pode automatizar passos 2 e 6.

## Interactions & Behavior
- Menu inferior: troca de aba mantém a pilha de cada aba; "Mais" abre a gaveta (sheet) em vez de navegar.
- Listas: pull-to-refresh; swipe em conversas (assumir / arquivar); long-press em tickets ativa seleção em massa.
- Chat: alternador Cliente/Interno muda cor do composer; Enter/botão envia; "/" abre mensagens rápidas; "@" menciona (só interno); segurar mic grava áudio; tocar no contato abre painel (sheet com abas).
- Bottom sheets: handle, arrastar para fechar, backdrop 45% escuro; substituem todos os modais/dropdowns.
- Transições: push horizontal 250ms ease-out para stack; sheet sobe 300ms cubic-bezier(.2,.8,.2,1); toasts 3s com ação Desfazer.
- Estados: skeleton em listas, vazio com ícone + CTA, erro inline abaixo do campo (vermelho 11px), 403, add-on, offline (barra persistente + fila de envio com retry exponencial).
- Presença: chip do header alterna Disponível / Em pausa (motivo) / Offline.
- Notificações push: novas mensagens, menções, SLA em risco, ticket atribuído.

## State Management
- Sessão: token, empresa ativa (multiempresa), perfil/permissões, MFA confiado, tema/cor white-label e logo.
- Conversas: filtro ativo, lista (paginada, tempo real via WebSocket), conversa aberta, modo do composer (cliente/interno), rascunho por conversa, fila offline de envios.
- Tickets: filtros, seleção em massa, ticket aberto (aba ativa), worklog em andamento.
- CRM: coluna/etapas do Kanban, negócio aberto.
- Agenda: modo Dia/Semana/Mês, data selecionada.
- Campanhas: rascunho do wizard (4 passos), variáveis mapeadas.
- NPS público: nota selecionada (null | 0–10) → habilita botão e define pergunta de follow-up.
- Conectividade: online/offline, saúde dos canais.

## Design Tokens
Cores: `--primary #7e3e97` (white-label), `--primary-dark #5b2a70` (pressed), `--primary-light #a76bc0`, `--primary-surface #f3ecf7`; `--success #22c55e`, `--danger #ef4444`, `--warning #f59e0b`, `--info #3b82f6`. Cinzas: texto `#111827`, secundário `#374151`, terciário `#4b5563`, muted `#6b7280`, placeholder `#9ca3af`, borda `#d1d5db`, divisor `#e5e7eb`, superfície `#f3f4f6`, fundo de tela `#f5f6f8`, branco `#fff`. Tints: azul `#dbeafe/#1d4ed8`, amarelo `#fef3c7/#92400e/#78350f`, verde `#dcfce7/#166534`, `#ecfdf5/#047857`, vermelho `#fee2e2/#b91c1c`, `#fef2f2/#991b1b`. Marcas: WhatsApp `#25d366`, Instagram `#e1306c`.
Tipografia: Inter (fallback system-ui). Título de tela 20/600; título de seção 16–18/600; corpo 14/400; secundário 13; meta 12; badge 10–11/600; rótulo do menu 10/500; KPI 22/600.
Espaçamento: 4 · 6 · 8 · 10 · 12 · 16 · 20 · 24 (padding de tela 16–24).
Raios: inputs/botões 8; cards 10; quadrados de ícone 12–14; sheets 20 (topo); chips/badges pill (10–16); FAB 12–14.
Sombras: frame `0 12px 32px rgba(0,0,0,.14)`; sheet `0 -8px 30px rgba(0,0,0,.15)`; toast/segmentado `0 1px 2px rgba(0,0,0,.1)`.
Alturas: status bar 44; header 46–56; input 44; botão primário 46; linha de lista 44–72; menu inferior ~56 + home indicator 28.

## Assets
- `logo-vipdesk.png` — logo oficial VipDesk (usada nas telas 04–06). Logo do cliente = slot white-label (upload em Configurações).
- Ícones: **Phosphor Icons** (regular; fill para estados ativos) — https://phosphoricons.com. Nomes usados no HTML como classes `ph ph-<nome>` (ex.: `chats-circle`, `ticket`, `kanban`, `calendar-blank`, `squares-four`, `whatsapp-logo`, `shield-check`).
- Fonte: Inter (Google Fonts).

## Files
- `VipDesk Mobile v2.dc.html` — todas as 42 telas em canvas (abrir no navegador; requer `support.js` na mesma pasta).
- `support.js` — runtime do arquivo de design.
- `logo-vipdesk.png` — logo.
- `PROMPT.md` — prompt pronto para colar em uma IA de geração de código junto com este pacote.
