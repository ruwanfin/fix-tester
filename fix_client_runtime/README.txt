Finexa FIX Tester - Windows Runtime
===================================

How to run
----------
1. Install Java 21 or newer on the Windows machine.
2. Double-click run.bat.
3. Keep the command window open while using the application.
4. Open this URL:

   http://localhost:8091/

Health check:

   http://localhost:8091/api/fix/health

How to stop
-----------
Press Ctrl+C in the command window, then confirm with Y if Windows asks.

Files needed
------------
This runtime folder only needs:

   finexa-fix-tester.jar
   run.bat

README.txt is only for instructions.

Notes
-----
The app runs on port 8091 by default.
Kafka is configured in run.bat by default:

   192.168.122.68:9092,192.168.122.195:9092,192.168.122.224:9092

If Kafka or the target gRPC services are on another machine, edit run.bat and
change KAFKA_BOOTSTRAP_SERVERS before starting the app.
