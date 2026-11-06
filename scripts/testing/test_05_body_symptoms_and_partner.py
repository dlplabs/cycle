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

video_path = os.path.join(VIDEO_DIR, "test_05_body_symptoms_and_partner.mp4")

print("=== INICIANDO TESTE 05: MAPA CORPORAL DE SINTOMAS E MODO PARCEIRO(A) ===")

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
    take_screenshot(os.path.join(EVIDENCE_DIR, "test05_01_wellness.png"))

    # 2. Entrar em 'Sintomas por Região do Corpo'
    print("Entrando em Sintomas por Região do Corpo...")
    swipe(540, 1600, 540, 1000, 300)
    time.sleep(1.0)
    tap_element(text="Sintomas por Região", contains=True, retry=3, wait_after=2.0)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test05_02_body_symptoms_screen.png"))

    # Testar seleção de filtros por região corporal
    print("Filtrando regiões corporais...")
    tap_element(text="Cabeça", retry=2, wait_after=1.0)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test05_03_filter_head.png"))

    tap_element(text="Pelve", retry=2, wait_after=1.0)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test05_04_filter_pelvis.png"))

    tap_element(text="Todas", retry=2, wait_after=1.0)

    # 3. Adicionar novo sintoma corporal via Floating Action Button (+)
    print("Abrindo diálogo de adição de sintoma corporal...")
    tap(960, 2150)
    time.sleep(1.5)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test05_05_add_symptom_dialog.png"))

    # Selecionar região Pelve no diálogo
    tap_element(text="Pelve", retry=2, wait_after=1.0)

    # Digitar nome do sintoma
    root = dump_ui()
    el = find_element(root, text="Nome do Sintoma", contains=True)
    if el:
        (cx, cy), _, _ = el
        tap(cx, cy)
        time.sleep(0.5)
        type_text("Colica\\ pelvica\\ moderada")
        time.sleep(1.0)

    # Selecionar intensidade Intensa
    tap_element(text="Intensa", retry=2, wait_after=1.0)

    # Digitar observação
    el = find_element(dump_ui(), text="Observações", contains=True)
    if el:
        (cx, cy), _, _ = el
        tap(cx, cy)
        time.sleep(0.5)
        type_text("Iniciou\\ apos\\ caminhada")
        time.sleep(1.0)

    # Fechar teclado
    press_back()
    time.sleep(0.5)

    take_screenshot(os.path.join(EVIDENCE_DIR, "test05_06_symptom_dialog_filled.png"))

    # Clicar em "Adicionar"
    print("Salvando sintoma corporal...")
    tap_element(text="Adicionar", retry=3, wait_after=2.0)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test05_07_symptom_added_in_list.png"))

    # Voltar para a tela Bem-Estar pelo botão Voltar da barra superior
    tap_element(content_desc="Voltar", contains=True, retry=2, wait_after=1.5)

    # 4. Entrar em 'Acesso para Parceiro'
    print("Entrando no Modo Parceiro(a)...")
    swipe(540, 1600, 540, 800, 300)
    time.sleep(1.0)
    tap_element(text="Acesso para Parceiro", contains=True, retry=3, wait_after=2.0)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test05_08_partner_screen.png"))

    # Clicar no FAB (+) para criar convite
    print("Abrindo diálogo de convite do parceiro...")
    tap(960, 2150)
    time.sleep(1.5)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test05_09_invite_partner_dialog.png"))

    # Digitar nome do parceiro
    el = find_element(dump_ui(), text="Nome do Parceiro(a)", contains=True)
    if el:
        (cx, cy), _, _ = el
        tap(cx, cy)
        time.sleep(0.5)
        type_text("Lucas\\ Oliveira")
        time.sleep(1.0)

    # Digitar email
    el = find_element(dump_ui(), text="E-mail", contains=True)
    if el:
        (cx, cy), _, _ = el
        tap(cx, cy)
        time.sleep(0.5)
        type_text("lucas.oliveira@teste.com")
        time.sleep(1.0)

    press_back()
    time.sleep(0.5)

    take_screenshot(os.path.join(EVIDENCE_DIR, "test05_10_invite_dialog_filled.png"))

    # Clicar em "Gerar Convite"
    print("Gerando convite para o parceiro...")
    tap_element(text="Gerar Convite", retry=3, wait_after=2.0)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test05_11_invite_created_in_list.png"))

    # Voltar para a tela Bem-Estar pelo botão Voltar
    tap_element(content_desc="Voltar", contains=True, retry=2, wait_after=1.5)

    print("=== TESTE 05 CONCLUÍDO COM SUCESSO ===")

finally:
    recorder.stop(video_path)
    print("Gravação de vídeo finalizada e salva em:", video_path)
