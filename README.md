iRwaver
=======

There are many pages out there describing how you can actually [control IR devices using LED connected to your audio output jack](http://rtfms.com/episode-4-turn-your-iphoneandroid-mac-pc-player-etc-into-a-universal-remote.htm).

Once you got the hardware, the question is, how can you generate the codes. I needed those for my [arbot](https://github.com/jernejkase/arbot)
and since I'm more of a java programmer, here's a simple java script to generate RC5 IR wav files.

### The IR

How does IR actually work? On the most basic level the transmitter (IR LED) either transmits or doesn't transmit. But while it transmits,
it's not turned on the whole time - it blinks (turns on and off) rapidly, 38.000 times per second!

If you want to transmit information to your friend by turning light on and off, you need to agree in advanced how the information
is going to be encoded. For example, you could use the morse code.

The same way, the transmitter (the remote) and the receiver need to agree on the protocol to be able to exchange information. One such protocol
is [RC-5](http://en.wikipedia.org/wiki/RC-5). 

In RC5, bit is represended by _space_ folowed by _mark_. Space means "light off" and mark "light on". Both signals are 889 microseconds long - that's less than 1 millisecond, which is like, really short time. By RC5 standard, the frequency of the blinking should be 36kHz, but many implementations out there us 38kHz.

|space mark| represents bit 1

|mark space| represents bit 0

That's as simple as encoding can be. But sending 1 and 0 doesn't meen much if you don't know what to expect. That's why RC5 code has standardised message protocol, which you cann look up on wiki page.

The Arduino Library we are using is doing all the hard work for us and transforms the incomming blinking into a RC5 code.
In my case, I'm using:
- 0x820 and 0x20 for forwards
- 0x810 and 0x10 for left
- 0x821 and 0x21 for right
- 0x811 and 0x11 for backwards

Why those? Because they are assigned the "cursor keys" on my remote. You can use IR library to display codes on your computer and adjust them as needed.

### The .dat files
There are two sets of dat files, one for 36 and the other for 38 kHz signal. Each sets contains mark (blinking) and space (turned off).

The mark files were generated with Audacity. Since mark is 889 microseconds long, 
and sampling frequency is 44100Hz that's 0.0441 samples per microsecond (not milli - micro!) or 39 samples.
For 889ms mark sample we need to create a square tone of 19000Hz for 39 samples. Why 19000? Because 38kHz is too much with sampling
frequency of 44100Hz, and that's why we need two leds to blink at each turning point to get up to 38kHz.

In the same way, the space is created with 39 samples of silence, and 36kHz files are created with frequency of 18kHz.

If you want to create any other frequencies or any other codes than RC5, you need to generate custom samples and tweak the code.
Note that it is possible to generate binary representations of those dat files in code, but this seemed like the simplest solution.

### The generator
Simply buld it and run java -jar iRwaver.jar [code] [address] [frequency], eg java -jar iRwaver.jar 20 0 38 to generate RC5 wav files which you can include in your projects.
