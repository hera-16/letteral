param(
    [string]$Path
)

if (-not (Test-Path -LiteralPath $Path)) {
    Write-Error "File not found: $Path"
    exit 1
}

$bytes = [System.IO.File]::ReadAllBytes($Path)
if ($bytes.Length -gt 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
    [System.IO.File]::WriteAllBytes($Path, $bytes[3..($bytes.Length - 1)])
    Write-Output "BOM removed from $Path"
} else {
    Write-Output "No BOM detected in $Path"
}
