from fastapi import FastAPI
from models import LintRequest, RunRequest, LintResponse, RunResponse
from services.java_runner import lint_java, run_java



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

if __name__ == "__main__":
    import os
    import uvicorn
    port = int(os.environ.get("PORT", 8080))
    uvicorn.run(app, host="0.0.0.0", port=port, log_level="debug")
