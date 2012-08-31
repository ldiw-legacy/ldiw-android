Let's do it world Android client

This is Android client for Let's do it [World Waste Map] (http://www.letsdoitworld.org/mapthewaste)

Info: jaak - at - letsdoitworld.org

Translations: please contribute using https://www.transifex.com/projects/p/ldiwandroid/
![Translations in Transifex](https://www.transifex.com/projects/p/ldiwandroid/resource/strings/chart/image_png)

Developers: register user in transifex.com, install transifex client and pull translation updates from there. Push source file if new strings are added. Details:
<pre><code>
 cd <project dir>
 sudo easy_install --upgrade transifex-client
 tx init 
   -> provide your transifex account
</code></pre>   
Get updated translations: 
<pre><code>
 tx pull -a
 </code></pre>
Push local source and translation changes:
<pre><code>
 tx push -s -t
</code></pre>


License:

 Copyright 2011 Let's Do It World. All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY Let's Do It World ''AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Let's Do It World OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those of the authors and should not be interpreted as representing official policies, either expressed or implied, of Let's Do It World.