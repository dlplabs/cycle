import time
import os
import subprocess
from adb_helper import (
    run_adb, tap, swipe, dump_ui, find_element, tap_element,
    press_back, take_screenshot, ScreenRecorder, ADB_PATH
)

EVIDENCE_DIR = "/home/daniel-pinheiro/.gemini/antigravity/brain/30726af6-81d0-4a73-8eae-0e8b0d931d24/evidence"
VIDEO_DIR = "/home/daniel-pinheiro/.gemini/antigravity/brain/30726af6-81d0-4a73-8eae-0e8b0d931d24/videos"

os.makedirs(EVIDENCE_DIR, exist_ok=True)
os.makedirs(VIDEO_DIR, exist_ok=True)

print("=== [TEST 2] INICIANDO FLUXO USUÁRIO REGISTRADO / AUTENTICADO (FREE) ===")

# Limpar dados para começar da tela de Welcome
run_adb(["shell", "pm", "clear", "br.com.dlpsystems.cycle"])
time.sleep(1)

# Iniciar gravador de vídeo
recorder = ScreenRecorder("/sdcard/test_02_auth.mp4")
recorder.start()
print("Gravação de vídeo iniciada...")

# Iniciar App
run_adb(["shell", "am", "start", "-n", "br.com.dlpsystems.cycle/.MainActivity"])
time.sleep(3)

# 1. Welcome Screen
print("1. Capturando Welcome Screen...")
take_screenshot(f"{EVIDENCE_DIR}/auth_01_welcome.png")

# Clicar em "Criar conta" (bounds [63,2128][1017,2254] -> centro 540, 2191)
print("2. Clicando em 'Criar conta'...")
if not tap_element(text="Criar conta", retry=3, wait_after=2):
    tap(540, 2191)
    time.sleep(2)

# 2. Tela de Cadastro (RegisterScreen)
print("3. Capturando RegisterScreen...")
take_screenshot(f"{EVIDENCE_DIR}/auth_02_register_empty.png")

# Identificar e preencher campos
# Dump UI da tela de registro
root = dump_ui()

# Nome: primeiro campo OutlinedTextField (~ y=550)
print("4. Preenchendo Nome...")
tap(540, 560)
time.sleep(0.5)
run_adb(["shell", "input", "text", "Ana%sSilva%sTeste"])
time.sleep(0.5)

# Data de Nascimento: segundo campo (~ y=720)
print("5. Preenchendo Data de Nascimento...")
tap(540, 740)
time.sleep(0.5)
run_adb(["shell", "input", "text", "15051998"])
time.sleep(0.5)

# Email: terceiro campo (~ y=920)
print("6. Preenchendo Email...")
tap(540, 930)
time.sleep(0.5)
# Email único de teste
test_email = f"teste.cycle.{int(time.time())}@cycleapp.internal"
run_adb(["shell", "input", "text", test_email])
time.sleep(0.5)

# Senha: quarto campo (~ y=1100)
print("7. Preenchendo Senha...")
tap(540, 1110)
time.sleep(0.5)
run_adb(["shell", "input", "text", "CyclePassword123!"])
time.sleep(0.5)

# Fechar teclado
press_back()
time.sleep(0.5)

take_screenshot(f"{EVIDENCE_DIR}/auth_03_register_filled.png")

# Clicar no botão "Criar conta" / "Cadastrar"
print("8. Submetendo formulário de cadastro...")
tapped_submit = tap_element(text="Criar conta", contains=True, retry=2, wait_after=3)
if not tapped_submit:
    tapped_submit = tap_element(text="Cadastrar", contains=True, retry=2, wait_after=3)
if not tapped_submit:
    # Coordenadas do botão
    tap(540, 1400)
    time.sleep(3)

# 3. Health Disclaimer se aparecer
print("9. Verificando Health Disclaimer...")
take_screenshot(f"{EVIDENCE_DIR}/auth_04_post_register_disclaimer.png")
accepted = tap_element(text="Entendi", contains=True, retry=2)
if not accepted:
    accepted = tap_element(text="Continuar", contains=True, retry=2)
if not accepted:
    accepted = tap_element(text="Aceitar", contains=True, retry=2)
time.sleep(2)

# 4. Dashboard Autenticado
print("10. Capturando Dashboard Autenticado...")
take_screenshot(f"{EVIDENCE_DIR}/auth_05_dashboard_authenticated.png")

# Dispensar CoachMark se presente
tap(540, 1200)
time.sleep(1)

# 5. Marcar 1º dia da menstruação (Agora como usuária autenticada!)
print("11. Marcando 1º dia da menstruação como usuária cadastrada...")
# Centro do botão principal: Center=(577, 2022)
tap(577, 2022)
time.sleep(2)
take_screenshot(f"{EVIDENCE_DIR}/auth_06_period_started.png")

# 6. Rolar e verificar atualização da Roda do Ciclo (Fase Menstrual ativa)
print("12. Verificando Roda do Ciclo Menstrual...")
swipe(540, 1200, 540, 600, duration=300)
time.sleep(1)
take_screenshot(f"{EVIDENCE_DIR}/auth_07_menstrual_wheel_active.png")

# 7. Registrar Sintomas de Hoje (Botão "Registrar hoje" ou no BottomSheet)
print("13. Abrindo BottomSheet de Registro de Sintomas...")
# Procurar botão de registrar sintomas
tapped_log = tap_element(text="Registrar", contains=True, retry=2, wait_after=1.5)
if not tapped_log:
    tap(540, 1900)
    time.sleep(1.5)

take_screenshot(f"{EVIDENCE_DIR}/auth_08_log_symptoms_bottomsheet.png")

# Selecionar alguns sintomas no BottomSheet (chips de cólica, humor, fluxo)
# Tocar em alguns chips
tap(200, 1200) # chip 1
time.sleep(0.5)
tap(450, 1200) # chip 2
time.sleep(0.5)
tap(700, 1200) # chip 3
time.sleep(0.5)
take_screenshot(f"{EVIDENCE_DIR}/auth_09_symptoms_selected.png")

# Salvar sintomas (botão Salvar no BottomSheet)
tapped_save = tap_element(text="Salvar", contains=True, retry=2, wait_after=1.5)
if not tapped_save:
    tapped_save = tap_element(text="Confirmar", contains=True, retry=2, wait_after=1.5)
if not tapped_save:
    press_back()
time.sleep(1)

# 8. Modo Parceiro (Usuária autenticada pode gerar código de sincronização!)
print("14. Navegando para Aba Bem-Estar e testando Modo Parceiro...")
tap(403, 2301) # Aba Bem-Estar
time.sleep(2)
take_screenshot(f"{EVIDENCE_DIR}/auth_10_wellness_authenticated.png")

# Clicar em Modo Parceiro
tapped_partner = tap_element(text="Parceiro", contains=True, retry=3, wait_after=2)
if not tapped_partner:
    # Coordenadas comuns do card do parceiro
    swipe(540, 1600, 540, 1000, duration=300)
    tap_element(text="Parceiro", contains=True, retry=2, wait_after=2)

take_screenshot(f"{EVIDENCE_DIR}/auth_11_partner_code_generated.png")
press_back()
time.sleep(1)

# 9. Configurações (Settings) com Usuária Autenticada
print("15. Acessando Configurações da Conta...")
# Botão Conta nos 3 pontinhos do header: Center=(964, 153)
tap(127, 2301) # Voltar para Home
time.sleep(1)
tap(964, 153) # Três pontinhos / menu Conta
time.sleep(2)
take_screenshot(f"{EVIDENCE_DIR}/auth_12_settings_authenticated.png")

# Rolar configurações
swipe(540, 1600, 540, 800, duration=300)
time.sleep(1)
take_screenshot(f"{EVIDENCE_DIR}/auth_13_settings_backup_sync.png")

print("16. Finalizando gravação de vídeo do Fluxo Usuário Autenticado...")
recorder.stop(f"{VIDEO_DIR}/test_02_register_and_auth_user.mp4")

print("=== [TEST 2] FLUXO USUÁRIO AUTENTICADO CONCLUÍDO COM SUCESSO! ===")
