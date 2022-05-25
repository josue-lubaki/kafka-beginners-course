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
<p>As a rule, for a replication factor of N, you can permanently lose up to <b>N-1 brokers</b> and still recover your data</p>

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
<li><b>retry.backoff.ms (default 100ms)</b> : combien de temps attendre avant de retenter</li>
<li><b>delivery.timeout.ms (default 120 000 ms)</b> : Records will be failed if they can't be acknowledgement within time</li>
<li><b>max.in.flight.requests.per.connection (default 5) </b>: For this, you can set the setting while controls how many produce requests can be made in parallel; set it to 1 if you ensure ordering (may impact throughput)</li>

<p>Since kafka 3.0, the producer is "safe" by default : </p>
<li>acks=all (-1)</li>
<li>enable.idempotence=true</li>
<li>retries=MAX_INT</li>
<li>max.in.flight.requests,per.connection=5</li>

<h3>Message Compression at the Broker / Topic level</h3>
<li><b>compression.type=producer (default)</b>, the broker takes the compressed batch from the producer client and writes
it directly to the topic's log fil without recompressing the data.</li>

<li><b>compression.type=none</b> : all batches are decompressed by the broker</li>
<li><b>compression.type=lz4</b> : (for exemple)</li>
<ol>
    <li>if it's matching the producer setting, data is stored on disk as is</li>
    <li>if it's different compression setting, batches are decompressed by the broker and then recompressed using the compression algorithm specified</li>
</ol>
<li><b>linger.ms : (default 0)</b> : how long to wait until we send a batch. Adding a small number for example 5 ms helps add more messages in the batch at the expense of latency.</li>
<li><b>batch.size (default 16KB)</b> : if a batch is filled before linger.ms, increase the batch size. Increasing a batch size to something like 32KB or 64KB can help increasing the compression, throughput, and efficiency of requests.</li>

```properties
properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "20");
properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32 * 1024))
```