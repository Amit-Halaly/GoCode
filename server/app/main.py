from fastapi import FastAPI
from models import LintRequest, RunRequest, LintResponse, RunResponse, HintRequest, HintResponse
from services.java_runner import lint_java, run_java
from openai import OpenAI



client = OpenAI()

app = FastAPI(title="GoCode Execution API", version="0.1.0")


@app.get("/health")
def health():
    return {"ok": True}


@app.post("/lint", response_model=LintResponse)
def lint(req: LintRequest):
    return lint_java(req.code)


@app.post("/run", response_model=RunResponse)
def run(req: RunRequest):
    return run_java(req.code, req.input or "")

@app.post("/hint", response_model=HintResponse)
def hint(req: HintRequest):
    instructions = (
        "You are a programming tutor. Return exactly ONE short hint sentence (max 15 words). "
        "Do NOT provide code. Do NOT provide the full solution. Do NOT give steps. "
        "Focus on the most likely cause given the task, code, and error/output."
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
    import os
    import uvicorn
    port = int(os.environ.get("PORT", 8080))
    uvicorn.run(app, host="0.0.0.0", port=port, log_level="debug")
