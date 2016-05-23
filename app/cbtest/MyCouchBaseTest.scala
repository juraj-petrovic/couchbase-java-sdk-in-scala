package cbtest

import com.couchbase.client.java._
import com.couchbase.client.java.document.JsonDocument
import com.couchbase.client.java.document.json.JsonObject
import com.couchbase.client.java.query._
import com.couchbase.client.java.view.{AsyncViewResult, AsyncViewRow, ViewQuery}

import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.collection.JavaConverters._
import rx.functions._
import rx.lang.scala.JavaConversions._
import rx.lang.scala.{Observable, Observer}

import scala.collection.mutable.ListBuffer

/**
  * Created by j.petrovic on 5/17/2016.
  */
object MyCouchBaseTest extends App {
  private[this] val viewName = "guest_view_by_date"
  private[this] val docName = "dev_guest_design_doc"
  val cluster = CouchbaseCluster.create("127.0.0.1")
  val bucket: Bucket = cluster.openBucket("profile")
  val bucketManager = bucket.bucketManager()
  testN1QL(bucket)
  val doc = testGetById("1",bucket)
  upsertDoc(JsonDocument.create("-1",doc.content().put("newValue", "inserted")),bucket)
  testGetById("-1",bucket)
  loadView(docName, viewName, bucket)


  def loadView(docName: String, viewName: String, bucket: Bucket):Unit = {
    val observableResult: Observable[AsyncViewResult] = bucket.async().query(ViewQuery.from(docName,viewName).includeDocs(true).limit(5))
    val promise = Promise[List[JsonDocument]]
    observableResult.subscribe(// -- 3
      result => {
        val values: ListBuffer[JsonDocument] = new ListBuffer
        val rows: Observable[AsyncViewRow] = result.rows()
        rows.subscribe(
          onNext = row => {val doc: Observable[JsonDocument] = row.document()
          doc.subscribe(
            onNext = document => values+=document
          )},
          onError = throwable => promise.failure(throwable),
          onCompleted = () => promise.success(values.toList)
        )
      },
      throwable => println("failure:" +throwable)
    )
    val future: Future[List[JsonDocument]] = promise.future // -- 4
    println()
    println("result of view: " + docName + "/" + viewName)
    val jsonList: List[JsonDocument] = Await.result(future, Duration.Inf)
    jsonList.foreach(println)
  }


  def upsertDoc(document: JsonDocument, bucket: Bucket):Unit = {
    val observableResult: Observable[JsonDocument] =  bucket.async().upsert(document)
    val promise = Promise[JsonDocument]
    observableResult.subscribe(// -- 3
      result => promise.success(result),
      throwable => promise.failure(throwable)
    )
    val future: Future[JsonDocument] = promise.future
    val document2: JsonDocument = Await.result(future, Duration.Inf)
    println()
    println("upsertDoc")
    println(document2.content().toString)
//    Json.parse
  }

  def testGetById(id: String,bucket: Bucket):JsonDocument = {
    val observableResult: Observable[JsonDocument] = bucket.async().get(id)
    val promise = Promise[JsonDocument]
    observableResult.subscribe(// -- 3
      result => promise.success(result),
      throwable => promise.failure(throwable)
    )
    val future: Future[JsonDocument] = promise.future
    val document: JsonDocument = Await.result(future, Duration.Inf)
    println()
    println("testGetById: " +id)
    //println(document.content().toString)
    val parsed = play.libs.Json.parse(document.content().toString)
    println("parsed by play: " + parsed)
    document
  }

  def testN1QL(bucket: Bucket): Unit = {
    println()
    println("testN1QL")
    val n1ql = "select * from profile where cabinDetails.cabinNumber = \"1-D301\" limit 10"
    val observableResult: Observable[AsyncN1qlQueryResult] = bucket.async().query(N1qlQuery.simple(n1ql))
    val promise = Promise[List[JsonObject]]
    observableResult.subscribe(// -- 3
      result => {
        val values: ListBuffer[JsonObject] = new ListBuffer
        val rows: Observable[AsyncN1qlQueryRow] = result.rows()
        rows.subscribe(
          onNext = row => values += row.value,
          onError = throwable => promise.failure(throwable),
          onCompleted = () => promise.success(values.toList)
        )
      },
      throwable => println("failure:" +throwable)
    )
    val future: Future[List[JsonObject]] = promise.future // -- 4
    println("result of N1QL: " + n1ql)
    val jsonList: List[JsonObject] = Await.result(future, Duration.Inf)
    jsonList.foreach(println)
  }

}
