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

video_path = os.path.join(VIDEO_DIR, "test_04_maternal_and_clinical.mp4")

print("=== INICIANDO TESTE 04: JORNADA MATERNA, GESTAÇÃO, PÍLULA, LUNAR E EXAMES ===")

# Garantir app aberto
run_adb(["shell", "am", "start", "-n", "br.com.dlpsystems.cycle/.MainActivity"])
time.sleep(2)

recorder = ScreenRecorder()
recorder.start()
print("Gravação de vídeo iniciada:", video_path)

try:
    # 1. Navegar para a aba Bem-Estar
    print("Navegando para a aba Bem-Estar...")
    tap_element(text="Bem-Estar", retry=3, wait_after=1.5)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test04_01_wellness_hub.png"))

    # 2. Testar Diálogo Clínico: Tentante (TTC)
    print("Testando card: Tentante (TTC)...")
    tap_element(text="Tentante (TTC)", contains=True, retry=3, wait_after=1.5)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test04_02_ttc_dialog.png"))
    # Fechar diálogo
    if not tap_element(text="Cancelar", retry=2, wait_after=1.0):
        if not tap_element(text="Fechar", retry=2, wait_after=1.0):
            press_back()
    time.sleep(1.0)

    # 3. Testar Diálogo Clínico: Pós-Parto & EPDS
    print("Testando card: Pós-Parto & EPDS...")
    tap_element(text="Pós-Parto", contains=True, retry=3, wait_after=1.5)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test04_03_epds_dialog.png"))
    if not tap_element(text="Cancelar", retry=2, wait_after=1.0):
        if not tap_element(text="Fechar", retry=2, wait_after=1.0):
            press_back()
    time.sleep(1.0)

    # 4. Testar Diálogo Clínico: Amamentação & MELA
    print("Testando card: Amamentação & MELA...")
    tap_element(text="Amamentação", contains=True, retry=3, wait_after=1.5)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test04_04_lam_dialog.png"))
    if not tap_element(text="Cancelar", retry=2, wait_after=1.0):
        if not tap_element(text="Fechar", retry=2, wait_after=1.0):
            press_back()
    time.sleep(1.0)

    # 5. Testar Diálogo Clínico: Climatério & Menopausa
    print("Testando card: Climatério & Menopausa...")
    tap_element(text="Climatério", contains=True, retry=3, wait_after=1.5)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test04_05_mrs_dialog.png"))
    if not tap_element(text="Cancelar", retry=2, wait_after=1.0):
        if not tap_element(text="Fechar", retry=2, wait_after=1.0):
            press_back()
    time.sleep(1.0)

    # 6. Modo Gestante Completo
    print("Entrando no Modo Gestante...")
    tap_element(text="Modo Gestante", contains=True, retry=3, wait_after=2.0)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test04_06_pregnancy_inactive.png"))

    # Ativar modo gestante
    print("Ativando Modo Gestante...")
    if tap_element(text="Ativar Modo Gestante", retry=3, wait_after=1.5):
        take_screenshot(os.path.join(EVIDENCE_DIR, "test04_07_pregnancy_setup_dialog.png"))
        # Confirmar no diálogo de DUM
        tap_element(text="Confirmar", retry=3, wait_after=2.0)

    take_screenshot(os.path.join(EVIDENCE_DIR, "test04_08_pregnancy_active_evolution.png"))

    # Alternar para aba Contrações
    print("Testando Aba Contrações...")
    tap_element(text="Contrações", retry=3, wait_after=1.5)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test04_09_pregnancy_contractions_tab.png"))
    
    # Iniciar temporizador de contração se houver botão
    tap_element(text="Iniciar Contração", contains=True, retry=2, wait_after=1.0)
    time.sleep(3.0)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test04_10_pregnancy_contraction_timing.png"))
    tap_element(text="Parar Contração", contains=True, retry=2, wait_after=1.0)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test04_11_pregnancy_contraction_recorded.png"))

    # Alternar para aba Cuidados
    print("Testando Aba Cuidados...")
    tap_element(text="Cuidados", retry=3, wait_after=1.5)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test04_12_pregnancy_care_tab.png"))
    swipe(540, 1500, 540, 700, 400)
    time.sleep(1.0)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test04_13_pregnancy_care_scrolled.png"))

    # Voltar para o Bem-Estar
    press_back()
    time.sleep(1.5)

    # 7. Alarme da Pílula Anticoncepcional
    print("Entrando no Alarme da Pílula...")
    swipe(540, 1600, 540, 1000, 300)
    time.sleep(1.0)
    tap_element(text="Alarme da Pílula", contains=True, retry=3, wait_after=2.0)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test04_14_pill_screen.png"))

    # Registrar dose de hoje
    print("Registrando dose de pílula tomada...")
    if tap_element(text="Tomei a pílula", contains=True, retry=2, wait_after=1.5):
        take_screenshot(os.path.join(EVIDENCE_DIR, "test04_15_pill_dose_taken.png"))
    
    swipe(540, 1600, 540, 900, 400)
    time.sleep(1.0)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test04_16_pill_settings_scrolled.png"))

    press_back()
    time.sleep(1.5)

    # 8. Mandala Lunar
    print("Entrando na Mandala Lunar...")
    swipe(540, 1600, 540, 800, 300)
    time.sleep(1.0)
    tap_element(text="Mandala Lunar", contains=True, retry=3, wait_after=2.0)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test04_17_lunar_mandala.png"))

    # Alternar para visualização em Lista
    print("Alternando para lista de ciclos lunares...")
    tap_element(text="Lista", contains=True, retry=3, wait_after=1.5)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test04_18_lunar_list_view.png"))

    press_back()
    time.sleep(1.5)

    # 9. Pasta de Exames
    print("Entrando na Pasta de Exames...")
    swipe(540, 1000, 540, 1600, 300) # Voltar scroll
    time.sleep(1.0)
    tap_element(text="Pasta de Exames", contains=True, retry=3, wait_after=2.0)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test04_19_exams_screen.png"))
    swipe(540, 1500, 540, 900, 300)
    time.sleep(1.0)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test04_20_exams_screen_scrolled.png"))

    press_back()
    time.sleep(1.5)

    print("=== TESTE 04 CONCLUÍDO COM SUCESSO ===")

finally:
    recorder.stop(video_path)
    print("Gravação de vídeo finalizada e salva em:", video_path)
