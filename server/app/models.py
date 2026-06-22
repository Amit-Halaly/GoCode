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


class TestCase(BaseModel):
    name: Optional[str] = None
    input: Optional[str] = ""
    expectedOutput: str
    hidden: bool = False


class TestResult(BaseModel):
    name: Optional[str] = None
    passed: bool
    input: Optional[str] = ""
    expectedOutput: Optional[str] = None
    actualOutput: Optional[str] = None
    error: str = ""
    exitCode: int = 0
    hidden: bool = False


class RunRequest(BaseModel):
    language: str = Field(default="java")
    code: str
    input: Optional[str] = ""
    expectedOutput: Optional[str] = None
    compareMode: CompareMode = "normalize"
    testCases: Optional[List[TestCase]] = None


class RunResponse(BaseModel):
    output: str
    error: str
    exitCode: int
    passed: Optional[bool] = None
    expectedOutput: Optional[str] = None
    actualOutput: Optional[str] = None
    testResults: Optional[List[TestResult]] = None
    summary: Optional[str] = None


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
