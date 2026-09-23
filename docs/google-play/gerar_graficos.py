"""Gera os gráficos da ficha da Play Console nos tamanhos exigidos."""

from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

OUT = Path(__file__).resolve().parent / "graficos"
FONT = "/usr/share/fonts/truetype/noto/NotoSans-Regular.ttf"
FONT_BOLD = "/usr/share/fonts/truetype/noto/NotoSans-Bold.ttf"

BG = (250, 249, 246)
SURFACE = (255, 255, 255)
INK = (28, 27, 31)
MUTED = (92, 88, 96)
RED = (179, 74, 74)
GREEN = (61, 120, 88)
AMBER = (184, 115, 38)
PURPLE = (101, 83, 138)
RED_SOFT = (252, 234, 234)
GREEN_SOFT = (234, 246, 238)
AMBER_SOFT = (253, 244, 231)
PURPLE_SOFT = (243, 239, 251)


def font(size, bold=False):
    return ImageFont.truetype(FONT_BOLD if bold else FONT, size)


def ring(draw, box, width=28):
    colors = ((270, 360, RED), (0, 90, GREEN), (90, 180, AMBER), (180, 270, PURPLE))
    for start, end, color in colors:
        draw.arc(box, start=start, end=end, fill=color, width=width)


def rounded(draw, box, radius, fill):
    draw.rounded_rectangle(box, radius=radius, fill=fill)


def save(image, name):
    OUT.mkdir(parents=True, exist_ok=True)
    path = OUT / name
    image.save(path, "PNG", optimize=True)
    print(f"{name} {image.size[0]}x{image.size[1]}")


def icon():
    image = Image.new("RGBA", (512, 512), BG + (255,))
    draw = ImageDraw.Draw(image)
    ring(draw, (76, 76, 436, 436), width=36)
    save(image, "icone-512.png")


def feature_graphic():
    image = Image.new("RGB", (1024, 500), BG)
    draw = ImageDraw.Draw(image)
    ring(draw, (64, 70, 424, 430), width=28)
    draw.text((480, 150), "Cycle", font=font(92, True), fill=INK)
    draw.text((480, 270), "Seu ciclo, com clareza.", font=font(36), fill=GREEN)
    draw.text((480, 340), "Fases, bem-estar e registro diário.", font=font(26), fill=MUTED)
    save(image, "grafico-destaque-1024x500.png")


def status_bar(draw, width):
    draw.text((48, 36), "09:41", font=font(28, True), fill=INK)
    draw.text((width - 180, 36), "LTE  100%", font=font(24), fill=MUTED)


def screen(name, paint):
    image = Image.new("RGB", (1080, 1920), BG)
    draw = ImageDraw.Draw(image)
    status_bar(draw, 1080)
    paint(draw)
    save(image, name)


def home(draw):
    draw.text((48, 120), "Olá", font=font(28), fill=MUTED)
    draw.text((48, 164), "Dia 12", font=font(72, True), fill=INK)
    rounded(draw, (48, 280, 420, 360), 24, GREEN_SOFT)
    draw.text((72, 300), "Fase folicular", font=font(28, True), fill=GREEN)
    ring(draw, (250, 430, 830, 1010), width=42)
    draw.text((400, 650), "12", font=font(88, True), fill=INK)
    draw.text((430, 760), "de 28", font=font(32), fill=MUTED)
    rounded(draw, (48, 1080, 1032, 1280), 28, SURFACE)
    draw.text((80, 1112), "Movimento", font=font(32, True), fill=INK)
    draw.text((80, 1170), "Treinos de força combinam com esta fase.", font=font(28), fill=MUTED)
    draw.text((80, 1220), "McNulty 2020", font=font(26, True), fill=GREEN)
    rounded(draw, (48, 1320, 1032, 1460), 28, GREEN)
    draw.text((360, 1368), "Registrar hoje", font=font(32, True), fill=(255, 255, 255))
    draw.text((48, 1520), "Check-in rápido", font=font(32, True), fill=INK)
    for index, label in enumerate(("Leve", "Moderado", "Intenso")):
        x = 48 + index * 250
        rounded(draw, (x, 1590, x + 220, 1680), 40, SURFACE)
        draw.text((x + 48, 1614), label, font=font(26), fill=INK)
    draw.text((48, 1740), "Dor  3 / 10", font=font(28), fill=MUTED)
    draw.rounded_rectangle((48, 1800, 1032, 1824), radius=12, fill=(230, 226, 220))
    draw.rounded_rectangle((48, 1800, 340, 1824), radius=12, fill=GREEN)


def sos(draw):
    draw.text((48, 140), "Alívio SOS", font=font(64, True), fill=INK)
    draw.text((48, 240), "Calor local por 25 minutos.", font=font(34), fill=MUTED)
    draw.text((48, 300), "O app só marca o tempo. Não controla aparelhos.", font=font(28), fill=MUTED)
    rounded(draw, (48, 400, 1032, 760), 32, RED_SOFT)
    draw.text((80, 450), "Timer", font=font(28), fill=RED)
    draw.text((80, 520), "25:00", font=font(96, True), fill=RED)
    draw.text((80, 660), "Entre 20 e 30 minutos", font=font(28), fill=INK)
    rounded(draw, (48, 820, 1032, 980), 28, RED)
    draw.text((330, 868), "Iniciar timer", font=font(36, True), fill=(255, 255, 255))
    rounded(draw, (48, 1040, 1032, 1400), 32, SURFACE)
    draw.text((80, 1090), "Respiração 4-7-8", font=font(36, True), fill=INK)
    draw.text((80, 1160), "Inspire 4 s · segure 7 s · solte 8 s", font=font(30), fill=MUTED)
    draw.text((80, 1240), "Cada etapa vibra de um jeito.", font=font(30), fill=MUTED)
    rounded(draw, (80, 1288, 520, 1376), 28, PURPLE)
    draw.text((180, 1310), "Começar", font=font(32, True), fill=(255, 255, 255))


def planner(draw):
    draw.text((48, 140), "Planner", font=font(64, True), fill=INK)
    draw.text((48, 240), "Fase prevista para uma data futura.", font=font(32), fill=MUTED)
    rounded(draw, (48, 340, 1032, 980), 32, SURFACE)
    draw.text((80, 380), "Setembro 2026", font=font(32, True), fill=INK)
    days = ["D", "S", "T", "Q", "Q", "S", "S"]
    for index, label in enumerate(days):
        draw.text((90 + index * 130, 470), label, font=font(26, True), fill=MUTED)
    for row in range(5):
        for col in range(7):
            number = row * 7 + col - 1
            if number < 1 or number > 30:
                continue
            x = 72 + col * 130
            y = 540 + row * 80
            if number == 18:
                rounded(draw, (x - 8, y - 8, x + 72, y + 52), 16, PURPLE)
                color = (255, 255, 255)
            else:
                color = INK
            draw.text((x + 8, y), f"{number:02d}", font=font(28, True), fill=color)
    rounded(draw, (48, 1040, 1032, 1280), 32, PURPLE_SOFT)
    draw.text((80, 1088), "18 de setembro", font=font(28), fill=PURPLE)
    draw.text((80, 1150), "Fase lútea · dia 22", font=font(40, True), fill=INK)
    draw.text((80, 1220), "No plano grátis, só o próximo ciclo.", font=font(28), fill=MUTED)
    rounded(draw, (48, 1700, 1032, 1840), 16, (232, 230, 226))
    draw.text((360, 1748), "Anúncio", font=font(28), fill=MUTED)


def paywall(draw):
    draw.text((48, 160), "Cycle Premium", font=font(64, True), fill=INK)
    draw.text((48, 270), "Sem anúncios, planner estendido", font=font(34), fill=MUTED)
    draw.text((48, 330), "e relatório em PDF para a consulta.", font=font(34), fill=MUTED)
    rounded(draw, (48, 460, 1032, 760), 32, SURFACE)
    draw.text((80, 510), "Mensal", font=font(32), fill=MUTED)
    draw.text((80, 570), "cycle_premium_monthly", font=font(28), fill=GREEN)
    draw.text((80, 660), "Preço definido na Play Console", font=font(28), fill=MUTED)
    rounded(draw, (48, 820, 1032, 1120), 32, GREEN_SOFT)
    draw.text((80, 870), "Anual", font=font(32), fill=GREEN)
    draw.text((80, 930), "cycle_premium_yearly", font=font(28), fill=INK)
    draw.text((80, 1020), "Preço definido na Play Console", font=font(28), fill=MUTED)
    rounded(draw, (48, 1220, 1032, 1360), 28, GREEN)
    draw.text((300, 1264), "Assinar anual", font=font(36, True), fill=(255, 255, 255))
    rounded(draw, (48, 1400, 1032, 1540), 28, SURFACE)
    draw.text((300, 1444), "Assinar mensal", font=font(36, True), fill=INK)


def settings(draw):
    draw.text((48, 140), "Configurações", font=font(64, True), fill=INK)
    buttons = (
        (280, "Assinatura"),
        (460, "Exportar PDF"),
        (640, "Sair"),
    )
    for y, label in buttons:
        rounded(draw, (48, y, 1032, y + 140), 28, SURFACE)
        draw.text((80, y + 44), label, font=font(34, True), fill=INK)
    draw.text((48, 860), "O PDF dos últimos 6 ciclos é Premium.", font=font(28), fill=MUTED)
    rounded(draw, (48, 1700, 1032, 1840), 16, (232, 230, 226))
    draw.text((360, 1748), "Anúncio", font=font(28), fill=MUTED)


def insights(draw):
    draw.text((48, 140), "Evidências", font=font(64, True), fill=INK)
    draw.text((48, 240), "Toque abre a fonte e registra a leitura.", font=font(30), fill=MUTED)
    cards = (
        (GREEN, GREEN_SOFT, "Nutrição", "Bull 2019"),
        (AMBER, AMBER_SOFT, "Exercício", "McNulty 2020"),
        (PURPLE, PURPLE_SOFT, "Pele", "Raghunath 2015"),
        (RED, RED_SOFT, "Mente", "Baker & Driver 2007"),
    )
    y = 340
    for color, soft, title, source in cards:
        rounded(draw, (48, y, 1032, y + 280), 32, SURFACE)
        rounded(draw, (80, y + 36, 200, y + 156), 24, soft)
        draw.ellipse((104, y + 60, 176, y + 132), outline=color, width=10)
        draw.text((240, y + 48), title, font=font(36, True), fill=INK)
        draw.text((240, y + 120), "Orientação de bem-estar desta fase.", font=font(28), fill=MUTED)
        draw.text((240, y + 190), source, font=font(28, True), fill=color)
        y += 320


def tablet(name, title, subtitle, color, soft):
    image = Image.new("RGB", (1920, 1080), BG)
    draw = ImageDraw.Draw(image)
    draw.text((64, 40), "09:41", font=font(28, True), fill=INK)
    draw.text((80, 160), title, font=font(64, True), fill=INK)
    draw.text((80, 260), subtitle, font=font(32), fill=MUTED)
    ring(draw, (120, 380, 720, 980), width=36)
    rounded(draw, (860, 380, 1800, 980), 36, soft)
    draw.text((920, 460), "Fase em destaque", font=font(28), fill=color)
    draw.text((920, 540), title, font=font(56, True), fill=INK)
    draw.text((920, 660), "O mesmo cálculo da roda, em uma data futura.", font=font(30), fill=MUTED)
    save(image, name)


def main():
    icon()
    feature_graphic()
    screen("01-home-1080x1920.png", home)
    screen("02-evidencias-1080x1920.png", insights)
    screen("03-sos-1080x1920.png", sos)
    screen("04-planner-1080x1920.png", planner)
    screen("05-premium-1080x1920.png", paywall)
    screen("06-configuracoes-1080x1920.png", settings)
    tablet(
        "tablet-10-planner-1920x1080.png",
        "Planner",
        "Horizonte maior no Premium.",
        PURPLE,
        PURPLE_SOFT,
    )
    tablet(
        "tablet-7-home-1920x1080.png",
        "Dia 12",
        "Fase folicular",
        GREEN,
        GREEN_SOFT,
    )


if __name__ == "__main__":
    main()
