# Windsong
**Windsong** is a rhythm game.  It is written in Java using
[Slick2D](http://slick.ninjacave.com/) and [LWJGL](http://lwjgl.org/),
wrappers around the OpenGL and OpenAL libraries, and runs on Windows, OS X, and
Linux platforms.  It uses the [Leap Motion](https://www.leapmotion.com/)
controller for input.

## Getting Started
TODO

## Building
Windsong is distributed as a [Maven](https://maven.apache.org/) project.

### Maven
Maven builds are built to the `target` directory.
* To run the project, execute the Maven goal `compile`.
* To create a single executable jar, execute the Maven goal `package -Djar`.
  This will compile a jar to `target/windsong-${version}.jar` with the libraries,
  resources and natives packed inside the jar.  Setting the "XDG" property
  (`-DXDG=true`) will make the application use XDG folders under Unix-like
  operating systems.

Note that the program will only support 64-bit versions of Windows and Linux
with the default settings, due to how the Leap Motion SDK is set up.  To run
the program on a 32-bit machine, replace `leap-platform-1.0.0-natives-(windows|linux).jar`
with `leap-platform-1.0.0-natives-(windows|linux).x86.jar` in the local Maven
repository (`repo/com/leapmotion/leap-platform/1.0.0/`).

## Credits
This work is based on [opsu!](https://github.com/itdelatrisu/opsu).
