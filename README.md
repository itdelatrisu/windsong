# Potato
**Potato** is a rhythm game.  It is written in Java using
[Slick2D](http://slick.ninjacave.com/) and [LWJGL](http://lwjgl.org/),
wrappers around the OpenGL and OpenAL libraries, and runs on Windows, OS X, and
Linux platforms.

## Getting Started
TODO

## Building
Potato is distributed as both a [Maven](https://maven.apache.org/) and
[Gradle](https://gradle.org/) project.

### Maven
Maven builds are built to the `target` directory.
* To run the project, execute the Maven goal `compile`.
* To create a single executable jar, execute the Maven goal `package -Djar`.
  This will compile a jar to `target/potato-${version}.jar` with the libraries,
  resources and natives packed inside the jar.  Setting the "XDG" property
  (`-DXDG=true`) will make the application use XDG folders under Unix-like
  operating systems.

## Credits
This work is based on [opsu!](https://github.com/itdelatrisu/opsu).
