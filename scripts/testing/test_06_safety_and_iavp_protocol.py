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

video_path = os.path.join(VIDEO_DIR, "test_06_safety_and_iavp_protocol.mp4")

print("=== INICIANDO TESTE 06: MÓDULO DE SEGURANÇA PESSOAL E PROTOCOLO IAVP ===")

recorder = ScreenRecorder()
recorder.start()
print("Gravação de vídeo iniciada:", video_path)

try:
    # 1. Garantir que estamos em Segurança Pessoal
    root = dump_ui()
    if not find_element(root, text="Segurança Pessoal", contains=True):
        print("Navegando para Segurança...")
        tap_element(text="Hoje", retry=2, wait_after=1.0)
        tap_element(text="Segurança", contains=True, retry=3, wait_after=2.0)

    take_screenshot(os.path.join(EVIDENCE_DIR, "test06_01_personal_safety_screen.png"))

    # 2. Rolar até o formulário do Círculo de Confiança
    print("Rolando até o formulário de contatos...")
    swipe(540, 1800, 540, 1200, 300)
    time.sleep(1.0)

    # Tocar no campo Nome
    print("Preenchendo nome do contato de confiança...")
    tap(300, 1300)
    time.sleep(0.5)
    type_text("Camila\\ Santos")
    time.sleep(0.8)

    # Tocar no campo Telefone
    print("Preenchendo telefone do contato...")
    tap(730, 1300)
    time.sleep(0.5)
    type_text("11987654321")
    time.sleep(0.8)

    # Fechar teclado com Escape sem sair da tela
    run_adb(["shell", "input", "keyevent", "111"])
    time.sleep(0.5)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test06_02_contact_filled.png"))

    # Adicionar ao círculo
    print("Salvando contato no Círculo de Confiança...")
    tap_element(text="Adicionar ao Círculo de Confiança", contains=True, retry=3, wait_after=1.5)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test06_03_contact_added.png"))

    # 3. Personalizar mensagem de SOS
    print("Personalizando mensagem de SOS...")
    tap_element(text="Personalizar texto SOS", contains=True, retry=3, wait_after=1.0)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test06_04_sos_customization_opened.png"))

    # Campo de mensagem SOS
    el_sos = find_element(dump_ui(), text="Mensagem de Alerta SOS", contains=True)
    if el_sos:
        (cx, cy), _, _ = el_sos
        tap(cx, cy)
        time.sleep(0.5)
        type_text("\\ Preciso\\ de\\ ajuda\\ urgente.")
        time.sleep(0.5)

    run_adb(["shell", "input", "keyevent", "111"])
    time.sleep(0.5)
    tap_element(text="Salvar Mensagem", contains=True, retry=2, wait_after=1.0)

    # 4. Rolar para o Pilar de Monitoramento em Trânsito (Safe Walk)
    print("Rolando para o Pilar de Proteção em Trânsito...")
    swipe(540, 1800, 540, 1000, 300)
    time.sleep(1.0)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test06_05_safe_walk_section.png"))

    # Iniciar monitoramento silencioso
    print("Iniciando monitoramento silencioso (30 min)...")
    tap_element(text="Iniciar Monitoramento Silencioso", contains=True, retry=3, wait_after=2.0)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test06_06_safe_walk_active.png"))

    # Encerrar com chegada segura
    print("Confirmando chegada segura...")
    tap_element(text="Cheguei Bem (Encerrar Trajeto)", contains=True, retry=3, wait_after=1.5)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test06_07_safe_walk_ended.png"))

    # 5. Rolar para a Camuflagem Rápida
    print("Rolando para Camuflagem Rápida...")
    swipe(540, 1800, 540, 1000, 300)
    time.sleep(1.0)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test06_08_camouflage_section.png"))

    print("Ativando Tela de Camuflagem Rápida...")
    tap_element(text="Ativar Tela de Camuflagem Rápida", contains=True, retry=2, wait_after=1.5)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test06_09_disguise_hydration.png"))

    # Sair da camuflagem
    print("Saindo do modo camuflado...")
    tap(540, 1200)
    time.sleep(1.5)

    # 6. Rolar para o Pilar IAVP
    print("Rolando para o Pilar de Avaliação Preventiva (IAVP)...")
    swipe(540, 1800, 540, 1200, 300)
    time.sleep(1.0)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test06_10_iavp_entry.png"))

    print("Iniciando Questionário IAVP (13 Etapas)...")
    tap_element(text="Iniciar Avaliação Preventiva", contains=True, retry=3, wait_after=2.0)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test06_11_iavp_question_01.png"))

    # Etapa 1: Não
    tap(760, 855)
    time.sleep(0.3)
    tap(540, 1080)
    time.sleep(0.8)

    # Etapa 2: Sim (para indicar risco moderado/elevado e gerar diagnóstico)
    tap(320, 855)
    time.sleep(0.3)
    tap(540, 1080)
    time.sleep(0.8)

    # Etapa 3: Testar Saída Rápida de Emergência no Questionário
    print("Testando Saída Rápida emergencial no questionário...")
    tap_element(text="Saída Rápida", contains=True, retry=2, wait_after=1.5)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test06_12_iavp_emergency_exit.png"))
    # Sair do disfarce
    tap(540, 1200)
    time.sleep(1.0)

    # Responder etapas 3 até 13
    print("Completando as 13 etapas do questionário IAVP...")
    for _ in range(3, 14):
        tap(760, 855) # Não
        time.sleep(0.3)
        tap(540, 1080) # Próxima ou Ver Diagnóstico
        time.sleep(0.8)

    time.sleep(2.0)
    print("Diagnóstico IAVP alcançado!")
    take_screenshot(os.path.join(EVIDENCE_DIR, "test06_13_iavp_result_fixed.png"))

    # Rolar tela de resultado para conferir os canais de apoio com layout corrigido
    swipe(540, 1800, 540, 800, 300)
    time.sleep(1.0)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test06_14_iavp_resources.png"))

    # Destruir histórico e fechar com segurança
    print("Destruindo histórico do questionário com segurança...")
    tap_element(text="Destruir Histórico e Fechar", contains=True, retry=2, wait_after=2.0)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test06_15_after_destroy.png"))

    # Retornar à Dashboard
    print("Retornando para a tela inicial...")
    tap_element(content_desc="Voltar", retry=2, wait_after=1.5)
    take_screenshot(os.path.join(EVIDENCE_DIR, "test06_16_back_to_dashboard.png"))

    print("=== TESTE 06 CONCLUÍDO COM SUCESSO ===")

finally:
    recorder.stop(video_path)
    print("Gravação de vídeo finalizada e salva em:", video_path)
