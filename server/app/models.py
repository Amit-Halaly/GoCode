from pydantic import BaseModel, Field
from typing import List, Optional

class LintRequest(BaseModel):
    language: str = Field(default="java")
    code: str

class LintError(BaseModel):
    line: int
    col: Optional[int] = None
    message: str

class LintResponse(BaseModel):
    errors: List[LintError]

class RunRequest(BaseModel):
    language: str = Field(default="java")
    code: str
    input: Optional[str] = ""

class RunResponse(BaseModel):
    output: str
    error: str
    exitCode: int

class HintRequest(BaseModel):
    task: str
    language: str
    code: str
    input: Optional[str] = ""
    output: Optional[str] = ""
    error: Optional[str] = ""
    exitCode: Optional[int] = None

class HintResponse(BaseModel):
    hint: str
