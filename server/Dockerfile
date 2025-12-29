FROM python:3.11-slim-bookworm

RUN apt-get update && apt-get install -y --no-install-recommends \
    openjdk-17-jdk-headless \
    && rm -rf /var/lib/apt/lists/*

RUN pip install uv

WORKDIR /app

COPY requirements.txt .
RUN uv init
RUN uv add -r requirements.txt

COPY app ./app

EXPOSE 8080
WORKDIR /app/app
CMD ["uv", "run", "main.py"]
