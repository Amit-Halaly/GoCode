import os
from fastapi import FastAPI, HTTPException, WebSocket
from app.models import LintRequest, RunRequest, LintResponse, RunResponse, HintRequest, HintResponse
from app.services.c_runner import lint_c, run_c
from app.services.csharp_runner import lint_csharp, run_csharp
from app.services.java_runner import lint_java, run_java
from app.services.python_runner import lint_python, run_python
from app.arena import arena_manager
from openai import OpenAI


app = FastAPI(title="GoCode Execution API", version="0.1.0")


@app.get("/")
def root():
    return {"service": "GoCode Execution API", "ok": True}


@app.get("/health")
def health():
    return {"ok": True, "languages": ["java", "python", "c", "csharp"]}


@app.get("/arena/status")
def arena_status():
    return {"ok": True, "websocket": "/arena/ws"}


@app.websocket("/arena/ws")
async def arena_ws(websocket: WebSocket):
    await arena_manager.connect(websocket)


@app.post("/lint", response_model=LintResponse)
def lint(req: LintRequest):
    language = req.language.lower()
    if language == "python":
        return lint_python(req.code)
    if language in {"c", "clang"}:
        return lint_c(req.code)
    if language in {"csharp", "c#", "cs"}:
        return lint_csharp(req.code)
    return lint_java(req.code)


@app.post("/run", response_model=RunResponse)
def run(req: RunRequest):
    language = req.language.lower()
    if language == "python":
        return run_python(
            req.code,
            req.input or "",
            expected_output=req.expectedOutput,
            compare_mode=req.compareMode,
            test_cases=[tc.model_dump() for tc in req.testCases] if req.testCases else None,
        )
    if language in {"c", "clang"}:
        return run_c(
            req.code,
            req.input or "",
            expected_output=req.expectedOutput,
            compare_mode=req.compareMode,
            test_cases=[tc.model_dump() for tc in req.testCases] if req.testCases else None,
        )
    if language in {"csharp", "c#", "cs"}:
        return run_csharp(
            req.code,
            req.input or "",
            expected_output=req.expectedOutput,
            compare_mode=req.compareMode,
            test_cases=[tc.model_dump() for tc in req.testCases] if req.testCases else None,
        )
    return run_java(
        req.code,
        req.input or "",
        expected_output=req.expectedOutput,
        compare_mode=req.compareMode,
        test_cases=[tc.model_dump() for tc in req.testCases] if req.testCases else None,
        )


@app.post("/hint", response_model=HintResponse)
def hint(req: HintRequest):
    if not os.environ.get("OPENAI_API_KEY"):
        raise HTTPException(status_code=503, detail="AI hints are not configured")

    client = OpenAI()
    instructions = (
        "You are a teaching assistant for beginner programmers. "
        "ABSOLUTE RULES (NO EXCEPTIONS): "
        "You must NOT give the solution. "
        "You must NOT suggest an exact change. "
        "You must NOT quote code, strings, characters, words, or symbols. "
        "You must NOT mention specific values, literals, outputs, or variable contents. "
        "You must NOT say what to replace with what. "
        "You must NOT use quotation marks, backticks, or code formatting. "
        "You must NOT reference exact spelling, capitalization, or characters directly. "
        "OUTPUT REQUIREMENTS: "
        "Write EXACTLY ONE sentence. "
        "Maximum 8 words. "
        "The sentence must be a gentle hint, not an instruction. "
        "The sentence must describe WHAT TO CHECK, not WHAT TO CHANGE. "
        "Use beginner-friendly, encouraging language. "
        "IF YOU ARE ABOUT TO REVEAL THE ANSWER: "
        "STOP and rewrite the hint in a more general and indirect way. "
        "If you cannot follow all rules, respond with: "
        "'Look carefully at your program’s output.'"
    )

    input_text = f"""TASK:
{req.task}

LANGUAGE: {req.language}

CODE:
{req.code}

STDIN:
{req.input}

OUTPUT:
{req.output}

ERROR:
{req.error}

EXIT CODE: {req.exitCode}
"""

    r = client.responses.create(
        model="gpt-5.2",
        instructions=instructions,
        input=input_text,
    )

    hint_text = (r.output_text or "").strip()
    if not hint_text:
        hint_text = "Read the first error line carefully; it usually points to the root cause."

    return {"hint": hint_text}


if __name__ == "__main__":
    import uvicorn
    port = int(os.environ.get("PORT", 8080))
    uvicorn.run(app, host="0.0.0.0", port=port, log_level="debug")
