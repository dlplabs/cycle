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

print("=== [TEST 3] INICIANDO FLUXO COMPLETO DE FUNCIONALIDADES E MÓDULOS ===")

# Iniciar gravador de vídeo
recorder = ScreenRecorder("/sdcard/test_03_features.mp4")
recorder.start()
print("Gravação de vídeo Teste 3 iniciada...")

# Trazer o app para frente
run_adb(["shell", "am", "start", "-n", "br.com.dlpsystems.cycle/.MainActivity"])
time.sleep(2)

# Fechar qualquer prompt ou diálogo remanescente (ex: Google Password Manager)
press_back()
time.sleep(1)

# 1. Dashboard com Ciclo Ativo
print("1. Capturando Dashboard...")
take_screenshot(f"{EVIDENCE_DIR}/feat_01_dashboard.png")

# Tocar no botão de registrar sintomas (Center: ~ 540, 1900 ou buscar por texto)
print("2. Abrindo BottomSheet de Registro de Sintomas...")
tapped_log = tap_element(text="Registrar", contains=True, retry=2, wait_after=1.5)
if not tapped_log:
    tap(540, 1900)
    time.sleep(1.5)

take_screenshot(f"{EVIDENCE_DIR}/feat_02_log_symptoms.png")

# Interagir com chips de sintomas e humor
swipe(540, 1500, 540, 1100, duration=300)
time.sleep(0.5)
tap(200, 1300)
time.sleep(0.3)
tap(450, 1300)
time.sleep(0.3)
tap(700, 1300)
time.sleep(0.5)
take_screenshot(f"{EVIDENCE_DIR}/feat_03_symptoms_selected.png")

# Fechar BottomSheet
press_back()
time.sleep(1)

# 2. Navegar para Aba Bem-Estar (Center: 403, 2301)
print("3. Navegando para Aba Bem-Estar...")
tap(403, 2301)
time.sleep(2)
take_screenshot(f"{EVIDENCE_DIR}/feat_04_wellness.png")

# 3. Módulo de Gravidez / Modo Gestante
print("4. Testando Módulo Gestante & Gravidez...")
tapped_preg = tap_element(text="Gestante", contains=True, retry=2, wait_after=2)
if not tapped_preg:
    tapped_preg = tap_element(text="Gravidez", contains=True, retry=2, wait_after=2)
if tapped_preg:
    take_screenshot(f"{EVIDENCE_DIR}/feat_05_pregnancy_screen.png")
    # Rolar tela de gravidez
    swipe(540, 1600, 540, 800, duration=300)
    time.sleep(1)
    take_screenshot(f"{EVIDENCE_DIR}/feat_06_pregnancy_scrolled.png")
    press_back()
    time.sleep(1)

# 4. Módulo Calendário Lunar
print("5. Testando Módulo Calendário Lunar...")
tapped_lunar = tap_element(text="Lunar", contains=True, retry=2, wait_after=2)
if tapped_lunar:
    take_screenshot(f"{EVIDENCE_DIR}/feat_07_lunar_screen.png")
    swipe(540, 1600, 540, 800, duration=300)
    time.sleep(1)
    take_screenshot(f"{EVIDENCE_DIR}/feat_08_lunar_scrolled.png")
    press_back()
    time.sleep(1)

# 5. Módulo Anticoncepcional / Pílula
print("6. Testando Módulo Controle de Pílula...")
tapped_pill = tap_element(text="Pílula", contains=True, retry=2, wait_after=2)
if tapped_pill:
    take_screenshot(f"{EVIDENCE_DIR}/feat_09_pill_screen.png")
    swipe(540, 1600, 540, 800, duration=300)
    time.sleep(1)
    take_screenshot(f"{EVIDENCE_DIR}/feat_10_pill_scrolled.png")
    press_back()
    time.sleep(1)

# 6. Módulo Central de Exames
print("7. Testando Central de Exames...")
tapped_exams = tap_element(text="Exames", contains=True, retry=2, wait_after=2)
if tapped_exams:
    take_screenshot(f"{EVIDENCE_DIR}/feat_11_exams_screen.png")
    swipe(540, 1600, 540, 800, duration=300)
    time.sleep(1)
    take_screenshot(f"{EVIDENCE_DIR}/feat_12_exams_scrolled.png")
    press_back()
    time.sleep(1)

# 7. Módulo Mapa Corporal de Sintomas
print("8. Testando Mapa Corporal de Sintomas...")
tapped_body = tap_element(text="Corporais", contains=True, retry=2, wait_after=2)
if not tapped_body:
    tapped_body = tap_element(text="Sintomas", contains=True, retry=2, wait_after=2)
if tapped_body:
    take_screenshot(f"{EVIDENCE_DIR}/feat_13_body_symptoms.png")
    swipe(540, 1600, 540, 800, duration=300)
    time.sleep(1)
    take_screenshot(f"{EVIDENCE_DIR}/feat_14_body_symptoms_scrolled.png")
    press_back()
    time.sleep(1)

# 8. Aba Alívio SOS (Center: 677, 2301)
print("9. Navegando para Aba Alívio SOS...")
tap(677, 2301)
time.sleep(2)
take_screenshot(f"{EVIDENCE_DIR}/feat_15_sos_screen.png")

# Interagir com o timer de respiração
tap(540, 850) # Iniciar / pausar respiração
time.sleep(3)
take_screenshot(f"{EVIDENCE_DIR}/feat_16_sos_breathing_active.png")

# Rolar SOS para posições corporais
swipe(540, 1700, 540, 700, duration=300)
time.sleep(1)
take_screenshot(f"{EVIDENCE_DIR}/feat_17_sos_positions.png")

# 9. Aba Planejar (Center: 952, 2301)
print("10. Navegando para Aba Planejar...")
tap(952, 2301)
time.sleep(2)
take_screenshot(f"{EVIDENCE_DIR}/feat_18_planner_screen.png")

# Rolar Planejar
swipe(540, 1700, 540, 700, duration=300)
time.sleep(1)
take_screenshot(f"{EVIDENCE_DIR}/feat_19_planner_scrolled.png")

# 10. Acessar Paywall / Assinatura Premium
print("11. Acessando Paywall Screen...")
tapped_paywall = tap_element(text="Premium", contains=True, retry=2, wait_after=2)
if not tapped_paywall:
    tapped_paywall = tap_element(text="Assinar", contains=True, retry=2, wait_after=2)
if not tapped_paywall:
    # Coordenadas ou abrir via Aba Bem-estar
    tap(403, 2301)
    time.sleep(1)
    tap_element(text="Premium", contains=True, retry=2, wait_after=2)

take_screenshot(f"{EVIDENCE_DIR}/feat_20_paywall_screen.png")
press_back()
time.sleep(1)

# 11. Finalizar gravação
print("12. Finalizando gravação de vídeo Teste 3...")
recorder.stop(f"{VIDEO_DIR}/test_03_features_and_modules.mp4")

print("=== [TEST 3] CONCLUÍDO COM SUCESSO! ===")
