"""
Uploads content/ to Firestore from your PC (no phone or emulator needed).
Run from the repo root:   python seed_firestore.py            (real upload)
                          python seed_firestore.py --dry-run  (only checks and counts)
It reads the project id and API key from app/google-services.json.
Needs the Firestore rules to allow writes (test mode) at the moment you run it.
"""
import glob, json, os, sys, urllib.request, urllib.error

DRY = "--dry-run" in sys.argv

gs = json.load(open("app/google-services.json", encoding="utf-8"))
PROJECT = gs["project_info"]["project_id"]
KEY = gs["client"][0]["api_key"][0]["current_key"]
BASE = f"https://firestore.googleapis.com/v1/projects/{PROJECT}/databases/(default)/documents"


def to_value(v):
    if isinstance(v, bool):
        return {"booleanValue": v}
    if isinstance(v, int):
        return {"integerValue": str(v)}
    if isinstance(v, float):
        return {"doubleValue": v}
    if v is None:
        return {"nullValue": None}
    if isinstance(v, str):
        return {"stringValue": v}
    if isinstance(v, list):
        return {"arrayValue": {"values": [to_value(x) for x in v]}}
    if isinstance(v, dict):
        return {"mapValue": {"fields": {k: to_value(x) for k, x in v.items()}}}
    raise TypeError(type(v))


def put(collection, doc_id, data):
    if DRY:
        return True
    url = f"{BASE}/{collection}/{doc_id}?key={KEY}"
    body = json.dumps({"fields": {k: to_value(v) for k, v in data.items()}}).encode("utf-8")
    req = urllib.request.Request(url, data=body, method="PATCH", headers={"Content-Type": "application/json"})
    try:
        urllib.request.urlopen(req, timeout=30).read()
        return True
    except urllib.error.HTTPError as e:
        print(f"FAILED {collection}/{doc_id}: HTTP {e.code} {e.read().decode('utf-8', 'ignore')[:300]}")
        return False


def load(path):
    return json.load(open(path, encoding="utf-8"))


ok = bad = 0
counts = {}
for col, file in [("subjects", "content/subjects.json"), ("chapters", "content/chapters.json")]:
    for item in load(file):
        if put(col, item["id"], item): ok += 1; counts[col] = counts.get(col, 0) + 1
        else: bad += 1
for path in sorted(glob.glob("content/*/*.json")):
    lesson = load(path)
    if lesson["id"] != os.path.basename(path)[:-5]:
        print("WARNING: id does not match file name:", path)
    if put("lessons", lesson["id"], lesson): ok += 1; counts["lessons"] = counts.get("lessons", 0) + 1
    else: bad += 1

print(("DRY RUN " if DRY else "") + f"project={PROJECT} ok={ok} failed={bad} {counts}")
sys.exit(1 if bad else 0)