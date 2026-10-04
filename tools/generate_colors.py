#!/usr/bin/env python3
"""Generate Android light and dark color resources from design/tokens.json.

Usage: python3 tools/generate_colors.py
"""
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TOKENS = ROOT / "design" / "tokens.json"
OUTPUTS = {
    "light": ROOT / "app/src/main/res/values/colors.xml",
    "dark": ROOT / "app/src/main/res/values-night/colors.xml",
}
HEX = re.compile(r"^#(?:[0-9A-Fa-f]{6}|[0-9A-Fa-f]{8})$")
NAME = re.compile(r"^[a-z][A-Za-z0-9]*$")
HEADER = "<!-- Generated from design/tokens.json by tools/generate_colors.py. Do not edit. -->"


def fail(message):
    sys.exit(f"error: {message}")


def resource_name(token):
    """onPrimary -> on_primary"""
    return re.sub(r"(?<=[a-z0-9])([A-Z])", r"_\1", token).lower()


def load_tokens():
    try:
        data = json.loads(TOKENS.read_text(encoding="utf-8"))
    except FileNotFoundError:
        fail(f"{TOKENS.relative_to(ROOT)} not found")
    except json.JSONDecodeError as e:
        fail(f"{TOKENS.relative_to(ROOT)} is not valid JSON: {e}")

    colors = data.get("color") if isinstance(data, dict) else None
    if not isinstance(colors, dict) or not colors:
        fail('tokens.json must contain a non-empty "color" object')

    errors = []
    for token, values in colors.items():
        if not NAME.match(token):
            errors.append(f'"{token}": name must be camelCase (letters and digits, starting lowercase)')
            continue
        if not isinstance(values, dict):
            errors.append(f'"{token}": expected an object with "light" and "dark"')
            continue
        for mode in OUTPUTS:
            value = values.get(mode)
            if value is None:
                errors.append(f'"{token}": missing "{mode}" value')
            elif not isinstance(value, str) or not HEX.match(value):
                errors.append(f'"{token}".{mode}: invalid hex {value!r}, expected #RRGGBB or #AARRGGBB')
        extra = set(values) - set(OUTPUTS)
        if extra:
            errors.append(f'"{token}": unknown keys {sorted(extra)}')

    names = {}
    for token in colors:
        names.setdefault(resource_name(token), []).append(token)
    for name, tokens in names.items():
        if len(tokens) > 1:
            errors.append(f"tokens {tokens} map to the same resource name '{name}'")

    if errors:
        fail("invalid tokens:\n  " + "\n  ".join(errors))
    return colors


def render(colors, mode):
    lines = ['<?xml version="1.0" encoding="utf-8"?>', HEADER, "<resources>"]
    for token in sorted(colors, key=resource_name):
        lines.append(f'    <color name="{resource_name(token)}">{colors[token][mode].upper()}</color>')
    lines.append("</resources>")
    return "\n".join(lines) + "\n"


def main():
    colors = load_tokens()
    for mode, path in OUTPUTS.items():
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(render(colors, mode), encoding="utf-8")
        print(f"wrote {path.relative_to(ROOT)} ({len(colors)} colors)")


if __name__ == "__main__":
    main()
