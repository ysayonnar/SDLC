$ErrorActionPreference = "Stop"

$projectDir = $PSScriptRoot
$sourceDir = Join-Path $projectDir "src\main\java"
$classesDir = Join-Path $projectDir "build\classes"

if (-not (Get-Command javac -ErrorAction SilentlyContinue)) {
    Write-Error "javac was not found. Install JDK 17 or newer and add its bin directory to PATH."
}

if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    Write-Error "java was not found. Install JDK 17 or newer and add its bin directory to PATH."
}

New-Item -ItemType Directory -Force -Path $classesDir | Out-Null

$sourceFiles = @(
    Get-ChildItem -Path $sourceDir -Recurse -Filter "*.java" -File |
        Select-Object -ExpandProperty FullName
)

if ($sourceFiles.Count -eq 0) {
    Write-Error "No Java source files were found in $sourceDir."
}

Write-Host "Compiling the application..."
& javac --release 17 -encoding UTF-8 -d $classesDir $sourceFiles

if ($LASTEXITCODE -ne 0) {
    throw "Compilation failed with exit code $LASTEXITCODE."
}

Write-Host "Starting the Morse code decoder..."
& java -cp $classesDir by.bsuir.morse.App

if ($LASTEXITCODE -ne 0) {
    throw "The application exited with code $LASTEXITCODE."
}
