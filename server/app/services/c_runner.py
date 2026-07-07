import os
import re
import subprocess
import tempfile
from typing import Any, Dict, Optional

from app.services.java_runner import normalize_output

COMPILE_TIMEOUT_SEC = 3
RUN_TIMEOUT_SEC = 3

GCC_ERR_RE = re.compile(r"(?:^|/|\\)main\.c:(\d+):(?:(\d+):)?\s*(?:error|warning):\s*(.*)")


def _write_main_c(tmp_dir: str, code: str) -> str:
    path = os.path.join(tmp_dir, "main.c")
    with open(path, "w", encoding="utf-8") as f:
        f.write(code)
    return path


def lint_c(code: str) -> Dict[str, Any]:
    with tempfile.TemporaryDirectory() as tmp:
        file_path = _write_main_c(tmp, code)

        try:
            proc = subprocess.run(
                ["gcc", "-std=c11", "-Wall", "-Wextra", "-fsyntax-only", file_path],
                capture_output=True,
                text=True,
                timeout=COMPILE_TIMEOUT_SEC,
            )
        except FileNotFoundError:
            return {"errors": [{"line": 0, "col": None, "message": "C compiler is not installed"}]}
        except subprocess.TimeoutExpired:
            return {"errors": [{"line": 0, "col": None, "message": "Compilation timed out"}]}

        errors = []
        for line_text in proc.stderr.splitlines():
            match = GCC_ERR_RE.search(line_text)
            if match:
                errors.append({
                    "line": int(match.group(1)),
                    "col": int(match.group(2)) if match.group(2) else None,
                    "message": match.group(3).strip(),
                })

        return {"errors": errors}


def run_c(
    code: str,
    input_data: str,
    expected_output: Optional[str] = None,
    compare_mode: str = "normalize",
    test_cases: Optional[list[Dict[str, Any]]] = None,
) -> Dict[str, Any]:
    with tempfile.TemporaryDirectory() as tmp:
        file_path = _write_main_c(tmp, code)
        binary_path = os.path.join(tmp, "main")

        try:
            compile_proc = subprocess.run(
                ["gcc", "-std=c11", "-Wall", "-Wextra", file_path, "-o", binary_path],
                capture_output=True,
                text=True,
                timeout=COMPILE_TIMEOUT_SEC,
            )
        except FileNotFoundError:
            return _compile_failure("C compiler is not installed", 127, expected_output, test_cases)
        except subprocess.TimeoutExpired:
            return _compile_failure("Compilation timed out", 124, expected_output, test_cases)

        if compile_proc.returncode != 0:
            return _compile_failure(compile_proc.stderr, compile_proc.returncode, expected_output, test_cases)

        if test_cases:
            return _run_test_cases(binary_path, test_cases, compare_mode)

        try:
            run_proc = _execute_binary(binary_path, input_data)
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


def _execute_binary(binary_path: str, input_data: str):
    return subprocess.run(
        [binary_path],
        input=input_data,
        capture_output=True,
        text=True,
        timeout=RUN_TIMEOUT_SEC,
    )


def _run_test_cases(binary_path: str, test_cases: list[Dict[str, Any]], compare_mode: str) -> Dict[str, Any]:
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
            proc = _execute_binary(binary_path, case_input)
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

    return {
        "output": first_output,
        "error": first_error,
        "exitCode": first_exit_code,
        "passed": passed_count == total,
        "expectedOutput": None,
        "actualOutput": first_output,
        "testResults": results,
        "summary": f"{passed_count}/{total} tests passed",
    }


def _compile_failure(
    error: str,
    exit_code: int,
    expected_output: Optional[str],
    test_cases: Optional[list[Dict[str, Any]]],
) -> Dict[str, Any]:
    return {
        "output": "",
        "error": error,
        "exitCode": exit_code,
        "passed": False if expected_output is not None or test_cases else None,
        "expectedOutput": expected_output,
        "actualOutput": "",
        "testResults": _failure_results(test_cases, error, exit_code),
        "summary": "Compilation failed",
    }


def _failure_results(test_cases: Optional[list[Dict[str, Any]]], error: str, exit_code: int):
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
