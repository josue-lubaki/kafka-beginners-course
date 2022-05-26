# kafka-beginners-course
introduction to the concept related to the use of Kafka

<h5>Kafka est un système distribué, possèdant une architecture resiliente et tolérant aux pannes.
capable de supporter des gros debits des messages par seconde.</h5>

<h5><u>Use Case</u></h5>
<ul>
    <li>Messaging system</li>
    <li>Activity tracking</li>
    <li>Collecter des métriques àpartir de nombreux emplacements</li>
    <li>Journaux d'application</li>
    <li>Traitement de flux</li>
    <li>Découpler les dépendances système et microservices (Spark, Flink, Hadoop)</li>
    <li>etc...</li>
</ul>

<h3>Topic</h3>
<ul>
    <li>a particular stream of data, it's like a table in a database (without all the constraints)</li>
    <li>You can have as many topics as you want</li>
    <li>A topic is identified by its <u>name</u></li>
    <li>kafka topics are immutable : once data is written to a partition, it cannot be changed. Vous pouvez continuer à écrire sur le partition, mais pas mettre à jour ni supprimer</li>
    <li>data kept only for a limited time (default is one week - configurable)</li>
    <li>Order is guaranteed only within partition (not across partitions)</li>
</ul>

<h3>Producers</h3>
<ul>
    <li>Producers write data to topics (which are made of partitions)</li>
    <li>In case of kafka broker failures, producers will automatically recover</li>
    <li>Producers can choose to send a key with the message (String, number binary, etc...)</li>
    <li>if key = null, data is sent round robin (partition 0, then 1, then 2,...)</li>
    <li>if key != null, then all messages for that will always go to the same partition (hashing)</li>
    <li>A key are typically sent if you need message ordering for a specific field</li>
    <li>In the defaut kafka partitionner, the keys are hashed using the murmur2 algorithm, will the formula below for the curious:</li>
</ul>

```properties 
targetPartition = Math.abs(Utils.murmur2(keyBytes)) % (numPartition - 1)
```
    
<h3>Consumers</h3>
<ul>
    <li>Consumers read data from a topic (identified by name) - ce n'est pas kafka qui transmet automatiquement les données aux consumers, mais ils doivent le demander (pull model)</li>
    <li>Consumers automatically know wich broker to read from</li>
    <li>In case of broker failures, consumers know how to recover</li>
    <li>Data is read in order from low to high offset within each partitions</li>
    <li>if a consumer dies, it willbe able be able to read back from where it left off, thanks to the committed consumer offsets !</li>
</ul>

<h3>Deserializer</h3>
<ul>
    <li>Deserializer indicates how to transfor bytes into objects/data</li>
    <li>They are used on the value and the key of the message</li>
    <li>Common deserializers (JSON, Avro, protobuf, etc...)</li>
    <li>the serialization/deserialization type must not change during a topic lifecycle (create a new topic instead)</li>
</ul>

<h3>Brokers</h3>
<ul>
    <li>A kafka cluster is composed of multiple brokers (servers) - ils reçoivent et envoient des données</li>
    <li>Each broker is identified with its ID (integer)</li>
    <li>Each broker contains certain topic partitions</li>
    <li>il n'est pas important de connaître tous les brokers du cluster, il suffit juste de se connaître comment se connscter à un broker et les clients se connecteront automatiquement aux autres.</li>
    <li>Each broker knows about all brokers, topics and partitions (metadata)</li>
</ul>

<h3>Producer Acknowledgement</h3>
<ul>
    Producer can choose to receive acknowledgement of data writes :
    <li><b>acks = 0</b> : Producer won't wait for acknowledgement (possible data lose)</li>
    <li><b>acks = 1</b> : producer will wait for leader acknowledgement (limited data loss)</li>
    <li><b>acks = all</b> : leader + replicas acknowledgement (no data loss)</li>
</ul>
<blockquote>As a rule, for a replication factor of N, you can permanently lose up to <b>N-1 brokers</b> and still recover your data</blockquote>
<p>Producer Default Partition when key = null</p>
<li><b>Round Robin</b> : for kafka 2.3 and below</li>
<li><b>Sticky Partition</b> : for kafka 2.4 and above</li>
<blockquote>Sticky Partition improves the performance of the producer especially when high throughput when the key is null</blockquote>

<h3>Zookeeper</h3>
<ul>
    <li>Zookeeper manages brokers (keeps a list of them)</li>
    <li>Zookeeper helps in performing leader election for partitions</li>
    <li>Zookeeper sends notifications to kafka in case of changes (e.g. new topic broker dies, broker comes up, delete topics, etc...)</li>
    <li>Zookeeper by design operates with an odd number of servers (1, 3, 5, 7)</li>
    <li>Zookeeper has a leader (writes) the rest of the servers are followers (reads)</li>
</ul>

<h3>Start Zookeeper and kafka with Docker</h3>
docker-compose.yml

```yml
    version: '3'

    services:
      zookeeper:
        image: wurstmeister/zookeeper
        container_name: zookeeper
        ports:
          - "2181:2181"
      kafka:
        image: wurstmeister/kafka
        container_name: kafka
        ports:
          - "9092:9092"
        environment:
          KAFKA_ADVERTISED_HOST_NAME: localhost
          KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
```

<h2>CLI commands</h2>
<p>Start Zookeeper and kafka server</p>

```properties
zookeeper-server-start.sh ~/kafka_<version>/config/zookeeper.properties
```

```properties
kafka-server-start.sh ~/kafka_<version>/config/server.properties
```

<h3>Topics</h3>
<li>List of topics</li>

```properties
kafka-topics.sh --bootstrap-server localhost:9092 --list
```

<li>Create a topic</li>

```properties
kafka-topics.sh --bootstrap-server localhost:9092 --create --topic [topic_name] \
--partitions 3 --replication-factor 1
```

<li>Describe a topic</li>

```properties
kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic [topic_name]
```

<h3>Producer</h3>
<li>create a producer</li>

```properties
kafka-console-producer.sh --bootstrap-server localhost:9092 --topic [topic_name] \
--producer-property acks=all --property parse.key=true --property key.separator=:
```

<h3>Consumer</h3>
<li>create a consumer</li>

```properties
kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic [topic_name] --from-beginning
```

```properties
[base] --formatter kafka.tools.DefaultMessageFormatter \
--property print.timestamp=true --property print.key=true \
--property print.value=true --from-beginning
```

<h3>Consumer Group</h3>
<li>List of Consumer groups</li>

```properties
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --list
```

<li>Describe</li>

```properties
kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group [group_name]
```

<li>Reset</li>

```properties
[base] --group [group_name] --reset-offsets --execute --to-earliest \
[--all-topics | --topic {topic_name}]
```

<li>Reculer de N Lag partition</li>

```properties
[base] --group [group_name] --reset-offsets --execute --shift-by -N \
[--all-topics | --topic {topic_name}]
```

<h3>Properties</h3>
<blockquote><b>retry.backoff.ms (default 100ms)</b> : combien de temps attendre avant de retenter</blockquote>
<blockquote><b>delivery.timeout.ms (default 120 000 ms)</b> : Records will be failed if they can't be acknowledgement within time</blockquote>
<blockquote><b>max.in.flight.requests.per.connection (default 5) </b>: For this, you can set the setting while controls how many produce requests can be made in parallel; set it to 1 if you ensure ordering (may impact throughput)</blockquote>

<p>Since kafka 3.0, the producer is "safe" by default : </p>
<li>acks=all (-1)</li>
<li>enable.idempotence=true</li>
<li>retries=MAX_INT</li>
<li>max.in.flight.requests.per.connection=5</li>

<h3>Message Compression at the Broker / Topic level</h3>
<blockquote><b>compression.type=producer (default)</b>, the broker takes the compressed batch from the producer client and writes
it directly to the topic's log fil without recompressing the data.</blockquote>

<blockquote><b>compression.type=none</b> : all batches are decompressed by the broker</blockquote>
<blockquote><b>compression.type=lz4</b> : (for exemple)</blockquote>
<ol>
    <li>if it's matching the producer setting, data is stored on disk as is</li>
    <li>if it's different compression setting, batches are decompressed by the broker and then recompressed using the compression algorithm specified</li>
</ol>
<blockquote><b>linger.ms : (default 0)</b> : how long to wait until we send a batch. Adding a small number for example 5 ms helps add more messages in the batch at the expense of latency.</blockquote>
<blockquote><b>batch.size (default 16KB)</b> : if a batch is filled before linger.ms, increase the batch size. Increasing a batch size to something like 32KB or 64KB can help increasing the compression, throughput, and efficiency of requests.</blockquote>

```properties
properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "20");
properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32 * 1024))
```

<blockquote><b>max.block.ms=60000</b> : the time .send() will block until throwing an exception</blockquote>
<p>Exceptions are thrown when :</p>
<pre>
    <li>The producer has filed up its buffer</li>
    <li>The broker is not accepting any new data</li>
    <li>60 seconds has elapsed</li>
</pre>

<h3>Delivery Semantics</h3>
<ol>
    <li><b>At least once</b> : offsets are committed as soon as the message batch is received If the processing goes wrong, the message will be lost (it won't be read again)</li>
    <li><b>At least once</b> : offsets are committed after the message is processed. If the processing goes wrong, the message will be read again. This can rsult in duplicate processing of messages. 
    Make sure your processing is idempotent.</li>
    <li><b>Exactly once</b> : can be achieved for kafka => kafka workflows using the transactional API (easy with kafka streams API).
    For kafka => sink workflows, use an idempotent consumer.</li>
</ol>

<blockquote>
For most applications you should use <b>at least once processing</b>
(we'll see in pratice how to do it) and ensure your transformations / processing are idempotent.
</blockquote>

<h3>Consumer Offset Commits Strategies</h3>
<blockquote>enable.auto.commit=true & synchronous processing of batches</blockquote>

```java
    while(true){
        List<Records> batch = consumer.poll(Duration.ofMillis(100));
        doSomethingSynchronous(batch)
    }
```
<li>enable.auto.commit=false& Synchronous processing of batches</li>

```java
    while(true){
        List<Records> batch = consumer.poll(Duration.ofMillis(100));
        if(isReady(batch)){
            doSomethingSynchronous(batch);
            consumer.commitAsynch
        }
    }
```

<blockquote><b>heart.interval.ms (default 3 seconds)</b> : howw often to send heartbeats. <br>
Usually set to 1/3rd of session.timeout.ms</blockquote>
<blockquote><b>session.timeout.ms (default 45 seconds kafka 3.0+)</b> : Heartbeats are sent periodically to the broker; If no heartbeat is sent during that period, the consumer is considered dead</blockquote>
<blockquote><b>max.poll.interval.ms (default 5 minutes)</b> : Maximun amount of time between two .poll() calls before declaring ths consumer died.</blockquote>
<blockquote><b>max.poll.records (default 500)</b> : controls how many records to receive per poll request; 
increase if your messages are very small and have a lot of available RAM; 
Lower if it takes you too much time to process records.
</blockquote>
<blockquote><b>fetch.min.bytes (default 1)</b> : controls how much data you want to pull at least on each request; helps improving throughput and decreasing request number; At the cost of latency.</blockquote>
<blockquote><b>fetch.max.wait.ms (default 500)</b> : the maximum amount of time the kafka broker will block before answering the fetch request if there isn't sufficient data to immediately satisfy the requirement given by fetch.min.bytes.<br>
This means that until the requirement of etch.min.bytes to be satisfied, you will have up to 500 ms of latency before the fetch returns datat to the consumer (e.g. introducing a potential delay to be more efficient in requests)
</blockquote>
<blockquote><b>max.partition.fetch.bytes (default 1MB)</b> : The maximum amount of data per partition the server will return.<br>
If you read from 100 partitions, you'll need a lot of memory (RAM)</blockquote>
<blockquote><b>fetch.max.bytes (default 55MB)</b> : Maximum data returned for each fetch request.<br>
If you have available memory, try increasing fetch.max.bytes to allow the consumer to read more data in each request.</blockquote>

<h3>Partitions and Segments</h3>
<blockquote><b>log.segment.bytes (default 1GB)</b> : the max size of a single segment in bytes.</blockquote>
<blockquote><b>log.segment.ms (default 1 week)</b> : the time kafka will wait before committing the segment if not full</blockquote>

<h3>Log Cleanup Policies</h3>
<blockquote><b>Policy 1: log.cleanup.policy=delete (kafka default for all users topics)</b> <br>
<li>Delete based on age of data (default is a week)</li>
<li>Delete based on max size of log (default is -1 == infinite)</li>
</blockquote>

<blockquote><b>Policy 2 : log.cleanup.policy=compact (kafka default for topic __consumer_offsets)</b></blockquote>

<h3>Log Compaction</h3>
<blockquote>
<b>log.cleanup.policy=compact is compacted by :</b>
<blockquote><b>segment.ms (default 7 days)</b> : max amount of time to wait to close active segment.</blockquote>
<blockquote><b>segment.bytes (default 1GB)</b> : max size of a segment</blockquote>
<blockquote><b>min.compaction.log.ms (default 0)</b> : how long to wait before a message can be compacted.</blockquote>
<blockquote><b>delete.retention.ms (default 24 hours)</b> : wait before deleting data marked for compaction</blockquote>
<blockquote><b>min.cleanable.dirty.ratio (default 0.5)</b> : higher => less, more efficient cleaning.lower => opposite</blockquote>
</blockquote>

<h3>Add Config</h3>

```properties
kafka-configs --bootstrap-server localhost:9092 --entity-type [topics | users] \
--entity-name [topic_name] --alter --add-config min.insync.replicas=2
```

<h4>summarized by Josue Lubaki</h4>
<h4>Source : https://www.udemy.com/course/apache-kafka/ </h4>

<div align="center">
<img src="https://udemy-certificate.s3.amazonaws.com/image/UC-072eb7e8-6e57-4d94-9848-1044b298cb07.jpg?v=1653424140000" alt="certification" width="85%">
</div>