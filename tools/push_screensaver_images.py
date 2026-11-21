#!/usr/bin/env python3
"""
Pushes screensaver images and captions from work/screensaver to TV storage:
/sdcard/Android/data/com.gathin.tvhome/files/screensaver/art/
/sdcard/Android/data/com.gathin.tvhome/files/screensaver/nature/
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import tv

def push_dir(dev, local_dir, remote_dir):
    print(f"Pushing {local_dir} -> {remote_dir}...")
    dev.shell(f"mkdir -p '{remote_dir}'")
    files = sorted(os.listdir(local_dir))
    for fname in files:
        lpath = os.path.join(local_dir, fname)
        rpath = f"{remote_dir}/{fname}"
        if os.path.isfile(lpath):
            print(f"  Pushing {fname} ({os.path.getsize(lpath)} bytes)...")
            dev.push(lpath, rpath, transport_timeout_s=300.0, read_timeout_s=300.0)
    print(f"Finished {local_dir}")

def main():
    dev = tv.connect()
    try:
        base = os.path.join(os.path.dirname(__file__), "..", "work", "screensaver")
        push_dir(dev, os.path.join(base, "art"), "/sdcard/Android/data/com.gathin.tvhome/files/screensaver/art")
        push_dir(dev, os.path.join(base, "nature"), "/sdcard/Android/data/com.gathin.tvhome/files/screensaver/nature")
        print("All screensaver images pushed successfully!")
    finally:
        dev.close()

if __name__ == "__main__":
    main()
