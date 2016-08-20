# Single node Cassandra

This is a single node Cassandra setup, based on the official Cassandra Docker 3.7 release. In order to run it, execute:

```bash
./run.sh
```

Which will create a Docker volume named "cassandra-data", then launch a new container named "cassandra-single" using the official cassandra:3.7 image.