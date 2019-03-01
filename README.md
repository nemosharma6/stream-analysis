## stream-analysis
real time stream processing

### versions
kafka_2.11-2.1.0  
elasticsearch-6.6.0   
kibana-6.6.0-darwin-x86_64   
spark libraries v2.3.0    
stanford-corenlp libraries v3.7.0

### setup

bin/zookeeper-server-start.sh config/zookeeper.properties   
bin/kafka-server-start.sh config/server.properties    
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic toby    
bin/elasticsearch   
bin/kibana    

Create an index in ES as follows:
```
PUT toby
{
  "mappings": {
    "_doc": {
      "properties": {
        "timestamp": {
          "type": "long"
        },
        "category": {
          "type": "text"
        },
        "location": {
          "type": "geo_point"
        },
        "rating": {
          "type": "double"
        },
        "content": {
          "type": "text"
        }
      }
    }
  }
}
```
Make changes to the twitter filter keyword and execute Runner object. Finally, tweets having geo location would be inserted into the ES index which can be visualised via kibana geo location graph. 
