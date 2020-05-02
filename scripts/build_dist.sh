BASE_DIR=$(dirname "$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)")
OUT_DIR="$BASE_DIR/target"
BUILD_DIR="$BASE_DIR/distribution"

mkdir -p "$OUT_DIR" &>/dev/null

#mvn -DskipTests clean install

cp "${BASE_DIR:?}/scripts/hermes" "$OUT_DIR"
cp "${BASE_DIR:?}/docs/hermes.pdf" "$OUT_DIR"
rsync -avg /data/hermes/en "$OUT_DIR"/resources/
cd "$BUILD_DIR" || exit

rm -rf "${OUT_DIR:?}/lib/" &>/dev/null
rm -rf "${OUT_DIR:?}/spark/" &>/dev/null

mvn dependency:copy-dependencies -DoutputDirectory="${OUT_DIR:?}/lib/" -DincludeScope=runtime

# Remove test dependencies
rm "${OUT_DIR:?}/lib/junit*" &>/dev/null
rm "${OUT_DIR:?}/lib/hamcrest*" &>/dev/null
rm "${OUT_DIR:?}/lib/metainf-services*" &>/dev/null

cd "$OUT_DIR" || exit
tar -zcf ../hermes_no_spark.tar.gz .

cd "$BUILD_DIR" || exit
mvn dependency:copy-dependencies -DoutputDirectory="${OUT_DIR:?}/spark/" -DexcludeScope=runtime -DincludeScope=provided
cd "$OUT_DIR" || exit
tar -zcf ../hermes_spark.tar.gz .
