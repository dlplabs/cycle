import time
import os
import sys
from adb_helper import (
    ADB_PATH, run_adb, tap, swipe, type_text, press_back,
    dump_ui, find_element, tap_element, take_screenshot, ScreenRecorder
)

EVIDENCE_DIR = "/home/daniel-pinheiro/.gemini/antigravity/brain/30726af6-81d0-4a73-8eae-0e8b0d931d24/evidence"
VIDEO_DIR = "/home/daniel-pinheiro/.gemini/antigravity/brain/30726af6-81d0-4a73-8eae-0e8b0d931d24/videos"

os.makedirs(EVIDENCE_DIR, exist_ok=True)
os.makedirs(VIDEO_DIR, exist_ok=True)

video_path = os.path.join(VIDEO_DIR, "test_07_planner_calendar_settings.mp4")

print("=== INICIANDO TESTE 07: PLANEJADOR, CALENDÁRIO MENSTRUAL E CONFIGURAÇÕES ===")

# Garantir app na Dashboard
tap_element(text="Hoje", retry=2, wait_after=1.0)

recorder = ScreenRecorder()
recorder.start()
print("Gravação de vídeo iniciada:", video_path)

try:
    # 1. Testar Planejador de Eventos Futuros (Aba Planejar)
    print("Navegando para a aba Planejar...")
    tap_element(text="Planejar", retry=3, wait_after=2.0)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test07_01_planner_screen.png"))

    # Fechar CoachMark se visível
    root = dump_ui()
    if find_element(root, text="Entendi"):
        print("Dispensando coachmark inicial do planejador...")
        tap_element(text="Entendi", wait_after=1.0)
        take_screenshot(os.path.join(EVIDENCE_DIR, "test07_02_planner_coachmark_dismissed.png"))

    # Selecionar uma data no DatePicker
    print("Selecionando dia futuro no calendário do planejador...")
    # Tocar em um dia na grade do DatePicker (~540, 1150)
    tap(540, 1150)
    time.sleep(1.0)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test07_03_planner_date_picked.png"))

    # Clicar no botão "Calcular previsão"
    print("Calculando projeção da fase futura...")
    tap_element(text="Calcular previsão", contains=True, retry=3, wait_after=2.0)

    # Rolar para baixo para ver a projeção detalhada (ProjectedPhaseCard)
    swipe(540, 1600, 540, 750, 300)
    time.sleep(1.5)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test07_04_planner_projected_phase.png"))

    # 2. Testar Calendário Menstrual Detalhado (Modal do Dashboard)
    print("Retornando para a aba Hoje...")
    tap_element(text="Hoje", retry=3, wait_after=2.0)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test07_05_dashboard_for_calendar.png"))

    # Clicar no centro da roda do ciclo para abrir o BottomSheet de calendário
    print("Abrindo CycleCalendarBottomSheet via toque na roda do ciclo...")
    tap(540, 1060)
    time.sleep(2.0)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test07_06_calendar_bottom_sheet.png"))

    # Rolar pelo calendário do ciclo
    swipe(540, 1600, 540, 900, 300)
    time.sleep(1.0)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test07_07_calendar_scrolled.png"))

    # Fechar o BottomSheet de calendário via botão 'Fechar' no topo direito
    print("Fechando o BottomSheet de calendário...")
    tap(960, 320)
    time.sleep(1.5)

    # 3. Testar Configurações da Conta
    print("Acessando Configurações da Conta...")
    tap_element(content_desc="Conta", retry=3, wait_after=2.0)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test07_08_settings_screen.png"))

    # Testar seleção de Fase de Vida (ex: Tentante)
    print("Alternando Fase de Vida para Tentante...")
    tap_element(text="Tentante", contains=True, retry=2, wait_after=1.5)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test07_09_settings_life_stage_ttc.png"))

    # Retornar para Ciclo Regular
    print("Restaurando Fase de Vida para Ciclo Regular...")
    tap_element(text="Ciclo Regular", contains=True, retry=2, wait_after=1.0)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test07_10_settings_life_stage_regular.png"))

    # Rolar para a seção de Médias do Ciclo
    print("Rolando para Médias do Ciclo...")
    swipe(540, 1600, 540, 950, 300)
    time.sleep(1.0)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test07_11_settings_averages.png"))

    # Rolar para a seção de Notificações, Backup e Termos
    print("Rolando para Notificações e Privacidade...")
    swipe(540, 1600, 540, 850, 300)
    time.sleep(1.0)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test07_12_settings_notifications_and_terms.png"))

    # Retornar para a tela inicial via botão Voltar da toolbar
    print("Retornando para a tela inicial...")
    tap_element(content_desc="Voltar", retry=2, wait_after=1.5)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test07_13_back_to_dashboard.png"))

    print("=== TESTE 07 CONCLUÍDO COM SUCESSO ===")

finally:
    recorder.stop(video_path)
    print("Gravação de vídeo finalizada e salva em:", video_path)
