# PeopleFisher

A mesh based positioning and messaging application , that is completelely offline and is free to use without any installation cost.
<br/>

**Working of the App:- https://www.youtube.com/watch?v=ptAid6plkws**
<br/>
**Introduction**<br/>
The PeopleFisher application was designed to work under the harshest of conditions and in the remotest of the locations , and these things<br/>
are usually common during disasters.<br/>

**Tech Stack**<br/>
Android - Used to create the mobile application. Heavily focusses on WifiP2p.<br/>
AZURE database - Used to sync the messages sent to an online server (Under development)<br/>
AZURE blob - Used to upload the apk file.<br/>


**How the App satisfies all the criteria**<br/>
1.Uniqueness of the solution:<br/>
Our app was built from the ground up considering disasters in the recent times.
<br/>

2.Feasibility of the solution:<br/>
PeopleFisher is a free application and involves zero installation cost for the government and the NGOS.Since a wifi device in a phone <br/>
can reach upto hundred meters , this is extremely feasible for even modestly populated areas, as the mesh <br/>
can extend for ever.<br/>

3.Maximum impact to successful outcomes:<br/>
It is extremely economical to implement and can potentially save thousands of lives at zero cost.<br/>

4.Relevance:<br/>
People Fisher is relevant to the topic chosen<br/>

**Tips for using the app**:<br/>
Follow these tips to avoid errors:<br/>
1.Since peer to peer connection is a resource intensive task,
<br/>once the send message button is pressed, the user has to wait till the phone has attempted to send the messae to all the nearby peers(usually a minut or two)
<br/>This is a LIMITATION of the ANDROID device, and not the APPLICATION.
<br/>
2.If the app choses not to respond, please switch off the wifi, and the application and try again.
<br/>
3.Trilateration is currently not supported on all the android devices so please use the Trilateration button with utmost caution.
 <br/>


3.**Buttons**:
<br/>
send : runs a for loop through the peers in range,  and sends messages to them<br/> 
takes time to implement a=due to device limitation so please be patient and do not spam the send button.<br/>
trilateration: this is similar but instead it requests data from the client , which is further used to calculate the latiude longitude of
the user<br/>
this is also a slow process due to device limitation so please do not spam the trilateration button.<br/>

**Problems Faced**<br/>

1.The main problem we faced was in implementing the wifiP2p chat proess , as in our app, every time you<br/>
send a message, you act as a message sender and send to clients without them joining your wifi group.<br/>
Since group formation takes a lot of time in allocating resources for a lot of connections(sometimes <br/>
leading to a delay of 40 50 seconds).so we had to set individual p2p connections for every phone in the vicinity, which took 
<br/> considerably less time compared to group formation(30 seconds for 3 peers) by running a for loop.<br/>

2.We had tried different types of connections , such as Android Messages, Android nearby, which extremely didnt suit <br/>
our purpose, as they relied heavily on the internet and bluetooth.So we discarded them and made the custom p2p connection model, 
<br/> with custom methods for the sockets(server and client).

3.We have used AZURE blob to upload our apk.
<br/>




**THANK YOU!!**
