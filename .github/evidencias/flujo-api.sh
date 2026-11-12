#!/usr/bin/env bash
# Recorre el flujo completo de la API: crear en las 3 categorias, rechazos y cambios de estado
BASE=http://localhost:8080/api/solicitudes
paso() { echo; echo "\$ $1"; }
crear() {
    paso "curl -X POST /api/solicitudes -d '$1'"
    curl -s -X POST "$BASE" -H "Content-Type: application/json" -d "$1" -w '\nHTTP %{http_code}\n'
}
estado() {
    paso "curl -X PUT /api/solicitudes/$1/estado -d '\"$2\"'"
    curl -s -X PUT "$BASE/$1/estado" -H "Content-Type: application/json" -d "\"$2\"" -w '\nHTTP %{http_code}\n'
}
echo "## Crear una solicitud de cada categoria"
crear '{"empleadoId":"E-201","categoria":"VIAJE","montoSolicitado":400000,"fechaGasto":"2026-10-15","diasViaje":3,"documentosSoporte":true}'
crear '{"empleadoId":"E-202","categoria":"REPRESENTACION","montoSolicitado":1200000,"fechaGasto":"2026-10-15","documentosSoporte":true}'
crear '{"empleadoId":"E-203","categoria":"CAPACITACION","montoSolicitado":1000000,"fechaGasto":"2026-10-15","documentosSoporte":true}'
echo; echo "## Rechazos de la cadena de validacion"
crear '{"empleadoId":"E-204","categoria":"VIAJE","montoSolicitado":100000,"fechaGasto":"2026-10-15","diasViaje":1,"documentosSoporte":false}'
crear '{"empleadoId":"E-205","categoria":"REPRESENTACION","montoSolicitado":2500000,"fechaGasto":"2026-10-15","documentosSoporte":true}'
crear '{"empleadoId":"E-202","categoria":"REPRESENTACION","montoSolicitado":300000,"fechaGasto":"2026-10-20","documentosSoporte":true}'
echo; echo "## Cambios de estado"
estado 3 APROBADA
estado 1 RECHAZADA
estado 2 REQUIERE_CORRECCION
estado 2 REQUIERE_CORRECCION
estado 999 APROBADA
