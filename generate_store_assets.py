#!/usr/bin/env python3
"""
Wrapper para gerar todos os assets de loja (Google Play Console e store_assets).
"""
import subprocess
import sys
from pathlib import Path

if __name__ == "__main__":
    script = Path(__file__).resolve().parent / "docs" / "google-play" / "gerar_graficos.py"
    res = subprocess.run([sys.executable, str(script)])
    sys.exit(res.returncode)
