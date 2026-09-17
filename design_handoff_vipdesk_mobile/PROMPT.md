# Prompt para a IA (colar junto com o pacote)

Você vai implementar o aplicativo mobile **VipDesk** a partir do pacote de design anexado.

1. Leia `README.md` por completo — ele descreve as 42 telas, tokens, interações e estado. Abra `VipDesk Mobile v2.dc.html` para inspecionar visualmente cada tela (os post-its amarelos são anotações, não UI).
2. Os arquivos HTML são **referência de design**, não código para copiar. Recrie as telas em **[Kotlin]**, usando navegação por abas (Conversas, Tickets, CRM, Agenda, Mais) com stack por aba.
3. Comece pelo **design system**: crie os tokens (cores com `primary` white-label configurável, tipografia Inter, espaçamentos, raios) e os componentes da tela 40 (Button, Input, Chip, Segmented, Toggle, Badge, Card, ListRow, BottomSheet, Toast, EmptyState, StatusBar/Header, TabBar).
4. Implemente as telas nesta ordem: 01–02 shell → 04–07 auth → 09–12 Conversas → 13–15 Helpdesk → 16–19 CRM → 20–21 Contato → 22–24 Agenda → 25–27 Campanhas → 28–30 → 31–35 Config → 36–39 públicas (web/deep link) → 41 estados de erro.
5. Use dados mockados tipados (interfaces por entidade: Conversation, Message, Ticket, Deal, Contact, Appointment, Campaign, Channel, User) atrás de uma camada `api/` para trocar por REST/WebSocket depois.
6. Respeite: alvos ≥44px, corpo ≥14px, DataTable → cards, modal → bottom sheet, cor primária vinda de um único token.
7. Entregue por módulo, com screenshots ou preview de cada tela ao lado da referência, e liste qualquer desvio em relação ao design.
