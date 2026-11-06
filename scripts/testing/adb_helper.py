import subprocess
import time
import xml.etree.ElementTree as ET
import os
import re

ADB_PATH = "/home/daniel-pinheiro/Android/Sdk/platform-tools/adb"

def run_adb(args, timeout=30):
    cmd = [ADB_PATH] + args
    res = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True, timeout=timeout)
    return res.stdout.strip()

def tap(x, y):
    run_adb(["shell", "input", "tap", str(x), str(y)])

def swipe(x1, y1, x2, y2, duration=300):
    run_adb(["shell", "input", "swipe", str(x1), str(y1), str(x2), str(y2), str(duration)])

def type_text(text):
    # Escape spaces or special characters
    run_adb(["shell", "input", "text", text])

def press_back():
    run_adb(["shell", "input", "keyevent", "4"])

def dump_ui():
    run_adb(["shell", "uiautomator", "dump", "/sdcard/window_dump.xml"])
    xml_str = run_adb(["shell", "cat", "/sdcard/window_dump.xml"])
    try:
        return ET.fromstring(xml_str)
    except Exception as e:
        return None

def find_element(root, text=None, content_desc=None, contains=False):
    if root is None:
        return None
    for node in root.iter("node"):
        n_text = node.attrib.get("text", "")
        n_desc = node.attrib.get("content-desc", "")
        match = False
        if text:
            if contains and text.lower() in n_text.lower():
                match = True
            elif not contains and text.lower() == n_text.lower():
                match = True
        if content_desc:
            if contains and content_desc.lower() in n_desc.lower():
                match = True
            elif not contains and content_desc.lower() == n_desc.lower():
                match = True
        if match:
            bounds = node.attrib.get("bounds", "")
            m = re.match(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", bounds)
            if m:
                x1, y1, x2, y2 = map(int, m.groups())
                return ((x1 + x2) // 2, (y1 + y2) // 2), (x1, y1, x2, y2), node
    return None

def tap_element(text=None, content_desc=None, contains=False, retry=3, wait_after=1.0):
    for _ in range(retry):
        root = dump_ui()
        el = find_element(root, text=text, content_desc=content_desc, contains=contains)
        if el:
            (cx, cy), _, _ = el
            tap(cx, cy)
            time.sleep(wait_after)
            return True
        time.sleep(0.5)
    return False

def take_screenshot(target_path):
    run_adb(["shell", "screencap", "-p", "/sdcard/screenshot.png"])
    run_adb(["pull", "/sdcard/screenshot.png", target_path])

class ScreenRecorder:
    def __init__(self, remote_path="/sdcard/record.mp4"):
        self.remote_path = remote_path
        self.proc = None

    def start(self):
        run_adb(["shell", "rm", "-f", self.remote_path])
        # screenrecord runs up to 3 minutes by default or specified limit
        cmd = [ADB_PATH, "shell", "screenrecord", "--time-limit", "180", "--bit-rate", "4000000", self.remote_path]
        self.proc = subprocess.Popen(cmd)
        time.sleep(1)

    def stop(self, local_dest):
        if self.proc:
            # Send SIGINT to gracefully finish the MP4 container
            run_adb(["shell", "pkill", "-2", "screenrecord"])
            try:
                self.proc.wait(timeout=5)
            except Exception:
                self.proc.kill()
        time.sleep(1)
        run_adb(["pull", self.remote_path, local_dest])
        run_adb(["shell", "rm", "-f", self.remote_path])

print("Adb helper ready")
