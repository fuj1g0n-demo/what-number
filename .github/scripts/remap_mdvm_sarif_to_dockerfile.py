#!/usr/bin/env python3
"""Remap MDVM container SARIF locations to the Dockerfile.

Microsoft Defender (MDVM) reports container vulnerabilities located at the image
reference (e.g. ``registry/quiz-app`` line 1), so GitHub code scanning cannot show
them as pull-request inline annotations (those require a location on a changed
line of a file in the repository). This script rewrites each OS-package finding's
location to the Dockerfile instruction that introduced the package, so the
GitHub Advanced Security bot annotates the Dockerfile on PRs - matching how
CodeQL behaves.

Classification (robust, single-tool):
  * OS vs application is decided from the CycloneDX SBOM ``purl`` ecosystem
    (``pkg:deb/...`` = OS, ``pkg:maven/...`` / others = application/language).
  * "Explicitly added" vs "from the base image" is decided by whether the package
    appears as an installed/downloaded artifact in the Dockerfile (e.g. a
    ``<name>_<version>.deb`` filename or a ``<name>=<version>`` apt pin); base-image
    packages do not appear in the Dockerfile.

Mapping:
  * injected OS package  -> the Dockerfile line that installs/downloads it (any severity)
  * base OS package      -> the runtime ``FROM`` line (only high/critical kept inline)
  * application package  -> left at the image location (covered by Dependabot/CodeQL)

Usage:
  remap_mdvm_sarif_to_dockerfile.py <sarif> <sbom-cyclonedx-json> <Dockerfile>

The SARIF file is rewritten in place. Only ``results[].locations`` and
``results[].partialFingerprints`` are touched; rules/messages are preserved.
"""
from __future__ import annotations

import hashlib
import json
import re
import sys


def _read(path: str) -> str:
    with open(path, encoding="utf-8") as fh:
        return fh.read()


def build_pkg_ecosystems(sbom: dict) -> dict[str, set[str]]:
    """name -> set of purl ecosystem types (deb, maven, golang, ...)."""
    out: dict[str, set[str]] = {}
    for comp in sbom.get("components") or []:
        purl = comp.get("purl") or ""
        m = re.match(r"pkg:([^/]+)/", purl)
        if not m:
            continue
        name = comp.get("name") or ""
        if name:
            out.setdefault(name, set()).add(m.group(1).lower())
    return out


def find_runtime_from_line(lines: list[str]) -> int:
    """1-based line of the runtime-stage FROM (fallback: last FROM, else 1)."""
    runtime = None
    last_from = None
    for idx, raw in enumerate(lines, start=1):
        stripped = raw.strip()
        if re.match(r"(?i)^FROM\s", stripped):
            last_from = idx
            if re.search(r"(?i)\bAS\s+runtime\b", stripped):
                runtime = idx
    if runtime is not None:
        return runtime
    return last_from if last_from is not None else 1


def make_install_line_finder(lines: list[str]):
    """Return a function name -> 1-based Dockerfile line that installs it, or None.

    Matches the package name used as a deb filename (``<name>_<digit>``) or an
    install/version token (``<name>=``). The negative look-behind avoids matching
    a longer package name (e.g. ``libcurl4`` must not match inside ``xlibcurl4``).
    """

    def finder(name: str):
        if not name:
            return None
        pat = re.compile(r"(?<![A-Za-z0-9.+_-])" + re.escape(name) + r"[_=]")
        for idx, raw in enumerate(lines, start=1):
            if pat.search(raw):
                return idx
        return None

    return finder


def main() -> int:
    if len(sys.argv) != 4:
        sys.stderr.write(
            "usage: remap_mdvm_sarif_to_dockerfile.py <sarif> <sbom.json> <Dockerfile>\n"
        )
        return 2

    sarif_path, sbom_path, dockerfile_path = sys.argv[1], sys.argv[2], sys.argv[3]
    sarif = json.loads(_read(sarif_path))
    try:
        sbom = json.loads(_read(sbom_path))
    except (OSError, ValueError):
        sbom = {}
    df_lines = _read(dockerfile_path).splitlines()

    ecosystems = build_pkg_ecosystems(sbom)
    from_line = find_runtime_from_line(df_lines)
    install_line_of = make_install_line_finder(df_lines)

    def end_col(line_no: int) -> int:
        try:
            return max(2, len(df_lines[line_no - 1]) + 1)
        except IndexError:
            return 2

    counters = {"injected": 0, "base": 0, "left": 0}

    for run in sarif.get("runs") or []:
        for res in run.get("results") or []:
            text = ((res.get("message") or {}).get("text")) or ""
            m_name = re.search(r"Package:\s*([^,\n]+?)\s*,", text)
            if not m_name:
                counters["left"] += 1
                continue
            name = m_name.group(1).strip()
            m_sev = re.search(r"Severity:\s*([A-Za-z]+)", text)
            severity = (m_sev.group(1).upper() if m_sev else "")

            # Application/language packages (non-deb) stay at the image location.
            if "deb" not in ecosystems.get(name, set()):
                counters["left"] += 1
                continue

            line_no = install_line_of(name)
            if line_no is not None:
                bucket = "injected"
            else:
                # Base-image OS package: anchor to the runtime FROM line, but keep
                # only high/critical as inline annotations to limit noise.
                if severity not in ("HIGH", "CRITICAL"):
                    counters["left"] += 1
                    continue
                line_no = from_line
                bucket = "base"

            locations = res.get("locations") or []
            if not locations:
                counters["left"] += 1
                continue
            phys = locations[0].setdefault("physicalLocation", {})
            phys["artifactLocation"] = {"uri": "Dockerfile"}
            phys["region"] = {
                "startLine": line_no,
                "startColumn": 1,
                "endLine": line_no,
                "endColumn": end_col(line_no),
            }
            # Stable, line-independent identity so re-runs do not churn alerts.
            digest = hashlib.sha1(
                f"{res.get('ruleId', '')}:{name}".encode("utf-8")
            ).hexdigest()[:16]
            res["partialFingerprints"] = {"primaryLocationLineHash": f"{digest}:1"}
            counters[bucket] += 1

    with open(sarif_path, "w", encoding="utf-8") as fh:
        json.dump(sarif, fh)

    sys.stderr.write(
        "remap: injected=%(injected)d base(FROM)=%(base)d left-at-image=%(left)d\n"
        % counters
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
