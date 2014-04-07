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

It runs on every device supporting Java and the Eclipse SWT ([The Standard Widget Toolkit](http://www.eclipse.org/swt/)) and can be used for all screens able to be attached to those.

Here are a few examples:
* check gamma curves of monitors
* check gamma curves of televisions
* check Ambilight Colors

## Download and installation instructions

1. You need to have Java installed
2. Download the file of your platform from [jars/]
3. Run the tool with your Java installation. If encountering problems please write an github [issue](../issues)

### Manual installation
1. Download/clone this repository
2. Setup your java project and import the source code from 'src/*'
3. Download the [Eclipse SWT](http://www.eclipse.org/swt/))
4. Add SWT to your project build path

## How to control the tool

The tool is controlled via keyboard and reacts to the following mapping:

*'f':\t\t\t\t\t\t\t\tToggle Fullscreen
*'x','q' or 'ESC':\t\t\t\tClose program
*'Space' or 'Enter/CR':\t\t\t\tPause/Resume auto increment colors
*'-' / '+':\t\t\t\t\t\t\tDecrease/Increase Color update frequency for the auto increment. Value is given in ms
*'Down-Arrow' / 'Up-Arrow':\tDecrease/Increase the interval for color changes
*'Left-Arrow' / 'Right-Arrow':\tManually decrease/increase color
*'r':\t\t\t\t\t\t\t\tSet color to constant 'red' <-> RGB(255,0,0)
*'g':\t\t\t\t\t\t\t\tSet color to constant 'green' <-> RGB(0,255,0)
*'b':\t\t\t\t\t\t\t\tSet color to constant 'blue' <-> RGB(0,0,255)
*'i','h' or 'F1':\t\t\t\t\tShow the help

