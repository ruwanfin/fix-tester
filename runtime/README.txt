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

Kafka
-----
Kafka is configured in run.bat:

   192.168.122.68:9092,192.168.122.195:9092,192.168.122.224:9092

If Kafka is on another machine, edit KAFKA_BOOTSTRAP_SERVERS in run.bat.
