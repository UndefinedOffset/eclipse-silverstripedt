@echo off
PATH=%PATH%;c:\jdk1.4.2_08\bin
java -Xmx470000000 -cp "C:\Users\Ed Chipman\workspace\ca.edchipman.silverStripePDT.lexer\DevTimeSupport\resources\JFlex.jar";. JFlex.Main SSTokenizer.jflex -skel skeleton.sse && rm -f SSTokenizer.java~ SSTokenizer~ && copy SSTokenizer.java ..\..\..\..\src\ca\edchipman\silverstripepdt\parser\SSTokenizer.java
