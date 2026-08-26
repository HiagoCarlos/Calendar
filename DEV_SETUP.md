# 🚀 Guia de Desenvolvimento & Ambiente (Project A / Calendar)

Este documento detalha **tudo o que foi configurado e otimizado no projeto** e serve como um guia completo para qualquer desenvolvedor rodar, debugar e contribuir com a aplicação em sua própria máquina (Linux, macOS ou Windows com WSL2).

---

## 📌 1. Resumo das Modificações e Otimizações Realizadas

Para tornar a aplicação estável em servidores VPS e fluida no desenvolvimento local, implementamos as seguintes melhorias:

### A. Proteção de Memória e Estabilidade (OOM Hardening)

- **Backend (Spring Boot 4 / Java 21+):**
  - Definimos limites de Heap JVM explícitos: `MAVEN_OPTS="-Xms128m -Xmx384m"` e `JAVA_OPTS="-Xms128m -Xmx384m -XX:+UseG1GC -XX:MaxRAMPercentage=75.0"`.
  - Isso impede que a JVM tente alocar 2GB+ de memória RAM da máquina host.
- **Frontend (Angular 22 + Node 22):**
  - Otimizamos o uso de memória do Node via `NODE_OPTIONS="--max-old-space-size=1280"` (no compose de desenvolvimento) e `384M` (em produção).
- **Docker Compose Limits:**
  - Configuramos limites rígidos de recursos (`deploy.resources.limits`) para cada container:
    - `calendar-db-dev`: 256MB RAM / 1.0 CPU
    - `calendar-backend-dev`: 768MB RAM / 1.5 CPU
    - `calendar-frontend-dev`: 1536MB RAM / 1.5 CPU

### B. Angular 22 & Vite Dev Server (Acesso Externo e Hot Reload)

- O Angular 22 utiliza o Vite como servidor de desenvolvimento. Por padrão, o Vite bloqueia requisições vindas de IPs externos ou domínios não locais (`Blocked request from host...`).
- **Correção Aplicada:** O comando de inicialização do frontend foi configurado com:
  ```bash
  ng serve --host 0.0.0.0 --allowed-hosts --poll 2000
  ```

  - `--host 0.0.0.0`: Permite acesso de qualquer IP na rede local ou VPS.
  - `--allowed-hosts`: Evita tela branca por bloqueio de host do Vite.
  - `--poll 2000`: Garante que o Hot Reload funcione perfeitamente em bind-mounts de Windows (WSL2), Mac e Linux.

### C. Modernização do Frontend (Angular 22 + Tailwind CSS v4)

- **Padrão de Código Moderno:** Componentes Standalone com `ChangeDetectionStrategy.OnPush`, Signals (`signal()`), controle de fluxo moderno (`@if`, `@for`) e formulários reativos tipados (`NonNullableFormBuilder`).
- **Tipografia Global:** Fonte **Inter** (`Google Fonts`) configurada no `styles.css` e `@theme` do TailwindCSS v4.
- **Roteamento Canônico:** Rota de cadastro padronizada como `/signup`, com alias automático para `/cadastro` e integração na tela de `/login`.

---

## 🛠️ 2. Como Rodar o Projeto na Sua Máquina

Você tem duas opções de ambiente: **Via Docker (Zero Config)** ou **Híbrido/Local Nativo**.

---

### 🐳 Opção A: 100% via Docker (Recomendado)

> **Ideal para quem quer subir tudo com um único comando sem precisar instalar Java, Maven ou Node.js na máquina host.**

#### Pré-requisitos:

- Docker & Docker Compose instalados.

#### Passo a Passo:

1. **Clone o repositório:**

   ```bash
   git clone git@github.com:Tecnologia-da-Informacao-BR/Calendar.git
   cd Calendar
   ```

2. **Inicie o ambiente de desenvolvimento (com Hot Reload ativo):**

   ```bash
   docker compose -f docker-compose.dev.yml up --build
   ```

   _(Ou adicione `-d` no final para rodar em background)_.

3. **Acesse as aplicações:**
   - 🌐 **Frontend (Angular):** [http://localhost:4200/signup](http://localhost:4200/signup)
   - ⚙️ **Backend (API Spring Boot):** [http://localhost:8080](http://localhost:8080)
   - 🗄️ **Banco PostgreSQL:** `localhost:5432` (User: `calendar`, Pass: `calendar`, DB: `calendar`)

---

### 💻 Opção B: Desenvolvimento Híbrido / Local Nativo

> **Ideal para quem prefere rodar o backend no IntelliJ/VS Code e o frontend com `npm start` para velocidade máxima de compilação e debugging.**

#### Pré-requisitos:

- **Java:** JDK 21 ou 25 instalado.
- **Node.js:** Versão 20+ ou 22+ e npm.
- **Docker:** Apenas para o banco de dados PostgreSQL.

#### Passo a Passo:

#### 1. Suba apenas o Banco de Dados via Docker:

```bash
docker compose -f docker-compose.dev.yml up -d db
```

#### 2. Execute o Backend (Spring Boot):

No terminal ou no seu IDE favorito:

```bash
cd backend
./mvnw spring-boot:run
```

> _No Windows (PowerShell/CMD): `mvnw.cmd spring-boot:run`_

#### 3. Execute o Frontend (Angular):

Em outro terminal:

```bash
cd frontend
npm install
npm start
```

> Acesse [http://localhost:4200/signup](http://localhost:4200/signup)

---

## 🧪 3. Como Executar os Testes

### Testes do Frontend (Vitest):

```bash
cd frontend
npm test -- --watch=false
```

### Testes do Backend (JUnit 5 / Testcontainers):

```bash
cd backend
./mvnw test
```

### Validar Build de Produção do Frontend:

```bash
cd frontend
npm run build
```

---

## 🗺️ 4. Mapa de Portas e Variáveis de Ambiente

| Serviço               | Porta Padrão (Local) | Variável de Ambiente | Descrição                         |
| :-------------------- | :------------------- | :------------------- | :-------------------------------- |
| **Frontend**          | `4200`               | `FRONTEND_PORT`      | Interface Web Angular             |
| **Backend**           | `8080`               | `BACKEND_PORT`       | API REST Spring Boot              |
| **PostgreSQL (Dev)**  | `5432`               | `POSTGRES_PORT`      | Banco de dados de desenvolvimento |
| **PostgreSQL (Test)** | `5433`               | `POSTGRES_TEST_PORT` | Banco isolado para testes         |

---

## ❓ 5. Solução de Problemas Comuns (Troubleshooting)

### 1. "Recebo tela branca ao abrir o frontend"

- **Causa:** O Vite bloqueou o host ou o cache do navegador reteve uma versão antiga.
- **Solução:** Abra com **`Ctrl + Shift + R`** ou verifique se o frontend foi iniciado com `--allowed-hosts`.

### 2. "O Docker diz 'port is already allocated' (ex: porta 5432 ou 8080 em uso)"

- **Solução:** Outro serviço (ou postgres local) está rodando na sua máquina. Você pode definir portas alternativas no arquivo `.env`:
  ```env
  FRONTEND_PORT=4201
  BACKEND_PORT=8081
  POSTGRES_PORT=5435
  ```

### 3. "Hot reload não atualiza no Windows / WSL2"

- **Solução:** O parâmetro `--poll 2000` já configurado no `Dockerfile.dev` e `npm start` garante que o sistema de arquivos notifique alterações a cada 2 segundos.
