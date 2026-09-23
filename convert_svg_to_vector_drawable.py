import os
import re
import xml.etree.ElementTree as ET

svg_dir = "/home/daniel-pinheiro/Pictures/cycle"
drawable_dir = "/home/daniel-pinheiro/AndroidStudioProjects/Cycle/app/src/main/res/drawable"

os.makedirs(drawable_dir, exist_ok=True)

# File name mapping to valid Android resource names (lowercase, no spaces, no accents)
mapping = {
    "Gráfico circular.svg": "ic_grafico_circular.xml",
    "Moldura botânica.svg": "ic_moldura_botanica.xml",
    "Ícone alimentação.svg": "ic_alimentacao.xml",
    "Ícone Chá.svg": "ic_cha.xml",
    "Ícone Lua e Lavanda.svg": "ic_lua_lavanda.xml",
    "Ícone skincare.svg": "ic_skincare.xml",
    "Ícone Sérum.svg": "ic_serum.xml",
    "Ícone Toalha e Flor.svg": "ic_toalha_flor.xml",
    "Ícone yoga.svg": "ic_yoga.xml",
}

def svg_to_vector_drawable(svg_path, xml_path):
    tree = ET.parse(svg_path)
    root = tree.getroot()

    # Extract width, height, viewBox
    width = root.get('width', '24').replace('px', '')
    height = root.get('height', '24').replace('px', '')
    viewBox = root.get('viewBox', f'0 0 {width} {height}')

    vb_parts = viewBox.split()
    if len(vb_parts) == 4:
        vw, vh = vb_parts[2], vb_parts[3]
    else:
        vw, vh = width, height

    paths = []
    # Find all path tags recursively
    for elem in root.iter():
        tag = elem.tag.split('}')[-1] # remove namespace
        if tag == 'path':
            d = elem.get('d')
            fill = elem.get('fill', '#000000')
            stroke = elem.get('stroke')
            stroke_width = elem.get('stroke-width')

            if d:
                path_str = f'    <path\n        android:pathData="{d}"'
                if fill and fill.lower() != 'none':
                    path_str += f'\n        android:fillColor="{fill}"'
                if stroke and stroke.lower() != 'none':
                    path_str += f'\n        android:strokeColor="{stroke}"'
                if stroke_width:
                    path_str += f'\n        android:strokeWidth="{stroke_width}"'
                path_str += ' />'
                paths.append(path_str)

    vector_xml = f"""<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="{width}dp"
    android:height="{height}dp"
    android:viewportWidth="{vw}"
    android:viewportHeight="{vh}">
""" + "\n".join(paths) + "\n</vector>"

    with open(xml_path, 'w', encoding='utf-8') as f:
        f.write(vector_xml)
    print(f"Converted {svg_path} -> {xml_path}")

for svg_name, xml_name in mapping.items():
    svg_path = os.path.join(svg_dir, svg_name)
    xml_path = os.path.join(drawable_dir, xml_name)
    if os.path.exists(svg_path):
        svg_to_vector_drawable(svg_path, xml_path)
    else:
        print(f"Warning: {svg_path} not found")

print("All SVG icons converted to Android Vector Drawables successfully!")
