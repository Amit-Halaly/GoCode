from __future__ import annotations

import asyncio
import random
import time
import uuid
from dataclasses import dataclass, field
from typing import Any, Optional

from fastapi import WebSocket, WebSocketDisconnect


QUESTION_TIME_MS = 12_000
CORRECT_ANSWER_POINTS = 100
WRONG_ANSWER_PENALTY = -35
TIMEOUT_PENALTY = -50


ARENA_QUESTIONS = [
    {
        "id": 'java-001',
        "language": 'Java',
        "course": 'Easy Output',
        "prompt": 'What is the output?\nint x = 4;\nSystem.out.println(x++);',
        "options": ['4', '5', '3', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'java-002',
        "language": 'Java',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nString s = "Go";\nSystem.out.println(s + 2 + 3);',
        "options": ['Go5', 'Go23', '5Go', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'java-003',
        "language": 'Java',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nSystem.out.println(2 + 3 + "4");',
        "options": ['54', '234', '9', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'java-004',
        "language": 'Java',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nSystem.out.println("4" + 2 + 3);',
        "options": ['9', '423', '45', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'java-005',
        "language": 'Java',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nint a = 7 / 2;\nSystem.out.println(a);',
        "options": ['3.5', '4', '3', '2'],
        "correctIndex": 2,
    },
    {
        "id": 'java-006',
        "language": 'Java',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nSystem.out.println(7 % 3);',
        "options": ['1', '2', '3', '0'],
        "correctIndex": 0,
    },
    {
        "id": 'java-007',
        "language": 'Java',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nboolean ready = true;\nSystem.out.println(!ready);',
        "options": ['true', 'false', '0', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'java-008',
        "language": 'Java',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nint[] nums = {10, 20, 30};\nSystem.out.println(nums[1]);',
        "options": ['10', '20', '30', '1'],
        "correctIndex": 1,
    },
    {
        "id": 'java-009',
        "language": 'Java',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nString word = "Java";\nSystem.out.println(word.length());',
        "options": ['3', '4', 'Java', 'true'],
        "correctIndex": 1,
    },
    {
        "id": 'java-010',
        "language": 'Java',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nint age = 14;\nSystem.out.println(age + 1);',
        "options": ['14', '15', 'age1', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'java-011',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint x = 3;\nSystem.out.println(++x);',
        "options": ['3', '4', '2', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'java-012',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint x = 3;\nint y = x++ + 2;\nSystem.out.println(y + " " + x);',
        "options": ['5 4', '6 4', '5 3', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'java-013',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint x = 1;\nx += x++ + ++x;\nSystem.out.println(x);',
        "options": ['4', '5', '6', 'Compilation error'],
        "correctIndex": 2,
    },
    {
        "id": 'java-014',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nfor (int i = 0; i < 3; i++) {\n    System.out.print(i);\n}',
        "options": ['012', '123', '0123', '3'],
        "correctIndex": 0,
    },
    {
        "id": 'java-015',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint total = 0;\nfor (int i = 1; i <= 3; i++) {\n    total += i;\n}\nSystem.out.println(total);',
        "options": ['3', '5', '6', '7'],
        "correctIndex": 2,
    },
    {
        "id": 'java-016',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nfor (int i = 1; i <= 5; i += 2) {\n    System.out.print(i);\n}',
        "options": ['12345', '135', '246', '15'],
        "correctIndex": 1,
    },
    {
        "id": 'java-017',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint x = 5;\nif (x > 5) System.out.println("A");\nelse System.out.println("B");',
        "options": ['A', 'B', '5', 'Nothing'],
        "correctIndex": 1,
    },
    {
        "id": 'java-018',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nString a = "hi";\nString b = "hi";\nSystem.out.println(a.equals(b));',
        "options": ['true', 'false', 'hi', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'java-019',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nString s = "Code";\nSystem.out.println(s.charAt(1));',
        "options": ['C', 'o', 'd', '1'],
        "correctIndex": 1,
    },
    {
        "id": 'java-020',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nString s = "GoCode";\nSystem.out.println(s.substring(2, 4));',
        "options": ['Co', 'Cod', 'oC', 'Code'],
        "correctIndex": 0,
    },
    {
        "id": 'java-021',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint[] a = {2, 4, 6};\nSystem.out.println(a.length + a[0]);',
        "options": ['3', '5', '6', '8'],
        "correctIndex": 1,
    },
    {
        "id": 'java-022',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint x = 0;\nwhile (x < 3) {\n    x++;\n}\nSystem.out.println(x);',
        "options": ['2', '3', '4', 'Infinite loop'],
        "correctIndex": 1,
    },
    {
        "id": 'java-023',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint x = 0;\ndo {\n    x++;\n} while (x < 0);\nSystem.out.println(x);',
        "options": ['0', '1', '2', 'Nothing'],
        "correctIndex": 1,
    },
    {
        "id": 'java-024',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nSystem.out.println(Math.max(4, 9));',
        "options": ['4', '9', '13', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'java-025',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nSystem.out.println(10 > 3 && 2 > 5);',
        "options": ['true', 'false', '10', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'java-026',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nSystem.out.println(10 > 3 || 2 > 5);',
        "options": ['true', 'false', '10', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'java-027',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint n = 2;\nswitch (n) {\n    case 1: System.out.print("A"); break;\n    case 2: System.out.print("B");\n    default: System.out.print("C");\n}',
        "options": ['B', 'BC', 'C', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'java-028',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint x = 8;\nSystem.out.println(x == 8 ? "yes" : "no");',
        "options": ['yes', 'no', 'true', '8'],
        "correctIndex": 0,
    },
    {
        "id": 'java-029',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nString s = "  hi  ";\nSystem.out.println(s.trim().length());',
        "options": ['2', '4', '6', 'hi'],
        "correctIndex": 0,
    },
    {
        "id": 'java-030',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint a = 2;\nint b = 3;\nSystem.out.println(a * b + a);',
        "options": ['10', '8', '12', '7'],
        "correctIndex": 1,
    },
    {
        "id": 'java-031',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = 2;\nSystem.out.println(x++ + ++x);',
        "options": ['5', '6', '7', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'java-032',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = 10;\nSystem.out.println(x-- - --x);',
        "options": ['0', '1', '2', 'Compilation error'],
        "correctIndex": 2,
    },
    {
        "id": 'java-033',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint[] nums = {1, 2, 3};\nnums[1] = nums[0] + nums[2];\nSystem.out.println(nums[1]);',
        "options": ['2', '3', '4', '6'],
        "correctIndex": 2,
    },
    {
        "id": 'java-034',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nString s = "abc";\nSystem.out.println(s.indexOf("b") + s.length());',
        "options": ['3', '4', '5', '-1'],
        "correctIndex": 1,
    },
    {
        "id": 'java-035',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint total = 0;\nfor (int i = 0; i < 4; i++) {\n    if (i == 2) continue;\n    total += i;\n}\nSystem.out.println(total);',
        "options": ['4', '6', '3', '5'],
        "correctIndex": 0,
    },
    {
        "id": 'java-036',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint total = 0;\nfor (int i = 0; i < 5; i++) {\n    if (i == 3) break;\n    total += i;\n}\nSystem.out.println(total);',
        "options": ['3', '6', '10', '4'],
        "correctIndex": 0,
    },
    {
        "id": 'java-037',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = 1;\nif (x++ == 1 && ++x == 3) {\n    System.out.println(x);\n}',
        "options": ['1', '2', '3', 'Nothing'],
        "correctIndex": 2,
    },
    {
        "id": 'java-038',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = 1;\nif (x++ == 2 || ++x == 3) {\n    System.out.println(x);\n}',
        "options": ['1', '2', '3', 'Nothing'],
        "correctIndex": 2,
    },
    {
        "id": 'java-039',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nString s = "Java";\ns = s.replace("a", "o");\nSystem.out.println(s);',
        "options": ['Jovo', 'Jova', 'Java', 'Jovo?'],
        "correctIndex": 0,
    },
    {
        "id": 'java-040',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint[][] grid = {{1, 2}, {3, 4}};\nSystem.out.println(grid[1][0]);',
        "options": ['1', '2', '3', '4'],
        "correctIndex": 2,
    },
    {
        "id": 'java-041',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = 4;\nSystem.out.println(x << 1);',
        "options": ['2', '4', '8', '16'],
        "correctIndex": 2,
    },
    {
        "id": 'java-042',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = 9;\nSystem.out.println(x >> 1);',
        "options": ['3', '4', '4.5', '18'],
        "correctIndex": 1,
    },
    {
        "id": 'java-043',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nInteger a = 100;\nInteger b = 100;\nSystem.out.println(a == b);',
        "options": ['true', 'false', '100', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'java-044',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nInteger a = 200;\nInteger b = 200;\nSystem.out.println(a == b);',
        "options": ['true', 'false', '200', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'java-045',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nString s = null;\nSystem.out.println(s.length());',
        "options": ['0', 'null', 'Compilation error', 'Runtime error'],
        "correctIndex": 3,
    },
    {
        "id": 'java-046',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = 5;\nSystem.out.println((double) x / 2);',
        "options": ['2', '2.0', '2.5', '3'],
        "correctIndex": 2,
    },
    {
        "id": 'java-047',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": "What is printed?\nchar c = 'A';\nSystem.out.println(c + 1);",
        "options": ['A1', 'B', '66', 'Compilation error'],
        "correctIndex": 2,
    },
    {
        "id": 'java-048',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": "What is printed?\nchar c = 'A';\nSystem.out.println((char)(c + 1));",
        "options": ['A1', 'B', '66', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'java-049',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = 2;\nSystem.out.println(x += x *= 3);',
        "options": ['6', '8', '12', 'Compilation error'],
        "correctIndex": 2,
    },
    {
        "id": 'java-050',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nboolean a = false;\nboolean b = true;\nSystem.out.println(a || b && !a);',
        "options": ['true', 'false', 'Compilation error', '0'],
        "correctIndex": 0,
    },
    {
        "id": 'java-051',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nString s = "abc";\nSystem.out.println(s.substring(1));',
        "options": ['a', 'ab', 'bc', 'abc'],
        "correctIndex": 2,
    },
    {
        "id": 'java-052',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint n = 0;\nSystem.out.println(n++ == 0 ? n : 9);',
        "options": ['0', '1', '9', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'python-001',
        "language": 'Python',
        "course": 'Easy Output',
        "prompt": 'What is the output?\nnums = [1, 2, 3]\nprint(nums[1])',
        "options": ['1', '2', '3', 'Error'],
        "correctIndex": 1,
    },
    {
        "id": 'python-002',
        "language": 'Python',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nprint(7 // 2)',
        "options": ['3.5', '4', '3', '2'],
        "correctIndex": 2,
    },
    {
        "id": 'python-003',
        "language": 'Python',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nprint(7 / 2)',
        "options": ['3', '3.5', '4', '2'],
        "correctIndex": 1,
    },
    {
        "id": 'python-004',
        "language": 'Python',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nprint(5 % 2)',
        "options": ['1', '2', '2.5', '0'],
        "correctIndex": 0,
    },
    {
        "id": 'python-005',
        "language": 'Python',
        "course": 'Easy Output',
        "prompt": "What is printed?\nname = 'Leo'\nprint('Hi ' + name)",
        "options": ['Hi Leo', 'Hi name', 'Leo Hi', 'Error'],
        "correctIndex": 0,
    },
    {
        "id": 'python-006',
        "language": 'Python',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nscore = 10\nscore = score + 5\nprint(score)',
        "options": ['10', '15', 'score5', 'Error'],
        "correctIndex": 1,
    },
    {
        "id": 'python-007',
        "language": 'Python',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nprint(True and False)',
        "options": ['True', 'False', '0', 'Error'],
        "correctIndex": 1,
    },
    {
        "id": 'python-008',
        "language": 'Python',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nready = True\nprint(not ready)',
        "options": ['True', 'False', 'None', 'Error'],
        "correctIndex": 1,
    },
    {
        "id": 'python-009',
        "language": 'Python',
        "course": 'Easy Output',
        "prompt": "What is printed?\nitems = ['Java', 'Python', 'C']\nprint(len(items))",
        "options": ['2', '3', 'Python', 'Error'],
        "correctIndex": 1,
    },
    {
        "id": 'python-010',
        "language": 'Python',
        "course": 'Easy Output',
        "prompt": "What is printed?\nword = 'code'\nprint(word.upper())",
        "options": ['code', 'CODE', 'Code', 'Error'],
        "correctIndex": 1,
    },
    {
        "id": 'python-011',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": "What is printed?\nprint('4' + '2')",
        "options": ['6', '42', "'42'", 'Error'],
        "correctIndex": 1,
    },
    {
        "id": 'python-012',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nprint(2 + 3 * 4)',
        "options": ['20', '14', '24', '9'],
        "correctIndex": 1,
    },
    {
        "id": 'python-013',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nprint((2 + 3) * 4)',
        "options": ['20', '14', '24', '9'],
        "correctIndex": 0,
    },
    {
        "id": 'python-014',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": "What is printed?\nfor i in range(3):\n    print(i, end='')",
        "options": ['012', '123', '0123', '3'],
        "correctIndex": 0,
    },
    {
        "id": 'python-015',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": "What is printed?\nfor i in range(1, 4):\n    print(i, end='')",
        "options": ['012', '123', '1234', '14'],
        "correctIndex": 1,
    },
    {
        "id": 'python-016',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": "What is printed?\nfor i in range(1, 6, 2):\n    print(i, end='')",
        "options": ['12345', '135', '246', '15'],
        "correctIndex": 1,
    },
    {
        "id": 'python-017',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nx = 0\nwhile x < 3:\n    x += 1\nprint(x)',
        "options": ['2', '3', '4', 'Infinite loop'],
        "correctIndex": 1,
    },
    {
        "id": 'python-018',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": "What is printed?\ntext = '  GoCode  '\nprint(text.strip())",
        "options": ['GoCode', '  GoCode', 'GoCode  ', 'Error'],
        "correctIndex": 0,
    },
    {
        "id": 'python-019',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": "What is printed?\nword = 'Python'\nprint(word[0])",
        "options": ['P', 'y', '0', 'Error'],
        "correctIndex": 0,
    },
    {
        "id": 'python-020',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": "What is printed?\nword = 'Python'\nprint(word[-1])",
        "options": ['P', 'n', '-1', 'Error'],
        "correctIndex": 1,
    },
    {
        "id": 'python-021',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": "What is printed?\nword = 'Python'\nprint(word[1:4])",
        "options": ['Pyt', 'yth', 'ytho', 'tho'],
        "correctIndex": 1,
    },
    {
        "id": 'python-022',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nnums = [2, 4, 6]\nnums.append(8)\nprint(len(nums))',
        "options": ['3', '4', '8', 'Error'],
        "correctIndex": 1,
    },
    {
        "id": 'python-023',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nnums = [1, 2, 3]\nprint(nums[-1])',
        "options": ['1', '2', '3', 'Error'],
        "correctIndex": 2,
    },
    {
        "id": 'python-024',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": 'What is printed?\ndef double(n):\n    return n * 2\nprint(double(5))',
        "options": ['5', '7', '10', 'Error'],
        "correctIndex": 2,
    },
    {
        "id": 'python-025',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": "What is printed?\ndef greet(name='Leo'):\n    print(name)\ngreet()",
        "options": ['name', 'Leo', 'None', 'Error'],
        "correctIndex": 1,
    },
    {
        "id": 'python-026',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": "What is printed?\nstudent = {'name': 'Maya', 'age': 14}\nprint(student['name'])",
        "options": ['name', 'Maya', '14', 'Error'],
        "correctIndex": 1,
    },
    {
        "id": 'python-027',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nx = 5\nprint(x > 3 and x < 10)',
        "options": ['True', 'False', '5', 'Error'],
        "correctIndex": 0,
    },
    {
        "id": 'python-028',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nx = 5\nprint(x > 8 or x == 5)',
        "options": ['True', 'False', '5', 'Error'],
        "correctIndex": 0,
    },
    {
        "id": 'python-029',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": "What is printed?\nprint(bool('False'))",
        "options": ['True', 'False', 'Error', 'None'],
        "correctIndex": 0,
    },
    {
        "id": 'python-030',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": "What is printed?\nprint(bool(''))",
        "options": ['True', 'False', 'Error', 'None'],
        "correctIndex": 1,
    },
    {
        "id": 'python-031',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nx = [1, 2]\ny = x\ny.append(3)\nprint(x)',
        "options": ['[1, 2]', '[1, 2, 3]', '[3]', 'Error'],
        "correctIndex": 1,
    },
    {
        "id": 'python-032',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nx = [1, 2]\ny = x.copy()\ny.append(3)\nprint(x)',
        "options": ['[1, 2]', '[1, 2, 3]', '[3]', 'Error'],
        "correctIndex": 0,
    },
    {
        "id": 'python-033',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nprint(0.1 + 0.2 == 0.3)',
        "options": ['True', 'False', '0.3', 'Error'],
        "correctIndex": 1,
    },
    {
        "id": 'python-034',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": "What is printed?\nprint('a' * 3)",
        "options": ['aaa', 'a3', '3a', 'Error'],
        "correctIndex": 0,
    },
    {
        "id": 'python-035',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nprint([1, 2] * 2)',
        "options": ['[1, 2, 1, 2]', '[2, 4]', '[1, 2, 2]', 'Error'],
        "correctIndex": 0,
    },
    {
        "id": 'python-036',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nnums = [1, 2, 3, 4]\nprint(nums[::2])',
        "options": ['[1, 3]', '[2, 4]', '[1, 2]', 'Error'],
        "correctIndex": 0,
    },
    {
        "id": 'python-037',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nnums = [1, 2, 3]\nprint(nums[::-1])',
        "options": ['[1, 2, 3]', '[3, 2, 1]', '[1, 3]', 'Error'],
        "correctIndex": 1,
    },
    {
        "id": 'python-038',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": "What is printed?\nfor i in range(4):\n    if i == 2:\n        continue\n    print(i, end='')",
        "options": ['0123', '013', '01', '023'],
        "correctIndex": 1,
    },
    {
        "id": 'python-039',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": "What is printed?\nfor i in range(5):\n    if i == 3:\n        break\n    print(i, end='')",
        "options": ['012', '0123', '0134', '123'],
        "correctIndex": 0,
    },
    {
        "id": 'python-040',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nx = 1\nx += x + 1\nprint(x)',
        "options": ['2', '3', '4', 'Error'],
        "correctIndex": 1,
    },
    {
        "id": 'python-041',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nprint(3 < 4 < 5)',
        "options": ['True', 'False', 'Error', '4'],
        "correctIndex": 0,
    },
    {
        "id": 'python-042',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nprint(3 < 2 < 5)',
        "options": ['True', 'False', 'Error', '2'],
        "correctIndex": 1,
    },
    {
        "id": 'python-043',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nprint(None == False)',
        "options": ['True', 'False', 'None', 'Error'],
        "correctIndex": 1,
    },
    {
        "id": 'python-044',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nprint(type(3).__name__)',
        "options": ['int', 'float', 'str', 'type'],
        "correctIndex": 0,
    },
    {
        "id": 'python-045',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": 'What is printed?\na, b = 1, 2\na, b = b, a\nprint(a, b)',
        "options": ['1 2', '2 1', '1, 2', 'Error'],
        "correctIndex": 1,
    },
    {
        "id": 'python-046',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": "What is printed?\ntext = 'banana'\nprint(text.count('a'))",
        "options": ['1', '2', '3', '4'],
        "correctIndex": 2,
    },
    {
        "id": 'python-047',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": "What is printed?\nprint('go' in 'gocode')",
        "options": ['True', 'False', 'go', 'Error'],
        "correctIndex": 0,
    },
    {
        "id": 'python-048',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nprint(len(set([1, 1, 2, 3])))',
        "options": ['4', '3', '2', '1'],
        "correctIndex": 1,
    },
    {
        "id": 'python-049',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": "What is printed?\ndata = {'a': 1}\nprint(data.get('b', 9))",
        "options": ['None', '9', 'Error', 'b'],
        "correctIndex": 1,
    },
    {
        "id": 'python-050',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": "What is printed?\ntry:\n    print(int('x'))\nexcept ValueError:\n    print('bad')",
        "options": ['x', 'bad', '0', 'Error'],
        "correctIndex": 1,
    },
    {
        "id": 'python-051',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nprint(round(2.5))',
        "options": ['2', '3', '2.5', 'Error'],
        "correctIndex": 0,
    },
    {
        "id": 'python-052',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": "What is printed?\nprint('10' > '2')",
        "options": ['True', 'False', 'Error', '10'],
        "correctIndex": 1,
    },
    {
        "id": 'c-001',
        "language": 'C',
        "course": 'Easy Output',
        "prompt": 'What is the output?\nint x = 4;\nprintf("%d", x++);',
        "options": ['4', '5', '3', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'c-002',
        "language": 'C',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nprintf("%d", 5 / 2);',
        "options": ['2.5', '3', '2', 'Compilation error'],
        "correctIndex": 2,
    },
    {
        "id": 'c-003',
        "language": 'C',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nprintf("%d", 7 % 3);',
        "options": ['1', '2', '3', '0'],
        "correctIndex": 0,
    },
    {
        "id": 'c-004',
        "language": 'C',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nint a = 3;\nprintf("%d", a + 2);',
        "options": ['3', '5', '32', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'c-005',
        "language": 'C',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nprintf("%d", 2 + 3 * 4);',
        "options": ['20', '14', '24', '9'],
        "correctIndex": 1,
    },
    {
        "id": 'c-006',
        "language": 'C',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nprintf("%d", (2 + 3) * 4);',
        "options": ['20', '14', '24', '9'],
        "correctIndex": 0,
    },
    {
        "id": 'c-007',
        "language": 'C',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nint nums[] = {10, 20, 30};\nprintf("%d", nums[1]);',
        "options": ['10', '20', '30', '1'],
        "correctIndex": 1,
    },
    {
        "id": 'c-008',
        "language": 'C',
        "course": 'Easy Output',
        "prompt": 'What is the first index in a C array?',
        "options": ['0', '1', '-1', 'Depends on compiler'],
        "correctIndex": 0,
    },
    {
        "id": 'c-009',
        "language": 'C',
        "course": 'Easy Output',
        "prompt": 'What does &x represent in C?',
        "options": ['The value of x', 'The address of x', 'A copy of x', 'The size of x'],
        "correctIndex": 1,
    },
    {
        "id": 'c-010',
        "language": 'C',
        "course": 'Easy Output',
        "prompt": 'Which header declares printf?',
        "options": ['stdio.h', 'string.h', 'math.h', 'printf.h'],
        "correctIndex": 0,
    },
    {
        "id": 'c-011',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint x = 3;\nprintf("%d", ++x);',
        "options": ['3', '4', '2', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'c-012',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint x = 3;\nint y = x++ + 2;\nprintf("%d %d", y, x);',
        "options": ['5 4', '6 4', '5 3', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'c-013',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nfor (int i = 0; i < 3; i++) {\n    printf("%d", i);\n}',
        "options": ['012', '123', '0123', '3'],
        "correctIndex": 0,
    },
    {
        "id": 'c-014',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint total = 0;\nfor (int i = 1; i <= 3; i++) {\n    total += i;\n}\nprintf("%d", total);',
        "options": ['3', '5', '6', '7'],
        "correctIndex": 2,
    },
    {
        "id": 'c-015',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nfor (int i = 1; i <= 5; i += 2) {\n    printf("%d", i);\n}',
        "options": ['12345', '135', '246', '15'],
        "correctIndex": 1,
    },
    {
        "id": 'c-016',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint x = 5;\nif (x > 5) printf("A");\nelse printf("B");',
        "options": ['A', 'B', '5', 'Nothing'],
        "correctIndex": 1,
    },
    {
        "id": 'c-017',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint x = 0;\nwhile (x < 3) {\n    x++;\n}\nprintf("%d", x);',
        "options": ['2', '3', '4', 'Infinite loop'],
        "correctIndex": 1,
    },
    {
        "id": 'c-018',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint x = 0;\ndo {\n    x++;\n} while (x < 0);\nprintf("%d", x);',
        "options": ['0', '1', '2', 'Nothing'],
        "correctIndex": 1,
    },
    {
        "id": 'c-019',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nprintf("%d", 10 > 3 && 2 > 5);',
        "options": ['1', '0', 'true', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'c-020',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nprintf("%d", 10 > 3 || 2 > 5);',
        "options": ['1', '0', 'true', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'c-021',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nchar c = \'A\';\nprintf("%c", c);',
        "options": ['A', '65', 'c', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'c-022',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nchar c = \'A\';\nprintf("%d", c);',
        "options": ['A', '65', '66', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'c-023',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nprintf("%zu", strlen("code"));',
        "options": ['3', '4', 'code', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'c-024',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nprintf("%d", strcmp("hi", "hi") == 0);',
        "options": ['1', '0', 'hi', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'c-025',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint a = 2;\nint b = 3;\nprintf("%d", a * b + a);',
        "options": ['10', '8', '12', '7'],
        "correctIndex": 1,
    },
    {
        "id": 'c-026',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint x = 8;\nprintf("%d", x == 8 ? 1 : 0);',
        "options": ['1', '0', '8', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'c-027',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint x = 5;\nprintf("%.1f", (double)x / 2);',
        "options": ['2', '2.0', '2.5', '3.0'],
        "correctIndex": 2,
    },
    {
        "id": 'c-028',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint nums[] = {1, 2, 3};\nprintf("%zu", sizeof(nums) / sizeof(nums[0]));',
        "options": ['3', '12', '4', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'c-029',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint x = 10;\nint *p = &x;\nprintf("%d", *p);',
        "options": ['10', 'Address of x', '0', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'c-030',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint x = 10;\nint *p = &x;\n*p = 12;\nprintf("%d", x);',
        "options": ['10', '12', 'Address of x', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'c-031',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = 2;\nprintf("%d", x++ + ++x);',
        "options": ['5', '6', '7', 'Undefined behavior'],
        "correctIndex": 3,
    },
    {
        "id": 'c-032',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = 10;\nprintf("%d", x-- - --x);',
        "options": ['0', '1', '2', 'Undefined behavior'],
        "correctIndex": 3,
    },
    {
        "id": 'c-033',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint nums[] = {1, 2, 3};\nnums[1] = nums[0] + nums[2];\nprintf("%d", nums[1]);',
        "options": ['2', '3', '4', '6'],
        "correctIndex": 2,
    },
    {
        "id": 'c-034',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint total = 0;\nfor (int i = 0; i < 4; i++) {\n    if (i == 2) continue;\n    total += i;\n}\nprintf("%d", total);',
        "options": ['4', '6', '3', '5'],
        "correctIndex": 0,
    },
    {
        "id": 'c-035',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint total = 0;\nfor (int i = 0; i < 5; i++) {\n    if (i == 3) break;\n    total += i;\n}\nprintf("%d", total);',
        "options": ['3', '6', '10', '4'],
        "correctIndex": 0,
    },
    {
        "id": 'c-036',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = 4;\nprintf("%d", x << 1);',
        "options": ['2', '4', '8', '16'],
        "correctIndex": 2,
    },
    {
        "id": 'c-037',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = 9;\nprintf("%d", x >> 1);',
        "options": ['3', '4', '4.5', '18'],
        "correctIndex": 1,
    },
    {
        "id": 'c-038',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nchar s[] = "abc";\nprintf("%c", s[1]);',
        "options": ['a', 'b', 'c', '1'],
        "correctIndex": 1,
    },
    {
        "id": 'c-039',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nchar s[] = "abc";\ns[0] = \'z\';\nprintf("%s", s);',
        "options": ['abc', 'zbc', 'z', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'c-040',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = 1;\nint y = 0;\nprintf("%d", x && y);',
        "options": ['1', '0', 'true', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'c-041',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = 1;\nint y = 0;\nprintf("%d", x || y);',
        "options": ['1', '0', 'true', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'c-042',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint a = 5;\nint b = 2;\nprintf("%d", a / b * b);',
        "options": ['4', '5', '2', '5.0'],
        "correctIndex": 0,
    },
    {
        "id": 'c-043',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint a = 5;\nprintf("%d", a == 5 ? 10 : 20);',
        "options": ['5', '10', '20', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'c-044',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint a = 0;\nprintf("%d", !a);',
        "options": ['0', '1', 'true', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'c-045',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint a = 3;\nprintf("%d", a += 2);',
        "options": ['3', '5', '6', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'c-046',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint a = 3;\nprintf("%d", a *= 2 + 1);',
        "options": ['9', '7', '6', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'c-047',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint a = 1;\nint b = 2;\nint temp = a;\na = b;\nb = temp;\nprintf("%d %d", a, b);',
        "options": ['1 2', '2 1', '1 1', '2 2'],
        "correctIndex": 1,
    },
    {
        "id": 'c-048',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint grid[2][2] = {{1, 2}, {3, 4}};\nprintf("%d", grid[1][0]);',
        "options": ['1', '2', '3', '4'],
        "correctIndex": 2,
    },
    {
        "id": 'c-049',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = 6;\nprintf("%d", x & 3);',
        "options": ['0', '2', '3', '6'],
        "correctIndex": 1,
    },
    {
        "id": 'c-050',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = 6;\nprintf("%d", x | 3);',
        "options": ['6', '7', '3', '2'],
        "correctIndex": 1,
    },
    {
        "id": 'c-051',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nchar text[] = "go";\nprintf("%zu", sizeof(text));',
        "options": ['2', '3', '4', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'java-053',
        "language": 'Java',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nint x = 6;\nSystem.out.println(x - 2);',
        "options": ['2', '4', '6', '8'],
        "correctIndex": 1,
    },
    {
        "id": 'java-054',
        "language": 'Java',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nSystem.out.println(3 * 3 + 1);',
        "options": ['10', '12', '9', '7'],
        "correctIndex": 0,
    },
    {
        "id": 'java-055',
        "language": 'Java',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nString a = "Go";\nString b = "Code";\nSystem.out.println(a + b);',
        "options": ['Go Code', 'GoCode', 'CodeGo', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'java-056',
        "language": 'Java',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nint x = 2;\nx = x + x;\nSystem.out.println(x);',
        "options": ['2', '3', '4', '22'],
        "correctIndex": 2,
    },
    {
        "id": 'java-057',
        "language": 'Java',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nSystem.out.println(12 / 4);',
        "options": ['3', '4', '3.0', '48'],
        "correctIndex": 0,
    },
    {
        "id": 'java-058',
        "language": 'Java',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nSystem.out.println(12 % 5);',
        "options": ['1', '2', '3', '5'],
        "correctIndex": 1,
    },
    {
        "id": 'java-059',
        "language": 'Java',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nboolean ok = false;\nSystem.out.println(ok);',
        "options": ['true', 'false', '0', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'java-060',
        "language": 'Java',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nint[] a = {4, 5};\nSystem.out.println(a[0]);',
        "options": ['0', '4', '5', '2'],
        "correctIndex": 1,
    },
    {
        "id": 'java-061',
        "language": 'Java',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nString s = "cat";\nSystem.out.println(s + s);',
        "options": ['catcat', 'cat s', 's s', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'java-062',
        "language": 'Java',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nint n = 9;\nSystem.out.println(n >= 10);',
        "options": ['true', 'false', '9', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'java-063',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint x = 2;\nSystem.out.println(x++);\nSystem.out.println(x);',
        "options": ['2 then 2', '2 then 3', '3 then 3', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'java-064',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint x = 2;\nSystem.out.println(++x);\nSystem.out.println(x);',
        "options": ['2 then 2', '2 then 3', '3 then 3', 'Compilation error'],
        "correctIndex": 2,
    },
    {
        "id": 'java-065',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint sum = 0;\nfor (int i = 2; i <= 6; i += 2) {\n    sum += i;\n}\nSystem.out.println(sum);',
        "options": ['6', '8', '10', '12'],
        "correctIndex": 3,
    },
    {
        "id": 'java-066',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nfor (int i = 3; i > 0; i--) {\n    System.out.print(i);\n}',
        "options": ['012', '321', '123', '30'],
        "correctIndex": 1,
    },
    {
        "id": 'java-067',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint x = 4;\nif (x % 2 == 0) System.out.println("even");\nelse System.out.println("odd");',
        "options": ['even', 'odd', 'true', '4'],
        "correctIndex": 0,
    },
    {
        "id": 'java-068',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nString s = "level";\nSystem.out.println(s.contains("eve"));',
        "options": ['true', 'false', 'eve', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'java-069',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nString s = "level";\nSystem.out.println(s.indexOf("e"));',
        "options": ['0', '1', '2', '-1'],
        "correctIndex": 1,
    },
    {
        "id": 'java-070',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nString s = "level";\nSystem.out.println(s.lastIndexOf("e"));',
        "options": ['1', '2', '3', '4'],
        "correctIndex": 2,
    },
    {
        "id": 'java-071',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint[] nums = {5, 6, 7, 8};\nSystem.out.println(nums.length - 1);',
        "options": ['2', '3', '4', '8'],
        "correctIndex": 1,
    },
    {
        "id": 'java-072',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint[] nums = {5, 6, 7};\nSystem.out.println(nums[nums.length - 1]);',
        "options": ['5', '6', '7', '2'],
        "correctIndex": 2,
    },
    {
        "id": 'java-073',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint x = 1;\nwhile (x <= 4) {\n    x *= 2;\n}\nSystem.out.println(x);',
        "options": ['4', '5', '8', '16'],
        "correctIndex": 2,
    },
    {
        "id": 'java-074',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint count = 0;\nfor (int i = 0; i < 5; i++) {\n    if (i % 2 == 0) count++;\n}\nSystem.out.println(count);',
        "options": ['2', '3', '4', '5'],
        "correctIndex": 1,
    },
    {
        "id": 'java-075',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint a = 3;\nint b = 4;\nSystem.out.println(a > b ? a : b);',
        "options": ['3', '4', 'true', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'java-076',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nSystem.out.println("Hi".toLowerCase());',
        "options": ['Hi', 'HI', 'hi', 'Compilation error'],
        "correctIndex": 2,
    },
    {
        "id": 'java-077',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nSystem.out.println("hi".toUpperCase());',
        "options": ['hi', 'HI', 'Hi', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'java-078',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nString s = "abc";\nSystem.out.println(s.equals("ABC"));',
        "options": ['true', 'false', 'abc', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'java-079',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nString s = "abc";\nSystem.out.println(s.equalsIgnoreCase("ABC"));',
        "options": ['true', 'false', 'abc', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'java-080',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint x = 6;\nSystem.out.println(x > 3 && x < 5);',
        "options": ['true', 'false', '6', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'java-081',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint x = 6;\nSystem.out.println(x > 3 || x < 5);',
        "options": ['true', 'false', '6', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'java-082',
        "language": 'Java',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint n = 1;\nswitch (n) {\n    case 1: System.out.print("A"); break;\n    default: System.out.print("B");\n}',
        "options": ['A', 'B', 'AB', 'Nothing'],
        "correctIndex": 0,
    },
    {
        "id": 'java-083',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = 1;\nint y = 2;\nSystem.out.println(x + y + "x" + y);',
        "options": ['3x2', '12x2', '3xy', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'java-084',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nSystem.out.println("x" + 1 + 2 + 3);',
        "options": ['x6', 'x123', '6x', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'java-085',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = 0;\nSystem.out.println(x++ == 0 && x == 1);',
        "options": ['true', 'false', '1', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'java-086',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = 0;\nSystem.out.println(x++ == 1 || x == 1);',
        "options": ['true', 'false', '1', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'java-087',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = 5;\nSystem.out.println(x / 2 * 2);',
        "options": ['4', '5', '2', '5.0'],
        "correctIndex": 0,
    },
    {
        "id": 'java-088',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = 5;\nSystem.out.println(x * 2 / 4);',
        "options": ['2', '2.5', '3', '10'],
        "correctIndex": 0,
    },
    {
        "id": 'java-089',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = -5;\nSystem.out.println(x % 2);',
        "options": ['1', '-1', '0', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'java-090',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = 1;\nx = x + (x = 5);\nSystem.out.println(x);',
        "options": ['2', '5', '6', '10'],
        "correctIndex": 2,
    },
    {
        "id": 'java-091',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint[] a = {1, 2, 3};\nint[] b = a;\nb[0] = 9;\nSystem.out.println(a[0]);',
        "options": ['1', '2', '9', 'Compilation error'],
        "correctIndex": 2,
    },
    {
        "id": 'java-092',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nString s = "abc";\nSystem.out.println(s == "abc");',
        "options": ['true', 'false', 'abc', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'java-093',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nString s = new String("abc");\nSystem.out.println(s == "abc");',
        "options": ['true', 'false', 'abc', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'java-094',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nString s = new String("abc");\nSystem.out.println(s.equals("abc"));',
        "options": ['true', 'false', 'abc', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'java-095',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = 1;\nSystem.out.println(x++ + x++ + x);',
        "options": ['3', '4', '5', '6'],
        "correctIndex": 3,
    },
    {
        "id": 'java-096',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = 1;\nSystem.out.println(++x + ++x);',
        "options": ['3', '4', '5', '6'],
        "correctIndex": 2,
    },
    {
        "id": 'java-097',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = 7;\nSystem.out.println(x & 3);',
        "options": ['1', '2', '3', '7'],
        "correctIndex": 2,
    },
    {
        "id": 'java-098',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = 7;\nSystem.out.println(x | 8);',
        "options": ['7', '8', '15', '0'],
        "correctIndex": 2,
    },
    {
        "id": 'java-099',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = 7;\nSystem.out.println(x ^ 3);',
        "options": ['3', '4', '7', '0'],
        "correctIndex": 1,
    },
    {
        "id": 'java-100',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nSystem.out.println((int) 3.9);',
        "options": ['3', '4', '3.9', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'java-101',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\ndouble d = 5 / 2;\nSystem.out.println(d);',
        "options": ['2', '2.0', '2.5', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'java-102',
        "language": 'Java',
        "course": 'Hard Output',
        "prompt": 'What is printed?\ndouble d = 5 / 2.0;\nSystem.out.println(d);',
        "options": ['2', '2.0', '2.5', 'Compilation error'],
        "correctIndex": 2,
    },
    {
        "id": 'python-053',
        "language": 'Python',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nx = 6\nprint(x - 2)',
        "options": ['2', '4', '6', '8'],
        "correctIndex": 1,
    },
    {
        "id": 'python-054',
        "language": 'Python',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nprint(3 * 3 + 1)',
        "options": ['10', '12', '9', '7'],
        "correctIndex": 0,
    },
    {
        "id": 'python-055',
        "language": 'Python',
        "course": 'Easy Output',
        "prompt": "What is printed?\na = 'Go'\nb = 'Code'\nprint(a + b)",
        "options": ['Go Code', 'GoCode', 'CodeGo', 'Error'],
        "correctIndex": 1,
    },
    {
        "id": 'python-056',
        "language": 'Python',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nx = 2\nx = x + x\nprint(x)',
        "options": ['2', '3', '4', '22'],
        "correctIndex": 2,
    },
    {
        "id": 'python-057',
        "language": 'Python',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nprint(12 / 4)',
        "options": ['3', '3.0', '4', '48'],
        "correctIndex": 1,
    },
    {
        "id": 'python-058',
        "language": 'Python',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nprint(12 // 5)',
        "options": ['1', '2', '2.4', '5'],
        "correctIndex": 1,
    },
    {
        "id": 'python-059',
        "language": 'Python',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nok = False\nprint(ok)',
        "options": ['True', 'False', '0', 'Error'],
        "correctIndex": 1,
    },
    {
        "id": 'python-060',
        "language": 'Python',
        "course": 'Easy Output',
        "prompt": 'What is printed?\na = [4, 5]\nprint(a[0])',
        "options": ['0', '4', '5', '2'],
        "correctIndex": 1,
    },
    {
        "id": 'python-061',
        "language": 'Python',
        "course": 'Easy Output',
        "prompt": "What is printed?\ns = 'cat'\nprint(s + s)",
        "options": ['catcat', 'cat s', 's s', 'Error'],
        "correctIndex": 0,
    },
    {
        "id": 'python-062',
        "language": 'Python',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nn = 9\nprint(n >= 10)',
        "options": ['True', 'False', '9', 'Error'],
        "correctIndex": 1,
    },
    {
        "id": 'python-063',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nx = 2\nx += 1\nprint(x)',
        "options": ['2', '3', '4', 'Error'],
        "correctIndex": 1,
    },
    {
        "id": 'python-064',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nsum_value = 0\nfor i in range(2, 7, 2):\n    sum_value += i\nprint(sum_value)',
        "options": ['6', '8', '10', '12'],
        "correctIndex": 3,
    },
    {
        "id": 'python-065',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": "What is printed?\nfor i in range(3, 0, -1):\n    print(i, end='')",
        "options": ['012', '321', '123', '30'],
        "correctIndex": 1,
    },
    {
        "id": 'python-066',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": "What is printed?\nx = 4\nprint('even' if x % 2 == 0 else 'odd')",
        "options": ['even', 'odd', 'True', '4'],
        "correctIndex": 0,
    },
    {
        "id": 'python-067',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": "What is printed?\ns = 'level'\nprint('eve' in s)",
        "options": ['True', 'False', 'eve', 'Error'],
        "correctIndex": 0,
    },
    {
        "id": 'python-068',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": "What is printed?\ns = 'level'\nprint(s.find('e'))",
        "options": ['0', '1', '2', '-1'],
        "correctIndex": 1,
    },
    {
        "id": 'python-069',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": "What is printed?\ns = 'level'\nprint(s.rfind('e'))",
        "options": ['1', '2', '3', '4'],
        "correctIndex": 2,
    },
    {
        "id": 'python-070',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nnums = [5, 6, 7, 8]\nprint(len(nums) - 1)',
        "options": ['2', '3', '4', '8'],
        "correctIndex": 1,
    },
    {
        "id": 'python-071',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nnums = [5, 6, 7]\nprint(nums[len(nums) - 1])',
        "options": ['5', '6', '7', '2'],
        "correctIndex": 2,
    },
    {
        "id": 'python-072',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nx = 1\nwhile x <= 4:\n    x *= 2\nprint(x)',
        "options": ['4', '5', '8', '16'],
        "correctIndex": 2,
    },
    {
        "id": 'python-073',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": 'What is printed?\ncount = 0\nfor i in range(5):\n    if i % 2 == 0:\n        count += 1\nprint(count)',
        "options": ['2', '3', '4', '5'],
        "correctIndex": 1,
    },
    {
        "id": 'python-074',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": 'What is printed?\na = 3\nb = 4\nprint(a if a > b else b)',
        "options": ['3', '4', 'True', 'Error'],
        "correctIndex": 1,
    },
    {
        "id": 'python-075',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": "What is printed?\nprint('Hi'.lower())",
        "options": ['Hi', 'HI', 'hi', 'Error'],
        "correctIndex": 2,
    },
    {
        "id": 'python-076',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": "What is printed?\nprint('hi'.upper())",
        "options": ['hi', 'HI', 'Hi', 'Error'],
        "correctIndex": 1,
    },
    {
        "id": 'python-077',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": "What is printed?\ns = 'abc'\nprint(s == 'ABC')",
        "options": ['True', 'False', 'abc', 'Error'],
        "correctIndex": 1,
    },
    {
        "id": 'python-078',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": "What is printed?\ns = 'abc'\nprint(s.lower() == 'ABC'.lower())",
        "options": ['True', 'False', 'abc', 'Error'],
        "correctIndex": 0,
    },
    {
        "id": 'python-079',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nx = 6\nprint(x > 3 and x < 5)',
        "options": ['True', 'False', '6', 'Error'],
        "correctIndex": 1,
    },
    {
        "id": 'python-080',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nx = 6\nprint(x > 3 or x < 5)',
        "options": ['True', 'False', '6', 'Error'],
        "correctIndex": 0,
    },
    {
        "id": 'python-081',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": "What is printed?\nn = 1\nif n == 1:\n    print('A')\nelse:\n    print('B')",
        "options": ['A', 'B', 'AB', 'Nothing'],
        "correctIndex": 0,
    },
    {
        "id": 'python-082',
        "language": 'Python',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nprint(list(range(2, 8, 2)))',
        "options": ['[2, 4, 6]', '[2, 4, 6, 8]', '[0, 2, 4, 6]', 'Error'],
        "correctIndex": 0,
    },
    {
        "id": 'python-083',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": "What is printed?\nx = 1\ny = 2\nprint(str(x + y) + 'x' + str(y))",
        "options": ['3x2', '12x2', '3xy', 'Error'],
        "correctIndex": 0,
    },
    {
        "id": 'python-084',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": "What is printed?\nprint('x' + str(1 + 2 + 3))",
        "options": ['x6', 'x123', '6x', 'Error'],
        "correctIndex": 0,
    },
    {
        "id": 'python-085',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nx = 0\nprint(x == 0 and x + 1 == 1)',
        "options": ['True', 'False', '1', 'Error'],
        "correctIndex": 0,
    },
    {
        "id": 'python-086',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nx = 0\nprint(x == 1 or x + 1 == 1)',
        "options": ['True', 'False', '1', 'Error'],
        "correctIndex": 0,
    },
    {
        "id": 'python-087',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nx = 5\nprint(x // 2 * 2)',
        "options": ['4', '5', '2', '5.0'],
        "correctIndex": 0,
    },
    {
        "id": 'python-088',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nx = 5\nprint(x * 2 // 4)',
        "options": ['2', '2.5', '3', '10'],
        "correctIndex": 0,
    },
    {
        "id": 'python-089',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nx = -5\nprint(x % 2)',
        "options": ['1', '-1', '0', 'Error'],
        "correctIndex": 0,
    },
    {
        "id": 'python-090',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nx = [1, 2, 3]\ny = x\ny[0] = 9\nprint(x[0])',
        "options": ['1', '2', '9', 'Error'],
        "correctIndex": 2,
    },
    {
        "id": 'python-091',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nx = [1, 2, 3]\ny = x[:]\ny[0] = 9\nprint(x[0])',
        "options": ['1', '2', '9', 'Error'],
        "correctIndex": 0,
    },
    {
        "id": 'python-092',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": "What is printed?\nprint('abc' is 'abc')",
        "options": ['True', 'False', 'abc', 'Error'],
        "correctIndex": 0,
    },
    {
        "id": 'python-093',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": "What is printed?\nprint('abc'.replace('a', 'z'))",
        "options": ['abc', 'zbc', 'azbc', 'Error'],
        "correctIndex": 1,
    },
    {
        "id": 'python-094',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": "What is printed?\nprint('abc'.find('x'))",
        "options": ['0', '-1', 'Error', 'x'],
        "correctIndex": 1,
    },
    {
        "id": 'python-095',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nnums = [1, 2, 3]\nprint(nums.pop())',
        "options": ['1', '2', '3', '[1, 2]'],
        "correctIndex": 2,
    },
    {
        "id": 'python-096',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nnums = [1, 2, 3]\nnums.pop()\nprint(nums)',
        "options": ['[1, 2]', '[1, 2, 3]', '3', 'Error'],
        "correctIndex": 0,
    },
    {
        "id": 'python-097',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nprint(7 & 3)',
        "options": ['1', '2', '3', '7'],
        "correctIndex": 2,
    },
    {
        "id": 'python-098',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nprint(7 | 8)',
        "options": ['7', '8', '15', '0'],
        "correctIndex": 2,
    },
    {
        "id": 'python-099',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nprint(7 ^ 3)',
        "options": ['3', '4', '7', '0'],
        "correctIndex": 1,
    },
    {
        "id": 'python-100',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nprint(int(3.9))',
        "options": ['3', '4', '3.9', 'Error'],
        "correctIndex": 0,
    },
    {
        "id": 'python-101',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nprint(5 / 2 == 2.5)',
        "options": ['True', 'False', '2.5', 'Error'],
        "correctIndex": 0,
    },
    {
        "id": 'python-102',
        "language": 'Python',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nprint(5 // 2 == 2.5)',
        "options": ['True', 'False', '2.5', 'Error'],
        "correctIndex": 1,
    },
    {
        "id": 'c-052',
        "language": 'C',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nint x = 6;\nprintf("%d", x - 2);',
        "options": ['2', '4', '6', '8'],
        "correctIndex": 1,
    },
    {
        "id": 'c-053',
        "language": 'C',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nprintf("%d", 3 * 3 + 1);',
        "options": ['10', '12', '9', '7'],
        "correctIndex": 0,
    },
    {
        "id": 'c-054',
        "language": 'C',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nint x = 2;\nx = x + x;\nprintf("%d", x);',
        "options": ['2', '3', '4', '22'],
        "correctIndex": 2,
    },
    {
        "id": 'c-055',
        "language": 'C',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nprintf("%d", 12 / 4);',
        "options": ['3', '4', '3.0', '48'],
        "correctIndex": 0,
    },
    {
        "id": 'c-056',
        "language": 'C',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nprintf("%d", 12 % 5);',
        "options": ['1', '2', '3', '5'],
        "correctIndex": 1,
    },
    {
        "id": 'c-057',
        "language": 'C',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nint ok = 0;\nprintf("%d", ok);',
        "options": ['1', '0', 'false', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'c-058',
        "language": 'C',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nint a[] = {4, 5};\nprintf("%d", a[0]);',
        "options": ['0', '4', '5', '2'],
        "correctIndex": 1,
    },
    {
        "id": 'c-059',
        "language": 'C',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nchar s[] = "cat";\nprintf("%s", s);',
        "options": ['cat', 's', 'c', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'c-060',
        "language": 'C',
        "course": 'Easy Output',
        "prompt": 'What is printed?\nint n = 9;\nprintf("%d", n >= 10);',
        "options": ['1', '0', '9', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'c-061',
        "language": 'C',
        "course": 'Easy Output',
        "prompt": 'Which operator gives the address of a variable?',
        "options": ['*', '&', '%', '->'],
        "correctIndex": 1,
    },
    {
        "id": 'c-062',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint x = 2;\nx += 1;\nprintf("%d", x);',
        "options": ['2', '3', '4', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'c-063',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint sum = 0;\nfor (int i = 2; i <= 6; i += 2) {\n    sum += i;\n}\nprintf("%d", sum);',
        "options": ['6', '8', '10', '12'],
        "correctIndex": 3,
    },
    {
        "id": 'c-064',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nfor (int i = 3; i > 0; i--) {\n    printf("%d", i);\n}',
        "options": ['012', '321', '123', '30'],
        "correctIndex": 1,
    },
    {
        "id": 'c-065',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint x = 4;\nprintf("%s", x % 2 == 0 ? "even" : "odd");',
        "options": ['even', 'odd', '1', '4'],
        "correctIndex": 0,
    },
    {
        "id": 'c-066',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nprintf("%d", strchr("level", \'e\') != NULL);',
        "options": ['1', '0', 'e', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'c-067',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nprintf("%ld", strchr("level", \'v\') - "level");',
        "options": ['0', '1', '2', '-1'],
        "correctIndex": 2,
    },
    {
        "id": 'c-068',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nprintf("%ld", strrchr("level", \'e\') - "level");',
        "options": ['1', '2', '3', '4'],
        "correctIndex": 2,
    },
    {
        "id": 'c-069',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint nums[] = {5, 6, 7, 8};\nprintf("%zu", sizeof(nums) / sizeof(nums[0]) - 1);',
        "options": ['2', '3', '4', '8'],
        "correctIndex": 1,
    },
    {
        "id": 'c-070',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint nums[] = {5, 6, 7};\nprintf("%d", nums[2]);',
        "options": ['5', '6', '7', '2'],
        "correctIndex": 2,
    },
    {
        "id": 'c-071',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint x = 1;\nwhile (x <= 4) {\n    x *= 2;\n}\nprintf("%d", x);',
        "options": ['4', '5', '8', '16'],
        "correctIndex": 2,
    },
    {
        "id": 'c-072',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint count = 0;\nfor (int i = 0; i < 5; i++) {\n    if (i % 2 == 0) count++;\n}\nprintf("%d", count);',
        "options": ['2', '3', '4', '5'],
        "correctIndex": 1,
    },
    {
        "id": 'c-073',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint a = 3;\nint b = 4;\nprintf("%d", a > b ? a : b);',
        "options": ['3', '4', '1', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'c-074',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nprintf("%d", strcmp("abc", "ABC") == 0);',
        "options": ['1', '0', 'abc', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'c-075',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint x = 6;\nprintf("%d", x > 3 && x < 5);',
        "options": ['1', '0', '6', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'c-076',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint x = 6;\nprintf("%d", x > 3 || x < 5);',
        "options": ['1', '0', '6', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'c-077',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint n = 1;\nswitch (n) {\n    case 1: printf("A"); break;\n    default: printf("B");\n}',
        "options": ['A', 'B', 'AB', 'Nothing'],
        "correctIndex": 0,
    },
    {
        "id": 'c-078',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint x = 5;\nprintf("%d", ++x);',
        "options": ['5', '6', '4', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'c-079',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint x = 5;\nprintf("%d", x++);',
        "options": ['5', '6', '4', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'c-080',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint x = 5;\nx++;\nprintf("%d", x);',
        "options": ['5', '6', '4', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'c-081',
        "language": 'C',
        "course": 'Medium Output',
        "prompt": 'What is printed?\nint x = 5;\n--x;\nprintf("%d", x);',
        "options": ['5', '6', '4', 'Compilation error'],
        "correctIndex": 2,
    },
    {
        "id": 'c-082',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = 5;\nprintf("%d", x / 2 * 2);',
        "options": ['4', '5', '2', '5.0'],
        "correctIndex": 0,
    },
    {
        "id": 'c-083',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = 5;\nprintf("%d", x * 2 / 4);',
        "options": ['2', '2.5', '3', '10'],
        "correctIndex": 0,
    },
    {
        "id": 'c-084',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = -5;\nprintf("%d", x % 2);',
        "options": ['1', '-1', '0', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'c-085',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint a[] = {1, 2, 3};\nint *p = a;\nprintf("%d", *(p + 2));',
        "options": ['1', '2', '3', 'Compilation error'],
        "correctIndex": 2,
    },
    {
        "id": 'c-086',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint a[] = {1, 2, 3};\nint *p = a + 1;\nprintf("%d", *p);',
        "options": ['1', '2', '3', 'Address'],
        "correctIndex": 1,
    },
    {
        "id": 'c-087',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nchar s[] = "abc";\nprintf("%c", *(s + 2));',
        "options": ['a', 'b', 'c', 'Compilation error'],
        "correctIndex": 2,
    },
    {
        "id": 'c-088',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nchar s[] = "abc";\nprintf("%zu", strlen(s));',
        "options": ['3', '4', '2', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'c-089',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nchar s[] = "abc";\nprintf("%zu", sizeof(s));',
        "options": ['3', '4', '2', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'c-090',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nprintf("%d", 7 & 3);',
        "options": ['1', '2', '3', '7'],
        "correctIndex": 2,
    },
    {
        "id": 'c-091',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nprintf("%d", 7 | 8);',
        "options": ['7', '8', '15', '0'],
        "correctIndex": 2,
    },
    {
        "id": 'c-092',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nprintf("%d", 7 ^ 3);',
        "options": ['3', '4', '7', '0'],
        "correctIndex": 1,
    },
    {
        "id": 'c-093',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nprintf("%d", (int)3.9);',
        "options": ['3', '4', '3.9', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'c-094',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\ndouble d = 5 / 2;\nprintf("%.1f", d);',
        "options": ['2', '2.0', '2.5', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'c-095',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\ndouble d = 5 / 2.0;\nprintf("%.1f", d);',
        "options": ['2', '2.0', '2.5', 'Compilation error'],
        "correctIndex": 2,
    },
    {
        "id": 'c-096',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = 0;\nprintf("%d", x == 0 && x + 1 == 1);',
        "options": ['1', '0', '2', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'c-097',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = 0;\nprintf("%d", x == 1 || x + 1 == 1);',
        "options": ['1', '0', '2', 'Compilation error'],
        "correctIndex": 0,
    },
    {
        "id": 'c-098',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint a = 1;\nint b = 2;\nprintf("%d", a += b += 3);',
        "options": ['4', '5', '6', 'Compilation error'],
        "correctIndex": 2,
    },
    {
        "id": 'c-099',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint a = 1;\nint b = 2;\nprintf("%d %d", a, b += 3);',
        "options": ['1 2', '1 5', '4 5', 'Compilation error'],
        "correctIndex": 1,
    },
    {
        "id": 'c-100',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = 16;\nprintf("%d", x >> 2);',
        "options": ['2', '4', '8', '64'],
        "correctIndex": 1,
    },
    {
        "id": 'c-101',
        "language": 'C',
        "course": 'Hard Output',
        "prompt": 'What is printed?\nint x = 3;\nprintf("%d", x << 2);',
        "options": ['6', '9', '12', '1'],
        "correctIndex": 2,
    }
]

ARENA_QUESTIONS.extend([
    {
        "id": "cpp-001",
        "language": "C++",
        "course": "Easy Output",
        "prompt": "What is printed?\nint x = 4;\ncout << x++;",
        "options": ["4", "5", "3", "Compilation error"],
        "correctIndex": 0,
    },
    {
        "id": "cpp-002",
        "language": "C++",
        "course": "Easy Output",
        "prompt": "What is printed?\ncout << 2 + 3 << \"4\";",
        "options": ["54", "234", "9", "Compilation error"],
        "correctIndex": 0,
    },
    {
        "id": "cpp-003",
        "language": "C++",
        "course": "Easy Output",
        "prompt": "What is printed?\nint a = 7 / 2;\ncout << a;",
        "options": ["3.5", "4", "3", "2"],
        "correctIndex": 2,
    },
    {
        "id": "cpp-004",
        "language": "C++",
        "course": "Easy Output",
        "prompt": "What is printed?\ncout << 7 % 3;",
        "options": ["1", "2", "3", "0"],
        "correctIndex": 0,
    },
    {
        "id": "cpp-005",
        "language": "C++",
        "course": "Easy Output",
        "prompt": "What is printed?\nbool ready = true;\ncout << !ready;",
        "options": ["true", "false", "0", "1"],
        "correctIndex": 2,
    },
    {
        "id": "cpp-006",
        "language": "C++",
        "course": "Easy Output",
        "prompt": "What is printed?\nint nums[] = {10, 20, 30};\ncout << nums[1];",
        "options": ["10", "20", "30", "1"],
        "correctIndex": 1,
    },
    {
        "id": "cpp-007",
        "language": "C++",
        "course": "Medium Output",
        "prompt": "What is printed?\nstring word = \"Code\";\ncout << word.length();",
        "options": ["3", "4", "Code", "true"],
        "correctIndex": 1,
    },
    {
        "id": "cpp-008",
        "language": "C++",
        "course": "Medium Output",
        "prompt": "What is printed?\nfor (int i = 0; i < 3; i++) {\n    cout << i;\n}",
        "options": ["012", "123", "0123", "3"],
        "correctIndex": 0,
    },
    {
        "id": "cpp-009",
        "language": "C++",
        "course": "Medium Output",
        "prompt": "What is printed?\nint total = 0;\nfor (int i = 1; i <= 3; i++) {\n    total += i;\n}\ncout << total;",
        "options": ["3", "5", "6", "7"],
        "correctIndex": 2,
    },
    {
        "id": "cpp-010",
        "language": "C++",
        "course": "Medium Output",
        "prompt": "What is printed?\nvector<int> nums = {2, 4, 6};\ncout << nums.size() + nums[0];",
        "options": ["3", "5", "6", "8"],
        "correctIndex": 1,
    },
    {
        "id": "cpp-011",
        "language": "C++",
        "course": "Medium Output",
        "prompt": "What is printed?\nint x = 5;\nif (x > 5) cout << \"A\";\nelse cout << \"B\";",
        "options": ["A", "B", "5", "Nothing"],
        "correctIndex": 1,
    },
    {
        "id": "cpp-012",
        "language": "C++",
        "course": "Medium Output",
        "prompt": "What is printed?\nint x = 3;\ncout << ++x;",
        "options": ["3", "4", "2", "Compilation error"],
        "correctIndex": 1,
    },
    {
        "id": "cpp-013",
        "language": "C++",
        "course": "Hard Output",
        "prompt": "What is printed?\nint x = 5;\ncout << x++;",
        "options": ["5", "6", "4", "Compilation error"],
        "correctIndex": 0,
    },
    {
        "id": "cpp-014",
        "language": "C++",
        "course": "Hard Output",
        "prompt": "What is printed?\nint x = 16;\ncout << (x >> 2);",
        "options": ["2", "4", "8", "64"],
        "correctIndex": 1,
    },
    {
        "id": "cpp-015",
        "language": "C++",
        "course": "Hard Output",
        "prompt": "What is printed?\nint a = 1;\nint b = 2;\ncout << (a += b += 3);",
        "options": ["4", "5", "6", "Compilation error"],
        "correctIndex": 2,
    },
    {
        "id": "cs-001",
        "language": "C#",
        "course": "Easy Output",
        "prompt": "What is printed?\nint x = 4;\nConsole.WriteLine(x++);",
        "options": ["4", "5", "3", "Compilation error"],
        "correctIndex": 0,
    },
    {
        "id": "cs-002",
        "language": "C#",
        "course": "Easy Output",
        "prompt": "What is printed?\nstring s = \"Go\";\nConsole.WriteLine(s + 2 + 3);",
        "options": ["Go5", "Go23", "5Go", "Compilation error"],
        "correctIndex": 1,
    },
    {
        "id": "cs-003",
        "language": "C#",
        "course": "Easy Output",
        "prompt": "What is printed?\nConsole.WriteLine(2 + 3 + \"4\");",
        "options": ["54", "234", "9", "Compilation error"],
        "correctIndex": 0,
    },
    {
        "id": "cs-004",
        "language": "C#",
        "course": "Easy Output",
        "prompt": "What is printed?\nConsole.WriteLine(\"4\" + 2 + 3);",
        "options": ["9", "423", "45", "Compilation error"],
        "correctIndex": 1,
    },
    {
        "id": "cs-005",
        "language": "C#",
        "course": "Easy Output",
        "prompt": "What is printed?\nint a = 7 / 2;\nConsole.WriteLine(a);",
        "options": ["3.5", "4", "3", "2"],
        "correctIndex": 2,
    },
    {
        "id": "cs-006",
        "language": "C#",
        "course": "Easy Output",
        "prompt": "What is printed?\nConsole.WriteLine(7 % 3);",
        "options": ["1", "2", "3", "0"],
        "correctIndex": 0,
    },
    {
        "id": "cs-007",
        "language": "C#",
        "course": "Medium Output",
        "prompt": "What is printed?\nbool ready = true;\nConsole.WriteLine(!ready);",
        "options": ["True", "False", "0", "Compilation error"],
        "correctIndex": 1,
    },
    {
        "id": "cs-008",
        "language": "C#",
        "course": "Medium Output",
        "prompt": "What is printed?\nint[] nums = {10, 20, 30};\nConsole.WriteLine(nums[1]);",
        "options": ["10", "20", "30", "1"],
        "correctIndex": 1,
    },
    {
        "id": "cs-009",
        "language": "C#",
        "course": "Medium Output",
        "prompt": "What is printed?\nstring word = \"Code\";\nConsole.WriteLine(word.Length);",
        "options": ["3", "4", "Code", "true"],
        "correctIndex": 1,
    },
    {
        "id": "cs-010",
        "language": "C#",
        "course": "Medium Output",
        "prompt": "What is printed?\nfor (int i = 0; i < 3; i++) {\n    Console.Write(i);\n}",
        "options": ["012", "123", "0123", "3"],
        "correctIndex": 0,
    },
    {
        "id": "cs-011",
        "language": "C#",
        "course": "Medium Output",
        "prompt": "What is printed?\nint total = 0;\nfor (int i = 1; i <= 3; i++) {\n    total += i;\n}\nConsole.WriteLine(total);",
        "options": ["3", "5", "6", "7"],
        "correctIndex": 2,
    },
    {
        "id": "cs-012",
        "language": "C#",
        "course": "Medium Output",
        "prompt": "What is printed?\nint x = 3;\nConsole.WriteLine(++x);",
        "options": ["3", "4", "2", "Compilation error"],
        "correctIndex": 1,
    },
    {
        "id": "cs-013",
        "language": "C#",
        "course": "Hard Output",
        "prompt": "What is printed?\nint x = 5;\nif (x > 5) Console.WriteLine(\"A\");\nelse Console.WriteLine(\"B\");",
        "options": ["A", "B", "5", "Nothing"],
        "correctIndex": 1,
    },
    {
        "id": "cs-014",
        "language": "C#",
        "course": "Hard Output",
        "prompt": "What is printed?\nint? x = null;\nConsole.WriteLine(x ?? 7);",
        "options": ["0", "7", "null", "Compilation error"],
        "correctIndex": 1,
    },
    {
        "id": "cs-015",
        "language": "C#",
        "course": "Hard Output",
        "prompt": "What is printed?\nstring s = \"abc\";\nConsole.WriteLine(s[1]);",
        "options": ["a", "b", "c", "1"],
        "correctIndex": 1,
    },
])


@dataclass
class ArenaPlayer:
    id: str
    name: str
    rating: int
    languages: list[str]
    avatar_id: str | None
    websocket: Optional[WebSocket]
    invite_code: str | None = None
    is_bot: bool = False
    skill: int = 82

    def public(self) -> dict[str, Any]:
        return {
            "id": self.id,
            "name": self.name,
            "rating": self.rating,
            "languages": self.languages,
            "avatarId": self.avatar_id,
            "isBot": self.is_bot,
        }


@dataclass
class ArenaMatch:
    id: str
    players: list[ArenaPlayer]
    questions: list[dict[str, Any]]
    question_index: int = 0
    scores: dict[str, int] = field(default_factory=dict)
    streaks: dict[str, int] = field(default_factory=dict)
    answers: dict[str, dict[str, Any]] = field(default_factory=dict)
    question_started_at: float = field(default_factory=time.monotonic)
    tasks: list[asyncio.Task] = field(default_factory=list)


class ArenaManager:
    def __init__(self) -> None:
        self.waiting: list[ArenaPlayer] = []
        self.waiting_invites: dict[str, ArenaPlayer] = {}
        self.waiting_timeout_tasks: dict[str, asyncio.Task] = {}
        self.matches: dict[str, ArenaMatch] = {}
        self.player_matches: dict[str, str] = {}
        self.lock = asyncio.Lock()

    async def connect(self, websocket: WebSocket) -> None:
        await websocket.accept()
        player: ArenaPlayer | None = None

        try:
            hello = await websocket.receive_json()
            if hello.get("type") != "find_match":
                await websocket.send_json({"type": "error", "message": "Expected find_match"})
                return

            player = ArenaPlayer(
                id=str(hello.get("userId") or uuid.uuid4()),
                name=str(hello.get("name") or "Player"),
                rating=int(hello.get("rating") or 1000),
                languages=self._normalize_languages(hello.get("languages")),
                avatar_id=hello.get("avatarId"),
                websocket=websocket,
                invite_code=self._normalize_invite_code(hello.get("inviteCode")),
            )
            await self.enqueue(player)

            while True:
                message = await websocket.receive_json()
                if message.get("type") == "answer":
                    await self.submit_answer(player.id, message)
                elif message.get("type") == "cancel_matchmaking":
                    await self.remove_player(player.id)
                    await websocket.send_json({"type": "matchmaking_cancelled"})
                elif message.get("type") == "forfeit":
                    await self.forfeit(player.id)

        except WebSocketDisconnect:
            if player is not None:
                await self.remove_player(player.id)

    async def enqueue(self, player: ArenaPlayer) -> None:
        if player.invite_code:
            await self.enqueue_invite(player)
            return

        async with self.lock:
            self.waiting = [waiting for waiting in self.waiting if waiting.id != player.id]
            opponent = self._best_waiting_opponent(player)
            if opponent is None:
                self.waiting.append(player)
                self._cancel_waiting_timeout(player.id)
                self.waiting_timeout_tasks[player.id] = asyncio.create_task(self._match_with_bot_after_timeout(player.id))
                await player.websocket.send_json({"type": "matchmaking_started", "timeoutMs": 3_000})
                return

            self.waiting = [waiting for waiting in self.waiting if waiting.id != opponent.id]
            self._cancel_waiting_timeout(opponent.id)

        await self.start_match(opponent, player)

    async def enqueue_invite(self, player: ArenaPlayer) -> None:
        assert player.invite_code is not None
        async with self.lock:
            opponent = self.waiting_invites.get(player.invite_code)
            if opponent is None or opponent.id == player.id:
                self.waiting_invites[player.invite_code] = player
                await player.websocket.send_json({"type": "matchmaking_started", "timeoutMs": 0})
                return

            self.waiting_invites.pop(player.invite_code, None)

        await self.start_match(opponent, player)

    async def _match_with_bot_after_timeout(self, waiting_player_id: str) -> None:
        await asyncio.sleep(3)
        async with self.lock:
            player = next((waiting for waiting in self.waiting if waiting.id == waiting_player_id), None)
            if player is None:
                return
            self.waiting = [waiting for waiting in self.waiting if waiting.id != waiting_player_id]
            self.waiting_timeout_tasks.pop(waiting_player_id, None)

        await self.start_match(player, self._create_bot_for(player))

    async def start_match(self, first: ArenaPlayer, second: ArenaPlayer) -> None:
        self._cancel_waiting_timeout(first.id)
        self._cancel_waiting_timeout(second.id)
        questions = self._select_questions(first.languages, second.languages)
        match = ArenaMatch(
            id=str(uuid.uuid4()),
            players=[first, second],
            questions=questions,
            scores={first.id: 0, second.id: 0},
            streaks={first.id: 0, second.id: 0},
        )
        self.matches[match.id] = match
        self.player_matches[first.id] = match.id
        self.player_matches[second.id] = match.id

        await self.broadcast(match, {
            "type": "match_found",
            "matchId": match.id,
            "players": [first.public(), second.public()],
            "questionCount": len(questions),
        })
        await self.send_question(match)

    async def send_question(self, match: ArenaMatch) -> None:
        self._cancel_match_tasks(match)
        match.answers.clear()
        match.question_started_at = time.monotonic()
        question = match.questions[match.question_index]
        public_question = {key: value for key, value in question.items() if key != "correctIndex"}
        await self.broadcast(match, {
            "type": "question",
            "matchId": match.id,
            "questionIndex": match.question_index,
            "questionCount": len(match.questions),
            "timeLimitMs": QUESTION_TIME_MS,
            "question": public_question,
        })
        for player in match.players:
            if player.is_bot:
                match.tasks.append(asyncio.create_task(self._answer_as_bot(match.id, player.id)))
        match.tasks.append(asyncio.create_task(self._timeout_unanswered(match.id, match.question_index)))

    async def submit_answer(self, player_id: str, message: dict[str, Any]) -> None:
        match = self._match_for_player(player_id)
        if match is None or player_id in match.answers:
            return

        await self._record_answer(player_id, int(message.get("selectedIndex", -1)))

    async def _record_answer(self, player_id: str, selected_index: int) -> None:
        match = self._match_for_player(player_id)
        if match is None or player_id in match.answers:
            return

        elapsed_ms = int((time.monotonic() - match.question_started_at) * 1000)
        question = match.questions[match.question_index]
        timed_out = elapsed_ms > QUESTION_TIME_MS
        correct = (not timed_out) and selected_index == question["correctIndex"]
        delta = self._score_answer(player_id, match, correct, elapsed_ms, timed_out)

        match.answers[player_id] = {
            "selectedIndex": selected_index,
            "correct": correct,
            "elapsedMs": elapsed_ms,
            "delta": delta,
        }

        await self.broadcast(match, {
            "type": "answer_result",
            "matchId": match.id,
            "playerId": player_id,
            "correct": correct,
            "elapsedMs": elapsed_ms,
            "delta": delta,
            "scores": match.scores,
        })

        if len(match.answers) == len(match.players):
            await self.advance_or_finish(match)

    async def _answer_as_bot(self, match_id: str, bot_id: str) -> None:
        match = self.matches.get(match_id)
        bot = self._player_in_match(match, bot_id)
        if match is None or bot is None:
            return

        delay_ms = self._bot_reaction_ms(bot, match.question_index)
        await asyncio.sleep(delay_ms / 1000)
        match = self.matches.get(match_id)
        bot = self._player_in_match(match, bot_id)
        if match is None or bot is None or bot_id in match.answers:
            return

        question = match.questions[match.question_index]
        accuracy = min(max(bot.skill + random.randint(-7, 5), 55), 92) / 100
        if random.random() <= accuracy:
            selected_index = question["correctIndex"]
        else:
            wrong_options = [index for index in range(len(question["options"])) if index != question["correctIndex"]]
            selected_index = random.choice(wrong_options)
        await self._record_answer(bot_id, selected_index)

    async def _timeout_unanswered(self, match_id: str, question_index: int) -> None:
        await asyncio.sleep((QUESTION_TIME_MS + 700) / 1000)
        match = self.matches.get(match_id)
        if match is None or match.question_index != question_index:
            return
        missing_player_ids = [player.id for player in match.players if player.id not in match.answers]
        for player_id in missing_player_ids:
            await self._record_answer(player_id, -1)

    async def advance_or_finish(self, match: ArenaMatch) -> None:
        await self.broadcast(match, {
            "type": "question_finished",
            "matchId": match.id,
            "correctIndex": match.questions[match.question_index]["correctIndex"],
            "scores": match.scores,
        })
        match.question_index += 1
        if match.question_index >= len(match.questions):
            await self.finish_match(match)
            return

        await asyncio.sleep(1.2)
        await self.send_question(match)

    async def finish_match(self, match: ArenaMatch) -> None:
        self._cancel_match_tasks(match)
        winner_id = max(match.scores, key=match.scores.get)
        if len(set(match.scores.values())) == 1:
            winner_id = None
        await self.broadcast(match, {
            "type": "match_finished",
            "matchId": match.id,
            "winnerId": winner_id,
            "scores": match.scores,
        })
        self._remove_match(match.id)

    async def forfeit(self, player_id: str) -> None:
        match = self._match_for_player(player_id)
        if match is None:
            await self.remove_player(player_id)
            return

        winner = next((player for player in match.players if player.id != player_id), None)
        await self.broadcast(match, {
            "type": "match_finished",
            "matchId": match.id,
            "winnerId": winner.id if winner else None,
            "forfeitBy": player_id,
            "scores": match.scores,
        })
        self._remove_match(match.id)

    async def remove_player(self, player_id: str) -> None:
        async with self.lock:
            waiting_count = len(self.waiting)
            self.waiting = [player for player in self.waiting if player.id != player_id]
            if len(self.waiting) != waiting_count:
                self._cancel_waiting_timeout(player_id)
            invite_code = next(
                (code for code, player in self.waiting_invites.items() if player.id == player_id),
                None,
            )
            if invite_code is not None:
                self.waiting_invites.pop(invite_code, None)

        match = self._match_for_player(player_id)
        if match is not None:
            await self.forfeit(player_id)

    async def broadcast(self, match: ArenaMatch, payload: dict[str, Any]) -> None:
        for player in match.players:
            if player.websocket is not None:
                await player.websocket.send_json(payload)

    def _score_answer(
        self,
        player_id: str,
        match: ArenaMatch,
        correct: bool,
        elapsed_ms: int,
        timed_out: bool,
    ) -> int:
        if not correct:
            delta = TIMEOUT_PENALTY if timed_out else WRONG_ANSWER_PENALTY
            match.streaks[player_id] = 0
        else:
            streak = match.streaks[player_id] + 1
            speed_bonus = min(max((QUESTION_TIME_MS - elapsed_ms) // 180, 0), 60)
            streak_bonus = min(streak * 8, 32)
            delta = CORRECT_ANSWER_POINTS + speed_bonus + streak_bonus
            match.streaks[player_id] = streak
        match.scores[player_id] += int(delta)
        return int(delta)

    def _select_questions(self, first_languages: list[str], second_languages: list[str]) -> list[dict[str, Any]]:
        first = {self._canonical_language(lang).lower() for lang in first_languages}
        second = {self._canonical_language(lang).lower() for lang in second_languages}
        shared = first & second
        question_languages = shared or (first | second)
        pool = [q for q in ARENA_QUESTIONS if q["language"].lower() in question_languages]
        if not pool:
            pool = ARENA_QUESTIONS
        random.shuffle(pool)
        return pool[:5]

    def _best_waiting_opponent(self, player: ArenaPlayer) -> ArenaPlayer | None:
        compatible = [
            waiting for waiting in self.waiting
            if self._language_overlap(waiting.languages, player.languages) > 0
        ]
        if not compatible:
            return None
        return max(
            compatible,
            key=lambda waiting: (
                self._language_overlap(waiting.languages, player.languages),
                -abs(waiting.rating - player.rating),
            ),
        )

    def _language_overlap(self, first_languages: list[str], second_languages: list[str]) -> int:
        first = {self._canonical_language(lang).lower() for lang in first_languages}
        second = {self._canonical_language(lang).lower() for lang in second_languages}
        return len(first & second)

    def _normalize_languages(self, value: Any) -> list[str]:
        if not isinstance(value, list):
            return ["Java"]
        languages = []
        for item in value:
            language = self._canonical_language(str(item).strip())
            if language and language not in languages:
                languages.append(language)
        return languages or ["Java"]

    def _canonical_language(self, language: str) -> str:
        aliases = {
            "java": "Java",
            "python": "Python",
            "c": "C",
            "clang": "C",
            "cpp": "C++",
            "c++": "C++",
            "cplusplus": "C++",
            "csharp": "C#",
            "c#": "C#",
            "cs": "C#",
        }
        return aliases.get(language.strip().lower(), language.strip())

    def _normalize_invite_code(self, value: Any) -> str | None:
        if value is None:
            return None
        code = "".join(ch for ch in str(value).upper() if ch.isalnum())
        return code[:12] or None

    def _create_bot_for(self, player: ArenaPlayer) -> ArenaPlayer:
        bot_names = ["ByteRunner", "StackQueen", "LoopMage", "AlgoNinja", "NullPointer"]
        bot_rating = player.rating + random.randint(-90, 130)
        return ArenaPlayer(
            id=f"bot-{uuid.uuid4()}",
            name=random.choice(bot_names),
            rating=max(bot_rating, 700),
            languages=player.languages or ["Java"],
            avatar_id=random.choice(["robot", "ninja", "owl", "alien"]),
            websocket=None,
            is_bot=True,
            skill=random.randint(76, 88),
        )

    def _bot_reaction_ms(self, bot: ArenaPlayer, question_index: int) -> int:
        base = random.randint(3_000, 8_600)
        if bot.skill >= 85:
            base -= random.randint(350, 1_200)
        if question_index == 0:
            base += random.randint(350, 900)
        return max(1_900, min(base, 10_500))

    def _player_in_match(self, match: ArenaMatch | None, player_id: str) -> ArenaPlayer | None:
        if match is None:
            return None
        return next((player for player in match.players if player.id == player_id), None)

    def _cancel_waiting_timeout(self, player_id: str | None = None) -> None:
        if player_id is None:
            tasks = list(self.waiting_timeout_tasks.values())
            self.waiting_timeout_tasks.clear()
        else:
            task = self.waiting_timeout_tasks.pop(player_id, None)
            tasks = [task] if task is not None else []
        for task in tasks:
            task.cancel()

    def _cancel_match_tasks(self, match: ArenaMatch) -> None:
        current_task = asyncio.current_task()
        remaining_tasks = []
        for task in match.tasks:
            if task is current_task:
                remaining_tasks.append(task)
            else:
                task.cancel()
        match.tasks = remaining_tasks

    def _match_for_player(self, player_id: str) -> ArenaMatch | None:
        match_id = self.player_matches.get(player_id)
        if not match_id:
            return None
        return self.matches.get(match_id)

    def _remove_match(self, match_id: str) -> None:
        match = self.matches.pop(match_id, None)
        if match is None:
            return
        for player in match.players:
            self.player_matches.pop(player.id, None)


arena_manager = ArenaManager()
