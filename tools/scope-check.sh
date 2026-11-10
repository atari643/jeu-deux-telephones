#!/bin/sh
# Vérifie que les fichiers modifiés appartiennent bien au propriétaire de la branche.
# Règles : .github/ownership.txt. Utilisé par .githooks/pre-commit et par le CI (job "scope").
#
#   sh tools/scope-check.sh --staged                    # hook : fichiers indexés, rôle = git config team.role
#   sh tools/scope-check.sh --ci <branche> <base>       # CI : diff <base>...HEAD
#   sh tools/scope-check.sh --files <branche> f1 f2...  # test manuel
set -u
ROOT=$(git rev-parse --show-toplevel) || exit 2
RULES="$ROOT/.github/ownership.txt"
MODE=${1:-}

fail() { printf '%s\n' "$@" >&2; exit 1; }

case "$MODE" in
  --staged)
    BRANCH=$(git rev-parse --abbrev-ref HEAD)
    ROLE=$(git config --get team.role || true)
    [ -n "$ROLE" ] || fail "ROLE INCONNU : lance d'abord  pwsh tools/setup-team.ps1 -Role A  (ou -Role B)."
    # Fusion de main dans sa branche : les fichiers de l'autre arrivent légitimement.
    if git rev-parse -q --verify MERGE_HEAD >/dev/null 2>&1; then echo "PERIMETRE OK (fusion)"; exit 0; fi
    FILES=$(git diff --cached --name-only --no-renames)
    ;;
  --ci)
    BRANCH=${2:?branche manquante}; BASE=${3:?base manquante}; ROLE=""
    FILES=$(git diff --name-only --no-renames "origin/$BASE...HEAD")
    ;;
  --files)
    BRANCH=${2:?branche manquante}; shift 2; ROLE=$(git config --get team.role || true)
    FILES=$(printf '%s\n' "$@")
    ;;
  *) fail "Usage : sh tools/scope-check.sh --staged | --ci <branche> <base> | --files <branche> <fichiers...>" ;;
esac

# 1. Nom de branche -> à qui elle appartient
case "$BRANCH" in
  main|master) fail "INTERDIT : pas de commit sur main. Crée ta branche :" \
                    "  git switch -c <a|b>/<NN-feature>   (ou commun/<sujet> pour un changement commun)" ;;
  a/*) KIND=A ;;
  b/*) KIND=B ;;
  commun/*) KIND=COMMUN ;;
  *) fail "NOM DE BRANCHE INVALIDE : '$BRANCH'. Utilise a/<NN-feature>, b/<NN-feature> ou commun/<sujet>." ;;
esac
if [ -n "$ROLE" ] && [ "$KIND" != COMMUN ] && [ "$KIND" != "$ROLE" ]; then
  fail "INTERDIT : tu es $ROLE mais la branche '$BRANCH' appartient à $KIND. Reviens sur une branche $(echo "$ROLE" | tr AB ab)/..."
fi
[ "$KIND" = COMMUN ] && { echo "PERIMETRE OK (branche commune : relecture des deux obligatoire)"; exit 0; }

# 2. Chaque fichier -> son propriétaire (premier motif qui correspond, sinon COMMUN)
owner_of() {
  while IFS=' 	' read -r who pat; do
    case "$who" in ''|'#'*) continue ;; esac
    # shellcheck disable=SC2254
    case "$1" in $pat) echo "$who"; return ;; esac
  done < "$RULES"
  echo COMMUN
}

BAD=""
OLDIFS=$IFS; IFS='
'
for f in $FILES; do
  [ -n "$f" ] || continue
  o=$(owner_of "$f")
  if [ "$o" != "$KIND" ] && [ "$o" != AB ]; then BAD="$BAD
  - $f  (appartient à $o)"; fi
done
IFS=$OLDIFS

if [ -n "$BAD" ]; then
  fail "HORS PERIMETRE pour la branche $KIND '$BRANCH' :$BAD" \
       "" \
       "Ne modifie pas ces fichiers. Annule :  git restore --staged <fichier> && git restore <fichier>" \
       "Besoin d'un changement chez l'autre ou dans le commun : écris-le dans docs/team/demandes.md et arrête-toi." \
       "Règles : .github/ownership.txt"
fi
echo "PERIMETRE OK ($KIND)"
