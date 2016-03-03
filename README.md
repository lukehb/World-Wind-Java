[![Build Status](https://travis-ci.org/wcmatthysen/World-Wind-Java.svg?branch=master)](https://travis-ci.org/wcmatthysen/World-Wind-Java)

If you've ever tried to use WorldWind as a dependency in Gradle-based Java project you'd know it is quite hellish.
WorldWind itself is fine, but it has dependencies on Jogl, Gluegen, and GDAL. All of which use jars to wrap native
libraries. For example, when the main Jogl and Gluegen jars load they then look at the OS and architecture 
and try to find the specific wrapped native jar to suit. So if you are on windows x64 jogl-all.jar will load and then it
will look for jogl-all-natives-windows-amd64.jar. The problem is it only looks for jars in the same folder. This is
fine for Maven projects, but for Gradle projects dependencies will not end up in the same folder.

So, how can we have WorldWind as a dependency in a Gradle-based project?

One solution for Jogl and Gluegen (not GDAL) is discussed [here][forum-post], he provides a custom loading action
for resolving the wrapped natives. So you can grab his `GLBootstrap.java` [here][glbootstrap] and you use it like this:
```java
JNILibLoaderBase.setLoadingAction(new GLBootstrap());
```

That still doesn't fully help for using WorldWind in Gradle-based projects, we still have to handle GDAL. What I have done in
this repo is re-write the WorldWind build configs in Gradle (because why not) so that WorldWind is packaged in one fat jar 
(uber jar) with Jogl, Gluegen, and GDAL inside it. 

TLDR - Grab the jar that "just works"
=====================================

Choose the jar that is suitable for your project:

**To do: host the built fat-jars in some artefactory.**

[forum-post]: http://forum.jogamp.org/Atomic-jars-in-Maven-Central-Gradle-build-system-td4029555.html
[glbootstrap]: https://github.com/jjzazuet/jgl/blob/965cb8030cbed1fbe15cf1ca23017f1b7817a520/jgl-opengl/src/main/java/net/tribe7/opengl/platform/GLBootstrap.java