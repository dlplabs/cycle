#!/usr/bin/env python3
"""
Gera os gráficos oficiais da ficha do Google Play Console para o app Cycle.
Tamanhos exigidos pela Google Play Console:
- Ícone de alta resolução: 512x512 PNG 32-bit
- Gráfico de recursos (feature graphic): 1024x500 PNG
- Capturas de tela (Telefone): 1080x1920 (9:16)
- Capturas de tela (Tablet 7"): 1920x1080 (16:9)
- Capturas de tela (Tablet 10"): 1920x1080 (16:9)
"""

import os
import math
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ImageFilter

BASE_DIR = Path(__file__).resolve().parents[2]
OUT_GRAFICOS = BASE_DIR / "docs" / "google-play" / "graficos"
OUT_STORE = BASE_DIR / "store_assets"
FONTS_DIR = BASE_DIR / "app" / "src" / "main" / "res" / "font"
BRAND_MARK = BASE_DIR / "docs" / "rebranding" / "brand_mark_official.png"

OUT_GRAFICOS.mkdir(parents=True, exist_ok=True)
OUT_STORE.mkdir(parents=True, exist_ok=True)

# Cores oficiais Wellness Premium
BG = (249, 247, 246)                  # OffWhiteBackground #F9F7F6
SURFACE = (255, 255, 255)             # SurfaceCard #FFFFFF
CARD_BG = (252, 250, 249)             # Card Tint
DEEP_PLUM = (74, 43, 77)              # #4A2B4D Primária e Títulos
DEEP_PLUM_LIGHT = (110, 69, 114)      # #6E4572
TEXT_PRIMARY = (44, 44, 44)           # #2C2C2C
TEXT_SECONDARY = (112, 112, 112)      # #707070
BORDER_COLOR = (237, 232, 229)        # #EDE8E5
DANGER_COLOR = (186, 26, 26)          # Error / Excluir conta

# Fases do Ciclo
MENSTRUAL = (208, 124, 112)           # Terracotta #D07C70
FOLLICULAR = (141, 176, 148)          # Sage #8DB094
OVULATORY = (244, 184, 134)           # Peach #F4B886
LUTEAL = (188, 166, 206)              # Lavender #BCA6CE

def get_font(size, bold=False, serif=False):
    font_path = None
    if serif:
        font_path = FONTS_DIR / ("playfair_display_bold.ttf" if bold else "playfair_display_regular.ttf")
    else:
        font_path = FONTS_DIR / ("inter_medium.ttf" if bold else "inter_regular.ttf")
    
    if font_path.exists():
        try:
            return ImageFont.truetype(str(font_path), size)
        except Exception:
            pass
    return ImageFont.load_default()

def rounded(draw, box, radius, fill, outline=None, width=1):
    draw.rounded_rectangle(box, radius=radius, fill=fill, outline=outline, width=width)

def save_image(img, filename):
    p_graficos = OUT_GRAFICOS / filename
    img.save(p_graficos, "PNG", optimize=True)
    print(f"Gerado: {p_graficos} ({img.size[0]}x{img.size[1]})")

# --- 1. Ícone 512x512 ---
def generate_icon():
    if BRAND_MARK.exists():
        src = Image.open(BRAND_MARK).convert("RGBA")
        icon_img = src.resize((512, 512), Image.Resampling.LANCZOS)
    else:
        icon_img = Image.new("RGBA", (512, 512), BG + (255,))
        draw = ImageDraw.Draw(icon_img)
        draw.ellipse([80, 80, 432, 432], fill=SURFACE, outline=DEEP_PLUM, width=8)
        draw.text((220, 180), "C", fill=DEEP_PLUM, font=get_font(160, bold=True, serif=True))

    save_image(icon_img, "icone-512.png")
    icon_img.save(OUT_STORE / "ic_launcher_512.png", "PNG", optimize=True)

# --- 2. Gráfico de Recursos 1024x500 ---
def generate_feature_graphic():
    img = Image.new("RGB", (1024, 500), BG)
    draw = ImageDraw.Draw(img)

    # Brilhos orgânicos de fundo
    draw.ellipse([640, -120, 1180, 420], fill=(244, 236, 240))
    draw.ellipse([-100, 220, 360, 680], fill=(238, 244, 240))

    # Textos da marca
    draw.text((80, 110), "Cycle", fill=DEEP_PLUM, font=get_font(76, bold=True, serif=True))
    draw.text((80, 210), "Acompanhe seu ciclo com ciência,", fill=DEEP_PLUM, font=get_font(26, bold=True, serif=True))
    draw.text((80, 250), "clareza e bem-estar em cada fase.", fill=DEEP_PLUM_LIGHT, font=get_font(24, serif=True))

    # Badges dos 4 pilares
    pilares = [("Nutrição", FOLLICULAR), ("Exercício", OVULATORY), ("Pele", MENSTRUAL), ("Mente", LUTEAL)]
    x = 80
    for name, col in pilares:
        rounded(draw, (x, 320, x + 120, 364), 14, col)
        draw.text((x + 20, 332), name, fill=(255, 255, 255), font=get_font(18, bold=True))
        x += 135

    draw.text((80, 430), "Privacidade total · Sem rastreio de dados de saúde", fill=TEXT_SECONDARY, font=get_font(18, bold=True))

    # Mockup do celular à direita
    px, py = 720, 60
    rounded(draw, (px, py, px + 240, py + 390), 32, (30, 26, 32))
    rounded(draw, (px + 6, py + 6, px + 234, py + 384), 26, BG)

    # Miniatura CycleWheel
    cx, cy = px + 120, py + 190
    r = 75
    bbox = [cx - r, cy - r, cx + r, cy + r]
    draw.arc(bbox, start=-90, end=-45, fill=MENSTRUAL, width=16)
    draw.arc(bbox, start=-40, end=60, fill=FOLLICULAR, width=16)
    draw.arc(bbox, start=65, end=110, fill=OVULATORY, width=16)
    draw.arc(bbox, start=115, end=265, fill=LUTEAL, width=16)

    draw.text((cx - 24, cy - 35), "14", fill=DEEP_PLUM, font=get_font(38, bold=True, serif=True))
    draw.text((cx - 36, cy + 10), "Fase Lútea", fill=DEEP_PLUM_LIGHT, font=get_font(13, bold=True))

    save_image(img, "grafico-destaque-1024x500.png")
    img.save(OUT_STORE / "feature_graphic_1024x500.png", "PNG", optimize=True)

# --- Template base para screenshots 1080x1920 ---
def create_phone_screen(app_title, subtitle=None):
    img = Image.new("RGB", (1080, 1920), BG)
    draw = ImageDraw.Draw(img)

    # Status Bar
    draw.text((64, 32), "09:41", fill=DEEP_PLUM, font=get_font(26, bold=True))
    draw.text((920, 32), "100%", fill=TEXT_SECONDARY, font=get_font(24, bold=True))

    # Header
    draw.text((64, 110), app_title, fill=DEEP_PLUM, font=get_font(52, bold=True, serif=True))
    if subtitle:
        draw.text((64, 185), subtitle, fill=TEXT_SECONDARY, font=get_font(24))

    # Bottom Navigation Bar
    draw.rectangle([0, 1780, 1080, 1920], fill=SURFACE)
    draw.line([0, 1780, 1080, 1780], fill=BORDER_COLOR, width=2)

    tabs = [("Hoje", DEEP_PLUM), ("Alívio", TEXT_SECONDARY), ("Planejar", TEXT_SECONDARY), ("Conta", TEXT_SECONDARY)]
    tab_w = 1080 // len(tabs)
    for i, (tab_name, col) in enumerate(tabs):
        tx = i * tab_w + (tab_w // 2) - 25
        draw.text((tx, 1835), tab_name, fill=col, font=get_font(24, bold=True))

    return img, draw

# --- Screenshot 1: Home / Dashboard ---
def generate_screenshot_home():
    img, draw = create_phone_screen("Olá, Daniel", "Acompanhe seu ciclo e bem-estar hoje.")

    # CycleWheel
    cx, cy = 540, 680
    r = 260
    bbox = [cx - r, cy - r, cx + r, cy + r]
    w = 40

    draw.arc(bbox, start=-90, end=-45, fill=MENSTRUAL, width=w)
    draw.arc(bbox, start=-40, end=60, fill=FOLLICULAR, width=w)
    draw.arc(bbox, start=65, end=110, fill=OVULATORY, width=w)
    draw.arc(bbox, start=115, end=265, fill=LUTEAL, width=w)

    draw.text((cx - 65, cy - 80), "14", fill=DEEP_PLUM, font=get_font(120, bold=True, serif=True))
    draw.text((cx - 100, cy + 45), "Fase Lútea", fill=DEEP_PLUM_LIGHT, font=get_font(38, bold=True, serif=True))
    draw.text((cx - 160, cy + 105), "Previsão da próxima menstruação em 14 dias", fill=TEXT_SECONDARY, font=get_font(22))

    # Card Cuidados da Fase
    rounded(draw, (64, 1060, 1016, 1260), 28, SURFACE, outline=BORDER_COLOR, width=2)
    rounded(draw, (96, 1095, 104, 1225), 4, LUTEAL)
    draw.text((128, 1100), "Cuidado da Fase Lútea", fill=DEEP_PLUM, font=get_font(32, bold=True, serif=True))
    draw.text((128, 1148), "Nutrição · Priorize alimentos ricos em magnésio e chás relaxantes.", fill=TEXT_PRIMARY, font=get_font(24))
    draw.text((128, 1195), "Fonte científica: Baker & Driver 2007 (Sleep & Cycle)", fill=LUTEAL, font=get_font(20, bold=True))

    # Botão Registrar Hoje
    rounded(draw, (64, 1310, 1016, 1420), 28, DEEP_PLUM)
    draw.text((410, 1348), "Registrar hoje", fill=(255, 255, 255), font=get_font(32, bold=True))

    # Check-in rápido
    draw.text((64, 1475), "Check-in de agora", fill=DEEP_PLUM, font=get_font(32, bold=True, serif=True))
    flows = [("Leve", False), ("Moderado", True), ("Intenso", False)]
    fx = 64
    for label, active in flows:
        bg_c = LUTEAL if active else SURFACE
        tx_c = (255, 255, 255) if active else TEXT_PRIMARY
        rounded(draw, (fx, 1530, fx + 280, 1610), 22, bg_c, outline=BORDER_COLOR, width=1)
        draw.text((fx + 75, 1552), label, fill=tx_c, font=get_font(26, bold=True))
        fx += 310

    draw.text((64, 1660), "Nível de dor: 2 de 10", fill=TEXT_SECONDARY, font=get_font(24))
    rounded(draw, (64, 1705, 1016, 1725), 10, (230, 226, 222))
    rounded(draw, (64, 1705, 300, 1725), 10, LUTEAL)

    save_image(img, "01-home-1080x1920.png")
    img.save(OUT_STORE / "screenshot_1_dashboard.png", "PNG", optimize=True)

# --- Screenshot 2: Evidências Científicas ---
def generate_screenshot_evidencias():
    img, draw = create_phone_screen("Cuidado por Fase", "Baseado em estudos científicos indexados.")

    cards = [
        ("Nutrição", "Consuma alimentos ricos em ferro e magnésio para auxiliar a reposição mineral.", "Bull 2019 · Nature Digital Medicine", FOLLICULAR),
        ("Exercício", "Treinos de força e alta energia combinam com a fase folicular.", "McNulty 2020 · Sports Medicine", OVULATORY),
        ("Pele", "Hidratação equilibrada e controle suave da barreira lipídica cutânea.", "Raghunath 2015 · Hormonal Skin Variations", MENSTRUAL),
        ("Mente & Sono", "Práticas de relaxamento e higiene do sono para modulação hormonal.", "Baker & Driver 2007 · Sleep Medicine Reviews", LUTEAL),
    ]

    y = 260
    for title, desc, source, col in cards:
        rounded(draw, (64, y, 1016, y + 300), 28, SURFACE, outline=BORDER_COLOR, width=2)
        rounded(draw, (96, y + 36, 196, y + 136), 24, col)
        draw.text((224, y + 42), title, fill=DEEP_PLUM, font=get_font(34, bold=True, serif=True))
        draw.text((224, y + 100), desc, fill=TEXT_PRIMARY, font=get_font(23))
        draw.text((224, y + 220), "Artigo: " + source, fill=col, font=get_font(21, bold=True))
        y += 330

    save_image(img, "02-evidencias-1080x1920.png")
    img.save(OUT_STORE / "screenshot_3_pillars.png", "PNG", optimize=True)

# --- Screenshot 3: Alívio SOS Cólica & Respiração 4-7-8 ---
def generate_screenshot_sos():
    img, draw = create_phone_screen("Alívio e Respiração", "Ferramentas práticas para alívio do desconforto.")

    # Card 1: Calor
    rounded(draw, (64, 260, 1016, 820), 32, SURFACE, outline=MENSTRUAL, width=2)
    draw.text((104, 305), "Bolsa de Calor Morna", fill=MENSTRUAL, font=get_font(36, bold=True, serif=True))
    draw.text((104, 365), "Apoie uma fonte de calor na região abdominal por 20 a 30 minutos.", fill=TEXT_PRIMARY, font=get_font(24))

    # Círculo Timer
    draw.ellipse([390, 440, 690, 740], fill=BG, outline=MENSTRUAL, width=8)
    draw.text((460, 560), "24:15", fill=MENSTRUAL, font=get_font(56, bold=True))
    draw.text((455, 635), "minutos restantes", fill=TEXT_SECONDARY, font=get_font(20))

    # Card 2: Respiração 4-7-8
    rounded(draw, (64, 860, 1016, 1720), 32, SURFACE, outline=LUTEAL, width=2)
    draw.text((104, 905), "Respiração Guiada 4-7-8", fill=DEEP_PLUM, font=get_font(36, bold=True, serif=True))
    draw.text((104, 965), "Pulsos vibratórios suaves guiam você de olhos fechados.", fill=TEXT_PRIMARY, font=get_font(24))

    draw.ellipse([360, 1060, 720, 1420], fill=BG, outline=LUTEAL, width=8)
    draw.text((440, 1210), "Inspire...", fill=DEEP_PLUM, font=get_font(44, bold=True, serif=True))
    draw.text((485, 1275), "4 segundos", fill=TEXT_SECONDARY, font=get_font(22))

    rounded(draw, (120, 1520, 960, 1630), 28, DEEP_PLUM)
    draw.text((400, 1558), "Parar respiração", fill=(255, 255, 255), font=get_font(32, bold=True))

    save_image(img, "03-sos-1080x1920.png")
    img.save(OUT_STORE / "screenshot_4_sos.png", "PNG", optimize=True)

# --- Screenshot 4: Previsor / Planner ---
def generate_screenshot_planner():
    img, draw = create_phone_screen("Previsor de Datas", "Consulte a fase prevista para viagens e compromissos.")

    # Calendário
    rounded(draw, (64, 260, 1016, 1020), 32, SURFACE, outline=BORDER_COLOR, width=2)
    draw.text((104, 305), "Outubro 2026", fill=DEEP_PLUM, font=get_font(36, bold=True, serif=True))

    dias = ["D", "S", "T", "Q", "Q", "S", "S"]
    for i, d in enumerate(dias):
        draw.text((120 + i * 122, 385), d, fill=TEXT_SECONDARY, font=get_font(26, bold=True))

    for row in range(5):
        for col in range(7):
            num = row * 7 + col - 3
            if 1 <= num <= 31:
                cx = 110 + col * 122
                cy = 460 + row * 95
                if num == 16:
                    rounded(draw, (cx - 10, cy - 10, cx + 66, cy + 60), 16, LUTEAL)
                    draw.text((cx + 8, cy), f"{num:02d}", fill=(255, 255, 255), font=get_font(26, bold=True))
                else:
                    draw.text((cx + 8, cy), f"{num:02d}", fill=TEXT_PRIMARY, font=get_font(26))

    # Previsão da data selecionada
    rounded(draw, (64, 1060, 1016, 1340), 32, CARD_BG, outline=LUTEAL, width=2)
    draw.text((104, 1100), "16 de Outubro de 2026", fill=DEEP_PLUM, font=get_font(34, bold=True, serif=True))
    draw.text((104, 1160), "Fase Lútea · Dia 22 do Ciclo", fill=LUTEAL, font=get_font(28, bold=True))
    draw.text((104, 1220), "• Tendência a menor tolerância ao estresse e maior necessidade de repouso.", fill=TEXT_PRIMARY, font=get_font(23))
    draw.text((104, 1265), "• Ótimo período para planejamento introspectivo e descanso.", fill=TEXT_PRIMARY, font=get_font(23))

    save_image(img, "04-planner-1080x1920.png")
    img.save(OUT_STORE / "screenshot_5_planner_pdf.png", "PNG", optimize=True)

# --- Screenshot 5: Assinatura Premium ---
def generate_screenshot_premium():
    img, draw = create_phone_screen("Cycle Premium", "Apoie o app e desbloqueie todos os recursos.")

    draw.text((64, 260), "Viva seu ciclo com total tranquilidade", fill=DEEP_PLUM, font=get_font(36, bold=True, serif=True))

    vantagens = [
        "✨ Sem nenhum anúncio em todas as telas",
        "📄 Exportação de relatório em PDF para levar ao médico",
        "📅 Previsor de datas com horizonte ampliado",
        "🔒 Privacidade e sincronização contínua na nuvem",
    ]
    vy = 330
    for v in vantagens:
        draw.text((64, vy), v, fill=TEXT_PRIMARY, font=get_font(25))
        vy += 50

    # Plano Anual (Destaque)
    rounded(draw, (64, 580, 1016, 920), 32, SURFACE, outline=FOLLICULAR, width=3)
    rounded(draw, (780, 605, 980, 655), 14, FOLLICULAR)
    draw.text((810, 617), "ECONOMIA", fill=(255, 255, 255), font=get_font(18, bold=True))

    draw.text((104, 620), "Plano Anual", fill=DEEP_PLUM, font=get_font(36, bold=True, serif=True))
    draw.text((104, 680), "Acesso completo durante o ano todo.", fill=TEXT_SECONDARY, font=get_font(24))
    draw.text((104, 760), "cycle_premium_yearly", fill=FOLLICULAR, font=get_font(24, bold=True))
    draw.text((104, 820), "Cobrança anual gerenciada com segurança pelo Google Play", fill=TEXT_SECONDARY, font=get_font(20))

    # Plano Mensal
    rounded(draw, (64, 960, 1016, 1260), 32, SURFACE, outline=BORDER_COLOR, width=2)
    draw.text((104, 1000), "Plano Mensal", fill=DEEP_PLUM, font=get_font(36, bold=True, serif=True))
    draw.text((104, 1060), "Flexibilidade para renovar todo mês.", fill=TEXT_SECONDARY, font=get_font(24))
    draw.text((104, 1140), "cycle_premium_monthly", fill=TEXT_PRIMARY, font=get_font(24, bold=True))

    # Botão Assinar
    rounded(draw, (64, 1340, 1016, 1450), 28, DEEP_PLUM)
    draw.text((360, 1378), "Assinar Cycle Premium", fill=(255, 255, 255), font=get_font(32, bold=True))

    save_image(img, "05-premium-1080x1920.png")

# --- Screenshot 6: Configurações, PDF & Excluir Conta ---
def generate_screenshot_configuracoes():
    img, draw = create_phone_screen("Configurações", "Ajustes, dados e gerenciamento da sua conta.")

    # Card Médias do ciclo
    rounded(draw, (64, 260, 1016, 600), 28, SURFACE, outline=BORDER_COLOR, width=2)
    draw.text((104, 300), "Seu ciclo", fill=DEEP_PLUM, font=get_font(32, bold=True, serif=True))

    draw.text((104, 370), "Duração média: 28 dias", fill=TEXT_PRIMARY, font=get_font(26))
    rounded(draw, (840, 355, 960, 415), 16, BG)
    draw.text((885, 368), "—  +", fill=DEEP_PLUM, font=get_font(24, bold=True))

    draw.text((104, 470), "Dias de fluxo: 5 dias", fill=TEXT_PRIMARY, font=get_font(26))
    rounded(draw, (840, 455, 960, 515), 16, BG)
    draw.text((885, 468), "—  +", fill=DEEP_PLUM, font=get_font(24, bold=True))

    # Lembrete Diário
    rounded(draw, (64, 630, 1016, 750), 24, SURFACE, outline=BORDER_COLOR, width=2)
    draw.text((104, 670), "Lembrete diário de fase", fill=DEEP_PLUM, font=get_font(28, bold=True))
    rounded(draw, (880, 665, 960, 715), 24, FOLLICULAR)

    # Botão Assinatura
    rounded(draw, (64, 790, 1016, 900), 28, DEEP_PLUM)
    draw.text((360, 828), "Assinatura sem anúncios", fill=(255, 255, 255), font=get_font(30, bold=True))

    # Botão Exportar PDF
    rounded(draw, (64, 930, 1016, 1040), 28, DEEP_PLUM)
    draw.text((340, 968), "Exportar relatório em PDF", fill=(255, 255, 255), font=get_font(30, bold=True))

    # Botão Sair
    rounded(draw, (64, 1070, 1016, 1180), 28, SURFACE, outline=DEEP_PLUM, width=2)
    draw.text((490, 1108), "Sair", fill=DEEP_PLUM, font=get_font(30, bold=True))

    # Botão Excluir Conta (Obrigatório Play Store)
    rounded(draw, (64, 1210, 1016, 1320), 28, SURFACE, outline=DANGER_COLOR, width=2)
    draw.text((420, 1248), "Excluir conta", fill=DANGER_COLOR, font=get_font(30, bold=True))
    draw.text((104, 1340), "A exclusão apaga permanentemente todos os registros e perfil.", fill=TEXT_SECONDARY, font=get_font(20))

    save_image(img, "06-configuracoes-1080x1920.png")

# --- Screenshots Tablets (1920x1080) ---
def generate_tablet_screens():
    # Tablet 7" Home
    t7 = Image.new("RGB", (1920, 1080), BG)
    draw7 = ImageDraw.Draw(t7)
    draw7.text((80, 60), "Cycle · Acompanhamento de Ciclo", fill=DEEP_PLUM, font=get_font(48, bold=True, serif=True))
    draw7.text((80, 130), "Dia 14 · Fase Lútea", fill=LUTEAL, font=get_font(32, bold=True))

    # Roda esquerda
    cx, cy = 480, 580
    r = 260
    bbox = [cx - r, cy - r, cx + r, cy + r]
    draw7.arc(bbox, start=-90, end=-45, fill=MENSTRUAL, width=40)
    draw7.arc(bbox, start=-40, end=60, fill=FOLLICULAR, width=40)
    draw7.arc(bbox, start=65, end=110, fill=OVULATORY, width=40)
    draw7.arc(bbox, start=115, end=265, fill=LUTEAL, width=40)
    draw7.text((cx - 65, cy - 70), "14", fill=DEEP_PLUM, font=get_font(110, bold=True, serif=True))

    # Cards direita
    rounded(draw7, (960, 260, 1820, 580), 32, SURFACE, outline=BORDER_COLOR, width=2)
    draw7.text((1010, 310), "Cuidados e Bem-Estar da Fase Lútea", fill=DEEP_PLUM, font=get_font(36, bold=True, serif=True))
    draw7.text((1010, 380), "• Nutrição rica em magnésio e chás relaxantes", fill=TEXT_PRIMARY, font=get_font(26))
    draw7.text((1010, 430), "• Atividades restaurativas como ioga suave", fill=TEXT_PRIMARY, font=get_font(26))
    draw7.text((1010, 480), "• Fontes científicas indexadas disponíveis a um toque", fill=TEXT_SECONDARY, font=get_font(22))

    rounded(draw7, (960, 630, 1820, 850), 32, SURFACE, outline=BORDER_COLOR, width=2)
    draw7.text((1010, 680), "Previsão do Próximo Ciclo", fill=DEEP_PLUM, font=get_font(32, bold=True, serif=True))
    draw7.text((1010, 740), "Previsão da próxima menstruação em 14 dias.", fill=TEXT_PRIMARY, font=get_font(26))

    save_image(t7, "tablet-7-home-1920x1080.png")

    # Tablet 10" Planner
    t10 = Image.new("RGB", (1920, 1080), BG)
    draw10 = ImageDraw.Draw(t10)
    draw10.text((80, 60), "Previsor de Datas Futuras", fill=DEEP_PLUM, font=get_font(48, bold=True, serif=True))
    draw10.text((80, 130), "Planejamento inteligente em sintonia com suas fases hormonais.", fill=TEXT_SECONDARY, font=get_font(28))

    rounded(draw10, (80, 240, 1080, 960), 32, SURFACE, outline=BORDER_COLOR, width=2)
    draw10.text((130, 290), "Calendário de Previsão", fill=DEEP_PLUM, font=get_font(36, bold=True, serif=True))

    rounded(draw10, (1140, 240, 1840, 960), 32, SURFACE, outline=LUTEAL, width=2)
    draw10.text((1190, 290), "Fase Prevista em Destaque", fill=LUTEAL, font=get_font(34, bold=True))
    draw10.text((1190, 360), "Fase Lútea · Dia 22 do Ciclo", fill=DEEP_PLUM, font=get_font(38, bold=True, serif=True))
    draw10.text((1190, 440), "Previsão calculada com base na média dos seus ciclos.", fill=TEXT_PRIMARY, font=get_font(26))
    draw10.text((1190, 520), "Ideal para programar descansos, reuniões e treinos.", fill=TEXT_SECONDARY, font=get_font(24))

    save_image(t10, "tablet-10-planner-1920x1080.png")

def main():
    print("Iniciando geração dos gráficos para Google Play Console...")
    generate_icon()
    generate_feature_graphic()
    generate_screenshot_home()
    generate_screenshot_evidencias()
    generate_screenshot_sos()
    generate_screenshot_planner()
    generate_screenshot_premium()
    generate_screenshot_configuracoes()
    generate_tablet_screens()
    print("Todos os gráficos foram gerados e sincronizados com sucesso!")

if __name__ == "__main__":
    main()
