#!/usr/bin/env bash
# Mide la evidencia del proyecto en GitHub Actions y la deja en $SALIDA.
# No se detiene cuando algo falla: varios experimentos existen justamente para fallar.
set -u
SALIDA=${SALIDA:-$PWD/evid}
REPO=$PWD
TRABAJO=$(mktemp -d)
cp -r "$REPO/.github/evidencias" "$TRABAJO/herramientas"
H=$TRABAJO/herramientas
mkdir -p "$SALIDA/logs" "$SALIDA/jacoco" "$SALIDA/surefire"
J17=$JAVA_HOME_17_X64
J23=$JAVA_HOME_23_X64

commit_con() { git -C "$REPO" log --format=%H -F --grep="$1" | tail -1; }
correr() {
    local nombre=$1 dir=$2 jdk=$3; shift 3
    echo "### $nombre: mvn $*"
    (cd "$dir" && JAVA_HOME=$jdk PATH="$jdk/bin:$PATH" mvn -B -ntp "$@") > "$SALIDA/logs/$nombre.log" 2>&1
    echo $? > "$SALIDA/logs/$nombre.exit"
    if [ -d "$dir/target/surefire-reports" ]; then
        mkdir -p "$SALIDA/surefire/$nombre" && cp "$dir"/target/surefire-reports/*.xml "$SALIDA/surefire/$nombre/" 2>/dev/null
    fi
    [ -f "$dir/target/site/jacoco/jacoco.xml" ] && rm -rf "$SALIDA/jacoco/$nombre" && cp -r "$dir/target/site/jacoco" "$SALIDA/jacoco/$nombre"
}
worktree() { git -C "$REPO" worktree add -q --detach "$TRABAJO/$1" "$2"; }
copia() { rm -rf "$2" && mkdir -p "$2" && (cd "$1" && git ls-files -z | xargs -0 cp --parents -t "$2"); }

{ echo "commit analizado: $(git -C "$REPO" rev-parse HEAD)"; JAVA_HOME=$J17 mvn -v; echo; "$J23/bin/java" -version 2>&1; } > "$SALIDA/entorno.txt" 2>&1
git -C "$REPO" log --format='%h %s' > "$SALIDA/historial.txt"

DISENO=$(commit_con "docs: analizar requisitos")
ENSAMBLE=$(commit_con "feat: ensamblar SolicitudReembolsoService")
REGLAS=$(commit_con "test: agregar dependencia ArchUnit")
IMPACTO=$(commit_con "docs: documentar analisis de impacto")
VIOLACION=$(commit_con "test: violacion de arquitectura intencional")
REVERT=$(git -C "$REPO" log --format=%H -F --grep='Revert "test: violacion' | tail -1)
printf 'DISENO %s\nENSAMBLE %s\nREGLAS %s\nIMPACTO %s\nVIOLACION %s\nREVERT %s\n' \
    "$DISENO" "$ENSAMBLE" "$REGLAS" "$IMPACTO" "$VIOLACION" "$REVERT" > "$SALIDA/commits.txt"

# ---------- Parte 1 ----------
worktree diseno "$DISENO"
correr p1-diseno-package "$TRABAJO/diseno" "$J17" clean package
(cd "$TRABAJO/diseno" && grep -rn "^import" src/main/java/com/empresa/reembolsos/dominio || echo "(ningun import)") > "$SALIDA/logs/imports-dominio.txt"

worktree ensamble "$ENSAMBLE"
correr p1-verify "$TRABAJO/ensamble" "$J17" verify
(cd "$TRABAJO/ensamble" && grep -n "^import" src/main/java/com/empresa/reembolsos/procesamiento/validacion/ValidadorDuplicados.java) > "$SALIDA/logs/imports-validador-duplicados.txt"
# la API real: se empaqueta, se arranca y se recorre el flujo completo con curl
correr p1-package-api "$TRABAJO/ensamble" "$J17" -DskipTests package
(cd "$TRABAJO/ensamble" && "$J17/bin/java" -jar target/reembolsos-integrado-0.0.1-SNAPSHOT.jar > "$SALIDA/logs/api-aplicacion.log" 2>&1 & echo $! > "$TRABAJO/api.pid")
for i in $(seq 1 60); do grep -q "Started ReembolsosApplication" "$SALIDA/logs/api-aplicacion.log" && break; sleep 2; done
bash "$H/flujo-api.sh" > "$SALIDA/logs/api-flujo.txt" 2>&1
sleep 2; kill "$(cat "$TRABAJO/api.pid")" 2>/dev/null; sleep 3

# ---------- Parte 2 ----------
worktree reglas "$REGLAS"
correr p2-reglas "$TRABAJO/reglas" "$J17" test -Dtest=ReglasArquitectura

worktree impacto "$IMPACTO"
correr p2-verify-jdk17 "$TRABAJO/impacto" "$J17" verify
correr p2-verify-jdk23 "$TRABAJO/impacto" "$J23" verify

# Experimentos sobre el codigo final: cada uno cambia una sola cosa
copia "$TRABAJO/impacto" "$TRABAJO/exp-guia"
cp "$H/experimentos/ReglasArquitecturaGuia.java" "$TRABAJO/exp-guia/src/test/java/com/empresa/reembolsos/ReglasArquitectura.java"
correr experimento-reglas-guia "$TRABAJO/exp-guia" "$J17" test -Dtest=ReglasArquitectura

copia "$TRABAJO/impacto" "$TRABAJO/exp-import"
sed -i 's#^import com.empresa.reembolsos.dominio.SolicitudReembolso;#&\nimport com.empresa.reembolsos.infraestructura.RepositorioSolicitudesJpa;#' \
    "$TRABAJO/exp-import/src/main/java/com/empresa/reembolsos/procesamiento/validacion/ValidadorDuplicados.java"
grep -n "^import" "$TRABAJO/exp-import/src/main/java/com/empresa/reembolsos/procesamiento/validacion/ValidadorDuplicados.java" > "$SALIDA/logs/experimento-solo-import-imports.txt"
correr experimento-solo-import "$TRABAJO/exp-import" "$J17" test -Dtest=ReglasArquitectura

copia "$TRABAJO/impacto" "$TRABAJO/exp-estricta"
cp "$H/experimentos/ReglaEstrictaTest.java" "$TRABAJO/exp-estricta/src/test/java/com/empresa/reembolsos/"
correr experimento-regla-estricta "$TRABAJO/exp-estricta" "$J17" test -Dtest=ReglaEstrictaTest

copia "$TRABAJO/impacto" "$TRABAJO/exp-sin-orm"
sed -i '/spring.jpa.mapping-resources/d' "$TRABAJO/exp-sin-orm/src/main/resources/application.properties"
rm "$TRABAJO/exp-sin-orm/src/main/resources/META-INF/orm.xml"
correr experimento-sin-orm-xml "$TRABAJO/exp-sin-orm" "$J17" test -Dtest=ReembolsosApplicationTests

copia "$TRABAJO/impacto" "$TRABAJO/exp-archunit121"
sed -i 's#<version>1.3.0</version>#<version>1.2.1</version>#' "$TRABAJO/exp-archunit121/pom.xml"
correr experimento-archunit-1.2.1-jdk17 "$TRABAJO/exp-archunit121" "$J17" test -Dtest=ReglasArquitectura
correr experimento-archunit-1.2.1-jdk23 "$TRABAJO/exp-archunit121" "$J23" test -Dtest=ReglasArquitectura

# ---------- La violacion intencional y su revert ----------
if [ -n "$VIOLACION" ]; then
    worktree violacion "$VIOLACION"
    correr p2-violacion "$TRABAJO/violacion" "$J17" test -Dtest=ReglasArquitectura
    git -C "$REPO" show "$VIOLACION" -- src > "$SALIDA/logs/violacion-diff.txt"
fi
if [ -n "$REVERT" ]; then
    worktree revert "$REVERT"
    correr p2-revert "$TRABAJO/revert" "$J17" verify
fi

for f in "$SALIDA"/logs/*.exit; do echo "$(basename "$f" .exit) -> $(cat "$f")"; done > "$SALIDA/resumen.txt"
cat "$SALIDA/resumen.txt"
