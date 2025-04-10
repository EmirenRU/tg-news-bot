

$KAFKA_DIR = "C:\SDK\Java\kafka"
$KAFKA_BIN = "$KAFKA_DIR\bin\windows"
$CONFIG = "$KAFKA_DIR\config\server.properties"
$LOG_DIR = "D:\tmp\kraft-combined-logs"

function Start-Kafka {
    Initialize-KraftStorage
    & "$KAFKA_BIN\kafka-server-start.bat" "$CONFIG"
}

function Initialize-KraftStorage {
    Write-Output "Initializing KRaft storage..."

    if (!(Test-Path $LOG_DIR)) {
        New-Item -ItemType Directory -Path $LOG_DIR | Out-Null
    }

    $clusterId = (& "$KAFKA_BIN\kafka-storage.bat" random-uuid).Trim()

    & "$KAFKA_BIN\kafka-storage.bat" format `
        --config "$CONFIG" `
        --cluster-id "$clusterId"

    if (!(Test-Path "$LOG_DIR\meta.properties")) {
        throw "Failed to create meta.properties in $LOG_DIR"
    }
}

function Clean-Kraft {
    if (Test-Path $LOG_DIR) {
        Remove-Item -Recurse -Force $LOG_DIR
    }
}

# Main execution
switch ($args[0]) {
    "clean" { Clean-Kraft }
    default { Start-Kafka }
}