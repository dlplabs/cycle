import time
import os
from adb_helper import (
    run_adb, tap, swipe, dump_ui, find_element, tap_element,
    press_back, take_screenshot, ScreenRecorder
)

EVIDENCE_DIR = "/home/daniel-pinheiro/.gemini/antigravity/brain/30726af6-81d0-4a73-8eae-0e8b0d931d24/evidence"
VIDEO_DIR = "/home/daniel-pinheiro/.gemini/antigravity/brain/30726af6-81d0-4a73-8eae-0e8b0d931d24/videos"

recorder = ScreenRecorder("/sdcard/test_02_auth_accurate.mp4")
recorder.start()
print("Gravação de vídeo Teste 2 iniciada...")

# 1. Preencher Nome
print("1. Preenchendo Nome...")
tap(540, 725)
time.sleep(0.5)
run_adb(["shell", "input", "text", "Ana%sSilva"])
time.sleep(0.5)

# 2. Preencher Data de Nascimento
print("2. Preenchendo Data de Nascimento...")
tap(540, 961)
time.sleep(0.5)
run_adb(["shell", "input", "text", "15051998"])
time.sleep(0.5)

# 3. Preencher E-mail
print("3. Preenchendo E-mail...")
tap(540, 1198)
time.sleep(0.5)
email = f"ana.silva.cycle.{int(time.time())}@gmail.com"
run_adb(["shell", "input", "text", email])
time.sleep(0.5)

# 4. Preencher Senha
print("4. Preenchendo Senha...")
tap(540, 1434)
time.sleep(0.5)
run_adb(["shell", "input", "text", "SenhaSegura123!"])
time.sleep(0.5)

# Fechar teclado para expor o botão de cadastro
press_back()
time.sleep(1)

take_screenshot(f"{EVIDENCE_DIR}/auth_accurate_01_filled.png")

# 5. Clicar no botão "Criar conta"
print("5. Clicando em Criar conta...")
tap(540, 1650)
time.sleep(4)

take_screenshot(f"{EVIDENCE_DIR}/auth_accurate_02_post_submit.png")

# 6. Disclaimer de Saúde se aparecer
accepted = tap_element(text="Entendi", contains=True, retry=3)
if not accepted:
    accepted = tap_element(text="Continuar", contains=True, retry=2)
if not accepted:
    accepted = tap_element(text="Aceitar", contains=True, retry=2)
time.sleep(2)

take_screenshot(f"{EVIDENCE_DIR}/auth_accurate_03_dashboard_user.png")

# 7. Dispensar CoachMark
tap(540, 1200)
time.sleep(1)

# 8. Marcar 1º dia da menstruação
print("6. Marcando 1º dia da menstruação como usuária cadastrada...")
tap(577, 2022) # Botão principal
time.sleep(2)
take_screenshot(f"{EVIDENCE_DIR}/auth_accurate_04_period_active.png")

# 9. Rolar para ver o ciclo ativo
swipe(540, 1600, 540, 800, duration=300)
time.sleep(1)
take_screenshot(f"{EVIDENCE_DIR}/auth_accurate_05_dashboard_active_cycle.png")

# 10. Abrir tela de Configurações
print("7. Acessando Configurações...")
tap(127, 2301) # Home
time.sleep(1)
tap(964, 153) # Três pontinhos do menu / Conta
time.sleep(2)
take_screenshot(f"{EVIDENCE_DIR}/auth_accurate_06_settings_user.png")

# Rolar configurações
swipe(540, 1600, 540, 800, duration=300)
time.sleep(1)
take_screenshot(f"{EVIDENCE_DIR}/auth_accurate_07_settings_user_scrolled.png")

recorder.stop(f"{VIDEO_DIR}/test_02_register_and_auth_accurate.mp4")
print("=== [TEST 2 ACCURATE] CONCLUÍDO COM SUCESSO! ===")
