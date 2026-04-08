# Git & GitHub 세팅 가이드

> **프로젝트**: dd_tableorder
> **원격 저장소**: https://github.com/khaerang-collab/dd_tableorder
> **팀원 수**: 4명

---

## 1. Git 설치 확인

```bash
git --version
```

설치되어 있지 않다면:

```bash
# Amazon Linux / RHEL
sudo dnf install -y git

# Ubuntu / Debian
sudo apt install -y git

# macOS
brew install git
```

---

## 2. Git 사용자 정보 설정

```bash
git config --global user.name "본인 GitHub 사용자명"
git config --global user.email "본인 GitHub 이메일"
```

설정 확인:

```bash
git config --global --list
```

---

## 3. GitHub CLI 설치

```bash
# Amazon Linux / RHEL
sudo dnf config-manager --add-repo https://cli.github.com/packages/rpm/gh-cli.repo
sudo dnf install -y gh

# Ubuntu / Debian
curl -fsSL https://cli.github.com/packages/githubcli-archive-keyring.gpg | sudo dd of=/usr/share/keyrings/githubcli-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/githubcli-archive-keyring.gpg] https://cli.github.com/packages stable main" | sudo tee /etc/apt/sources.list.d/github-cli.list > /dev/null
sudo apt update && sudo apt install -y gh

# macOS
brew install gh
```

---

## 4. GitHub 로그인

```bash
gh auth login
```

아래와 같이 선택:
1. `GitHub.com` 선택
2. `HTTPS` 선택
3. `Login with a web browser` 선택
4. 화면에 표시되는 코드를 복사 후 브라우저에서 입력

로그인 확인:

```bash
gh auth status
```

---

## 5. 저장소 클론

```bash
cd ~/environment
git clone https://github.com/khaerang-collab/dd_tableorder.git
cd dd_tableorder
```

---

## 6. 작업 흐름 (main 브랜치 직접 push)

> 빠른 개발을 위해 별도 브랜치 없이 `main`에 직접 push합니다.

### 작업 시작 전 - 최신 코드 가져오기

```bash
git pull origin main
```

### 변경사항 커밋 및 푸시

```bash
git add .
git commit -m "커밋 메시지"
git push origin main
```

### 충돌 발생 시

```bash
git pull origin main
# 충돌 파일 수정 후
git add .
git commit -m "충돌 해결: 설명"
git push origin main
```

---

## 7. 협업 시 주의사항

- **push 전에 반드시** `git pull origin main` 실행 (충돌 방지)
- 같은 파일을 동시에 수정하지 않도록 팀원 간 작업 영역 사전 협의
- 커밋 메시지는 명확하게 작성 (예: `feat: 메뉴 목록 API 추가`)

---

## 빠른 세팅 (한번에 실행)

아래 명령어를 순서대로 복사해서 실행하세요:

```bash
# 1) GitHub CLI 설치 (Amazon Linux)
sudo dnf config-manager --add-repo https://cli.github.com/packages/rpm/gh-cli.repo
sudo dnf install -y gh

# 2) GitHub 로그인
gh auth login

# 3) 저장소 클론
cd ~/environment
git clone https://github.com/khaerang-collab/dd_tableorder.git
cd dd_tableorder

# 4) Git 사용자 설정 (본인 정보로 변경)
git config user.name "본인 GitHub 사용자명"
git config user.email "본인 GitHub 이메일"

# 5) 확인
git remote -v
gh auth status
```

---

## 매일 작업 시작 시 실행

```bash
cd ~/environment/dd_tableorder
git pull origin main
```

## 작업 완료 후 push

```bash
git add .
git commit -m "작업 내용 설명"
git push origin main
```
