@echo off
echo --- Starting Auto-Sync to GitHub ---
:: This command suppresses the line-ending warnings
git config core.autocrlf true
git add .
set msg=Auto-update %date% %time%
echo Committing changes...
git commit -m "%msg%"
echo Pushing to GitHub...
git push
echo.
echo --- Sync Complete! ---
pause
