cat version.txt
echo " <- old version"
echo "v new version"
read VERSION
echo
echo -n "$VERSION" > version.txt
sed "s/$(cat previousVersion.txt)/$(cat version.txt)/g" < src/main/java/tudbut/mod/polyfire/PolyFire.java > tmp.PolyFire.java
sed "s/$(cat previousVersion.txt)/$(cat version.txt)/g" < src/main/resources/mcmod.info > tmp.mcmod.info
mv tmp.mcmod.info src/main/resources/mcmod.info
mv tmp.PolyFire.java src/main/java/tudbut/mod/polyfire/PolyFire.java
./gradlew jar
git commit -m "makerelease.sh: set version" version.txt build src/main/java/tudbut/mod/polyfire/PolyFire.java src/main/resources/mcmod.info
git push
cat > message.txt << EOF
> $(cat version.txt)

Additions:
none

Deletions:
none

Changes/Other:
none

Notes:
- Time taken (approx.): 1h

https://discord.gg/2WsVCQDpwy
EOF

git diff master > gitdiff
git log --graph --oneline --decorate > gitlog
vim -p message.txt gitdiff gitlog
rm gitdiff gitlog

xdg-open "https://github.com/tudbut/polyfire/releases/new" &

cp version.txt previousVersion.txt
git commit -m "makerelease.sh: set previous version" previousVersion.txt
git push
git checkout master
git merge dev
sleep 1
echo =========================================
cat message.txt
echo =========================================
echo "Please get ready to publish release,"
echo "then press enter and hit publish once the"
echo "script is done."
read
git push
git tag -aF message.txt "$(cat version.txt)"
git push --tags
git checkout dev
