# content security policy  One time soultion 

It is a one time solution for adding csp headers in SPA like Angular, React etc. I have tested it on only Angular 13 and 14, though. In angular as we know that after compilation the build updates the index.hmtl file that includes avery dependent css and js files for the application. 
When we add csp headers in http response it points to this index.html file and consider every *.css and *.js file as from external sources and blocks them. This can be resolved by adding hash value of each of these file in csp header (script-src). 

I have automated this process by using tomcat valve. Using this project you can create a jar file and just add that in your tomcat/lib and add an entry in server.xml and you will be good to go.

Basically, there are 3 steps involved in this automated process of adding csp headers in http response,

1: Run the ng build with this argument   --subresource-integrity

  After running now if you check your index.html in your build then you will find every css and js imports have an additional attribute of 'intergrity=SHA-**'. 

2: Import and run my project from 'Run Configuration'  it will create a jar with all dependecies included in it. Now add this jar in tomcat/lib

3: Add this line in your tomcat server.xml   <Valve className="com.elm.CSPValve"/>

Restart, you should be able to see csp headers in http reponse in 'Network' tab in your browser.

Whenever, you need to change anything in the jar file you can change that in the source recreate the jar and replace that in tomact/lib and restart the server. Now, you have a csp header solution which is decoupled from your application.

This jar file is specific to tomact server. If you use some other serve this jar won't work. But the idea is to locate index.html. You can rewrite the code to just get to your index.html in ROOT folder.


