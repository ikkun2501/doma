==================
ビルド
==================

.. contents:: 目次
   :depth: 3

Maven Central Repository
========================

Doma の jar ファイルは Maven Central Repository から入手できます。
GroupId と ArtifactId の名称は以下の通りです。

:GroupId: org.seasar.doma
:ArtifactId: doma

.. _eclipse-build:

Eclipse を使ったビルド
======================

Eclipse でビルドを行う際のポイントは以下の通りです。

* プロジェクトの設定で注釈処理を有効にする
* Build Path に加えて Factory Path にも Doma の jar ファイルを設定する

.. note::

  手動で設定するよりも Gradle の eclipse タスクで自動設定することを推奨します。
  詳細については、
  `domaframework/simple-boilerplate <https://github.com/domaframework/simple-boilerplate>`_ 
  に含まれる build.gradle と eclipse.gradle を参照ください。

注釈処理の有効化
----------------

注釈処理を有効化するには、メニューから Project > Properties を選んで画面を開き
左のメユーから Java Compiler > Annotation Processing を選択します。

そして、下記に示すチェックボックスにチェックを入れます。

.. image:: images/annotation-processing.png
   :width: 80 %

|

Factory Path の設定
-------------------

注釈処理を有効化するには、メニューから Project > Properties を選んで画面を開き
左のメユーから Java Compiler > Annotation Processing > Factory Path を選択します。

そして、下記に示すチェックボックスにチェックを入れ、
ビルドパスで指定しているのと同じバージョンの Doma の jar を登録します。

.. image:: images/factory-path.png
   :width: 80 %

|

IntelliJ IDEA を使ったビルド
================================

IntelliJ IDEA でビルドを行う際のポイントは以下の通りです。

* Module の設定で Inherit project compile output pathを有効にする
* Preferences の設定で注釈処理を有効にする
* 注釈処理で生成されたコードが出力されるディレクトリを Generated Sources Root に設定する

詳細な設定方法については :ref:`idea-annotation-processor` を参照してください。

Gradle を使ったビルド
=====================

Gradle でビルドを行う際のポイントは以下のとおりです。

* domaが注釈処理で参照するリソースをテンポラリディレクトリに抽出する
* テンポラリディレクトリ内のリソースをcompileJavaタスクの出力先ディレクトリにコピーする
* テンポラリディレクトリをcompileJavaタスクの入力ディレクトリに設定する
* テスト時は注釈処理を無効にする
* 依存関係の設定でdomaの注釈処理を実行することを示す
* 依存関係の設定でdomaへの依存を示す

サンプルのbuild.gradleです。

.. code-block:: groovy

  apply plugin: 'java'

  // テンポラリディレクトリのパスを定義する
  ext.domaResourcesDir = "${buildDir}/tmp/doma-resources"

  // domaが注釈処理で参照するリソースをテンポラリディレクトリに抽出
  task extractDomaResources(type: Copy, dependsOn: processResources)  {
      from processResources.destinationDir
      include 'doma.compile.config'
      include 'META-INF/**/*.sql'
      include 'META-INF/**/*.script'
      into domaResourcesDir
  }

 　// テンポラリディレクトリ内のリソースをcompileJavaタスクの出力先ディレクトリにコピーする
  task copyDomaResources(type: Copy, dependsOn: extractDomaResources)  {
      from domaResourcesDir
      into compileJava.destinationDir
  }

 　compileJava {
   　　 // 上述のタスクに依存させる
  　　  dependsOn copyDomaResources
       // テンポラリディレクトリをcompileJavaタスクの入力ディレクトリに設定する
    　　inputs.dir domaResourcesDir
    　　options.encoding = 'UTF-8'
 　}

  compileTestJava {
      options.encoding = 'UTF-8'
      // テストの実行時は注釈処理を無効にする
      options.compilerArgs = ['-proc:none']
  }

  dependencies {
      // domaの注釈処理を実行することを示す
      annotationProcessor "org.seasar.doma:doma:2.20.1-SNAPSHOT"
      // domaへの依存を示す
      implementation "org.seasar.doma:doma:2.20.1-SNAPSHOT"
  }

  repositories {
      mavenCentral()
      maven {url 'https://oss.sonatype.org/content/repositories/snapshots/'}
  }

.. note::

  リポジトリにおける https://oss.sonatype.org/content/repositories/snapshots/ の設定は
  Doma の SNAPSHOT を参照したい場合にのみ必要です。

  Doma の SNAPSHOT は `Travis-CI <https://travis-ci.org/domaframework/doma>`_
  でビルドが成功されるたびに作成されリポジトリに配置されます。

.. note::

  上述のbuild.gradleの書き方により、Gradle 5.0 で導入された
  `Incremental annotation processing <https://gradle.org/whats-new/gradle-5/#incremental-annotation-processing>`_ の恩恵を受けられます。
  

Gradle を使ったより詳細なビルドスクリプトの例として、
`domaframework/simple-boilerplate <https://github.com/domaframework/simple-boilerplate>`_ 
を参照にしてください。
