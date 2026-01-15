import os
import re
import subprocess
import tempfile
from typing import Dict, Any, Optional

COMPILE_TIMEOUT_SEC = 3
RUN_TIMEOUT_SEC = 3

JAVAC_ERR_RE = re.compile(r"(?:^|/|\\)Main\.java:(\d+):(?:\s*error:)?\s*(.*)")

def _write_main_java(tmp_dir: str, code: str) -> str:
    path = os.path.join(tmp_dir, "Main.java")
    with open(path, "w", encoding="utf-8") as f:
        f.write(code)
    return path

def lint_java(code: str) -> Dict[str, Any]:
    with tempfile.TemporaryDirectory() as tmp:
        file_path = _write_main_java(tmp, code)

        try:
            proc = subprocess.run(
                ["javac", "-Xlint", file_path],
                capture_output=True,
                text=True,
                timeout=COMPILE_TIMEOUT_SEC,
            )
        except subprocess.TimeoutExpired:
            return {"errors": [{"line": 0, "col": None, "message": "Compilation timed out"}]}

        lines = proc.stderr.splitlines()
        errors = []

        i = 0
        while i < len(lines):
            line_text = lines[i]
            m = JAVAC_ERR_RE.search(line_text)
            if not m:
                i += 1
                continue

            line_no = int(m.group(1))
            msg = m.group(2).strip()

            col: Optional[int] = None

            if i + 2 < len(lines):
                caret_line = lines[i + 2]
                caret_pos = caret_line.find("^")
                if caret_pos >= 0:
                    col = caret_pos + 1  # 1-based

            errors.append({"line": line_no, "col": col, "message": msg})
            i += 1

        return {"errors": errors}

def run_java(code: str, input_data: str) -> Dict[str, Any]:
    with tempfile.TemporaryDirectory() as tmp:
        file_path = _write_main_java(tmp, code)

        try:
            compile_proc = subprocess.run(
                ["javac", file_path],
                capture_output=True,
                text=True,
                timeout=COMPILE_TIMEOUT_SEC,
            )
        except subprocess.TimeoutExpired:
            return {"output": "", "error": "Compilation timed out", "exitCode": 124}

        if compile_proc.returncode != 0:
            return {"output": "", "error": compile_proc.stderr, "exitCode": compile_proc.returncode}

        try:
            run_proc = subprocess.run(
                ["java", "-cp", tmp, "Main"],
                input=input_data,
                capture_output=True,
                text=True,
                timeout=RUN_TIMEOUT_SEC,
            )
        except subprocess.TimeoutExpired:
            return {"output": "", "error": "Execution timed out", "exitCode": 124}

        return {"output": run_proc.stdout, "error": run_proc.stderr, "exitCode": run_proc.returncode}
