package br.com.vipdesk.mobile.data.demo

/**
 * Dados de demonstração do design (canvas "VIPDesk App") para as telas que
 * ainda não têm endpoint no backend: CRM, Quadros (kanban), Relatórios,
 * Busca global e equipe online. Quando a API existir, substituir por
 * repositórios reais.
 */

data class DemoContact(
    val id: Int,
    val name: String,
    val company: String,
    val lead: String,          // "Cliente" | "Lead qualificado" | "Lead novo"
    val phone: String,
    val email: String,
    val owner: String,
    val last: String,
    val origin: String,        // canal de origem (rótulo)
    val source: String,        // chave do canal p/ ícone
    val plan: String,
    val tags: List<String>
)

val DEMO_CONTACTS = listOf(
    DemoContact(1, "Mariana Souza", "Pão de Mel Confeitaria", "Cliente", "(11) 98765-4312", "mariana@paodemel.com.br", "Você", "Última interação: hoje, 09:42", "WhatsApp", "whatsapp", "Pro · R$ 489/mês", listOf("Entrega", "Plano Pro", "SP Capital")),
    DemoContact(2, "Carlos Pereira", "Tech Andrade Ltda", "Lead qualificado", "(19) 99123-8874", "carlos@techandrade.com.br", "Ana B.", "Última interação: hoje, 09:31", "Instagram", "instagram", "Trial · 9 dias restantes", listOf("Orçamento", "Interior SP")),
    DemoContact(3, "Roberto Nunes", "Auto Peças Nunes", "Cliente", "(31) 98456-2210", "roberto@autopecasnunes.com.br", "Você", "Última interação: hoje, 08:57", "Facebook", "facebook", "Essencial · R$ 189/mês", listOf("Financeiro")),
    DemoContact(4, "Juliana Castro", "Estúdio Ipê Arquitetura", "Cliente", "(41) 99887-1123", "juliana@estudioipe.arq.br", "Lucas M.", "Última interação: hoje, 08:20", "Web Chat", "webchat", "Pro · R$ 489/mês", listOf("Projetos")),
    DemoContact(5, "Pedro Almeida", "Mercado Bom Preço", "Cliente", "(51) 98234-9987", "pedro@mercadobompreco.com.br", "Você", "Última interação: ontem", "WhatsApp", "whatsapp", "Business · R$ 890/mês", listOf("Varejo")),
    DemoContact(6, "Fernanda Lima", "Clínica Vida Leve", "Cliente", "(21) 99456-3341", "fernanda@vidaleve.med.br", "Camila R.", "Última interação: ontem", "SMS", "sms", "Pro · R$ 489/mês", listOf("Saúde")),
    DemoContact(7, "Beatriz Gomes", "Padaria Estrela", "Lead novo", "(11) 97612-0034", "contato@padariaestrela.com.br", "—", "Criada há 23 min", "Instagram", "instagram", "—", listOf("Novo lead"))
)

data class DemoTeamMember(
    val name: String,
    val role: String,
    val online: Boolean,
    val load: String
)

val DEMO_TEAM = listOf(
    DemoTeamMember("Ana Beatriz", "Comercial", true, "6 conversas"),
    DemoTeamMember("Lucas Martins", "Suporte N2", true, "4 conversas"),
    DemoTeamMember("Camila Rocha", "Suporte N1", false, "ausente"),
    DemoTeamMember("Diego Faria", "Suporte N1", true, "3 conversas")
)

data class DemoKanbanCard(
    val id: String,
    val title: String,
    val customer: String,
    val labels: List<String>,
    val due: String,
    val dueUrgent: Boolean,
    val dueDone: Boolean,
    val check: String,
    val comments: Int,
    val assignee: String
)

data class DemoKanbanColumn(
    val name: String,
    val colorHex: Long,
    val cards: List<DemoKanbanCard>
)

val DEMO_KANBAN = listOf(
    DemoKanbanColumn("Backlog", 0xFF9397AB, listOf(
        DemoKanbanCard("k1", "Migrar base de conhecimento para o novo portal", "", listOf("Interno"), "Set", false, false, "", 0, "Lucas Martins"),
        DemoKanbanCard("k2", "Revisar macros de respostas rápidas", "", listOf("CX"), "30 ago", false, false, "", 1, "Camila Rocha")
    )),
    DemoKanbanColumn("A Fazer", 0xFF8AB0D9, listOf(
        DemoKanbanCard("k3", "Reenviar pedido #3412 sem custo", "Pão de Mel Confeitaria", listOf("Urgente", "Entrega"), "Hoje", true, false, "1/3", 2, "Rafael Santos"),
        DemoKanbanCard("k4", "Enviar proposta comercial atualizada", "Tech Andrade Ltda", listOf("Comercial"), "Amanhã", false, false, "", 0, "Ana Beatriz")
    )),
    DemoKanbanColumn("Em Andamento", 0xFFB5ABFC, listOf(
        DemoKanbanCard("k5", "Configurar chatbot de triagem no WhatsApp", "", listOf("Automação"), "27 ago", false, false, "3/5", 4, "Lucas Martins"),
        DemoKanbanCard("k6", "Auditoria de SLA de agosto", "", listOf("Gestão"), "29 ago", false, false, "2/6", 1, "Rafael Santos")
    )),
    DemoKanbanColumn("Aguardando", 0xFFD9A86A, listOf(
        DemoKanbanCard("k7", "Aprovação de crédito para reenvio", "Auto Peças Nunes", listOf("Financeiro"), "Hoje", true, false, "", 3, "Camila Rocha")
    )),
    DemoKanbanColumn("Concluído", 0xFF6FBF9B, listOf(
        DemoKanbanCard("k8", "Onboarding Clínica Vida Leve", "Clínica Vida Leve", listOf("Onboarding"), "20 ago", false, true, "6/6", 5, "Camila Rocha"),
        DemoKanbanCard("k9", "Treinamento de novas etiquetas do CRM", "", listOf("Interno"), "18 ago", false, true, "4/4", 0, "Ana Beatriz")
    ))
)

data class DemoChecklistItem(val text: String, val done: Boolean)

val DEMO_CHECKLIST = listOf(
    DemoChecklistItem("Confirmar endereço com a cliente", true),
    DemoChecklistItem("Solicitar aprovação do financeiro", false),
    DemoChecklistItem("Agendar coleta com motoboy", false)
)

data class DemoQuickReply(val shortcut: String, val text: String)

val DEMO_QUICK_REPLIES = listOf(
    DemoQuickReply("/saudacao", "Olá! Aqui é o time VIPdesk. Como posso ajudar?"),
    DemoQuickReply("/prazo-entrega", "Nosso prazo de entrega é de 2 a 4 dias úteis para a sua região."),
    DemoQuickReply("/segunda-via", "Você pode emitir a segunda via do boleto pelo portal, em Financeiro > Faturas."),
    DemoQuickReply("/encerramento", "Fico feliz em ajudar! Se precisar de algo mais, é só chamar.")
)

// ————— Relatórios —————

data class DemoReportKpi(val n: String, val label: String, val delta: String, val positive: Boolean)

val DEMO_REPORT_KPIS = listOf(
    DemoReportKpi("1.284", "Conversas recebidas", "+12%", true),
    DemoReportKpi("96%", "Conversas atendidas", "+2%", true),
    DemoReportKpi("1m48s", "Primeira resposta (média)", "-14s", true),
    DemoReportKpi("3h12m", "Tempo de resolução", "+9m", false),
    DemoReportKpi("94%", "SLA cumprido", "+1%", true),
    DemoReportKpi("31", "Leads convertidas", "+5", true)
)

val DEMO_REPORT_BARS = listOf(52, 61, 45, 70, 66, 38, 25, 58, 72, 64, 80, 74, 42, 88)

data class DemoAgentPerf(val name: String, val value: Int)

val DEMO_AGENT_PERF = listOf(
    DemoAgentPerf("Ana Beatriz", 312),
    DemoAgentPerf("Lucas Martins", 268),
    DemoAgentPerf("Camila Rocha", 244),
    DemoAgentPerf("Rafael Santos", 187)
)

data class DemoDeptPerf(val name: String, val value: Int, val colorHex: Long)

val DEMO_DEPT_PERF = listOf(
    DemoDeptPerf("Suporte N1", 44, 0xFF8AB0D9),
    DemoDeptPerf("Suporte N2", 31, 0xFFB5ABFC),
    DemoDeptPerf("Comercial", 15, 0xFF6FBF9B),
    DemoDeptPerf("Financeiro", 10, 0xFFD9A86A)
)

val DEMO_RECENT_SEARCHES = listOf("boleto", "pedido #3412", "NF-e", "Padaria Estrela")
