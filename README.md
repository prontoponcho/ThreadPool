# ThreadPool
###Server-side code managing a dynamically resizing pool of threads for handling parallel client queries.
####Contents:
<ul>
<li>report.pdf describes the class interactions with UML and program execution with screenshots.
<li>src/ contains all the code
</ul>
####Key points:
<ul>
<li>Each client requests the execution of a Job (in this case a simple math operation). 
<li>The server handles all requests--including malformed requests--gracefully.
<li>The ThreadManager dynamically resizes the ThreadPool to handle the number of incoming Jobs.
<li>A SharedQueue is a monitor object for proper thread synchronization.
</ul>
####To compile and run the server:
```
> cd src/
> javac *.java
> java Server
 ```

####To test the server's ability to handle parallel client requests, open another terminal and run SimpleClient:
```
> cd src/
> java SimpleClient N_CLIENTS IP_ADDRESS
```
where N_CLIENTS is the server load and IP_ADDRESS is localhost: 127.0.0.1

####To to test how the server handles various commands, open another terminal and run GUIClient:
```
> cd src/
> java GUIClient
```
where you'll be prompted for the server address: 127.0.0.1
