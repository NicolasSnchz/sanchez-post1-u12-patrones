#!/usr/bin/env bash
# Espera a que termine el run de arquitectura del commit actual y deja los ids de los runs de la violacion y del revert
set -u
API="https://api.github.com/repos/$GITHUB_REPOSITORY/actions/workflows/arquitectura.yml/runs"
run_de() { curl -s -H "Authorization: Bearer $GITHUB_TOKEN" "$API?head_sha=$1" | jq -r '.workflow_runs[0] | "\(.id) \(.status) \(.conclusion)"'; }
for i in $(seq 1 60); do
    read -r ID ESTADO CONCLUSION <<< "$(run_de "$GITHUB_SHA")"
    echo "run del revert: $ID $ESTADO $CONCLUSION"
    [ "$ESTADO" = completed ] && break
    sleep 20
done
read -r IDV EV CV <<< "$(run_de "$1")"
echo "run de la violacion: $IDV $EV $CV"
echo "$IDV $ID" > "$2"
