# ppp

Alpha Complex Ministy of Information.

## Overview

You are an investigator in Alpha Complex's Internal Security, searching through records to sniff out treason. Starting at RED clearance, you will work your way up through the ranks and gain control over staff, and eventually may even make it to head of IntSec. Just be aware of other players looking to clamber up over your corpse.

## Setup

To get an interactive development environment run:

    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. After the compilation process is complete, you will
get a Browser Connected REPL. An easy way to try it is:

    (js/alert "Am I connected?")

and you should see an alert in the browser window.

To clean all compiled files:

    lein clean

To create a production build run:

    lein do clean, cljsbuild once min

And open your browser in `resources/public/index.html`. You will not
get live reloading, nor a REPL. 

## License

Copyright © 2014 Logan Senjov

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
