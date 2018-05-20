# Liberty & Db2 express-c





## 利用するコンテナ

WebSphere Libery と Db2 Express Community Edition を利用します。

* https://hub.docker.com/_/websphere-liberty/
* https://hub.docker.com/r/ibmcom/db2express-c/



## Db2 JDBCドライバーのダウンロード


次のページから、JDBCドライバをダウンロードします。 IBM IDの登録が必要になりますので、IBM IDを持っていなければ、ダウンロード・ページのリンクから、IBM IDを取得してください。

https://www-01.ibm.com/marketing/iwm/iwm/web/reg/download.do?source=swg-idsdjs&S_PKG=dl&lang=ja_JP&cp=UTF-8&dlmethod=http

ダウンロードしたファイルの圧縮を解いて、db2jcc.jar と db2jcc4.jar を db2のディレクトの下にコピーします。



## dockerコマンドから起動方法

~~~
$ docker run -d --rm -p 9080:9080 -v /Users/maho/docker/java/config:/config -v /Users/maho/docker/java/db2:/opt/ibm/db2 --link db2 websphere-liberty:webProfile7
~~~


MacOSのコンテナホストのファイルシステムにテーブルスペースを作れない制限があるので、データベースのテーブルスペースのコンテナ領域は、コンテナ内部に持たせるため、次の様に起動する

~~~
$ docker run -d --rm --name db2 -p 50000:50000  -e DB2INST1_PASSWORD=db2inst1-pwd -e LICENSE=accept -v /Users/maho/git/tkr1/sql:/share  ibmcom/db2express-c db2start
~~~




## docker-compose から起動方法

docker-compose.ymlの存在するディレクトリから、次の様に起動する。

~~~
docker-compose up -d
~~~

一時停止と再開

~~~
docker-compose stop
docker-compose start
~~~

クリーンナップ（コンテナまで）

~~~
docker-compose down
~~~

クリーンナップ（コンテナイメージまで）

~~~
docker-compose down --rmi all
~~~



## Kubernetesからの起動方法

minikubeがインストールされている事を前提にします。

minikubeを起動して、kubectl コマンドが利用できることを確認します。

~~~
$ minikube start
There is a newer version of minikube available (v0.27.0).  Download it here:
https://github.com/kubernetes/minikube/releases/tag/v0.27.0

To disable this notification, run the following:
minikube config set WantUpdateNotification false
Starting local Kubernetes v1.10.0 cluster...
Starting VM...
Getting VM IP address...
Moving files into cluster...
Setting up certs...
Connecting to cluster...
Setting up kubeconfig...
Starting cluster components...
Kubectl is now configured to use the cluster.
Loading cached images from config file.

$ kubectl get node
NAME       STATUS    ROLES     AGE       VERSION
minikube   Ready     master    46s       v1.10.0
~~~

## DB2の起動

コンフィグマップの設定として、sqlディレクトリをConfigMapへ設定する

~~~
$ kubectl create configmap sql-files --from-file=sql
$ kubectl get configmap
$ kubectl describe configmap sql-files
~~~

~~~
kubectl apply -f k8s-db2.yml
~~~

configMapは、1Mバイドまでのサイズ制限がありますから、アプリとDB2ドライバが含まれたコンテナをビルドして、Docker Hubへ登録して利用します。

~~~
docker built -t java-apl-1st:0.1 -f Dockerfile.was .
~~~

ローカルでのテスト実行

~~~
docker run -d --rm -p 9080:9080 --link db2 java-apl-1st:0.1
~~~

~~~
docker login
docker tag java-apl-1st:0.1 (docker hub username)/java-apl-1st:0.1
docker push (docker hub username)/java-apl-1st:0.1
~~~



~~~
kubectl apply -f k8s-was.yml
~~~




# WebSphereリバティの環境セットアップ

## セッションテーブルのセットアップ

db2express-cの公式コンテナの起動時に、データベースの作成および初期化の機能が実装されていなかったために、手作業で実施する。


コンテナIDを調べ、コンテナに対話型シェルを起動する

~~~
imac:java maho$ docker ps
CONTAINER ID        IMAGE                           COMMAND                  CREATED             STATUS              PORTS                              NAMES
2d7776c9c4b9        websphere-liberty:webProfile7   "/opt/ibm/docker/doc…"   9 hours ago         Up 9 hours          0.0.0.0:9080->9080/tcp, 9443/tcp   agitated_agnesi
bece6b74d717        ibmcom/db2express-c             "/entrypoint.sh db2s…"   16 hours ago        Up 16 hours         22/tcp, 0.0.0.0:50000->50000/tcp   db2

imac:java maho$ docker exec -it bece6b74d717 bash
~~~

テーブルスペース用のディレクトリを作成して、データベースを作成して、接続する。

~~~
[root@bece6b74d717 /]# mkdir /tablespace
[root@bece6b74d717 /]# chown db2inst1:db2inst1 /tablespace
[root@bece6b74d717 /]# su - db2inst1
Last login: Fri May 18 11:47:20 UTC 2018 on pts/0
[db2inst1@bece6b74d717 ~]$ db2 create db session on /tablespace
[db2inst1@bece6b74d717 ~]$ db2 connect to session

   Database Connection Information

 Database server        = DB2/LINUXX8664 10.5.5
 SQL authorization ID   = DB2INST1
 Local database alias   = SESSION
~~~

WebSphereのセッション管理用のテーブルを作成する。

~~~
[db2inst1@bece6b74d717 ~]$ db2 -tf /share/create_session_table.sql
[db2inst1@bece6b74d717 ~]$ db2 list tables;

Table/View                      Schema          Type  Creation time             
------------------------------- --------------- ----- --------------------------
SESSIONS                        DB2INST1        T     2018-05-18-11.40.56.979113

  1 record(s) selected.
~~~


その他、確認用コマンド

* セッション管理用テーブルの構造表示
* セッション管理テーブルのSELECT
* テーブルスペースのリスト
* テーブルスペースのコンテナ１の詳細（パス等）の表示

~~~
$ db2 describe table sessions;

$ db2 select id, appname from sessions

$ db2 list tablespaces

$ db2 list tablespace containers for 1
~~~


## WebSphereリバティのアプリのデプロイ

* WebSphereの設定変更は、config/server.xmlを編集する
* アプリケーションのデプロイは、config/dropins下へ war または ear ファイルをコピーする


