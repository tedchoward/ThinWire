                      ThinWire(TM) RIA Ajax Framework
              Copyright (C) 2003-2006 Custom Credit Systems
   
 This program is free software; you can redistribute it and/or modify it under
 the terms of the GNU General Public License as published by the Free Software
 Foundation; either version 2 of the License, or (at your option) any later
 version.
   
 This program is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
   
 You should have received a copy of the GNU General Public License along with
 this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 Place, Suite 330, Boston, MA 02111-1307 USA
  
 Users wishing to use this library in proprietary products which are not 
 themselves to be released under the GNU Public License should contact Custom
 Credit Systems for a license to do so.
   
               Custom Credit Systems, Richardson, TX 75081, USA.
                          http://www.thinwire.com

===============================================================================
                     Rich Web Applications, Made Simple!
===============================================================================
ThinWire(R) is an open source development framework that allows you to easily 
build applications for the web that have responsive, expressive & interactive
user interfaces without the complexity of the alternatives. While virtually any
web application can be built with ThinWire(R), when it comes to enterprise 
applications, the framework excels with its highly interactive and rich user 
interface components. Use ThinWire(R) to handle the view-layer of your Java EE 
(J2EE) application and you'll be able to provide an unparalleled user 
experience, while at the same time completing your project faster than ever. 

===============================================================================
                          Building the Framework
===============================================================================
The build process for ThinWire is defined using the Apache Ant build tool. It
has only been built using Ant 1.6 or greater, but it may build correctly with
earlier releases as well.  You can learn about the Apache Ant project and
download a working version from: http://ant.apache.org/

Once you have Ant installed and added to your system path, you can build the
ThinWire(R) framework simply by typing 'ant dist' at the command shell from
the 'build' directory.  The following Ant build targets are supported:

 dist        compile the framework, create a jar and package
	         it along with other required runtime files into
             a distribution zip.

 dist14      compile a Java 1.4 compatible version of the
             framework, create a jar and package it along with
             other required runtime files into a distribution
             zip.
	
 source      create a source only distribution that contains
             everything necessary to build the framework.

 javadoc     generate the framework API documentation and
             package it into a javadoc only distribution zip
             file.
