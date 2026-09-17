package br.com.vipdesk.mobile.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.focusRequester
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.vipdesk.mobile.R
import br.com.vipdesk.mobile.ui.components.VdButton
import br.com.vipdesk.mobile.ui.components.VdButtonStyle
import br.com.vipdesk.mobile.ui.components.VdInput
import br.com.vipdesk.mobile.ui.theme.AppTheme

/** Login (tela 04): logo, slot white-label, formulário, SSO, rodapé. */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val c = AppTheme.colors
    val focus = LocalFocusManager.current
    var showPassword by remember { mutableStateOf(false) }
    var keepLogged by remember { mutableStateOf(true) }

    LaunchedEffect(state.isLoggedIn) { if (state.isLoggedIn) onLoginSuccess() }

    state.mfa?.let { challenge ->
        MfaScreen(state = state, challenge = challenge, viewModel = viewModel)
        return
    }

    Column(
        Modifier.fillMaxSize().background(c.surface).statusBarsPadding().navigationBarsPadding().imePadding().verticalScroll(rememberScrollState())
    ) {
        // Topo: logo + slot do cliente
        Column(Modifier.fillMaxWidth().padding(top = 36.dp, start = 24.dp, end = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(R.drawable.logo_vipdesk), contentDescription = "VipDesk", modifier = Modifier.height(64.dp))
            Spacer(Modifier.height(14.dp))
            Row(
                Modifier.background(c.surfaceAlt, RoundedCornerShape(20.dp)).padding(start = 6.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(Modifier.size(24.dp).background(c.primary, RoundedCornerShape(7.dp)), contentAlignment = Alignment.Center) {
                    Text("V", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Text("Atendimento omnichannel · Helpdesk · CRM", fontSize = 12.sp, color = Color(0xFF374151))
            }
            Spacer(Modifier.height(20.dp))
            Text("Atendimento omnichannel, Helpdesk e CRM em um só lugar.", fontSize = 13.sp, color = c.muted, textAlign = TextAlign.Center)
        }

        // Formulário
        Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Entrar", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = c.text)
            VdInput(
                label = "E-mail", value = state.email, onValueChange = viewModel::onEmailChange,
                placeholder = "voce@empresa.com.br",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )
            VdInput(
                label = "Senha", value = state.password, onValueChange = viewModel::onPasswordChange,
                placeholder = "••••••••",
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focus.clearFocus(); viewModel.login() }),
                trailing = {
                    Icon(
                        if (showPassword) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, "Mostrar senha",
                        tint = c.muted, modifier = Modifier.size(18.dp).clickable { showPassword = !showPassword }
                    )
                },
                error = state.error
            )
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(Modifier.clickable { keepLogged = !keepLogged }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        Modifier.size(16.dp).background(if (keepLogged) c.primary else c.surface, RoundedCornerShape(4.dp))
                            .border(1.dp, if (keepLogged) c.primary else c.placeholder, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) { if (keepLogged) Icon(Icons.Outlined.Check, null, tint = Color.White, modifier = Modifier.size(11.dp)) }
                    Text("Manter conectado", fontSize = 13.sp, color = c.text)
                }
                Spacer(Modifier.weight(1f))
                Text("Esqueci a senha", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = c.primary)
            }
            Spacer(Modifier.height(2.dp))
            if (state.isLoading) {
                Box(Modifier.fillMaxWidth().height(46.dp).background(c.primary, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                }
            } else {
                VdButton("Entrar", onClick = { focus.clearFocus(); viewModel.login() }, modifier = Modifier.fillMaxWidth())
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(vertical = 2.dp)) {
                Box(Modifier.weight(1f).height(1.dp).background(c.divider))
                Text("ou", fontSize = 12.sp, color = c.placeholder)
                Box(Modifier.weight(1f).height(1.dp).background(c.divider))
            }
            VdButton("Entrar com SSO da empresa", onClick = {}, style = VdButtonStyle.Secondary, icon = Icons.Outlined.Key, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(24.dp))
            Text(
                buildString { append("Ainda não tem conta? Teste grátis por 14 dias") },
                fontSize = 13.sp, color = c.muted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
            )
            Text("Termos · Privacidade", fontSize = 11.sp, color = c.placeholder, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}


/** Verificação em duas etapas (tela 06): código TOTP, por e-mail ou de recuperação. */
@Composable
private fun MfaScreen(state: LoginUiState, challenge: br.com.vipdesk.mobile.data.model.MfaChallenge, viewModel: LoginViewModel) {
    val c = AppTheme.colors
    val focus = LocalFocusManager.current
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    val isEmail = challenge.method == "email"
    androidx.activity.compose.BackHandler { viewModel.cancelMfa() }
    LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }

    Column(
        Modifier.fillMaxSize().background(c.surface).statusBarsPadding().navigationBarsPadding().imePadding().verticalScroll(rememberScrollState())
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Image(painterResource(R.drawable.logo_vipdesk), contentDescription = "VipDesk", modifier = Modifier.height(28.dp))
            Text("VipDesk", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = c.text)
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.divider))

        Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(52.dp).background(c.primarySurface, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Shield, null, tint = c.primary, modifier = Modifier.size(26.dp))
            }
            Text("Verificação em duas etapas", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = c.text)
            Text(
                if (isEmail) "Enviamos um código de 6 dígitos para ${challenge.emailHint ?: "seu e-mail"}. Ele vale por 5 minutos."
                else "Digite o código de 6 dígitos do seu aplicativo autenticador.",
                fontSize = 13.sp, color = c.muted, lineHeight = 19.sp
            )

            // 6 caixas (ou até 9 para código de recuperação) sobre um único campo invisível
            val code = state.mfaCode
            val slots = if (code.length > 6) 9 else 6
            Box {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(slots) { i ->
                        val ch = code.getOrNull(i)?.toString() ?: ""
                        val active = i == code.length.coerceAtMost(slots - 1)
                        Box(
                            Modifier.weight(1f).height(56.dp)
                                .background(c.surface, RoundedCornerShape(10.dp))
                                .border(if (active) 2.dp else 1.dp, if (state.mfaError != null) c.danger else if (active) c.primary else c.placeholder, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) { Text(ch, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = c.text) }
                    }
                }
                androidx.compose.foundation.text.BasicTextField(
                    value = code,
                    onValueChange = viewModel::onMfaCodeChange,
                    modifier = Modifier.matchParentSize().focusRequester(focusRequester).alpha(0.01f),
                    keyboardOptions = KeyboardOptions(keyboardType = if (isEmail) KeyboardType.NumberPassword else KeyboardType.Text, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focus.clearFocus(); viewModel.verifyMfa() }),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.Transparent)
                )
            }
            state.mfaError?.let { Text(it, fontSize = 12.sp, color = c.danger) }
            state.mfaInfo?.let { Text(it, fontSize = 12.sp, color = c.success) }

            if (state.isLoading) {
                Box(Modifier.fillMaxWidth().height(46.dp).background(c.primary, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                }
            } else {
                VdButton("Verificar", onClick = { focus.clearFocus(); viewModel.verifyMfa() }, modifier = Modifier.fillMaxWidth())
            }

            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (isEmail) Text("Reenviar código por e-mail", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = c.primary, modifier = Modifier.clickable { viewModel.resendMfaEmail() })
                Text("Tem um código de recuperação? Digite-o no campo acima.", fontSize = 12.sp, color = c.muted, textAlign = TextAlign.Center)
                Text("Voltar ao login", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = c.muted, modifier = Modifier.clickable { viewModel.cancelMfa() })
            }
        }
    }
}
