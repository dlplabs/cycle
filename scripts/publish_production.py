#!/usr/bin/env python3
import json
import os
import sys
import time
import requests
import jwt

PACKAGE_NAME = "br.com.dlpsystems.cycle"
SA_KEY_PATH = "/home/daniel-pinheiro/.config/gcloud/play-console-sa.json"
AAB_PATH = "app/build/outputs/bundle/release/app-release.aab"
VERSION_CODE = 18
VERSION_NAME = "1.0.17"

RELEASE_NOTES_PT = (
    "Novidades da versão 1.0.17:\n"
    "• Ciclos da Mulher: Tentante, Gravidez, Pós-Parto (EPDS), Amamentação (MELA) e Climatério (MRS).\n"
    "• Triagem de depressão pós-parto validada com suporte direto ao CVV 188.\n"
    "• Calculadora de eficácia MELA e conservação de leite materno (Fiocruz).\n"
    "• SOS Alívio com abas para cólicas, fogachos e acolhimento emocional imediato.\n"
    "• Previsor de fases para planejamento de treinos e rotinas.\n"
    "• Melhorias contínuas de estabilidade e privacidade."
)

RELEASE_NOTES_EN = (
    "What's new in version 1.0.17:\n"
    "• Woman Life Stages: TTC, Pregnancy, Postpartum (EPDS), Breastfeeding (LAM), and Menopause (MRS).\n"
    "• Postpartum depression screening with direct crisis support (CVV 188).\n"
    "• LAM effectiveness calculator & Fiocruz milk storage guidelines.\n"
    "• SOS Relief with dedicated tabs for cramps, hot flashes, and emotional care.\n"
    "• Cycle phase planner for workouts and daily routines.\n"
    "• Ongoing stability, privacy, and performance improvements."
)

def get_access_token():
    print(f"[*] Carregando credencial de serviço: {SA_KEY_PATH}")
    with open(SA_KEY_PATH, "r") as f:
        sa = json.load(f)

    now = int(time.time())
    payload = {
        "iss": sa["client_email"],
        "scope": "https://www.googleapis.com/auth/androidpublisher",
        "aud": "https://oauth2.googleapis.com/token",
        "exp": now + 3600,
        "iat": now,
    }
    signed_jwt = jwt.encode(payload, sa["private_key"], algorithm="RS256")
    resp = requests.post(
        "https://oauth2.googleapis.com/token",
        data={
            "grant_type": "urn:ietf:params:oauth:grant-type:jwt-bearer",
            "assertion": signed_jwt,
        },
    )
    if resp.status_code != 200:
        raise RuntimeError(f"Erro na autenticação OAuth2: {resp.text}")
    print("[+] Token de acesso obtido com sucesso.")
    return resp.json()["access_token"]

def main():
    if not os.path.exists(AAB_PATH):
        print(f"[-] Arquivo AAB não encontrado em: {AAB_PATH}")
        sys.exit(1)

    aab_size_mb = os.path.getsize(AAB_PATH) / (1024 * 1024)
    print(f"[*] AAB localizado: {AAB_PATH} ({aab_size_mb:.2f} MB)")

    token = get_access_token()
    headers = {"Authorization": f"Bearer {token}"}

    # 1. Criar novo Edit
    print(f"[*] Criando edit para {PACKAGE_NAME}...")
    r_edit = requests.post(
        f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits",
        headers=headers,
    )
    if r_edit.status_code != 200:
        print(f"[-] Erro ao criar edit: {r_edit.status_code} - {r_edit.text}")
        sys.exit(1)

    edit_id = r_edit.json()["id"]
    print(f"[+] Edit criado com ID: {edit_id}")

    try:
        # Verificar se o bundle versionCode 18 já existe no edit
        r_bundles = requests.get(
            f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits/{edit_id}/bundles",
            headers=headers,
        )
        existing_bundles = [b.get("versionCode") for b in r_bundles.json().get("bundles", [])]
        print(f"[*] Bundles existentes no edit: {existing_bundles}")

        uploaded_vc = VERSION_CODE
        if VERSION_CODE not in existing_bundles:
            # 2. Upload do AAB
            print(f"[*] Fazendo upload do App Bundle ({aab_size_mb:.2f} MB)...")
            upload_url = (
                f"https://androidpublisher.googleapis.com/upload/androidpublisher/v3/applications/"
                f"{PACKAGE_NAME}/edits/{edit_id}/bundles?uploadType=media"
            )
            upload_headers = {
                "Authorization": f"Bearer {token}",
                "Content-Type": "application/octet-stream",
            }
            with open(AAB_PATH, "rb") as f:
                r_upload = requests.post(upload_url, headers=upload_headers, data=f)

            if r_upload.status_code != 200:
                print(f"[-] Falha no upload do AAB: {r_upload.status_code} - {r_upload.text}")
                sys.exit(1)

            upload_data = r_upload.json()
            uploaded_vc = upload_data.get("versionCode")
            print(f"[+] Bundle enviado com sucesso! VersionCode retornado: {uploaded_vc}")
        else:
            print(f"[+] VersionCode {VERSION_CODE} já presente no edit, avançando para track production.")

        # 3. Configurar release na track production
        print("[*] Configurando release na trilha 'production'...")
        track_url = (
            f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/"
            f"{PACKAGE_NAME}/edits/{edit_id}/tracks/production"
        )
        track_payload = {
            "track": "production",
            "releases": [
                {
                    "name": f"{uploaded_vc} ({VERSION_NAME})",
                    "versionCodes": [str(uploaded_vc)],
                    "status": "completed",
                    "releaseNotes": [
                        {"language": "pt-BR", "text": RELEASE_NOTES_PT},
                        {"language": "en-US", "text": RELEASE_NOTES_EN},
                    ],
                }
            ],
        }
        r_track = requests.put(track_url, headers=headers, json=track_payload)
        if r_track.status_code != 200:
            print(f"[-] Erro ao configurar trilha production: {r_track.status_code} - {r_track.text}")
            sys.exit(1)
        print("[+] Trilha de produção configurada com sucesso!")

        # 4. Commit do Edit
        print("[*] Realizando commit da publicação no Google Play Console...")
        commit_url = (
            f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/"
            f"{PACKAGE_NAME}/edits/{edit_id}:commit"
        )
        r_commit = requests.post(commit_url, headers=headers)
        if r_commit.status_code != 200:
            print(f"[-] Erro no commit do edit: {r_commit.status_code} - {r_commit.text}")
            sys.exit(1)

        print("[+] ========================================================")
        print("[+] PUBLICAÇÃO EM PRODUÇÃO REALIZADA COM SUCESSO NO GOOGLE PLAY!")
        print(f"[+] Versão: {VERSION_NAME} (versionCode: {uploaded_vc})")
        print(f"[+] Resposta do Commit: {r_commit.text}")
        print("[+] ========================================================")

    except Exception as e:
        print(f"[-] Exceção durante publicação: {e}")
        requests.delete(
            f"https://androidpublisher.googleapis.com/androidpublisher/v3/applications/{PACKAGE_NAME}/edits/{edit_id}",
            headers=headers,
        )
        sys.exit(1)

if __name__ == "__main__":
    main()
