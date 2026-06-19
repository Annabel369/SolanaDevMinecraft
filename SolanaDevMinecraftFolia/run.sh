#!/bin/bash

PROJECT_DIR="/media/astral/7DFD-F7FB/Folia/SolanaDevMinecraft/SolanaDevMinecraftFolia"
DEST_DIR="/media/astral/7DFD-F7FB/Folia/plugins"
NOME_DESEJADO="SolanaDevMinecraftFolia-v2.7.8.jar"

echo "========================================"
echo "Iniciando compilação do plugin Solana..."
echo "========================================"

cd "$PROJECT_DIR" || exit 1
chmod +x gradlew
./gradlew clean build

if [ $? -eq 0 ]; then
    echo ""
    echo "======================================"
    echo "Compilação concluída com sucesso!"
    echo "Limpando versões antigas do plugin..."
    echo "======================================"

    mkdir -p "$DEST_DIR"

    # Limpa tudo que for antigo da Solana na pasta de plugins
    rm -f "$DEST_DIR/app.jar"
    rm -f "$DEST_DIR/SolanaDevMinecraft"*.jar

    # Pega o primeiro .jar que encontrar na pasta de build
    JAR_GERADO=$(find app/build/libs -name "*.jar" | head -n 1)

    if [ -n "$JAR_GERADO" ]; then
        echo "Arquivo encontrado no build: $(basename "$JAR_GERADO")"
        
        # Copia o arquivo FORÇANDO o nome que você quer ver na pasta
        cp -v "$JAR_GERADO" "$DEST_DIR/$NOME_DESEJADO"
        
        echo "======================================"
        echo "Plugin atualizado com sucesso: $NOME_DESEJADO"
        echo "======================================"
    else
        echo "Erro: Nenhum arquivo .jar foi gerado na pasta app/build/libs/"
    fi
else
    echo "======================================"
    echo "Erro durante a compilação!"
    echo "======================================"
    exit 1
fi
