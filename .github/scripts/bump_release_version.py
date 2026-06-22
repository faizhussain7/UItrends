#!/usr/bin/env python3
"""Bump Android version and generate Play Store / GitHub release notes from git history."""

from __future__ import annotations

import argparse
import os
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
VERSION_FILE = ROOT / "release" / "version.properties"
WHATSNEW_DIR = ROOT / "release" / "whatsnew"
WHATSNEW_EN = WHATSNEW_DIR / "en-US.txt"
GITHUB_NOTES = ROOT / "release" / "RELEASE_NOTES.md"
PLAY_STORE_WHATSNEW_LIMIT = 500


def sh(*args: str) -> str:
    return subprocess.check_output(args, cwd=ROOT, text=True).strip()


def read_version() -> tuple[int, str]:
    props: dict[str, str] = {}
    if VERSION_FILE.is_file():
        for line in VERSION_FILE.read_text(encoding="utf-8").splitlines():
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, value = line.split("=", 1)
            props[key.strip()] = value.strip()
    return int(props.get("versionCode", "1")), props.get("versionName", "1.0.0")


def write_version(code: int, name: str) -> None:
    VERSION_FILE.parent.mkdir(parents=True, exist_ok=True)
    VERSION_FILE.write_text(
        f"# Auto-maintained by .github/workflows/release.yml\n"
        f"versionCode={code}\n"
        f"versionName={name}\n",
        encoding="utf-8",
    )


def last_tag() -> str:
    try:
        return sh("git", "describe", "--tags", "--abbrev=0", "--match", "v*")
    except subprocess.CalledProcessError:
        return ""


def commits_since(ref: str) -> list[str]:
    range_spec = f"{ref}..HEAD" if ref else "HEAD"
    try:
        log = sh("git", "log", range_spec, "--pretty=format:%s%n%b%n----")
    except subprocess.CalledProcessError:
        return []
    return [block.strip() for block in log.split("----") if block.strip()]


def parse_bump(messages: list[str], override: str) -> str:
    if override != "auto":
        return override
    bump = "patch"
    for msg in messages:
        subject = msg.splitlines()[0]
        if "BREAKING CHANGE" in msg or re.match(r"^\w+!:", subject):
            return "major"
        if subject.startswith("feat"):
            bump = "minor"
    return bump


def bump_semver(name: str, bump: str) -> str:
    parts = (name.split(".") + ["0", "0", "0"])[:3]
    major, minor, patch = (int(p) for p in parts)
    if bump == "major":
        return f"{major + 1}.0.0"
    if bump == "minor":
        return f"{major}.{minor + 1}.0"
    return f"{major}.{minor}.{patch + 1}"


def format_release_notes(messages: list[str]) -> str:
    lines: list[str] = []
    icons = {"feat": "✨", "fix": "🐛", "perf": "⚡", "refactor": "♻️", "docs": "📝"}
    for msg in messages:
        subject = msg.splitlines()[0]
        if subject.startswith(("Merge ", "chore(release):")):
            continue
        match = re.match(
            r"^(feat|fix|perf|refactor|docs|chore|style|test)(?:\([^)]+\))?!?:\s*(.+)",
            subject,
        )
        if match:
            kind, summary = match.group(1), match.group(2)
            lines.append(f"{icons.get(kind, '•')} {summary}")
        else:
            lines.append(f"• {subject}")
    return "\n".join(lines[:25]) if lines else "• Bug fixes and improvements"


def write_github_output(key: str, value: str) -> None:
    output_path = os.environ.get("GITHUB_OUTPUT")
    if not output_path:
        return
    with open(output_path, "a", encoding="utf-8") as handle:
        handle.write(f"{key}={value}\n")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--bump",
        default="auto",
        choices=["auto", "patch", "minor", "major", "none"],
        help="Semver bump strategy from conventional commits",
    )
    parser.add_argument(
        "--from-tag",
        default="",
        help="Use an explicit tag (e.g. v1.2.0) for versionName; still increments versionCode",
    )
    args = parser.parse_args()

    version_code, version_name = read_version()
    tag = args.from_tag or last_tag()
    messages = commits_since(tag)

    if args.from_tag:
        new_name = args.from_tag.removeprefix("v")
        bump = "none"
    else:
        bump = parse_bump(messages, args.bump)
        new_name = version_name if bump == "none" else bump_semver(version_name, bump)

    new_code = version_code + 1
    notes = format_release_notes(messages)
    play_notes = notes[:PLAY_STORE_WHATSNEW_LIMIT].rstrip() + "\n"

    write_version(new_code, new_name)
    WHATSNEW_DIR.mkdir(parents=True, exist_ok=True)
    WHATSNEW_EN.write_text(play_notes, encoding="utf-8")
    GITHUB_NOTES.write_text(
        f"# UITrends {new_name}\n\n{notes}\n",
        encoding="utf-8",
    )

    print(f"versionCode={new_code}")
    print(f"versionName={new_name}")
    print(f"bump={bump}")
    print(f"sinceTag={tag or '(none)'}")
    print("--- release notes ---")
    print(notes)

    write_github_output("version_code", str(new_code))
    write_github_output("version_name", new_name)
    write_github_output("release_tag", f"v{new_name}")
    write_github_output("bump", bump)
    return 0


if __name__ == "__main__":
    sys.exit(main())
