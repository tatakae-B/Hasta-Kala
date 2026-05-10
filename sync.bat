@echo off
echo --- Starting Auto-Sync to GitHub ---
git add .
set msg=Auto-update %date% %time%
git commit -m "%msg%"
git push
echo --- Sync Complete! ---
pause
