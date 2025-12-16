# KMA — Secure Banking Web App

KMA est une application bancaire web (Frontend Next.js + Backend Spring Boot) avec une approche **Security-by-Design**.

## Fonctionnalités (par rôle)

### Rôle USER

- **Authentification**
  - Inscription avec **OTP email** (activation du compte après vérification)
  - Connexion avec **2FA OTP email**
  - Logout (JWT blacklist)
  - Forgot password + Reset password via **OTP email**

- **Profil**
  - Consultation / mise à jour du profil
  - Attribution d’un **RIP** unique (20 chiffres) pour recevoir des transferts
  - Changement du mot de passe avec **OTP email** (depuis la page Profile)

- **Compte & Transactions**
  - Consultation du solde
  - Historique des transactions (envoyées / reçues)
  - Virement en **2 étapes**:
    - `initiate` (création d’une transaction PENDING + envoi OTP)
    - `verify` (validation OTP + exécution du transfert)

### Rôle ADMIN

- Connexion admin (JWT)
- Gestion des utilisateurs
  - Liste / recherche
  - Activation / désactivation
  - Soft-delete
- Gestion des transactions
  - Liste / filtre (par statut / user)
  - Détails
  - Reverse d’une transaction
- Consultation du profil admin

## Mesures de sécurité implémentées

## Détails des mesures de sécurité (Threat → Solution → Justification)

Cette section décrit les menaces principales adressées par le projet, la solution mise en place, et la raison du choix.

### Vue synthétique

| Threat (menace) | Solution implémentée | Justification / choix techno |
| --- | --- | --- |
| **Vol de session / usurpation d’identité** | **JWT stateless** + durée de vie configurée (`JWT_EXPIRATION`) | JWT est adapté aux APIs stateless, simple à proxyfier via Nginx et à consommer via un frontend SPA/Next.js. |
| **Réutilisation d’un token après logout** | **Blacklist** (revocation par `jti`) | Un JWT est “stateless” par défaut; la blacklist réintroduit une révocation contrôlée sans sessions serveur classiques. |
| **Brute-force login / abus des endpoints** | **Rate limiting filter** | Protection applicative simple et efficace contre les rafales de requêtes. |
| **Account takeover via password-only** | **2FA OTP email** (login user) | Réduit l’impact d’un mot de passe compromis. L’email est déjà l’identifiant, donc l’OTP email est cohérent et facile à déployer. |
| **Création de compte frauduleuse / emails non valides** | **OTP email signup** (activation après vérification) | Empêche la création de comptes avec des emails non contrôlés. |
| **Reset password non autorisé** | **Forgot/Reset password via OTP email** | Le reset nécessite une preuve de possession de l’email. |
| **Fraude / virement non autorisé** | **Virement en 2 étapes** (`initiate` + `verify` OTP) | Ajoute une confirmation forte avant mouvement de fonds, limite l’impact d’un token volé. |
| **CSRF (Cross-Site Request Forgery)** | **Double Submit Cookie** (`XSRF-TOKEN` + `X-XSRF-TOKEN`) | Compatible SPA + APIs stateless. Remplace la CSRF session-based de Spring (désactivée car JWT stateless). |
| **Interception trafic / MITM** | **HTTPS via Nginx** (TLS) + redirection HTTP→HTTPS | Centralise le TLS et simplifie le déploiement. En dev, certificat self-signed généré automatiquement. |
| **Clickjacking / sniffing / leakage referrer** | **Security headers Nginx** (HSTS, XFO, nosniff, Referrer-Policy) | Défense en profondeur au niveau reverse-proxy, agnostique du backend. |
| **Données invalides / injections applicatives** | **Validation** via DTO + `@Valid` | Réduit les entrées incohérentes et les vecteurs d’attaque; bonne pratique Spring. |

### Détails par mesure

- **Authentification & Autorisation (RBAC)**
  - **Threat**: accès non autorisé aux endpoints admin/user.
  - **Solution**: Spring Security avec règles par rôles (`/api/admin/**` réservé ADMIN, `/api/user/**` réservé USER).
  - **Justification**: Spring Security est la référence sur Spring Boot pour sécuriser des APIs, supporte filtres custom (JWT, rate limit, CSRF header).

- **JWT stateless + expiration**
  - **Threat**: compromission d’un token durable.
  - **Solution**: tokens signés, expiration configurable via `JWT_EXPIRATION`.
  - **Justification**: stateless (scalable), standard, facile à intégrer avec Nginx et un frontend Next.js.

- **Revocation (Blacklist) par `jti`**
  - **Threat**: token volé réutilisé après logout.
  - **Solution**: ajout du `jti` dans le JWT + stockage de tokens révoqués jusqu’à expiration.
  - **Justification**: compromis pratique: garde les bénéfices du JWT tout en permettant le logout “réel”.

- **OTP email (signup / login 2FA / reset password / transaction)**
  - **Threat**: takeover compte / reset non autorisé / fraude transaction.
  - **Solution**: génération OTP, stockage hashé + expiration, usage unique.
  - **Justification**: l’email est déjà le canal d’identité; SMTP via variables d’environnement; simple à opérer.

- **Anti-CSRF (Double submit cookie)**
  - **Threat**: requêtes forgées depuis un site tiers.
  - **Solution**: cookie `XSRF-TOKEN` + exigence du header `X-XSRF-TOKEN` sur les requêtes state-changing.
  - **Justification**: fonctionne bien avec JWT/stateless; pattern standard pour SPAs.

- **Rate limiting**
  - **Threat**: brute-force, DoS applicatif, abus OTP.
  - **Solution**: filtre de rate limit.
  - **Justification**: barrière simple “first line of defense”.

- **TLS + Reverse proxy Nginx**
  - **Threat**: sniffing, MITM, exposition directe des services.
  - **Solution**: Nginx termine TLS et reverse-proxy vers frontend/back.
  - **Justification**: standard industrie; facilite l’ajout d’en-têtes de sécurité; simplifie l’exposition des ports en Docker.

### Pourquoi ces technologies ? (choix)

- **Spring Boot**
  - Rapidité de développement, écosystème mature, intégration facile DB/JPA, validation et sécurité.
- **Spring Security**
  - Gestion robuste des filtres, RBAC, intégration JWT, “defense in depth”.
- **PostgreSQL**
  - Base relationnelle fiable, contraintes/transactions, adaptée à un contexte bancaire.
- **Flyway**
  - Migrations versionnées, reproductibilité, cohérence des environnements.
- **Next.js (React)**
  - Frontend moderne, routing/app router, bonne intégration avec un backend API.
- **Nginx**
  - Reverse proxy standard, TLS, headers de sécurité, routage `/api`.
- **Docker Compose**
  - Déploiement reproductible (backend/frontend/db/nginx) et onboarding rapide.

- **JWT stateless**
- **Blacklist de token** (revocation par `jti` lors du logout)
- **2FA email OTP** (login user)
- **OTP email** pour:
  - activation signup
  - reset password
  - changement de mot de passe (user connecté)
  - validation des transactions
- **Anti-CSRF** (double submit cookie)
  - cookie `XSRF-TOKEN`
  - header `X-XSRF-TOKEN`
- **Rate limiting** (filtre)
- **HTTPS via Nginx** (auto-génération d’un certificat self-signed en dev)
- **En-têtes de sécurité Nginx** (HSTS, X-Frame-Options, nosniff, Referrer-Policy)
- **Validation d’entrée** (DTO + `@Valid`)
- **Messages d’erreurs explicites** (backend renvoie `ErrorResponse` / `AuthResponse.message`)

## Architecture du système

### Vue d’ensemble

- **Nginx** (reverse proxy + TLS)
  - `/api/*` -> backend
  - `/` -> frontend
- **Frontend**: Next.js (React/TypeScript)
- **Backend**: Spring Boot (Spring Security, JPA)
- **Base de données**: PostgreSQL
- **Migrations**: Flyway

### Ports (Docker)

- Nginx HTTPS: `https://localhost:8443`
- Nginx HTTP (redirect vers HTTPS): `http://localhost:8081`
- PostgreSQL: `localhost:5432`

## Outils utilisés

- Docker / Docker Compose
- Nginx (reverse proxy + TLS)
- PostgreSQL
- Spring Boot / Spring Security / Spring Data JPA
- Flyway
- Next.js + TypeScript

## Coordonnées Admin (dev)

L’admin est seedé au démarrage du backend.

- **Username**: `ADMIN_USERNAME` (par défaut: `admin@kma.local`)
- **Password**: `ADMIN_PASSWORD` (par défaut: `admin`)

Important:

- **Ne pas utiliser ces identifiants en production**.
- En prod, désactiver le seeding ou définir des credentials forts via env vars.

## Conception de la base de données (résumé)

Les tables principales (simplifié):

- `users`
  - `id`, `username` (email), `password` (hash), `role`, flags soft-delete
- `profiles`
  - `user_id`, `fullname`, `email`, `phone`, `birthday`, `rip` (**20 chiffres**), `is_active`
- `comptes`
  - `user_id`, `solde`, `is_active`
- `transactions`
  - `sender_username`, `receiver_username`, `amount`, `status`, timestamps
- `email_otps`
  - `email`, `purpose`, `code_hash`, `expires_at`, `used_at`, `created_at`

Les migrations sont dans: `backend/src/main/resources/db/migration`.

## Exécuter le projet avec Docker

### Prérequis

- Docker
- Docker Compose

### 1) Préparer l’environnement

À la racine du repo:

```bash
cp .env.example .env
```

Puis modifier `.env` (ne jamais commit) avec:

- credentials PostgreSQL
- `JWT_SECRET`
- SMTP (si tu veux vraiment envoyer les OTP)

### 2) Lancer

```bash
docker-compose up --build
```

Accès:

- App: `https://localhost:8443`
- API: `https://localhost:8443/api`

### 3) Arrêter / reset

```bash
docker-compose down
```

Reset DB (perte des données):

```bash
docker-compose down -v
```

## Notes GitHub (secrets)

- `.env` est ignoré par git (`.gitignore`).
- Ne jamais committer:
  - passwords SMTP
  - `JWT_SECRET`
  - mots de passe DB
