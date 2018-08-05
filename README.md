# Dynamo example 

In one terminal run local dynamo (by default on port 8000):
```bash
LATEST_URL=https://s3-us-west-2.amazonaws.com/dynamodb-local/dynamodb_local_latest.tar.gz
mkdir -p /tmp/dynamo && cd /tmp/dynamo && curl -s $LATEST_URL | tar xz
java -Djava.library.path=./DynamoDBLocal_lib -jar DynamoDBLocal.jar -sharedDb
```
_[read aws instructions here](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/DynamoDBLocal.html#DynamoDBLocal.Maven)_ 

Test local dynamo is up: 
```bash 
aws dynamodb list-tables --endpoint-url http://localhost:8000
# or once there's an item: 
aws dynamodb get-item --table-name User --key='{"id":{"S":"someId"}}'  --endpoint-url http://localhost:8000
```

Back in this project dir, set up your env and run the tests:
```bash
cat << EOF > .envrc
export dynamo_endpoint="http://localhost:8000/"
export aws_access_key=foo
export aws_secret_key=bar
EOF

gradle build test

```
