The groovy scripts should run out of the box, e.g., in groovysh. 
Just open groovysh and call KaisTests.doit(). Make sure that a backend installation is up and 
running as configured.

All dependencies are fetched automatically via Grape. So make sure that all dependencies 
are deployed on breda (Mannheim Maven Repo).

If you want to use your own versions, just configure your local maven 
repository as source. Simply put the grapeConfig.xml under ~/.groovy (at least under Linux).
Adjust the path to your local maven repo in the config. Default shoud be ~/.m2/repository

To update, you have to delete your locally cached copy (tell me if you find a way around that...)
A "rm -rf ~/.groovy/grapes/eu.dm2e.*" should work.
