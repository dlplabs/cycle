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

print("=== [TEST 1] INICIANDO FLUXO VISITANTE (GUEST USER) ===")

# Limpar dados para estado zero
run_adb(["shell", "pm", "clear", "br.com.dlpsystems.cycle"])
time.sleep(1)

# Iniciar gravador de vídeo
recorder = ScreenRecorder("/sdcard/test_01_guest.mp4")
recorder.start()
print("Gravação de vídeo iniciada...")

# Iniciar App
run_adb(["shell", "am", "start", "-n", "br.com.dlpsystems.cycle/.MainActivity"])
time.sleep(3)

# 1. Welcome Screen
print("1. Capturando Welcome Screen...")
take_screenshot(f"{EVIDENCE_DIR}/guest_01_welcome.png")

# Tocar em "Conhecer o aplicativo"
print("2. Clicando em 'Conhecer o aplicativo'...")
if not tap_element(text="Conhecer o aplicativo", retry=4, wait_after=2):
    # Coordenadas de fallback
    tap(540, 1874)
    time.sleep(2)

# 2. Disclaimer Dialog
print("3. Verificando Health Disclaimer Dialog...")
take_screenshot(f"{EVIDENCE_DIR}/guest_02_disclaimer.png")
# Tocar para aceitar disclaimer (procurar "Entendi", "Concordo", "Aceitar", "Continuar")
accepted = tap_element(text="Entendi", contains=True, retry=2)
if not accepted:
    accepted = tap_element(text="Continuar", contains=True, retry=2)
if not accepted:
    accepted = tap_element(text="Aceitar", contains=True, retry=2)
if not accepted:
    # Coordenada provável do botão no diálogo
    tap(540, 1500)
time.sleep(2)

# 3. Dashboard
print("4. Capturando Dashboard Visitante e CoachMark...")
take_screenshot(f"{EVIDENCE_DIR}/guest_03_dashboard_coach.png")

# Dispensar CoachMark tocando na tela
tap(540, 1200)
time.sleep(1)
take_screenshot(f"{EVIDENCE_DIR}/guest_04_dashboard_clean.png")

# 4. Tentar ação restrita no Dashboard (Botão principal de registrar início de menstruação)
print("5. Testando tentativa de registro como visitante (AuthRequiredDialog)...")
# O botão principal geralmente está por volta de y=1750 ou procurar por texto
tapped_register = tap_element(text="Registrar", contains=True, retry=2, wait_after=1.5)
if not tapped_register:
    tap(540, 1750)
    time.sleep(1.5)

take_screenshot(f"{EVIDENCE_DIR}/guest_05_auth_required_dialog.png")

# Fechar diálogo tocando fora ou no botão "Agora não" / cancelar / back
tapped_cancel = tap_element(text="Agora não", contains=True, retry=2)
if not tapped_cancel:
    tapped_cancel = tap_element(text="Cancelar", contains=True, retry=2)
if not tapped_cancel:
    press_back()
time.sleep(1)

# Rolar dashboard para baixo para ver cards científicos e AdBanner
print("6. Rolando Dashboard...")
swipe(540, 1600, 540, 600, duration=400)
time.sleep(1)
take_screenshot(f"{EVIDENCE_DIR}/guest_06_dashboard_scrolled.png")

# 5. Módulo de Segurança Pessoal (Ícone de escudo ou cabeçalho)
print("7. Acessando Módulo de Segurança Pessoal...")
# No topo direito do header: ícone de segurança fica por volta de x=940, y=140
tap(940, 140)
time.sleep(2)
take_screenshot(f"{EVIDENCE_DIR}/guest_07_personal_safety.png")

# Rolar tela de segurança
swipe(540, 1600, 540, 800, duration=300)
time.sleep(1)
take_screenshot(f"{EVIDENCE_DIR}/guest_08_safety_features.png")

# Tentar iniciar questionário IAVP
print("8. Iniciando Questionário IAVP...")
tapped_iavp = tap_element(text="Questionário", contains=True, retry=2, wait_after=1.5)
if not tapped_iavp:
    tapped_iavp = tap_element(text="Iniciar", contains=True, retry=2, wait_after=1.5)

take_screenshot(f"{EVIDENCE_DIR}/guest_09_iavp_questionnaire.png")

# Testar botão de Camuflagem / Saída de Pânico (ícone discreto de gota d'água no topo direito: ~ x=980, y=120)
print("9. Testando Saída Rápida / Camuflagem de Hidratação...")
tap(980, 120)
time.sleep(1.5)
take_screenshot(f"{EVIDENCE_DIR}/guest_10_iavp_disguise_hydration.png")

# Sair da camuflagem (tocar no botão de voltar ou sair)
press_back()
time.sleep(1)
press_back()
time.sleep(1)
press_back()
time.sleep(1)

# 6. Aba Bem-estar (Aba 2: ~ x=405, y=2320)
print("10. Navegando para Aba Bem-estar...")
tap(405, 2320)
time.sleep(2)
take_screenshot(f"{EVIDENCE_DIR}/guest_11_wellness_screen.png")

# Testar recurso: Calendário Lunar
print("11. Testando Calendário Lunar...")
tapped_lunar = tap_element(text="Lunar", contains=True, retry=2, wait_after=2)
if tapped_lunar:
    take_screenshot(f"{EVIDENCE_DIR}/guest_12_lunar_screen.png")
    press_back()
    time.sleep(1)

# Testar recurso: Controle de Pílula
print("12. Testando Controle de Pílula...")
tapped_pill = tap_element(text="Pílula", contains=True, retry=2, wait_after=2)
if tapped_pill:
    take_screenshot(f"{EVIDENCE_DIR}/guest_13_pill_screen.png")
    press_back()
    time.sleep(1)

# Testar recurso: Mapa Corporal de Sintomas
print("13. Testando Mapa Corporal de Sintomas...")
tapped_symptoms = tap_element(text="Sintomas", contains=True, retry=2, wait_after=2)
if not tapped_symptoms:
    tapped_symptoms = tap_element(text="Corporais", contains=True, retry=2, wait_after=2)
if tapped_symptoms:
    take_screenshot(f"{EVIDENCE_DIR}/guest_14_body_symptoms_screen.png")
    press_back()
    time.sleep(1)

# Testar recurso: Central de Exames
print("14. Testando Central de Exames...")
tapped_exams = tap_element(text="Exames", contains=True, retry=2, wait_after=2)
if tapped_exams:
    take_screenshot(f"{EVIDENCE_DIR}/guest_15_exams_screen.png")
    press_back()
    time.sleep(1)

# Testar recurso: Modo Parceiro
print("15. Testando Modo Parceiro...")
tapped_partner = tap_element(text="Parceiro", contains=True, retry=2, wait_after=2)
if tapped_partner:
    take_screenshot(f"{EVIDENCE_DIR}/guest_16_partner_screen.png")
    press_back()
    time.sleep(1)

# 7. Aba Alívio SOS (Aba 3: ~ x=675, y=2320)
print("16. Navegando para Aba Alívio SOS...")
tap(675, 2320)
time.sleep(2)
take_screenshot(f"{EVIDENCE_DIR}/guest_17_sos_relief_screen.png")

# Rolar Alívio SOS
swipe(540, 1600, 540, 800, duration=300)
time.sleep(1)
take_screenshot(f"{EVIDENCE_DIR}/guest_18_sos_relief_scrolled.png")

# 8. Aba Planejar (Aba 4: ~ x=945, y=2320)
print("17. Navegando para Aba Planejar...")
tap(945, 2320)
time.sleep(2)
take_screenshot(f"{EVIDENCE_DIR}/guest_19_planner_screen.png")

# 9. Configurações / Settings (Avatar / Perfil no header do topo: ~ x=120, y=140 ou x=960, y=140)
print("18. Acessando Configurações...")
tap(135, 2320) # Voltar para Home
time.sleep(1)
tap(100, 140) # Avatar / perfil no topo esquerdo do HomeHeader
time.sleep(2)
take_screenshot(f"{EVIDENCE_DIR}/guest_20_settings_screen.png")

# Rolar tela de configurações
swipe(540, 1600, 540, 800, duration=300)
time.sleep(1)
take_screenshot(f"{EVIDENCE_DIR}/guest_21_settings_scrolled.png")

print("19. Finalizando gravação de vídeo do Fluxo Visitante...")
recorder.stop(f"{VIDEO_DIR}/test_01_guest_user.mp4")

print("=== [TEST 1] FLUXO VISITANTE CONCLUÍDO COM SUCESSO! ===")
