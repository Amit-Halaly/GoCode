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

def run_java(
        code: str,
        input_data: str,
        expected_output: Optional[str] = None,
        compare_mode: str = "normalize",
        test_cases: Optional[list[Dict[str, Any]]] = None,
) -> Dict[str, Any]:
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
            return {
                "output": "",
                "error": "Compilation timed out",
                "exitCode": 124,
                "passed": False if expected_output is not None or test_cases else None,
                "expectedOutput": expected_output,
                "actualOutput": "",
                "testResults": _compile_failure_results(test_cases, "Compilation timed out", 124),
                "summary": "Compilation timed out",
            }

        if compile_proc.returncode != 0:
            return {
                "output": "",
                "error": compile_proc.stderr,
                "exitCode": compile_proc.returncode,
                "passed": False if expected_output is not None or test_cases else None,
                "expectedOutput": expected_output,
                "actualOutput": "",
                "testResults": _compile_failure_results(test_cases, compile_proc.stderr, compile_proc.returncode),
                "summary": "Compilation failed",
            }

        if test_cases:
            return _run_test_cases(tmp, test_cases, compare_mode)

        try:
            run_proc = _execute_main(tmp, input_data)
        except subprocess.TimeoutExpired:
            return {
                "output": "",
                "error": "Execution timed out",
                "exitCode": 124,
                "passed": False if expected_output is not None else None,
                "expectedOutput": expected_output,
                "actualOutput": "",
                "testResults": None,
                "summary": "Execution timed out",
            }

        actual = run_proc.stdout
        passed = None
        if expected_output is not None:
            passed = normalize_output(actual, compare_mode) == normalize_output(expected_output, compare_mode)

        return {
            "output": actual,
            "error": run_proc.stderr,
            "exitCode": run_proc.returncode,
            "passed": passed,
            "expectedOutput": expected_output,
            "actualOutput": actual,
            "testResults": None,
            "summary": "Program executed",
        }


def _execute_main(tmp_dir: str, input_data: str):
    return subprocess.run(
        ["java", "-cp", tmp_dir, "Main"],
        input=input_data,
        capture_output=True,
        text=True,
        timeout=RUN_TIMEOUT_SEC,
    )


def _run_test_cases(tmp_dir: str, test_cases: list[Dict[str, Any]], compare_mode: str) -> Dict[str, Any]:
    results = []
    first_output = ""
    first_error = ""
    first_exit_code = 0

    for index, case in enumerate(test_cases):
        name = case.get("name") or f"Test {index + 1}"
        case_input = case.get("input") or ""
        expected = case.get("expectedOutput") or ""
        hidden = bool(case.get("hidden", False))

        try:
            proc = _execute_main(tmp_dir, case_input)
            actual = proc.stdout
            error = proc.stderr
            exit_code = proc.returncode
            passed = exit_code == 0 and normalize_output(actual, compare_mode) == normalize_output(expected, compare_mode)
        except subprocess.TimeoutExpired:
            actual = ""
            error = "Execution timed out"
            exit_code = 124
            passed = False

        if index == 0:
            first_output = actual
            first_error = error
            first_exit_code = exit_code

        results.append({
            "name": name,
            "passed": passed,
            "input": "" if hidden else case_input,
            "expectedOutput": None if hidden else expected,
            "actualOutput": None if hidden else actual,
            "error": error,
            "exitCode": exit_code,
            "hidden": hidden,
        })

    passed_count = sum(1 for result in results if result["passed"])
    total = len(results)
    all_passed = passed_count == total

    return {
        "output": first_output,
        "error": first_error,
        "exitCode": first_exit_code,
        "passed": all_passed,
        "expectedOutput": None,
        "actualOutput": first_output,
        "testResults": results,
        "summary": f"{passed_count}/{total} tests passed",
    }


def _compile_failure_results(test_cases: Optional[list[Dict[str, Any]]], error: str, exit_code: int):
    if not test_cases:
        return None
    return [
        {
            "name": case.get("name") or f"Test {index + 1}",
            "passed": False,
            "input": "" if case.get("hidden") else (case.get("input") or ""),
            "expectedOutput": None if case.get("hidden") else case.get("expectedOutput"),
            "actualOutput": "",
            "error": error,
            "exitCode": exit_code,
            "hidden": bool(case.get("hidden", False)),
        }
        for index, case in enumerate(test_cases)
    ]

def normalize_output(s: str, mode: str) -> str:
    if s is None:
        return ""
    s = s.replace("\r\n", "\n").replace("\r", "\n")

    if mode == "exact":
        return s
    if mode == "trim":
        return s.strip()

    # normalize (default)
    return s.rstrip()
