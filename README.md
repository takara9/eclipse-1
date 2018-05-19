# Liberty & Db2 express-c


https://hub.docker.com/_/websphere-liberty/

https://hub.docker.com/r/ibmcom/db2express-c/


~~~
$ docker run -d -p 9080:9080 -v /Users/maho/docker/java/config:/config -v /Users/maho/docker/java/db2:/opt/ibm/db2 --link db2 websphere-liberty:webProfile7
~~~

~~~
docker run --name db2 -p 50000:50000  -e DB2INST1_PASSWORD=db2inst1-pwd -e LICENSE=accept  -d ibmcom/db2express-c db2start
or

docker run -d --rm --name db2 -p 50000:50000  -e DB2INST1_PASSWORD=db2inst1-pwd -e LICENSE=accept -v /Users/maho/docker/java/db2inst1:/tablespace -v /Users/maho/git/tkr1/sql:/share  ibmcom/db2express-c db2start

docker run -d --rm --name db2 -p 50000:50000  -e DB2INST1_PASSWORD=db2inst1-pwd -e LICENSE=accept -v /Users/maho/git/tkr1/sql:/share  ibmcom/db2express-c db2start
~~~

コンテナホストのボリュームにテーブルスペースを作れない




## セッションテーブルのセットアップ

~~~
imac:java maho$ docker ps
CONTAINER ID        IMAGE                           COMMAND                  CREATED             STATUS              PORTS                              NAMES
2d7776c9c4b9        websphere-liberty:webProfile7   "/opt/ibm/docker/doc…"   9 hours ago         Up 9 hours          0.0.0.0:9080->9080/tcp, 9443/tcp   agitated_agnesi
bece6b74d717        ibmcom/db2express-c             "/entrypoint.sh db2s…"   16 hours ago        Up 16 hours         22/tcp, 0.0.0.0:50000->50000/tcp   db2

imac:java maho$ docker exec -it bece6b74d717 bash
[root@bece6b74d717 /]# su - db2inst1
Last login: Fri May 18 11:47:20 UTC 2018 on pts/0
[db2inst1@bece6b74d717 ~]$ db2 create db session
[db2inst1@bece6b74d717 ~]$ db2 connect to session

   Database Connection Information

 Database server        = DB2/LINUXX8664 10.5.5
 SQL authorization ID   = DB2INST1
 Local database alias   = SESSION


[db2inst1@bece6b74d717 ~]$ db2 -tf create_session_table.sql

[db2inst1@bece6b74d717 ~]$ db2 list tables;

Table/View                      Schema          Type  Creation time             
------------------------------- --------------- ----- --------------------------
SESSIONS                        DB2INST1        T     2018-05-18-11.40.56.979113

  1 record(s) selected.

[db2inst1@bece6b74d717 ~]$ db2 describe table sessions;

[db2inst1@bece6b74d717 ~]$ db2 select id, appname from sessions

[db2inst1@bece6b74d717 ~]$ db2 list tablespaces

[db2inst1@bece6b74d717 ~]$ db2 list tablespace containers for 1

~~~


