from pydantic import BaseModel, Field
from typing import List, Optional, Literal


class LintRequest(BaseModel):
    language: str = Field(default="java")
    code: str


class LintError(BaseModel):
    line: int
    col: Optional[int] = None
    message: str


class LintResponse(BaseModel):
    errors: List[LintError]

CompareMode = Literal["exact", "trim", "normalize"]


class RunRequest(BaseModel):
    language: str = Field(default="java")
    code: str
    input: Optional[str] = ""
    expectedOutput: Optional[str] = None
    compareMode: CompareMode = "normalize"


class RunResponse(BaseModel):
    output: str
    error: str
    exitCode: int
    passed: Optional[bool] = None
    expectedOutput: Optional[str] = None
    actualOutput: Optional[str] = None


class HintRequest(BaseModel):
    task: str
    language: str
    code: str
    input: Optional[str] = ""
    output: Optional[str] = ""
    error: Optional[str] = ""
    exitCode: Optional[int] = None
    expectedOutput: Optional[str] = None
    actualOutput: Optional[str] = None
    passed: Optional[bool] = None
    compareMode: Optional[CompareMode] = None


class HintResponse(BaseModel):
    hint: str
    nextStep: Optional[str] = None
