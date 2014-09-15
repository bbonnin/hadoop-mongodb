
Lien vers binaires tels que winutils:
http://www.srccodes.com/p/article/39/error-util-shell-failed-locate-winutils-binary-hadoop-binary-path

Comment refaires ces binaires:
http://www.srccodes.com/p/article/38/build-install-configure-run-apache-hadoop-2.2.0-microsoft-windows-os

Autres liens:
http://stackoverflow.com/questions/18630019/running-apache-hadoop-2-1-0-on-windows
http://stackoverflow.com/questions/19620642/failed-to-locate-the-winutils-binary-in-the-hadoop-binary-path



https://github.com/spring-projects/spring-hadoop-samples/


https://cwiki.apache.org/confluence/display/Hive/LanguageManual+DML


============================

Etat des lieux
--------------

- Utilisation de Spring Hadoop permettant de construire aisément dans une seule JVM, un mini-cluster Hadoop
- Utilisation de mongo-hadoop-connector pour faire le lien Hadoop(Hive)-MongoDB

Runtime:
- Les requêtes simples fonctionnent même sur Windows avec un simple Hive Server + Hive Client
- Pour les requêtes plus complexes, Hive utilise des jobs Map/Reduce
  - même si un job peut foncionner en local cela nécessite des libs natives (même Linux?) 
  - sur Windows notamment, nécessite l'installation de libs (hadoop.dll) et de scripts (hadoop.cmd, ...) qui ne sont pas présents dans binaire Hadoop
  
Quelles orientations prendre ?
- Compilation de Hadoop pour Windows (cf liens)
- Faire fonctionner sur Linux (Windows est un plus, sans intérêt dans la démonstration) => let's go !!!


  
