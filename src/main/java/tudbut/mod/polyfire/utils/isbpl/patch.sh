diff --horizon-lines=20 --unified ISBPL.java.original ISBPL.java > ISBPL.patch
cp ~/gitshit/isbpl/bootstrap/ISBPL.java ISBPL.java
cp ~/gitshit/isbpl/bootstrap/ISBPL.java ISBPL.java.original
patch ISBPL.java ISBPL.patch