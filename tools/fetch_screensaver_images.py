#!/usr/bin/env python3
"""
Downloads public domain and CC0 images for cleanTV screensaver:
- 8 Art masterpieces from Wikimedia Commons
- 8 Nature landscapes from Wikimedia Commons
Saves to local folders and writes captions.txt (filename<TAB>title<TAB>subtitle)
"""
import os
import urllib.parse
import urllib.request
import json
import time

HEADERS = {
    'User-Agent': 'cleanTV/1.0 (https://github.com/Gathin23/cleanyourtv; contact@clean.tv)'
}

ART_ITEMS = [
    {
        "file": "art_01.jpg",
        "wiki_title": "File:Van Gogh - Starry Night - Google Art Project.jpg",
        "title": "La noche estrellada",
        "subtitle": "Vincent van Gogh (1889)"
    },
    {
        "file": "art_02.jpg",
        "wiki_title": "File:Claude Monet - Water Lilies - Google Art Project.jpg",
        "title": "Nenúfares",
        "subtitle": "Claude Monet (1916)"
    },
    {
        "file": "art_03.jpg",
        "wiki_title": "File:Great Wave off Kanagawa2.jpg",
        "title": "La gran ola de Kanagawa",
        "subtitle": "Katsushika Hokusai (c. 1831)"
    },
    {
        "file": "art_04.jpg",
        "wiki_title": "File:Joaquín Sorolla y Bastida - Strolling along the Seashore - Google Art Project.jpg",
        "title": "Paseo a orillas del mar",
        "subtitle": "Joaquín Sorolla (1909)"
    },
    {
        "file": "art_05.jpg",
        "wiki_title": "File:The Kiss - Gustav Klimt - Google Cultural Institute.jpg",
        "title": "El beso",
        "subtitle": "Gustav Klimt (1907–1908)"
    },
    {
        "file": "art_06.jpg",
        "wiki_title": "File:A Sunday on La Grande Jatte, Georges Seurat, 1884.jpg",
        "title": "Tarde de domingo en la Grande Jatte",
        "subtitle": "Georges Seurat (1884–1886)"
    },
    {
        "file": "art_07.jpg",
        "wiki_title": "File:1665 Girl with a Pearl Earring.jpg",
        "title": "La joven de la perla",
        "subtitle": "Johannes Vermeer (c. 1665)"
    },
    {
        "file": "art_08.jpg",
        "wiki_title": "File:Vincent Willem van Gogh - Cafe Terrace at Night (Yorck).jpg",
        "title": "Terraza de café por la noche",
        "subtitle": "Vincent van Gogh (1888)"
    }
]

NATURE_ITEMS = [
    {
        "file": "nature_01.jpg",
        "wiki_title": "File:Tunnel View, Yosemite Valley, Yosemite NP - Diliff.jpg",
        "title": "Valle de Yosemite",
        "subtitle": "Parque Nacional Yosemite, California"
    },
    {
        "file": "nature_02.jpg",
        "wiki_title": "File:Praia de Augas Santas ou das Catedrais. Devesa. Ribadeo. Galiza-04.jpg",
        "title": "Playa de las Catedrales",
        "subtitle": "Ribadeo, Galicia"
    },
    {
        "file": "nature_03.jpg",
        "wiki_title": "File:Fiordo de Geiranger desde Flydalsjuvet, Noruega, 2019-09-07, DD 59.jpg",
        "title": "Fiordo de Geiranger",
        "subtitle": "Møre og Romsdal, Noruega"
    },
    {
        "file": "nature_04.jpg",
        "wiki_title": "File:Chureito Pagoda and Mount Fuji.jpg",
        "title": "Monte Fuji y Pagoda Chureito",
        "subtitle": "Yamanashi, Japón"
    },
    {
        "file": "nature_05.jpg",
        "wiki_title": "File:Aurora_borealis_over_Eielson_Air_Force_Base,_Alaska.jpg",
        "title": "Auroras Boreales",
        "subtitle": "Alaska / Noruega"
    },
    {
        "file": "nature_06.jpg",
        "wiki_title": "File:Torres del Paine - Janvier 2010.jpg",
        "title": "Torres del Paine",
        "subtitle": "Patagonia, Chile"
    },
    {
        "file": "nature_07.jpg",
        "wiki_title": "File:00 1838 Iguazu Falls from the Brazilian side.jpg",
        "title": "Cataratas de Iguazú",
        "subtitle": "Parque Nacional Iguazú, Argentina / Brasil"
    },
    {
        "file": "nature_08.jpg",
        "wiki_title": "File:Lago di Braies South Tyrol 3.jpg",
        "title": "Lago de Braies",
        "subtitle": "Dolomitas, Alpes italianos"
    }
]

def resolve_wiki_url(wiki_title):
    api_url = f"https://commons.wikimedia.org/w/api.php?action=query&titles={urllib.parse.quote(wiki_title)}&prop=imageinfo&iiprop=url&iiurlwidth=1920&format=json"
    req = urllib.request.Request(api_url, headers=HEADERS)
    with urllib.request.urlopen(req, timeout=15) as resp:
        data = json.loads(resp.read().decode('utf-8'))
        pages = data.get("query", {}).get("pages", {})
        for _, page in pages.items():
            imageinfo = page.get("imageinfo", [])
            if imageinfo:
                return imageinfo[0].get("thumburl") or imageinfo[0].get("url")
    return None

def download_batch(items, out_dir):
    os.makedirs(out_dir, exist_ok=True)
    captions = []
    for it in items:
        out_path = os.path.join(out_dir, it["file"])
        if os.path.exists(out_path) and os.path.getsize(out_path) > 10000:
            print(f"Already downloaded {it['file']} ({os.path.getsize(out_path)} bytes)")
            captions.append(f"{it['file']}\t{it['title']}\t{it['subtitle']}")
            continue
        url = resolve_wiki_url(it["wiki_title"])
        if not url:
            print(f"FAILED to resolve: {it['wiki_title']}")
            continue
        print(f"Downloading {it['file']} from {url}...")
        try:
            req = urllib.request.Request(url, headers=HEADERS)
            with urllib.request.urlopen(req, timeout=30) as resp, open(out_path, 'wb') as f:
                f.write(resp.read())
            print(f"Saved {it['file']} ({os.path.getsize(out_path)} bytes)")
            captions.append(f"{it['file']}\t{it['title']}\t{it['subtitle']}")
        except Exception as e:
            print(f"Error downloading {it['file']}: {e}")
        time.sleep(0.5)

    captions_file = os.path.join(out_dir, "captions.txt")
    with open(captions_file, "w", encoding="utf-8") as f:
        f.write("\n".join(captions) + "\n")
    print(f"Wrote captions to {captions_file}")

if __name__ == "__main__":
    base = os.path.join(os.path.dirname(__file__), "..", "work", "screensaver")
    print("Downloading Art...")
    download_batch(ART_ITEMS, os.path.join(base, "art"))
    print("Downloading Nature...")
    download_batch(NATURE_ITEMS, os.path.join(base, "nature"))
    print("Done!")
