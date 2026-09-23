import os
import math
from PIL import Image, ImageDraw, ImageFont, ImageFilter

out_dir_project = "/home/daniel-pinheiro/AndroidStudioProjects/Cycle/store_assets"
out_dir_artifacts = "/home/daniel-pinheiro/.cache/Google/AndroidStudio2026.1.4/projects/cycle.d4faae08/.artifacts/e7c72dd4-4c46-45ed-8bb4-e06618a87850/store_assets"

os.makedirs(out_dir_project, exist_ok=True)
os.makedirs(out_dir_artifacts, exist_ok=True)

# Botanical Brand Palette
COLOR_BG = (247, 244, 238)         # #F7F4EE Warm organic cream
COLOR_PLUM = (74, 33, 56)           # #4A2138 Deep plum/serif text
COLOR_SAGE = (118, 144, 124)       # #76907C Follicular Sage Green
COLOR_ROSE = (197, 126, 118)       # #C57E76 Menstrual Terracotta Rose
COLOR_AMBER = (225, 165, 137)      # #E1A589 Ovulatory Peach/Amber
COLOR_PURPLE = (154, 134, 168)     # #9A86A8 Luteal Lavender
COLOR_SURFACE = (255, 255, 255)
COLOR_TEXT_MUTED = (120, 110, 115)
COLOR_CARD_BG = (250, 248, 243)

def get_font(size, bold=False, serif=False):
    font_candidates = []
    if serif:
        font_candidates = [
            "/usr/share/fonts/truetype/dejavu/DejaVuSerif-Bold.ttf" if bold else "/usr/share/fonts/truetype/dejavu/DejaVuSerif.ttf",
            "/usr/share/fonts/truetype/liberation/LiberationSerif-Bold.ttf" if bold else "/usr/share/fonts/truetype/liberation/LiberationSerif-Regular.ttf",
        ]
    else:
        font_candidates = [
            "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf" if bold else "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
            "/usr/share/fonts/truetype/liberation/LiberationSans-Bold.ttf" if bold else "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf",
        ]
    for fp in font_candidates:
        if os.path.exists(fp):
            try:
                return ImageFont.truetype(fp, size)
            except Exception:
                pass
    return ImageFont.load_default()

def draw_botanical_wreath(draw, center_x, center_y, radius):
    """Draws a delicate swirling botanical wreath with moon phases in the 4 phase colors."""
    num_swirls = 18
    for i in range(num_swirls):
        angle_deg = i * (360 / num_swirls)
        rad = math.radians(angle_deg)

        # Color gradient based on angle
        if 0 <= angle_deg < 90:
            color = COLOR_SAGE
        elif 90 <= angle_deg < 180:
            color = COLOR_AMBER
        elif 180 <= angle_deg < 270:
            color = COLOR_PURPLE
        else:
            color = COLOR_ROSE

        r_start = radius * 0.75
        r_end = radius * 1.15

        # Swirling arc path points
        p1_x = center_x + r_start * math.cos(rad)
        p1_y = center_y + r_start * math.sin(rad)
        p2_x = center_x + r_end * math.cos(rad + 0.6)
        p2_y = center_y + r_end * math.sin(rad + 0.6)

        draw.line([p1_x, p1_y, p2_x, p2_y], fill=color, width=3)

        # Leaf shapes on some strands
        if i % 2 == 0:
            leaf_x = center_x + (radius * 0.95) * math.cos(rad + 0.3)
            leaf_y = center_y + (radius * 0.95) * math.sin(rad + 0.3)
            draw.ellipse([leaf_x - 6, leaf_y - 12, leaf_x + 6, leaf_y + 12], fill=color)

    # Moon phase symbols at 4 cardinal points
    # Top: Quarter Moon
    draw.ellipse([center_x - 12, center_y - radius - 20, center_x + 12, center_y - radius + 4], fill=COLOR_SAGE)
    draw.ellipse([center_x - 12, center_y - radius - 20, center_x, center_y - radius + 4], fill=COLOR_BG)

    # Right: Full Moon Dot
    draw.ellipse([center_x + radius + 10, center_y - 10, center_x + radius + 30, center_y + 10], fill=COLOR_AMBER)

    # Bottom: Half Moon
    draw.ellipse([center_x - 12, center_y + radius - 4, center_x + 12, center_y + radius + 20], fill=COLOR_PURPLE)
    draw.ellipse([center_x, center_y + radius - 4, center_x + 12, center_y + radius + 20], fill=COLOR_BG)

    # Left: Crescent Moon
    draw.ellipse([center_x - radius - 30, center_y - 10, center_x - radius - 10, center_y + 10], fill=COLOR_ROSE)
    draw.ellipse([center_x - radius - 24, center_y - 10, center_x - radius - 4, center_y + 10], fill=COLOR_BG)

# --- 1. App Icon (512x512) ---
def create_app_icon():
    img = Image.new("RGBA", (512, 512), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Off-white squircle container
    draw.rounded_rectangle([12, 12, 500, 500], radius=110, fill=(250, 248, 243, 255))

    # Botanical Wreath Ring
    draw_botanical_wreath(draw, center_x=256, center_y=256, radius=160)

    # Center white circle
    draw.ellipse([146, 146, 366, 366], fill=(255, 255, 255, 255))

    # Center serif "C"
    font_c = get_font(130, bold=True, serif=True)
    draw.text((205, 170), "C", fill=COLOR_PLUM, font=font_c)

    # Sprouting leaves attached to C
    draw.ellipse([185, 215, 215, 255], fill=COLOR_SAGE)
    draw.ellipse([175, 240, 205, 275], fill=COLOR_PLUM)

    path_proj = os.path.join(out_dir_project, "ic_launcher_512.png")
    path_art = os.path.join(out_dir_artifacts, "ic_launcher_512.png")
    img.save(path_proj, "PNG")
    img.save(path_art, "PNG")
    print(f"Created: {path_proj}")

# --- 2. Feature Graphic (1024x500) ---
def create_feature_graphic():
    img = Image.new("RGB", (1024, 500), COLOR_BG)
    draw = ImageDraw.Draw(img)

    # Decorative watercolor background splashes
    draw.ellipse([600, -100, 1150, 450], fill=(240, 233, 230))
    draw.ellipse([-100, 200, 350, 650], fill=(235, 240, 235))

    # Left Content
    font_title = get_font(68, bold=True, serif=True)
    font_sub = get_font(26, serif=True)

    draw.text((70, 120), "Cycle", fill=COLOR_PLUM, font=font_title)

    # Subtitle with line wrap
    draw.text((70, 215), "Ciclo e Menstruação:", fill=COLOR_PLUM, font=get_font(28, bold=True, serif=True))
    draw.text((70, 255), "Viva em sintonia com", fill=COLOR_PLUM, font=font_sub)
    draw.text((70, 295), "o seu corpo", fill=COLOR_PLUM, font=font_sub)

    # Bottom tagline
    draw.text((70, 420), "Acolhimento · Ciência · Privacidade", fill=COLOR_PLUM, font=get_font(22, bold=True, serif=True))

    # Wreath Emblem
    draw_botanical_wreath(draw, center_x=530, center_y=250, radius=130)
    draw.ellipse([440, 160, 620, 340], fill=COLOR_SURFACE)
    draw.text((495, 185), "C", fill=COLOR_PLUM, font=get_font(100, bold=True, serif=True))

    # Phone mockup on right
    phone_x, phone_y = 700, 80
    draw.rounded_rectangle([phone_x, phone_y, phone_x + 250, phone_y + 380], radius=32, fill=(30, 30, 30))
    draw.rounded_rectangle([phone_x + 8, phone_y + 8, phone_x + 242, phone_y + 372], radius=26, fill=COLOR_BG)

    # Phone screen content
    draw.text((phone_x + 25, phone_y + 30), "Olá, Maria", fill=COLOR_PLUM, font=get_font(22, bold=True, serif=True))

    # Small Wheel Mockup
    cx, cy = phone_x + 125, phone_y + 200
    r = 75
    bbox = [cx - r, cy - r, cx + r, cy + r]
    draw.arc(bbox, start=270, end=40, fill=COLOR_ROSE, width=20)
    draw.arc(bbox, start=40, end=150, fill=COLOR_AMBER, width=20)
    draw.arc(bbox, start=150, end=270, fill=COLOR_PURPLE, width=20)

    draw.text((cx - 35, cy - 10), "CycleWheel", fill=COLOR_PLUM, font=get_font(12, bold=True))

    path_proj = os.path.join(out_dir_project, "feature_graphic_1024x500.png")
    path_art = os.path.join(out_dir_artifacts, "feature_graphic_1024x500.png")
    img.save(path_proj, "PNG")
    img.save(path_art, "PNG")
    print(f"Created: {path_proj}")

# --- Helper for Phone Screenshots ---
def create_phone_base(title="Olá, Maria"):
    img = Image.new("RGB", (1080, 1920), COLOR_BG)
    draw = ImageDraw.Draw(img)

    # Status bar
    font_sm = get_font(26, bold=True)
    draw.text((60, 20), "09:50", fill=COLOR_PLUM, font=font_sm)
    draw.text((920, 20), "100%", fill=COLOR_PLUM, font=font_sm)

    # App Bar
    draw.text((60, 90), title, fill=COLOR_PLUM, font=get_font(42, bold=True, serif=True))

    # Bottom Nav Bar
    draw.rectangle([0, 1800, 1080, 1920], fill=COLOR_SURFACE)
    draw.line([0, 1800, 1080, 1800], fill=(230, 225, 220), width=2)

    font_nav = get_font(20, bold=True)
    draw.text((120, 1840), "🌸 Fases", fill=COLOR_TEXT_MUTED, font=font_nav)
    draw.text((440, 1840), "⭕ CycleWheel", fill=COLOR_PLUM, font=font_nav)
    draw.text((820, 1840), "✨ Pele & Mente", fill=COLOR_TEXT_MUTED, font=font_nav)

    return img, draw

# --- 3. Screenshot 1: Dashboard ---
def create_screenshot_dashboard():
    img, draw = create_phone_base("Olá, Maria")

    # Large Wreath Ring Wheel
    cx, cy = 540, 680
    r = 280
    bbox = [cx - r, cy - r, cx + r, cy + r]
    width = 38

    draw.arc(bbox, start=270, end=350, fill=COLOR_ROSE, width=width)
    draw.arc(bbox, start=350, end=80, fill=COLOR_SAGE, width=width)
    draw.arc(bbox, start=80, end=140, fill=COLOR_AMBER, width=width)
    draw.arc(bbox, start=140, end=270, fill=COLOR_PURPLE, width=width)

    # Center text
    draw.text((cx - 65, cy - 80), "14", fill=COLOR_PLUM, font=get_font(120, bold=True, serif=True))
    draw.text((cx - 120, cy + 50), "Fase Lútea", fill=COLOR_PLUM, font=get_font(40, bold=True, serif=True))

    # Action buttons
    draw.rounded_rectangle([100, 1100, 980, 1210], radius=32, fill=COLOR_ROSE)
    draw.text((360, 1140), "Alívio SOS Cólica", fill=COLOR_SURFACE, font=get_font(32, bold=True))

    draw.rounded_rectangle([100, 1250, 980, 1360], radius=32, fill=COLOR_PURPLE)
    draw.text((380, 1290), "Previsor de Datas", fill=COLOR_SURFACE, font=get_font(32, bold=True))

    # Bottom Registre Hoje Button
    draw.rounded_rectangle([60, 1660, 1020, 1770], radius=32, fill=COLOR_PLUM)
    draw.text((410, 1700), "Registrar Hoje", fill=COLOR_SURFACE, font=get_font(32, bold=True))

    path_proj = os.path.join(out_dir_project, "screenshot_1_dashboard.png")
    path_art = os.path.join(out_dir_artifacts, "screenshot_1_dashboard.png")
    img.save(path_proj, "PNG")
    img.save(path_art, "PNG")
    print(f"Created: {path_proj}")

# --- 4. Screenshot 2: Check-in ---
def create_screenshot_checkin():
    img, draw = create_phone_base("Log de Sintomas")

    # Dim overlay
    draw.rectangle([0, 0, 1080, 1920], fill=(0, 0, 0, 100))

    # BottomSheet
    draw.rounded_rectangle([0, 350, 1080, 1920], radius=40, fill=COLOR_SURFACE)
    draw.rectangle([480, 375, 600, 385], fill=(220, 215, 210))

    draw.text((60, 420), "Como você está hoje?", fill=COLOR_PLUM, font=get_font(36, bold=True, serif=True))

    # Categories
    y = 510
    sections = [
        ("Fluxo Menstrual", [("Leve", True), ("Moderado", False), ("Intenso", False)], COLOR_ROSE),
        ("Humor", [("😊 Calma", True), ("⚡ Energia", False), ("😴 Cansaço", False)], COLOR_PURPLE),
        ("Sintomas Físicos", [("🩸 Cólica", True), ("🤕 Dor de cabeça", True), ("🎈 Inchaço", False)], COLOR_AMBER)
    ]
    for sec_title, chips, col in sections:
        draw.text((60, y), sec_title, fill=COLOR_TEXT_MUTED, font=get_font(26, bold=True))
        y += 45
        x = 60
        for label, sel in chips:
            bg_col = col if sel else COLOR_CARD_BG
            tx_col = COLOR_SURFACE if sel else COLOR_PLUM
            draw.rounded_rectangle([x, y, x + 260, y + 65], radius=22, fill=bg_col)
            draw.text((x + 30, y + 16), label, fill=tx_col, font=get_font(24, bold=True))
            x += 280
        y += 105

    # Notes field
    draw.text((60, y), "Notas do Dia", fill=COLOR_TEXT_MUTED, font=get_font(26, bold=True))
    y += 45
    draw.rounded_rectangle([60, y, 1020, y + 160], radius=22, fill=COLOR_CARD_BG, outline=(220, 215, 210), width=2)
    draw.text((90, y + 30), "Dia tranquilo, mantendo hidratação constante...", fill=COLOR_TEXT_MUTED, font=get_font(24))

    # Save Button
    draw.rounded_rectangle([60, 1660, 1020, 1770], radius=32, fill=COLOR_PLUM)
    draw.text((400, 1700), "Salvar Registro", fill=COLOR_SURFACE, font=get_font(32, bold=True))

    path_proj = os.path.join(out_dir_project, "screenshot_2_checkin.png")
    path_art = os.path.join(out_dir_artifacts, "screenshot_2_checkin.png")
    img.save(path_proj, "PNG")
    img.save(path_art, "PNG")
    print(f"Created: {path_proj}")

# --- 5. Screenshot 3: Pilares ---
def create_screenshot_pillars():
    img, draw = create_phone_base("Pilares de Bem-Estar")

    draw.text((60, 180), "Pilares de Bem-Estar", fill=COLOR_PLUM, font=get_font(42, bold=True, serif=True))

    y = 260
    pillars = [
        ("Nutrição", "Priorize alimentos ricos em magnésio e chá de camomila.", COLOR_SAGE),
        ("Exercício", "Movimentos suaves, ioga e alongamentos são recomendados.", COLOR_AMBER),
        ("Pele", "Hidratação intensa e produtos suaves para pele sensível.", COLOR_ROSE),
        ("Mente", "Momentos de repouso para redução da irritabilidade e ansiedade.", COLOR_PURPLE)
    ]
    for p_title, p_desc, col in pillars:
        draw.rounded_rectangle([60, y, 1020, y + 250], radius=28, fill=COLOR_SURFACE, outline=(230, 225, 220), width=2)
        draw.rounded_rectangle([90, y + 25, 240, y + 115], radius=20, fill=COLOR_CARD_BG)
        draw.text((115, y + 45), p_title[:2], font=get_font(36))
        draw.text((270, y + 35), p_title, fill=col, font=get_font(32, bold=True, serif=True))
        draw.text((270, y + 90), p_desc, fill=COLOR_TEXT_MUTED, font=get_font(24))
        y += 280

    path_proj = os.path.join(out_dir_project, "screenshot_3_pillars.png")
    path_art = os.path.join(out_dir_artifacts, "screenshot_3_pillars.png")
    img.save(path_proj, "PNG")
    img.save(path_art, "PNG")
    print(f"Created: {path_proj}")

# --- 6. Screenshot 4: SOS ---
def create_screenshot_sos():
    img, draw = create_phone_base("Alívio SOS")

    draw.text((60, 180), "Alívio SOS & Respiração", fill=COLOR_PLUM, font=get_font(40, bold=True, serif=True))

    # Heat Card
    draw.rounded_rectangle([60, 270, 1020, 780], radius=28, fill=COLOR_CARD_BG, outline=COLOR_ROSE, width=2)
    draw.text((100, 310), "🔥 Bolsa de Calor para Cólica", fill=COLOR_ROSE, font=get_font(32, bold=True))
    draw.text((100, 365), "Aplique calor morno na região abdominal por 15 min.", fill=COLOR_TEXT_MUTED, font=get_font(24))

    draw.ellipse([390, 440, 690, 740], fill=COLOR_SURFACE, outline=COLOR_ROSE, width=6)
    draw.text((460, 560), "12:45", fill=COLOR_ROSE, font=get_font(48, bold=True))

    # Breathing Card
    draw.rounded_rectangle([60, 830, 1020, 1480], radius=28, fill=COLOR_CARD_BG, outline=COLOR_PURPLE, width=2)
    draw.text((100, 870), "🧘 Respiração Guiada 4-7-8", fill=COLOR_PURPLE, font=get_font(32, bold=True))
    draw.text((100, 925), "Com vibração tátil suave em cada fase.", fill=COLOR_TEXT_MUTED, font=get_font(24))

    draw.ellipse([360, 1000, 720, 1360], fill=COLOR_SURFACE, outline=COLOR_PURPLE, width=8)
    draw.text((455, 1155), "Inspire...", fill=COLOR_PURPLE, font=get_font(38, bold=True))

    path_proj = os.path.join(out_dir_project, "screenshot_4_sos.png")
    path_art = os.path.join(out_dir_artifacts, "screenshot_4_sos.png")
    img.save(path_proj, "PNG")
    img.save(path_art, "PNG")
    print(f"Created: {path_proj}")

# --- 7. Screenshot 5: Planner ---
def create_screenshot_planner():
    img, draw = create_phone_base("Previsor")

    draw.text((60, 180), "Previsor de Datas Futuras", fill=COLOR_PLUM, font=get_font(40, bold=True, serif=True))

    draw.rounded_rectangle([60, 270, 1020, 700], radius=28, fill=COLOR_SURFACE, outline=(230, 225, 220), width=2)
    draw.text((100, 310), "📅 Escolha uma data futura:", fill=COLOR_PLUM, font=get_font(28, bold=True))

    draw.rounded_rectangle([100, 370, 980, 460], radius=20, fill=COLOR_CARD_BG)
    draw.text((140, 400), "Data: 15 de Novembro de 2024", fill=COLOR_PURPLE, font=get_font(26, bold=True))

    draw.rounded_rectangle([100, 490, 980, 640], radius=20, fill=(234, 246, 238))
    draw.text((140, 520), "Previsão: Dia 12 da Fase Folicular 🌿", fill=COLOR_SAGE, font=get_font(28, bold=True))
    draw.text((140, 570), "Disposição ideal e alta energia para viagens.", fill=COLOR_TEXT_MUTED, font=get_font(22))

    # PDF Section
    draw.text((60, 760), "Relatório Médico em PDF", fill=COLOR_PLUM, font=get_font(36, bold=True, serif=True))
    draw.rounded_rectangle([60, 820, 1020, 1420], radius=28, fill=COLOR_SURFACE, outline=(230, 225, 220), width=2)
    draw.text((100, 860), "📄 Histórico para Consulta Ginecológica", fill=COLOR_PLUM, font=get_font(28, bold=True))
    draw.text((100, 910), "Exporte um PDF completo com a média dos seus ciclos e sintomas.", fill=COLOR_TEXT_MUTED, font=get_font(22))

    draw.rounded_rectangle([140, 980, 940, 1360], radius=16, fill=COLOR_CARD_BG)
    draw.rectangle([180, 1010, 900, 1050], fill=COLOR_PLUM)
    draw.text((200, 1020), "RELATÓRIO MÉDICO - CYCLE APP", fill=COLOR_SURFACE, font=get_font(18, bold=True))
    draw.text((200, 1080), "• Duração média do ciclo: 28 dias", fill=COLOR_PLUM, font=get_font(22))
    draw.text((200, 1130), "• Histórico de fluxo: Moderado / Leve", fill=COLOR_PLUM, font=get_font(22))
    draw.text((200, 1180), "• Frequência de sintomas: Cólica (30%)", fill=COLOR_PLUM, font=get_font(22))

    draw.rounded_rectangle([60, 1500, 1020, 1610], radius=32, fill=COLOR_PLUM)
    draw.text((360, 1535), "Exportar Relatório em PDF", fill=COLOR_SURFACE, font=get_font(28, bold=True))

    path_proj = os.path.join(out_dir_project, "screenshot_5_planner_pdf.png")
    path_art = os.path.join(out_dir_artifacts, "screenshot_5_planner_pdf.png")
    img.save(path_proj, "PNG")
    img.save(path_art, "PNG")
    print(f"Created: {path_proj}")

if __name__ == "__main__":
    create_app_icon()
    create_feature_graphic()
    create_screenshot_dashboard()
    create_screenshot_checkin()
    create_screenshot_pillars()
    create_screenshot_sos()
    create_screenshot_planner()
    print("ALL BOTANICAL BRAND ASSETS GENERATED SUCCESSFULLY!")
