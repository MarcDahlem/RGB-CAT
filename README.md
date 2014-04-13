RGB-CAT
=======
RGB Color-Adjustment-Tool
-------------------------

This small java tool enables a fullscreen window filled with a RGB Color.

It includes the following features:

* manually decrease/increase color
* manually set color constants red, green and blue
* set incrementation step size/color changing interval
* auto increment colors
* changing speed of the auto increment


## What is it for?

I wrote this tool to better check gamma curves and color settings of monitors and other screens.

It runs on every device supporting Java and the Eclipse SWT ([The Standard Widget Toolkit](http://www.eclipse.org/swt/) ) and can be used for all screens able to be attached to those.

Here are a few examples:
* check gamma curves of monitors
* check gamma curves of televisions
* check Ambilight Colors
* check screens for defective pixel

## Download and installation instructions

1. You need to have Java installed
2. Download the file of your platform from [jars/](../master/jars/)
3. Run the tool with your Java installation. If encountering problems please write a github [issue](../../issues)

### Installation instructions on the raspberry pi
1. Install Java
  1. `sudo apt-get install openjdk-7-jdk`
  2. or `sudo apt-get install oracle-java7-jdk`
2. Install other dependencies with `sudo apt-get install libcanberra-gtk-module libswt-cairo-gtk-3-jni`
3. Run the tool with `java -jar ...`

### Manual installation
1. Download/clone this repository
2. Setup your java project and import the source code from 'src/*'
3. Download the [Eclipse SWT](http://www.eclipse.org/swt/)
4. Add SWT to your project build path

## How to control the tool

The tool is controlled via keyboard and reacts to the following mapping:

Key(s) | Action
--- | --- | ---
'f' |                          Toggle fullscreen
'x','q' or 'ESC' |             Close program
'Space' or 'Enter/CR' |        Pause/Resume auto incrementing colors
'-' / '+' |                    Decrease/Increase color update frequency for the auto increment. Value is given in ms
'Down-Arrow' / 'Up-Arrow' |    Decrease/Increase the interval for color changes
'Left-Arrow' / 'Right-Arrow' | Manually decrease/increase color
'w' | Toggle monochrome mode
'c' |                           Choose color manually from color picker
'r' |                          Set color to constant 'red' <-> RGB(255,0,0)
'g' |                         Set color to constant 'green' <-> RGB(0,255,0)
'b' |                         Set color to constant 'blue' <-> RGB(0,0,255)
'i','h' or 'F1' |              Show the help

## Screenshots
Click to view

[![help overlay disabled](http://abload.de/img/screenshotuyso8.png)](http://abload.de/img/screenshotuyso8.png)
[![help overlay enabled](http://abload.de/img/bildschirmfotovom2014lpu4y.png)](http://abload.de/img/bildschirmfotovom2014lpu4y.png)
