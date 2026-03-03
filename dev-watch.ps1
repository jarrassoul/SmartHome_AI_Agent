#Requires -Version 5.1
<#
.SYNOPSIS
    SmartHome AI Agent - Development File Watcher
.DESCRIPTION
    Monitors source files for changes and automatically recompiles using Gradle.
    Provides fast feedback loop during development.
#>

[CmdletBinding()]
param(
    [switch]$RunApp,
    [int]$WatchInterval = 1000
)

$ErrorActionPreference = 'Continue'

# Colors for output
$ColorInfo = 'Cyan'
$ColorSuccess = 'Green'
$ColorError = 'Red'
$ColorWarning = 'Yellow'

function Write-Status($Message, $Color = 'White') {
    Write-Host "[$(Get-Date -Format 'HH:mm:ss')] $Message" -ForegroundColor $Color
}

function Get-LatestFileTimestamp {
    $latest = Get-ChildItem -Path 'src\main\java' -Recurse -Filter '*.java' -ErrorAction SilentlyContinue | 
              Sort-Object LastWriteTime -Descending | 
              Select-Object -First 1
    return $latest.LastWriteTime
}

function Invoke-QuickCompile {
    $output = & .\gradlew.bat compileJava --quiet --console=plain 2>&1
    $exitCode = $LASTEXITCODE
    
    return @{ 
        Success = ($exitCode -eq 0)
        Output = $output
    }
}

# Header
Clear-Host
Write-Host "==========================================" -ForegroundColor $ColorInfo
Write-Host "  SmartHome AI Agent - Dev Watch Mode" -ForegroundColor $ColorInfo
Write-Host "==========================================" -ForegroundColor $ColorInfo
Write-Host ""
Write-Status "Features:" $ColorInfo
Write-Status "  - Fast incremental compilation" $ColorInfo
Write-Status "  - File change detection" $ColorInfo
Write-Status "  - Error notifications" $ColorInfo
Write-Host ""
Write-Status "Press Ctrl+C to stop watching" $ColorWarning
Write-Host ""

# Verify gradlew exists
if (-not (Test-Path '.\gradlew.bat')) {
    Write-Status "ERROR: gradlew.bat not found!" $ColorError
    exit 1
}

# Initial build
Write-Status "Performing initial build..." $ColorInfo
$result = Invoke-QuickCompile
if ($result.Success) {
    Write-Status "Initial build successful!" $ColorSuccess
} else {
    Write-Status "Initial build failed! Fix errors and save to retry." $ColorError
    if ($result.Output) {
        Write-Host $result.Output -ForegroundColor $ColorError
    }
}

# Get initial timestamp
$lastTimestamp = Get-LatestFileTimestamp
Write-Status "Monitoring: src\main\java" $ColorInfo
Write-Status "Last modified: $lastTimestamp" $ColorInfo
Write-Host ""

# Watch loop
try {
    while ($true) {
        Start-Sleep -Milliseconds $WatchInterval
        
        $currentTimestamp = Get-LatestFileTimestamp
        
        if ($currentTimestamp -ne $lastTimestamp) {
            Write-Host ""
            Write-Status "CHANGE DETECTED at $(Get-Date -Format 'HH:mm:ss')" $ColorWarning
            Write-Status "File: $($currentTimestamp)" $ColorWarning
            Write-Status "Recompiling..." $ColorInfo
            
            $result = Invoke-QuickCompile
            
            if ($result.Success) {
                Write-Status "BUILD SUCCESSFUL!" $ColorSuccess
                $lastTimestamp = $currentTimestamp
            } else {
                Write-Status "BUILD FAILED!" $ColorError
                if ($result.Output) {
                    Write-Host $result.Output -ForegroundColor $ColorError
                }
            }
            
            Write-Host ""
            Write-Status "Watching for changes..." $ColorInfo
        }
    }
}
catch {
    if ($_.Exception.Message -match 'pipeline') {
        Write-Host ""
        Write-Status "Stopped watching." $ColorWarning
    } else {
        Write-Status "Error: $_" $ColorError
    }
}
